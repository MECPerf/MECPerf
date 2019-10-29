package it.unipi.dii.mecperfng;

/*
This code was implemented by Enrico Alberti.
The use of this code is permitted by BSD licenses
 */

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;





import it.unipi.dii.common.Measure;

public class ResultsActivity extends AppCompatActivity {
    private SharedPreferences sp;
    private static final int AGGRPORT = 6766;

    private AsyncCMD mAsyncTask = null;

    private ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        listView = findViewById(R.id.dateListView);

        final Button btnKeywordResult = findViewById(R.id.keyword_result);
        btnKeywordResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText editTextKeyword = findViewById(R.id.edit_keyword);
                String keyword = editTextKeyword.getText().toString();
                editTextKeyword.setText("");
                if(keyword.equals(""))
                    keyword="null"; //SHOW ALL

                Log.d("TEST", "premuto btnKeywordResult keyword: " + keyword);

                mAsyncTask = new AsyncCMD("LOADER");
                mAsyncTask.execute((String) keyword);
            }
        });

        mAsyncTask = new AsyncCMD("LOADER");
        mAsyncTask.execute((String) "null"); //SHOW ALL
    }

    private class Result {
        private boolean ok;
        private List<String> value;


        public Result(List<String> v) {
            this(v,true);
        }

        public Result(List<String> v, boolean o) {
            value = v;
            ok = o;
        }

        public boolean isOK() {
            return ok;
        }
    }

    private class ListViewAdapter extends ArrayAdapter<String>{
        private int groupid;
        private List<String> item_list;
        private Context context;

        public ListViewAdapter(Context context, int vg, int id, List<String> item_list){
            super(context, vg, id, item_list);
            this.context = context;
            this.groupid = vg;

            this.item_list = item_list;
        }

        public String getitemList(int i){
            return item_list.get(i);
        }

        // Hold views of the ListView to improve its scrolling performance
        protected class ViewHolder {
            public TextView dateTextView;
            public TextView directionTextView;
            public Button rttButton;
            public Button bandwidthButton;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            // Inflate the list_result_activity.xml file if convertView is null
            if(rowView==null){ //All'inizio sempre null
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView= inflater.inflate(groupid, parent, false);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.dateTextView= (TextView) rowView.findViewById(R.id.dateTextView);
                viewHolder.directionTextView= (TextView) rowView.findViewById(R.id.directionTextView);
                viewHolder.rttButton= (Button) rowView.findViewById(R.id.rttButton);
                viewHolder.bandwidthButton= (Button) rowView.findViewById(R.id.bandwidthButton);
                rowView.setTag(viewHolder);
            }

            // Set text to each TextView of ListView item
            ViewHolder holder = (ViewHolder) rowView.getTag();

            String item_i = getitemList(position);

            String[] tmp = item_i.split("/");
            if(tmp.length > 3) {
                holder.dateTextView.setText(tmp[0] + " =>" + tmp[3]); //DATE
                holder.directionTextView.setText("Keyword: " + tmp[2]); //KEYWORD
            }

            holder.rttButton.setText("RTT");    //RTT
            holder.bandwidthButton.setText("Bandwidth"); //BANDWIDTH

            holder.rttButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("TEST", "premuto rttButton della data " + getitemList(position));
                    String item = getItem(position);
                    String[] splitted_item = item.split("/");
                    String direction ="";
                    if(splitted_item.length>3)
                        direction = splitted_item[3];

                    String[] subject = direction.split("->");

                    Intent intent = new Intent(getBaseContext(), RttActivity.class);
                    String item_i = getitemList(position);
                    String[] tmp = item_i.split("/");
                    intent.putExtra("EXTRA_DATE", tmp[0]);
                    intent.putExtra("EXTRA_SENDER", subject[0]);
                    startActivity(intent);
                }
            });

            holder.bandwidthButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("TEST", "premuto bandwidthButton della data " + getitemList(position));
                    String item_i = getitemList(position);
                    String[] tmp = item_i.split("/");

                    String direction ="";
                    if(tmp.length>3)
                        direction = tmp[3];

                    String[] subject = direction.split("->");

                    Intent intent = new Intent(getBaseContext(), BandwidthActivity.class);
                    intent.putExtra("EXTRA_DATE", tmp[0]);
                    intent.putExtra("EXTRA_PROTOCOL", tmp[1].substring(0,3)); //TCP o UDP
                    intent.putExtra("EXTRA_SENDER", subject[0]);
                    startActivity(intent);
                }
            });

            if(!tmp[1].substring(3).equals("RTT")){ //SE NON E' UGUALE A RTT ALLORA E' UGUALE A BANDWIDTH
                holder.rttButton.setEnabled(false);
                holder.bandwidthButton.setEnabled(true);
            }else{
                holder.bandwidthButton.setEnabled(false);
                holder.rttButton.setEnabled(true);
            }
            return rowView;
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


        /**
         *
         * @param 'args_0' contains keyword
         * @return
         */

        @Override
        protected Result doInBackground(String... args) {
            List<String> results_all = null;
            List<String> results = new ArrayList<>();

                switch (cmd) {
                    case "LOADER": {
                        results_all = restoreDataFromAggregator(); //ALL

                        if (!args[0].equals("null")) {
                            for (String entry : results_all) {
                                String[] tmp = entry.split("/");

                                if ((tmp.length > 3) && (tmp[2].toUpperCase().contains(args[0].toUpperCase()))) { //FILTER
                                    results.add(tmp[0] + "/" + tmp[1]+ "/"+ tmp[2] +"/"+tmp[3]);
                                }
                            }
                        } else{
                            results = results_all;
                        }
                        break;
                    }
                }
            if(results != null)
                return new Result(results); // new Result(results, type);
            else
                return null;
        }

        @Override
        protected void onPostExecute(Result result) {
            if(result != null) {
                if (result.isOK()) {
                    ListViewAdapter adapter=new ListViewAdapter(getApplicationContext(),R.layout.list_result_activity,R.id.dateTextView, result.value);
                    listView.setAdapter(adapter);
                } else {
                    String value = "Error in receive date list";
                    Toast.makeText(getApplicationContext(), value, Toast.LENGTH_LONG).show();
                }
            }else{
                String value = "Database is shut down!";
                Toast.makeText(getApplicationContext(), value, Toast.LENGTH_LONG).show();
            }
        }
    }

    protected List<String> restoreDataFromAggregator() {
        List<String> results = null;

        Socket socket = null;
        ObjectOutputStream objOutputStream = null;
        ObjectInputStream objInputStream;
        String aggregatorIP = sp.getString("aggregator_address", "NA");

        try {
            socket = new Socket(InetAddress.getByName(aggregatorIP), AGGRPORT);

            objOutputStream = new ObjectOutputStream(socket.getOutputStream());
            Measure measure = new Measure("GET_DATA_LIST", "", "", null, -1, null, -1, -1);

            objOutputStream.writeObject(measure);

            objInputStream = new ObjectInputStream(socket.getInputStream());

            results = (List<String>)objInputStream.readObject();
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
