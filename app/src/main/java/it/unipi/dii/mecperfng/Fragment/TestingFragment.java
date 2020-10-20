package it.unipi.dii.mecperfng.Fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.LinkedHashMap;
import java.util.Map;

import it.unipi.dii.mecperfng.MainUtils;
import it.unipi.dii.mecperfng.R;

public class TestingFragment extends Fragment {

    private ImageView areaGo;
    private boolean isStopped; //se true significa che è stoppata l'analisi

    private SharedPreferences preferences;
    private BottomNavigationView menu;

    private EditText keyword;

    //ANIMAZIONE RADAR
        //in ordine: icona test, forma del cerchio del radar, forma del cerchio del radar
    private ImageView radar1IMG, radar2IMG, radar5IMG, radar6IMG;
    private TextView infoMeasurement, goLabel;
    private LottieAnimationView loadTestingAnimation;
        //layout contenente l'icona di testing, la frase che avverte sull'esito della misura e i cerchi del radar
    private View layoutRadar;
    private Handler handlerAnimationRadar;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.testing_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        isStopped = true;

        menu = getActivity().findViewById(R.id.menu);

        loadTestingAnimation = getActivity().findViewById(R.id.spaceAnimation);
        loadTestingAnimation.setVisibility(View.INVISIBLE);

        initRadarElements();
        //faccio partire l'animazione statica del radar
        startStaticRadarAnimation();
        Glide.with(getActivity().getApplicationContext()).load(R.drawable.ic_testing)
                .apply(new RequestOptions());

        //----------------------------------------------BOTTONE TESTING ----------------------------------------------
        areaGo = getActivity().findViewById(R.id.areaStop);

        areaGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isStopped) {
                    areaGo.setAlpha((float) 1f);
                    stopStaticRadarAnimation();
                    isStopped = false;
                    avviaMisurazione((short) 0);
                    loadTestingAnimation.setVisibility(View.VISIBLE);
                }
            }
        });


        keyword = getActivity().findViewById(R.id.keyword);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());

        //ottengo la lista dalla risorsa values/array.xml per popolare lo spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity().getApplicationContext(),
                R.array.function_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        verifyStoragePermissions(getActivity());

    }


//____________________________________ RADAR ANIMATION _____________________________________________

//inizializza elementi utili all'animazione del radar


private void initRadarElements(){

        this.handlerAnimationRadar = new Handler();
        this.layoutRadar = getActivity().findViewById(R.id.radar_layout);
        this.goLabel = getActivity().findViewById(R.id.goLabel);
        this.radar1IMG = getActivity().findViewById(R.id.radar1);
        this.radar2IMG = getActivity().findViewById(R.id.radar2);
        this.radar5IMG = getActivity().findViewById(R.id.radar5);
        this.radar6IMG = getActivity().findViewById(R.id.radar6);
        this.infoMeasurement = getActivity().findViewById(R.id.measurement_outcome_notice);
        this.infoMeasurement.setVisibility(View.INVISIBLE);

}

//avvia l'animazione a radar
private void startRadarAnimation(){
        this.runnableRadarAnimation.run();
        this.goLabel.setVisibility(View.VISIBLE);
        this.layoutRadar.setVisibility(View.VISIBLE);
        this.radar1IMG.setVisibility(View.VISIBLE);
        this.radar2IMG.setVisibility(View.VISIBLE);
        this.infoMeasurement.setVisibility(View.VISIBLE);
}

//stoppa l'animazione a radar
private void stopRadarAnimation(){ ;
        this.handlerAnimationRadar.removeCallbacks(runnableRadarAnimation);
        this.goLabel.setVisibility(View.VISIBLE);
        this.radar1IMG.setVisibility(View.INVISIBLE);
        this.radar2IMG.setVisibility(View.INVISIBLE);
        this.infoMeasurement.setVisibility(View.INVISIBLE);
        isStopped = true;
        loadTestingAnimation.setVisibility(View.INVISIBLE);
        startStaticRadarAnimation();
    }

