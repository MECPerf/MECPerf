package it.unipi.dii.mecperfng;

/*
This code was implemented by Enrico Alberti.
The use of this code is permitted by BSD licenses
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import it.unipi.dii.common.Measure;
import it.unipi.dii.common.MeasureResult;

public class BandwidthActivity extends AppCompatActivity {
    private SharedPreferences sp;
    private static final int AGGRPORT = 6766;

    private AsyncCMD mAsyncTask = null;

    private ListView measureListView;
    private Button tcpDiagramBtn;
    private Button tcpMoreBtn;
    private Button tcpLessBtn;

    private class Result {
        private boolean ok;
        private List<MeasureResult> value;
        private String protocol;


        public Result(List<MeasureResult> v, String type) {
            this(v, type, true);
        }

        public Result(List<MeasureResult> v, String type, boolean o) {
            value = v;
            protocol = type;
            ok = o;
        }

        public boolean isOK() {
            return ok;
        }
    }


    private class AsyncCMD extends AsyncTask<String, Void, Result> {

        private String cmd;

        public AsyncCMD(String cmd) {
            super();
            this.cmd = cmd;
        }

        @Override
        protected void onCancelled() {
            mAsyncTask = null;
        }

        private class ShowDiagramOnClickListner implements View.OnClickListener{
            private List<Double> istantBandwidth;

            public ShowDiagramOnClickListner(List<Double> tmp){
                this.istantBandwidth = tmp;
            }

            @Override
            public void onClick(View v){
                Map<Double, Double> orderedMap = new LinkedHashMap<>();
                double len_previous=1;

                orderedMap.put(0.0, 0.0);
                for(Double tmp: istantBandwidth){
                    orderedMap.put(tmp, len_previous/istantBandwidth.size());
                    len_previous++;
                }

                String x ="";
                String y = "";
                for (Map.Entry<Double, Double> entry : orderedMap.entrySet()) {
                    Log.d("TEST", " (" + entry.getKey()+", " + entry.getValue()+")");

                    x += entry.getKey().toString()+"#";
                    y += entry.getValue().toString()+"#";
                }

                Intent intent = new Intent(getBaseContext(), BandwidthTCPGraphActivity.class);
                intent.putExtra("EXTRA_X", x);
                intent.putExtra("EXTRA_Y", y);
                startActivity(intent);
            }
        }


        private class ShowMoreOnClickListner implements View.OnClickListener {
            private List<MeasureResult> results;
            private String type;
            private String button;

            public ShowMoreOnClickListner(List<MeasureResult> tmp, String t, String btn) {
                this.results = tmp;
                this.type = t;
                this.button = btn;
            }

            @Override
            public void onClick(View v) {
                Log.d("TEST", "CLICK MORE/LESS");
                updateList(new Result(results, type));

                if(button.equals("MORE")){
                    Button btn_more=(Button)findViewById(R.id.tcp_bandwidth_more);
                    btn_more.setVisibility(View.INVISIBLE);
                    btn_more.setEnabled(false);

                    Button btn_less=(Button)findViewById(R.id.tcp_bandwidth_less);
                    btn_less.setVisibility(View.VISIBLE);
                    btn_less.setEnabled(true);

                }else if(button.equals("LESS")){
                    Button btn_less=(Button)findViewById(R.id.tcp_bandwidth_less);
                    btn_less.setVisibility(View.INVISIBLE);
                    btn_less.setEnabled(false);

                    Button btn_more=(Button)findViewById(R.id.tcp_bandwidth_more);
                    btn_more.setVisibility(View.VISIBLE);
                    btn_more.setEnabled(true);
                }
            }
        }



        /** alla funzione doInBackground sono passati i seguenti parametri come String
         *   args[0] = data della query
         *   args[1] = tipologia protocollo
         *   args[2] = sender
         */
        @Override
        protected Result doInBackground(String... args) {
            List<MeasureResult> results = null;
            List<MeasureResult> results_AVG = null;

            switch (cmd) {
                case "BANDWIDTH_LOADER":{
                    //results = getBandwidthFromDb(args[0], args[2]); //ALL BANDWIDTH
                    for (int i= 0; i<args.length; i++)
                        Log.d("ARGS", args[i].toString());
                    results = getBandwidthFromDb(args[3], args[2], false); //ALL BANDWIDTH

                    if(args[1].equals("TCP")){
                        results_AVG = getBandwidthFromDb(args[3], args[2], true);


                        //SHOW DIAGRAM
                        tcpDiagramBtn=(Button)findViewById(R.id.tcp_bandwidth_diagram);

                        List<Double> istantBandwidth = new ArrayList<>();
                        for(MeasureResult entry: results){
                            istantBandwidth.add(entry.getBandwidth());
                        }

                        Log.d("TEST", "LISTA ORIGINALE: " + istantBandwidth);
                        Collections.sort(istantBandwidth);//CRESCENTE
                        Log.d("TEST", "LISTA SORTED: " + istantBandwidth);
                            tcpDiagramBtn.setOnClickListener(new ShowDiagramOnClickListner(istantBandwidth));

                        //SEE MORE
                        tcpMoreBtn=(Button)findViewById(R.id.tcp_bandwidth_more);
                        tcpMoreBtn.setOnClickListener(new ShowMoreOnClickListner(results, args[1], "MORE"));

                        //SEE LESS
                        tcpLessBtn=(Button)findViewById(R.id.tcp_bandwidth_less);
                        tcpLessBtn.setOnClickListener(new ShowMoreOnClickListner(results_AVG, args[1], "LESS"));
                    }
                    break;
                }
            }

            if(args[1].equals("TCP")){
                return new Result(results_AVG, args[1]);
            }else if(args[1].equals("UDP")){
                return new Result(results, args[1]);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Result result) {
            if(result != null) {
                updateList(result);
            }else{
                String value = "Database is shut down!";
                Toast.makeText(getApplicationContext(), value, Toast.LENGTH_LONG).show();
            }
        }

        private void updateList(Result result){
            if(result.value.isEmpty()){
                setContentView(R.layout.list_single_textview);
                TextView empty = (TextView)findViewById(R.id.textViewMeasure);
                empty.setText("No RTT measurements to display!");
            }else {
                List<String> listItem = new ArrayList<>();

                for(MeasureResult entry: result.value){
                    String sender = entry.getSender();
                    String reveiver = entry.getReceiver();
                    String direction = sender +"-"+reveiver;
                    String protocol = entry.getCommand().substring(0, 3);

                    String bandwidth = String.format("%.2f",entry.getBandwidth());
                    bandwidth += " KB/s";

                    String keyword = entry.getKeyword();

                    String tot = direction+" "+protocol+"\n"+bandwidth + "\nKeyword: " + keyword;
                    listItem.add(tot);
                }

                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                        R.layout.list_single_textview, R.id.textViewMeasure, listItem);

                measureListView.setAdapter(adapter);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bandwidth_activity);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String date = getIntent().getStringExtra("EXTRA_DATE");
        String id = getIntent().getStringExtra("EXTRA_ID");
        String type = getIntent().getStringExtra("EXTRA_PROTOCOL"); //TCP o UDP
        String sender = getIntent().getStringExtra("EXTRA_SENDER");
        Log.d("EXTRA", date);
        Log.d("EXTRA", id);
        Log.d("EXTRA", type);
        Log.d("EXTRA", sender);


        measureListView=(ListView)findViewById(R.id.bandwidthMeasurementsListView);
        tcpDiagramBtn=(Button)findViewById(R.id.tcp_bandwidth_diagram);

        tcpMoreBtn=(Button)findViewById(R.id.tcp_bandwidth_more);
        tcpLessBtn=(Button)findViewById(R.id.tcp_bandwidth_less);
        Log.d("TEST ", "TYPE " + type);

        if (type == null) {
            Log.d("TEST", "TYPE NULL");
            return;
        }
        if(type.equals("UDP")){
            Log.d("TEST", "UDP");
            measureListView.setEnabled(false);

            tcpDiagramBtn.setEnabled(false);
            tcpDiagramBtn.setVisibility(View.INVISIBLE);

            tcpMoreBtn.setEnabled(false);
            tcpMoreBtn.setVisibility(View.INVISIBLE);

            tcpLessBtn.setEnabled(false);
            tcpLessBtn.setVisibility(View.INVISIBLE);

        }else if (type.equals("TCP")){
            Log.d("TEST", "TCP");
            measureListView.setEnabled(true);
            tcpDiagramBtn.setEnabled(true);
            tcpDiagramBtn.setVisibility(View.VISIBLE);

            tcpMoreBtn.setEnabled(true);
            tcpMoreBtn.setVisibility(View.VISIBLE);

            tcpLessBtn.setEnabled(false);
            tcpLessBtn.setVisibility(View.INVISIBLE);
        }

        mAsyncTask = new AsyncCMD("BANDWIDTH_LOADER");
        String[] tmp = new String[]{date, type, sender, id};

        mAsyncTask.execute((String[]) tmp);
    }


    protected List<MeasureResult> getBandwidthFromDb(String ID, String sender, boolean average) {
        List<MeasureResult> results = new ArrayList<>();
        String aggregatorIP = sp.getString("aggregator_address", "NA"),
               response,
               url ;
        if (average)
            url = "http://" + aggregatorIP + ":5001/get_AVGbandwidth_data?id=" + ID +
                    "&sender=" + sender;
        else
            url = "http://" + aggregatorIP + ":5001/get_bandwidth_data?id=" + ID +
                    "&sender=" + sender;
        Log.d("URL", url);

        RequestQueue requestQueue = Volley.newRequestQueue(this);  // this = context
        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest request = new StringRequest(Request.Method.GET, url, future, future) ;
        requestQueue.add(request);

        try {
            response = future.get(30, TimeUnit.SECONDS); // this will block
        }
        catch (InterruptedException | TimeoutException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }

        try {
            JSONArray jsonArray;
            jsonArray = new JSONArray(response);
            Log.d("BandwidthActivity Response", response);
            Log.d("BandwidthActivity Response", "length: " + jsonArray.length());
            Log.d("BandwidthActivity Response", "average: " + average);

            for (int i = 0; i < jsonArray.length(); i++) {
                MeasureResult tmp = new MeasureResult();
                tmp.setSender(jsonArray.getJSONObject(i).getString("SenderIdentity"));
                tmp.setReceiver(jsonArray.getJSONObject(i).getString("ReceiverIdentity"));
                tmp.setCommand(jsonArray.getJSONObject(i).getString("Command"));

                if (!average) {
                    Double time = jsonArray.getJSONObject(i).getDouble("nanoTimes");
                    Double bytes = jsonArray.getJSONObject(i).getDouble("kBytes");
                    time = time / 1000000; //ms
                    Double bandwidth = bytes / time; //KB/ms
                    //bandwidth = bandwidth / 1024; //KB/ms Già in KB sul DB
                    bandwidth = bandwidth * 1000; //KB/s

                    tmp.setBandwidth(bandwidth);
                }
                else
                    tmp.setBandwidth( jsonArray.getJSONObject(i).getDouble("Bandwidth"));

                tmp.setKeyword( jsonArray.getJSONObject(i).getString("Keyword"));

                results.add(tmp);


                Log.d("BandwidthActivity Respons:", "tmp" + tmp.getSender());
            }
        }
        catch (JSONException e){
                e.printStackTrace();
                return null;
            }

        return results;
    }

}
