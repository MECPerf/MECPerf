package it.unipi.dii.mecperfng;



/*
This code was implemented by Enrico Alberti.
The use of this code is permitted by BSD licenses
 */
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ArrayAdapter;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import it.unipi.dii.common.MeasureResult;



public class RttActivity extends AppCompatActivity {
    private SharedPreferences sp;
    private static final int AGGRPORT = 6766;
    private AsyncCMD mAsyncTask = null;
    private ListView measureListView;



    private class AsyncCMD extends AsyncTask<String, Void, List<MeasureResult>> {

        private String cmd;

        public AsyncCMD(String cmd) {
            super();
            this.cmd = cmd;
        }

        @Override
        protected void onCancelled() {
            mAsyncTask = null;
        }



        @Override
        protected List<MeasureResult> doInBackground(String... args) {
            List<MeasureResult> results = null;
            switch (cmd) {
                case "RTT_LOADER":{
                    results = getRttFromDb(args[0], args[1]);
                    break;
                }
            }

            return results;
        }

        @Override
        protected void onPostExecute(List<MeasureResult> result) {
            if(result != null) {
                if(result.isEmpty()){
                    setContentView(R.layout.list_single_textview);
                    TextView empty = (TextView)findViewById(R.id.textViewMeasure);
                    empty.setText("No RTT measurements to display!");
                }else {
                    List<String> listItem = new ArrayList<>();

                    for(MeasureResult entry: result){
                        String sender = entry.getSender();
                        String reveiver = entry.getReceiver();
                        String direction = sender +"-"+reveiver;
                        String protocol = entry.getCommand().substring(0, 3);
                        String latency = String.format("%.2f", entry.getLatency());
                        latency += " ms";
                        String keyword = entry.getKeyword();

                        String tot = direction+" "+protocol+" "+latency +"\n"+"Keyword: "+ keyword;
                        listItem.add(tot);
                    }

                    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                            R.layout.list_single_textview, R.id.textViewMeasure, listItem);

                    measureListView.setAdapter(adapter);
                }
            }else{
                String value = "Database is shut down!";
                Toast.makeText(getApplicationContext(), value, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rtt_activity);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        measureListView=(ListView)findViewById(R.id.measurementsListView);
        measureListView.setEnabled(false);

        String ID = getIntent().getStringExtra("EXTRA_ID");
        String sender = getIntent().getStringExtra("EXTRA_SENDER");

        mAsyncTask = new AsyncCMD("RTT_LOADER");
        mAsyncTask.execute(ID, sender);
    }



    protected List<MeasureResult> getRttFromDb(String ID, String sender) {
        String response = "",
               aggregatorIP = sp.getString("aggregator_address", "NA"),
               url = "http://" + aggregatorIP + ":5001/mobile/get_RTT_data?id=" + ID + "&sender=" + sender;
        ;

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

        Vector<MeasureResult> results = new Vector();
        JSONArray jsonArray;
        try{
            jsonArray = new JSONArray(response);
            Log.d("RTTActivity Response", response);
            Log.d("RTTActivity Response", "length: " + jsonArray.length());


            for (int i = 0; i < jsonArray.length(); i++) {
                MeasureResult tmp = new MeasureResult();
                tmp.setSender(jsonArray.getJSONObject(i).getString("SenderIdentity"));
                tmp.setReceiver(jsonArray.getJSONObject(i).getString("ReceiverIdentity"));
                tmp.setCommand(jsonArray.getJSONObject(i).getString("Command"));
                tmp.setLatency(jsonArray.getJSONObject(i).getDouble("latency"));
                tmp.setKeyword(jsonArray.getJSONObject(i).getString("Keyword"));
                results.add(tmp);


                Log.d("RTTActivity Respons", "array: " + jsonArray.getJSONObject(i).getString("SenderIdentity"));
                Log.d("RTTActivity Respons:", "tmp" + tmp.getSender());
            }
        }
        catch (JSONException e){
            e.printStackTrace();
            return null;
        }


        return results;
    }

}