//animazione dei due cerchi a radar
private Runnable runnableRadarAnimation = new Runnable() {
    @Override
    public void run() {

        radar1IMG.animate().scaleX(4f).scaleY(4f).alpha(0f).setDuration(1000).withEndAction(new Runnable() {
            @Override
            public void run() {
                radar1IMG.setScaleX(0f);
                radar1IMG.setScaleY(0f);
                radar1IMG.setAlpha(1f);
            }
        });

        radar2IMG.animate().scaleX(4f).scaleY(4f).alpha(0f).setDuration(700).withEndAction(new Runnable() {
            @Override
            public void run() {
                radar2IMG.setScaleX(0f);
                radar2IMG.setScaleY(0f);
                radar2IMG.setAlpha(1f);
            }
        });

        handlerAnimationRadar.postDelayed(runnableRadarAnimation,1500);
    }
};

    //animazione static dei due cerchi a radar
    private Runnable runnableStaticRadarAnimation = new Runnable() {
        @Override
        public void run() {

            radar5IMG.animate().scaleX(1.5f).scaleY(1.5f).alpha(0f).setDuration(3000).withEndAction(new Runnable() {
                @Override
                public void run() {
                    radar5IMG.setScaleX(1f);
                    radar5IMG.setScaleY(1f);
                    radar5IMG.setAlpha(1f);
                }
            });


            radar6IMG.animate().scaleX(1.5f).scaleY(1.5f).setDuration(3000).withEndAction(new Runnable() {
                @Override
                public void run() {
                    radar6IMG.setScaleX(1f);
                    radar6IMG.setScaleY(1f);
                }
            });


            handlerAnimationRadar.postDelayed(runnableStaticRadarAnimation,3500);
        }
    };

    //per il radard statico
    //avvia l'animazione a radar

    boolean isStaticRadarAnimationStarted = false;
    private void startStaticRadarAnimation(){
        if(!isStaticRadarAnimationStarted) {
            this.runnableStaticRadarAnimation.run();
            isStaticRadarAnimationStarted = true;
        }
        this.goLabel.setVisibility(View.VISIBLE);
        this.layoutRadar.setVisibility(View.VISIBLE);
        this.radar5IMG.setVisibility(View.VISIBLE);
        this.radar6IMG.setVisibility(View.VISIBLE);
        this.infoMeasurement.setVisibility(View.INVISIBLE);
    }

    //stoppa l'animazione a radar
    private void stopStaticRadarAnimation(){
        this.goLabel.setVisibility(View.INVISIBLE);
        this.radar5IMG.setVisibility(View.INVISIBLE);
        this.radar6IMG.setVisibility(View.INVISIBLE);
        this.infoMeasurement.setVisibility(View.INVISIBLE);
    }


//__________________________________________________________________________________________________

    // Avvia tutte e 4 le misurazioni
    private void avviaMisurazione(short n){

        //prelevo la keyword
        String keyName = keyword.getText().toString();
        if(keyName.equals(""))
            keyName = "TEST";


        if(n==0) {
            //avvio la prima delle 4 misurazioni. Le successive 3 verranno eseguite dal metodo onPostExecute dell'Async Task cosi da assicurarne la sequenzialità.
            startRadarAnimation();
            menu.getMenu().getItem(0).setEnabled(false);
            menu.getMenu().getItem(1).setEnabled(false);
            menu.getMenu().getItem(2).setEnabled(false);

            tcpTask = new TestingFragment.AsyncCMD("TCPBANDWIDTHBUTTON", keyName); //TODO: METTERE AL POSTO DI 'TEST' LA KEYWORD
            tcpTask.execute((Void) null);
            infoMeasurement.setText("Tcp Bandwidth measurement...");

        } else if (n==1){

            udpTask = new TestingFragment.AsyncCMD("UDPBANDWIDTHBUTTON", keyName); //TODO: METTERE AL POSTO DI 'TEST' LA KEYWORD
            udpTask.execute((Void) null);
            infoMeasurement.setText("Udp Bandwidth measurement...");
        } else if (n==2){

            rttTcpTask = new TestingFragment.AsyncCMD("TCPRTTBUTTON", keyName); //TODO: METTERE AL POSTO DI 'TEST' LA KEYWORD
            rttTcpTask.execute((Void) null);
            infoMeasurement.setText("Tcp Rtt measurement...");
        } else {

            rttUdpTask = new TestingFragment.AsyncCMD("UDPRTTBUTTON", keyName); //TODO: METTERE AL POSTO DI 'TEST' LA KEYWORD
            rttUdpTask.execute((Void) null);
            infoMeasurement.setText("Udp Rtt measurement...");
        }

    }
