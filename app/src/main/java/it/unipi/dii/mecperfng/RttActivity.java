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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import it.unipi.dii.common.Measure;
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


        //args[0] = date
        //args[1] = Sender

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

        String date = getIntent().getStringExtra("EXTRA_DATE");
        String sender = getIntent().getStringExtra("EXTRA_SENDER");
        mAsyncTask = new AsyncCMD("RTT_LOADER");
        mAsyncTask.execute((String) date, sender);
    }

    protected List<MeasureResult> getRttFromDb(String date, String sender) {
        List<MeasureResult> results = null;

        Socket socket = null;
        ObjectOutputStream objOutputStream = null;
        ObjectInputStream objInputStream;
        String aggregatorIP = sp.getString("aggregator_address", "NA");


        try {
            socket = new Socket(InetAddress.getByName(aggregatorIP), AGGRPORT);

            objOutputStream = new ObjectOutputStream(socket.getOutputStream());

            Measure measure = new Measure("GET_RTT_DATA", sender, "", null, -1, date, -1, -1);

            objOutputStream.writeObject(measure);

            objInputStream = new ObjectInputStream(socket.getInputStream());
            results = (List<MeasureResult>)objInputStream.readObject();
            Log.d("TEST", "LISTA DI OGGETTI COME RITORNO DA  QUERY: " + results);
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (objOutputStream != null)
                    objOutputStream.close(); // close the output stream when we're done.
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return results;
    }

}
