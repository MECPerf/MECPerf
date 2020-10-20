package it.unipi.dii.mecperfng.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import it.unipi.dii.common.MeasureResult;
import it.unipi.dii.mecperfng.Model.TcpPackResult;
import it.unipi.dii.mecperfng.R;
import it.unipi.dii.mecperfng.Results.BandwidthTCP_Results;
import it.unipi.dii.mecperfng.Results.BandwidthUDP_Results;
import it.unipi.dii.mecperfng.Results.Rtt_Results;

public class StorageFragment extends Fragment {


    private SharedPreferences sp;
    private StorageFragment.AsyncCMD mAsyncTask = null;
    private ListView listView;
    private EditText editKeyword;
    private boolean goToRTT;

    //loading
    private ProgressBar progressHomeBar;
    private LinearLayout loadingHomeScreen;

    @Override
    public void onResume() {
        inLoading = false;
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.storage_layout, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressHomeBar = getActivity().findViewById(R.id.progressBarHome);
        loadingHomeScreen = getActivity().findViewById(R.id.loadingHomeScreen);

        progressHomeBar.setVisibility(View.GONE);
        loadingHomeScreen.setVisibility(View.GONE);

        sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        listView = getActivity().findViewById(R.id.dateListView);

        editKeyword = getActivity().findViewById(R.id.edit_keyword);

        editKeyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String keyword = editKeyword.getText().toString();

                if(keyword.equals(""))
                    keyword="null"; //SHOW ALL

                mAsyncTask = new StorageFragment.AsyncCMD("LOADER");
                mAsyncTask.execute((String) keyword);
            }
        });

        mAsyncTask = new StorageFragment.AsyncCMD("LOADER");
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



    private class ListViewAdapter extends ArrayAdapter<String> {
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


        protected class ViewHolder {
            public TextView dateTextView;
            public TextView directionTextView;
            public TextView protocolTextView;
            public RelativeLayout areaItem;
            public Button goAheadButton;
        }




        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View rowView = convertView;

            goToRTT = false;


            // Prendo tutti i riferimenti che mi servono
            if(rowView==null){ //All'inizio sempre null
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView= inflater.inflate(groupid, parent, false);
                StorageFragment.ListViewAdapter.ViewHolder viewHolder = new StorageFragment.ListViewAdapter.ViewHolder();
                viewHolder.dateTextView= (TextView) rowView.findViewById(R.id.dateTextView);
                viewHolder.directionTextView= (TextView) rowView.findViewById(R.id.directionTextView);
                viewHolder.protocolTextView = rowView.findViewById(R.id.typeOfTest);
                viewHolder.areaItem = rowView.findViewById(R.id.areaItemListViewStorage);
                viewHolder.goAheadButton = rowView.findViewById(R.id.goAheadStorage);
                rowView.setTag(viewHolder);
            }

            //NB: ogni volta verranno scaricate e mostrate solo un numero x (es.5) di tuple. Scorrendo vengono scaricate le successive.
            final StorageFragment.ListViewAdapter.ViewHolder holder = (StorageFragment.ListViewAdapter.ViewHolder) rowView.getTag();

            String item_i = getitemList(position);

            final String[] tmp = item_i.split("/");
            if(tmp.length > 3) {
                //mi è stato detto di fare le seguenti sostituzioni: SERVER->CLOUD , OBSERVER->EDGE , CLIENT->ACCESS
                tmp[3] = tmp[3].replace("Server","Cloud");
                tmp[3] = tmp[3].replace("Observer","Edge");
                tmp[3] = tmp[3].replace("Client","Access");

                holder.dateTextView.setText(tmp[0] + "   " + tmp[3]); //DATE => direction
                holder.directionTextView.setText("Test name: " + tmp[2]); //KEYWORD
                holder.protocolTextView.setText(tmp[1]);//protocollo + tipo test
            }


            holder.areaItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (inLoading==false) {
                        if (!tmp[1].substring(3).equals("RTT")) { //SE NON E' UGUALE A RTT ALLORA E' UGUALE A BANDWIDTH
                            String item_i = getitemList(position);
                            String[] tmp = item_i.split("/");

                            String direction = "";
                            if (tmp.length > 3)
                                direction = tmp[3];

                            String[] subject = direction.split("->");

                            sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());

                            mAsyncTaskBandwidth = new StorageFragment.AsyncBandwidth("BANDWIDTH_LOADER");
                            String[] tmp2 = new String[]{tmp[0], tmp[1].substring(0, 3), subject[0], tmp[4]};

                            //prelevo tutto la lista
                            mAsyncTaskBandwidth.execute((String[]) tmp2);

                        } else {
                            String item = getItem(position);
                            String[] splitted_item = item.split("/");
                            String direction = "";
                            if (splitted_item.length > 3)
                                direction = splitted_item[3];

                            String[] subject = direction.split("->");

                            sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

                            StorageFragment.AsyncRTT mAsyncTaskRTT = new StorageFragment.AsyncRTT("RTT_LOADER");
                            mAsyncTaskRTT.execute(tmp[4], subject[0]);

                        }

                    }
                }
            });

            holder.goAheadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.areaItem.callOnClick();
                }
            });

            return rowView;
        }


    }

    boolean inLoading;

    private void showLoadingScreen(){

        progressHomeBar.setVisibility(View.VISIBLE);
        loadingHomeScreen.setVisibility(View.VISIBLE);
        editKeyword.setFocusable(false);
        editKeyword.setFocusableInTouchMode(false);
        editKeyword.setClickable(false);
        inLoading = true;

    }

    private void hideLoadingScreen(){
        editKeyword.setFocusable(true);
        editKeyword.setFocusableInTouchMode(true);
        editKeyword.setClickable(true);
        progressHomeBar.setVisibility(View.GONE);
        loadingHomeScreen.setVisibility(View.GONE);
    }

    //----------------------------------------------------------------------------------------------------
    private class AsyncCMD extends AsyncTask<String, Void, StorageFragment.Result> {

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
        protected void onPreExecute() {
            super.onPreExecute();

            showLoadingScreen();
        }

        /**
         *
         * @param 'args_0' contains keyword
         * @return
         */
        @Override
        protected StorageFragment.Result doInBackground(String... args) {
            List<String> results_all = null;
            List<String> results = new ArrayList<>();

            switch (cmd) {
                case "LOADER": {
                    results_all = restoreDataFromAggregator(); //ALL


                    if (!args[0].equals("null")) {
                        for (String entry : results_all) {
                            String[] tmp = entry.split("/");

                            if ((tmp.length > 3) && (tmp[2].toUpperCase().contains(args[0].toUpperCase()))) { //FILTER
                                results.add(tmp[0] + "/" + tmp[1]+ "/"+ tmp[2] +"/"+tmp[3]+"/"+tmp[4]);
                            }
                        }
                    } else{
                        results = results_all;
                    }
                    break;
                }
            }
            if(results != null)
                return new StorageFragment.Result(results); // new Result(results, type);
            else
                return null;
        }



        @Override
        protected void onPostExecute(StorageFragment.Result result) {

            hideLoadingScreen();
            inLoading = false;

            if(result != null) {
                if (result.isOK()) {
                    StorageFragment.ListViewAdapter adapter=new StorageFragment.ListViewAdapter(getActivity().getApplicationContext(),R.layout.list_result_activity,R.id.dateTextView, result.value);
                    listView.setAdapter(adapter);
                } else {
                    String value = "Error in receive date list";
                    Toast.makeText(getActivity().getApplicationContext(), value, Toast.LENGTH_LONG).show();
                }
            }else{
                String value = "Database is shut down!";
                Toast.makeText(getActivity().getApplicationContext(), value, Toast.LENGTH_LONG).show();
            }
        }
    }



    protected List<String> restoreDataFromAggregator() {
        final List<String> results = new Vector<>();

        String response = "";
        String aggregatorIP = sp.getString("aggregator_address", "NA");
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());


        final String url = "http://" + aggregatorIP + ":5001/mobile/get_data_list";
        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest request = new StringRequest(Request.Method.GET, url, future, future) ;
        requestQueue.add(request);

        try {
            Log.d("Future:","1");
            response = future.get(30, TimeUnit.SECONDS); // this will block
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (TimeoutException e) {
            e.printStackTrace();

        }
        catch (ExecutionException e) {
            e.printStackTrace();
        }

        JSONArray jsonArray;
        try{
            jsonArray = new JSONArray(response);

            for (int i = 0; i < jsonArray.length(); i++) {
                String sender = jsonArray.getJSONObject(i).getString("SenderIdentity");
                String receiver = jsonArray.getJSONObject(i).getString("ReceiverIdentity");
                String direction = sender +"->"+receiver;
                String total = jsonArray.getJSONObject(i).getString("Timestamp") + "/" +
                        jsonArray.getJSONObject(i).getString("Command") + "/" +
                        jsonArray.getJSONObject(i).getString("Keyword") + "/" +
                        direction + "/" + jsonArray.getJSONObject(i).getString("ID");
                results.add(total);
                Log.d("Future:", total);
            }
        }
        catch (JSONException e){
            e.printStackTrace();
            return null;
        }


        return results;
    }

    //------------------------------------------------------------------------------------

    private class AsyncRTT extends AsyncTask<String, Void, List<MeasureResult>> {

        private String cmd;
        private SharedPreferences sp;
        private static final int AGGRPORT = 6766;
        private StorageFragment.AsyncRTT mAsyncTaskRTT = null;

        public AsyncRTT(String cmd) {
            super();
            this.cmd = cmd;
        }

        @Override
        protected void onCancelled() {
            mAsyncTaskRTT = null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            showLoadingScreen();
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
                    hideLoadingScreen();
                    Toast.makeText(getActivity().getApplicationContext(), "No RTT measurements to display!", Toast.LENGTH_LONG).show();
                }else {
                    //(1) si riempie la lista/array delle misurazioni fatte nel formato d'esempio: Keyword: MyKey, Client-Observer UDP 59,00 ms
                    List<String> listItem = new ArrayList<>();
                    //lista che contiene le latenze
                    List<Integer> listLatency = new ArrayList<>();
                    //contiene il label protocol
                    String labelProtocol="";
                    //contiene il label direction
                    String labelDirection = "";

                    for(MeasureResult entry: result){
                        String sender = entry.getSender();
                        //mi è stato detto di fare le seguenti sostituzioni: SERVER->CLOUD , OBSERVER->EDGE , CLIENT->ACCESS
                        sender = sender.replace("Server","Cloud");
                        sender = sender.replace("Observer","Edge");
                        sender = sender.replace("Client","Access");

                        String receiver = entry.getReceiver();
                        receiver = receiver.replace("Server","Cloud");
                        receiver = receiver.replace("Observer","Edge");
                        receiver = receiver.replace("Client","Access");

                        String direction = sender +"-"+receiver;
                        labelDirection = direction;
                        String protocol = entry.getCommand().substring(0, 3);
                        labelProtocol = protocol;
                        String latency = String.format("%.2f", entry.getLatency());
                        listLatency.add(Double.valueOf(entry.getLatency()).intValue());

                        latency += " ms";
                        String keyword = entry.getKeyword();

                        String tot = direction+" "+protocol+" "+latency +"\n"+"Keyword: "+ keyword;
                        listItem.add(tot);
                    }


                    //(2) si associano tali misurazioni alla lista measureListView della 'results_details_activity.xmlvity.xml'
                    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(),
                            R.layout.list_single_textview, R.id.textViewMeasure, listItem);

                    //creata la stringa la passo all'activity Rtt_Results che la mostra come grafico
                    Intent intent = new Intent(getActivity().getApplicationContext(), Rtt_Results.class);
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList("rtt_measurements",new ArrayList(listItem));
                    bundle.putString("rtt_direction",labelDirection);
                    bundle.putString("rtt_protocol",labelProtocol);
                    bundle.putIntegerArrayList("rtt_latency",new ArrayList(listLatency));
                    intent.putExtras(bundle);

                    hideLoadingScreen();

                    startActivity(intent);

                }
            }else{
                hideLoadingScreen();
                String value = "Database is shut down!";
                Toast.makeText(getActivity().getApplicationContext(), value, Toast.LENGTH_LONG).show();
            }
        }
    }


    //ottiene tutti i dati nel formato sender-receiver-cmd (ossia UDP o TCP)-latency-keyword e ritorna una lista contenenti tutte le misurazioni
    protected List<MeasureResult> getRttFromDb(String ID, String sender) {
        String response = "",
                aggregatorIP = sp.getString("aggregator_address", "NA"),
                url = "http://" + aggregatorIP + ":5001/mobile/get_RTT_data?id=" + ID + "&sender=" + sender;
        ;

        Log.d("URL", url);
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
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

    //-------------------------------- PRELIEVO LISTA BANDWIDTH ---------------------------------

    private StorageFragment.AsyncBandwidth mAsyncTaskBandwidth = null;

    private class ResultBandwidth {
        private boolean ok;
        private List<MeasureResult> value;
        private List<MeasureResult> avgList; //significativo solo nel caso del TCP (conterrà una sintesi di tutto compresa la media)
        private String protocol;


        public ResultBandwidth(List<MeasureResult> v, String type) {
            this(v, type, true);
        }

        public ResultBandwidth(List<MeasureResult> v, String type, boolean o) {
            value = v;
            protocol = type;
            ok = o;
        }
        //costruttore tipico per il TCP: manterrà una lista completa di tutto più una 'lista' (un rigo) che sintetizza tutto compreso la bandwidth in media
        public ResultBandwidth(List<MeasureResult> completeList, List<MeasureResult> avgList, String protocol){
            this.ok = true;
            this.value = completeList;
            this.avgList = avgList;
            this.protocol = protocol;
        }

        public boolean isOK() {
            return ok;
        }
    }


    private class AsyncBandwidth extends AsyncTask<String, Void, StorageFragment.ResultBandwidth> {

        private String cmd;

        private AsyncBandwidth(String cmd) {
            super();
            this.cmd = cmd;
        }

        @Override
        protected void onPreExecute() {
            showLoadingScreen();
            super.onPreExecute();
        }

        @Override
        protected void onCancelled() {
            mAsyncTaskBandwidth = null;
        }

        private class CDFDiagram {
            private List<Double> istantBandwidth;

            public CDFDiagram(List<Double> tmp) {

                this.istantBandwidth = tmp;
            }

            public void showDiagram() {
                Map<Double, Double> orderedMap = new LinkedHashMap<>();
                double len_previous = 1;

                orderedMap.put(0.0, 0.0);
                for (Double tmp : istantBandwidth) {
                    orderedMap.put(tmp, len_previous / istantBandwidth.size());
                    len_previous++;
                }

                String x = "";
                String y = "";
                for (Map.Entry<Double, Double> entry : orderedMap.entrySet()) {
                    Log.d("TEST", " (" + entry.getKey() + ", " + entry.getValue() + ")");

                    x += entry.getKey().toString() + "#";
                    y += entry.getValue().toString() + "#";
                }

            }
        }


        /**
         * alla funzione doInBackground sono passati i seguenti parametri come String
         * args[0] = data della query
         * args[1] = tipologia protocollo
         * args[2] = sender
         */

        /*Una volta avviato il Task Bandwidth quello che si fa è prelevare e salvare in 'results' tutta la bandwidth (udp o tcp). Tale risultato è però
          una lista grezza del tipo: "{sender='Client', receiver='Observer', Command='TCPBandwidth', latency=null,...}
          Nel caso UDP tale risultato viene subito salvato nella classe Result che tramite un suo costruttore salva la lista delle misure e il protocollo.
         */

        @Override
        protected StorageFragment.ResultBandwidth doInBackground(String... args) {
            List<MeasureResult> results = null;
            List<MeasureResult> results_AVG = null;

            switch (cmd) {
                case "BANDWIDTH_LOADER": {
                    //results = getBandwidthFromDb(args[0], args[2]); //ALL BANDWIDTH
                    for (int i = 0; i < args.length; i++)
                        Log.d("ARGS", args[i].toString());
                    results = getBandwidthFromDb(args[3], args[2], false); //ALL BANDWIDTH SENZA MEDIA

                    if (args[1].equals("TCP")) {
                        results_AVG = getBandwidthFromDb(args[3], args[2], true); //ALL BANDWIDTH CON MEDIA

                        //La istantBandwidth conterrà solo lista della bandwidth del risultato grezzo e completo 'results'
                        List<Double> istantBandwidth = new ArrayList<>();
                        if (results != null){
                            for (MeasureResult entry : results) {
                                istantBandwidth.add(entry.getBandwidth());
                            }
                        }

                        Collections.sort(istantBandwidth);//CRESCENTE

                        //Passo la lista instantBandwidth alla funzione showCDFDiagram
                        CDFDiagram cdfDiagram = new StorageFragment.AsyncBandwidth.CDFDiagram(istantBandwidth);
                        cdfDiagram.showDiagram();
                    }
                    break;
                }
            }

            if (args[1].equals("TCP")) {
                return new StorageFragment.ResultBandwidth(results, results_AVG, args[1]);
            } else if (args[1].equals("UDP")) {
                return new StorageFragment.ResultBandwidth(results, args[1]);
            }
            return null;
        }

        @Override
        protected void onPostExecute(StorageFragment.ResultBandwidth result) {
            if (result != null) {
                updateList(result);
            } else {
                hideLoadingScreen();
                String value = "Database is shut down!";
                Toast.makeText(getActivity().getApplicationContext(), value, Toast.LENGTH_LONG).show();
            }
        }

        //questa funzione è chiamata quando il task è terminato. Results conterrà i dati completi di tutto
        private void updateList(StorageFragment.ResultBandwidth result) {

            if(result!=null) {
                if (result.value.isEmpty()) {
                    hideLoadingScreen();
                } else {
                    List<String> listItem = new ArrayList<>();
                    List<String> listBandwidth = new ArrayList<>();
                    List<Double> listBandwidthForCDF = new ArrayList<>(); //semplicemente una listBandwidth (però double) senza approssimazioni e ordinata
                    String labelDirection = "";
                    String labelProtocol = "";

                    for (MeasureResult entry : result.value) {
                        String sender = entry.getSender();
                        //mi è stato detto di fare le seguenti sostituzioni: SERVER->CLOUD , OBSERVER->EDGE , CLIENT->ACCESS
                        sender = sender.replace("Server", "Cloud");
                        sender = sender.replace("Observer", "Edge");
                        sender = sender.replace("Client", "Access");

                        String receiver = entry.getReceiver();
                        receiver = receiver.replace("Server", "Cloud");
                        receiver = receiver.replace("Observer", "Edge");
                        receiver = receiver.replace("Client", "Access");

                        String direction = sender + "-" + receiver;
                        labelDirection = direction;
                        String protocol = entry.getCommand().substring(0, 3);
                        labelProtocol = protocol;

                        listBandwidthForCDF.add(entry.getBandwidth());

                        String bandwidth = String.format("%.4f", entry.getBandwidth());
                        //converto da kb/s a mb/s
                        listBandwidth.add(Double.valueOf(entry.getBandwidth() / 1024).toString());
                        bandwidth = String.format("%.4f", Double.valueOf(bandwidth.replace(",", ".")) / 1024) + " MB/s";


                        String keyword = entry.getKeyword();

                        String tot = direction + " " + protocol + "\n" + bandwidth + "\nKeyword: " + keyword;
                        listItem.add(tot);
                    }
                    Collections.sort(listBandwidthForCDF);

                    //creata la stringa la passo all'activity Rtt_Results che la mostra come grafico
                    //se il protocollo è UDP...
                    if (labelProtocol.equals("UDP")) {
                        Intent intent = new Intent(getActivity().getApplicationContext(), BandwidthUDP_Results.class);

                        Bundle bundle = new Bundle();
                        bundle.putStringArrayList("bandwidthUdp_measurements", new ArrayList(listItem));
                        bundle.putString("bandwidthUdp_direction", labelDirection);
                        bundle.putString("bandwidthUdp_protocol", labelProtocol);
                        bundle.putStringArrayList("bandwidthUdp_bandwidth", new ArrayList(listBandwidth));
                        intent.putExtras(bundle);

                        hideLoadingScreen();
                        startActivity(intent);
                    } else {

                        String avg = "";
                        for (MeasureResult entry : result.avgList) {
                            avg = String.format("%.4f", entry.getBandwidth() / 1024) + " MB/s";
                        }

                        List<String> listBandwidthCDF = new ArrayList<String>();
                        for (int i = 0; i < listBandwidthForCDF.size(); i++) {
                            listBandwidthCDF.add(listBandwidthForCDF.get(i).toString());
                        }

                        TcpPackResult tcpPackResult = new TcpPackResult(listItem, listBandwidth, listBandwidthCDF, avg, labelDirection, labelProtocol);

                        new SaveTcpBandwidthResultsIntoFileTask(tcpPackResult).execute();

                    }

                }
            }else
                hideLoadingScreen();
        }

        //------------------ piccolo async task per memorizzare su file ----------------

        private class SaveTcpBandwidthResultsIntoFileTask extends AsyncTask<Void, Void, Void> {

            TcpPackResult packResult;

            public SaveTcpBandwidthResultsIntoFileTask(TcpPackResult packResult){
                this.packResult = packResult;
            }

            @Override
            protected Void doInBackground(Void... voids) {


                FileOutputStream fos = null;
                try {
                    fos = getActivity().getApplicationContext().openFileOutput("tcpPack", Context.MODE_PRIVATE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                ObjectOutputStream os = null;
                try {
                    os = new ObjectOutputStream(fos);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    os.writeObject(packResult);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    fos.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }


                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                Intent intent = new Intent(getActivity().getApplicationContext(), BandwidthTCP_Results.class);
                hideLoadingScreen();
                startActivity(intent);
            }
        }




        //------------------------------------------------------------------------------



        protected List<MeasureResult> getBandwidthFromDb(String ID, String sender, boolean average) {
            List<MeasureResult> results = new ArrayList<>();
            String aggregatorIP = sp.getString("aggregator_address", "NA"),
                    response,
                    url;
            if (average)
                url = "http://" + aggregatorIP + ":5001/mobile/get_AVGbandwidth_data?id=" + ID +
                        "&sender=" + sender;
            else
                url = "http://" + aggregatorIP + ":5001/mobile/get_bandwidth_data?id=" + ID +
                        "&sender=" + sender;
            Log.d("URL", url);

            RequestQueue requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
            RequestFuture<String> future = RequestFuture.newFuture();
            StringRequest request = new StringRequest(Request.Method.GET, url, future, future);
            requestQueue.add(request);

            try {
                response = future.get(30, TimeUnit.SECONDS); // this will block
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                e.printStackTrace();
                return null;
            }

            try {
                JSONArray jsonArray;
                jsonArray = new JSONArray(response);
                Log.d("BandwidthActivity Response", response);
                Log.d("BandwidthActivity Response", "length: " + jsonArray.length());
                Log.d("BandwidthActivity Response average", "average: " + average);
                Log.d("BandwidthActivity Response:", "json array = " + jsonArray);


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
                    } else
                        tmp.setBandwidth(jsonArray.getJSONObject(i).getDouble("Bandwidth"));

                    tmp.setKeyword(jsonArray.getJSONObject(i).getString("Keyword"));

                    results.add(tmp);


                    Log.d("BandwidthActivity Response:", "tmp " + tmp.getSender());
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }

            Log.d("BandwidthActivity Response:", "results " + results);

            return results;
        }


    }

}