/*
    Questo metodo è da implementare nella nuova versione. Attualmente le 4 misure non sono interrompibili

    private void fermaMisurazione(){

    }

 */


    //--------------------CLASSE PRIVATA PER LA GESTIONE DEL CALCOLO DEI PARAMETRI TRAMITE ASYNC TASK----------------------------
    private static final int REQUEST_INTERNET = 1;
    private static String[] PERMISSIONS_INTERNET = {
            Manifest.permission.INTERNET
    };
    private static final int CMDPORT = 6792;//6789
    private static final int TCPPORT = 6791;//6788
    private static final int UDPPORT = 6790;//6787
    private static final int AGGRPORT = 6766;
    private static final int NUMTEST_UDP_CAPACITY = 25; //TODO remove
    private TestingFragment.AsyncCMD tcpTask = null;
    private TestingFragment.AsyncCMD udpTask = null;
    private TestingFragment.AsyncCMD rttTcpTask = null;
    private TestingFragment.AsyncCMD rttUdpTask = null;

    //tiene conto dell'i-esima misurazione
    private static short n_measurement = 0;



    private class AsyncCMD extends AsyncTask<Void, Void, Integer> {
        private String cmd;
        private String keyword;



        public AsyncCMD(String cmd, String keyw) {
            super();
            this.cmd = cmd;
            this.keyword = keyw;
        }



        @Override
        protected Integer doInBackground(Void... voids) {

            //OTTIENI L'IP DELL'OBSERVER
            String observerAddress = preferences.getString("observer_address", "NESSUN INDIRIZZO");
            Log.d("MyLOG","observer_address:"+observerAddress);
            //OTTIENI L'IP DELL'AGGREGATOR TODO: NON E' USATO PERCHE' DEVI IMPLEMENTARE TU LA FUNZIONALITA' DI PRELIEVO DAL DBMS
            String aggregatorAddress = preferences.getString("aggregator_address", "NESSUN INDIRIZZO");
            Log.d("MyLOG","aggregator_address:"+aggregatorAddress);

            //OTTIENI IL NUMERO DI TEST CONSECUTIVI DA SVOLGERE
            int numberOfConsecutiveTests = Integer.parseInt(preferences.getString(
                    "number_of_consecutive_tests", "1"));

            //PRELEVA LA DIREZIONE DI MISURA: SI VUOLE INVIARE O RICEVERE
            String direction = "Sender";
            if(preferences.getString("Direction","0").equals("0"))
                direction = "Receiver";

            int outcome;
            //CMD CONTIENE UNO DEI 4 BOTTONI SELEZIONATI, E QUINDI DISCRIMINA TRA LARGHEZZA DI BANDA E LATENZA
            switch (cmd) {
                case "TCPBANDWIDTHBUTTON":{
                    //OTTENGO GLI L BIT DI OGNI PACCO
                    int tcp_bandwidth_pktsize = Integer.parseInt(preferences.getString(
                            "pack_size_TCP_BANDWIDTH", "1024")),
                            //OTTENGO IL NUMERO DI PACCHETTI DA INVIARE
                            tcp_bandwidth_num_pkt = Integer.parseInt(preferences.getString(
                                    "num_pack_TCP_BANDWIDTH", "1024"));

                    //PER IL NUMERO DI TEST DA EFFETTUARE...
                    for (int i = 0; i< numberOfConsecutiveTests;i++) {
                        //OTTENGO LA LARGHEZZA DI BANDA NELLA CONNESSIONE TRA ME E L'OBSERVER NELL'INVIO DI UN CERTO NUMERO DI PACCHETTI DI L BIT
                        outcome = MainUtils.tcpBandwidthMeasure(direction, keyword, CMDPORT,
                                observerAddress, TCPPORT,
                                tcp_bandwidth_pktsize, tcp_bandwidth_num_pkt, null);
                        String txt = "TCP bandwidth " + i + ": ";
                        if (outcome != 0) {
                            txt += "FAILED";
                            i--;
                        }
                        else
                            txt += "SUCCESS";

                        Log.d("Measures", txt);


                    }

                    return 0;
                }
                case "UDPBANDWIDTHBUTTON": {
                    int udp_bandwidth_pktsize = Integer.parseInt(preferences.getString(
                            "pack_size_UDP_BANDWIDTH", "1024"));

                    for (int i = 0; i < numberOfConsecutiveTests; i++) {

                        outcome = MainUtils.udpBandwidthMeasure(direction, keyword, CMDPORT,
                                observerAddress, UDPPORT,
                                udp_bandwidth_pktsize, null, NUMTEST_UDP_CAPACITY);
                        String txt = "UDP bandwidth " + i + ": ";
                        if (outcome != 0){
                            txt += "FAILED";
                            i--;
                        }
                        else
                            txt += "SUCCESS";
                        Log.d("Measures", txt);
                    }

                    return 0;
                }
                case "TCPRTTBUTTON": {
                    int tcp_rtt_pktsize = Integer.parseInt(preferences.getString(
                            "pack_size_TCP_RTT", "1"));
                    int tcp_rtt_num_pack = Integer.parseInt(preferences.getString(
                            "num_pack_TCP_RTT", "100"));

                    for (int i = 0; i < numberOfConsecutiveTests; i++) {

                        //SE outcome NON FALLISCE ALLORA LA MAP PASSATA COME ARGOMENTO FORMALE E' CONSISTENTE E
                         // CONTIENE PER OGNI PACCHETTO IL RELATIVO RTT  (NB: TUTTO QUESTO PER OGNI TEST EFFETTUATO!)

                        Map<Integer, Long[]> mappa = new LinkedHashMap<>();

                        outcome = MainUtils.tcpRTTMeasure(direction, keyword, CMDPORT,
                                observerAddress, TCPPORT,
                                tcp_rtt_pktsize, tcp_rtt_num_pack, null, mappa);
                        String txt = "TCP RTT " + i + ": ";
                        if (outcome != 0){
                            txt += "FAILED";
                            i--;
                        }
                        else {
                            txt += "SUCCESS";
                            for (Integer key : mappa.keySet()) {
                                System.out.println(key + " --TCPRTT-- " + mappa.get(key)[0]);
                            }
                        }

                    }
                    return 0;
                }
                case "UDPRTTBUTTON": {
                    int udp_rtt_pktsize = Integer.parseInt(preferences.getString(
                            "pack_size_UDP_RTT", "1"));
                    int udp_rtt_num_pack = Integer.parseInt(preferences.getString(
                            "num_pack_UDP_RTT", "100"));

                    for (int i = 0; i < numberOfConsecutiveTests; i++) {

                        /*SE outcome NON FALLISCE ALLORA LA MAP PASSATA COME ARGOMENTO FORMALE E' CONSISTENTE E
                          CONTIENE PER OGNI PACCHETTO IL RELATIVO RTT
                         */
                        Map<Integer, Long[]> mappa = new LinkedHashMap<>();
                        outcome = MainUtils.udpRTTMeasure(direction, keyword, CMDPORT,
                                observerAddress, UDPPORT,
                                udp_rtt_pktsize, udp_rtt_num_pack, null, mappa);
                        String txt = "UDP RTT " + i + ": ";
                        if (outcome != 0){
                            txt += "FAILED";
                            i--;
                        }
                        else {
                            txt += "SUCCESS";
                            for(Integer key : mappa.keySet()) {
                                System.out.println(key+" ---- "+mappa.get(key)[0]);
                            }
                        }
                        Log.d("Measures", txt);
                    }
                    return 0;
                }
            }
            return 0;
        }


        @Override
        protected void onPostExecute(Integer resultValue) {
            if(resultValue == 0) {
                CharSequence txt = "Successful Measurement! Available in Results section";
                int duration = Toast.LENGTH_SHORT;
                Context ctx = getActivity().getApplicationContext();
                Toast toast = Toast.makeText(ctx, txt, duration);
                toast.show();

                    n_measurement++;

                    //avvio la successiva msiurazione

                    if (n_measurement == 1) avviaMisurazione((short)1);

                    if (n_measurement == 2) avviaMisurazione((short)2);

                    if (n_measurement == 3) avviaMisurazione((short)3);

                    if (n_measurement == 4) {
                        Log.d("MyLog", "MISURAZIONI TERMINATE");
                        stopRadarAnimation();
                        n_measurement = 0;
                        //riattivo il menu
                        menu.getMenu().getItem(0).setEnabled(true);
                        menu.getMenu().getItem(1).setEnabled(true);
                        menu.getMenu().getItem(2).setEnabled(true);
                    }

            } else {
                stopRadarAnimation();
                n_measurement = 0;
                Context ctx = getActivity().getApplicationContext();
                CharSequence txt = "Error in Measurement!Try later...";
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(ctx, txt, duration);
                toast.show();
            }

        }


        @Override
        protected void onCancelled() {
        }
    }


    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.INTERNET);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_INTERNET, REQUEST_INTERNET);

        }
    }




}
