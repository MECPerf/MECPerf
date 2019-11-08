package it.unipi.dii.mecperfng;



import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;




public class MainActivity extends AppCompatActivity
                          implements NavigationView.OnNavigationItemSelectedListener {
    private static final int REQUEST_INTERNET = 1;
    private static String[] PERMISSIONS_INTERNET = {
                                                       Manifest.permission.INTERNET
                                                   };
    private static final int CMDPORT = 6792;//6789
    private static final int TCPPORT = 6791;//6788
    private static final int UDPPORT = 6790;//6787
    private static final int AGGRPORT = 6766;
    private AsyncCMD mAsyncTask = null;
    private SharedPreferences sp;




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
            String observerAddress = sp.getString("observer_address", "");
            String aggregatorAddress = sp.getString("aggregator_address", "NA");
            int numberOfConsecutiveTests = Integer.parseInt(sp.getString(
                                             "number_of_consecutive_tests", "1"));

            Spinner direction_spinner = findViewById(R.id.direction_spinner);
            String direction = direction_spinner.getItemAtPosition(direction_spinner
                                                .getSelectedItemPosition()).toString();

            int outcome;
            switch (cmd) {
                case "TCPBANDWIDTHBUTTON":{
                    int tcp_bandwidth_pktsize = Integer.parseInt(sp.getString(
                                                         "pack_size_TCP_BANDWIDTH", "1024")),
                           tcp_bandwidth_num_pkt = Integer.parseInt(sp.getString(
                                                         "num_pack_TCP_BANDWIDTH", "1024"));


                    for (int i = 0; i< numberOfConsecutiveTests;i++) {
                        outcome = MainUtils.tcpBandwidthMeasure(direction, keyword, CMDPORT,
                                                    observerAddress, TCPPORT,
                                                    aggregatorAddress, AGGRPORT,
                                                    tcp_bandwidth_pktsize, tcp_bandwidth_num_pkt);
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
                    int udp_bandwidth_pktsize = Integer.parseInt(sp.getString(
                            "pack_size_UDP_BANDWIDTH", "1024"));

                    for (int i = 0; i < numberOfConsecutiveTests; i++) {
                        outcome = MainUtils.udpBandwidthMeasure(direction, keyword, CMDPORT,
                                observerAddress, UDPPORT,
                                aggregatorAddress, AGGRPORT,
                                udp_bandwidth_pktsize);
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
                    int tcp_rtt_pktsize = Integer.parseInt(sp.getString(
                            "pack_size_TCP_RTT", "1"));
                    int tcp_rtt_num_pack = Integer.parseInt(sp.getString(
                            "num_pack_TCP_RTT", "100"));

                    for (int i = 0; i < numberOfConsecutiveTests; i++) {
                        outcome = MainUtils.tcpRTTMeasure(direction, keyword, CMDPORT,
                                observerAddress, TCPPORT,
                                aggregatorAddress, AGGRPORT,
                                tcp_rtt_pktsize, tcp_rtt_num_pack);
                        String txt = "TCP RTT " + i + ": ";
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
                case "UDPRTTBUTTON": {
                    int udp_rtt_pktsize = Integer.parseInt(sp.getString(
                            "pack_size_UDP_RTT", "1"));
                    int udp_rtt_num_pack = Integer.parseInt(sp.getString(
                            "num_pack_UDP_RTT", "100"));

                    for (int i = 0; i < numberOfConsecutiveTests; i++) {
                        outcome = MainUtils.udpRTTMeasure(direction, keyword, CMDPORT,
                                observerAddress, UDPPORT,
                                aggregatorAddress, AGGRPORT,
                                udp_rtt_pktsize, udp_rtt_num_pack);
                        String txt = "UDP RTT " + i + ": ";
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
                }

            return 0;
        }


        @Override
        protected void onPostExecute(Integer resultValue) {
            if(resultValue == 0) {
                    CharSequence txt = "Successful Measurement! Available in Results section";
                    int duration = Toast.LENGTH_SHORT;
                    Context ctx = getApplicationContext();
                    Toast toast = Toast.makeText(ctx, txt, duration);
                    toast.show();

                } else {
                    Context ctx = getApplicationContext();
                    CharSequence txt = "Error in Measurement!Try later...";
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(ctx, txt, duration);
                    toast.show();
                }

            enableAllButton();
        }


        @Override
        protected void onCancelled() {
            mAsyncTask = null;
        }


        private void enableAllButton(){

            Button TCPButton  = findViewById(R.id.TCPBandwidthbutton);
            TCPButton.setEnabled(true);

            Button TCPRTTButton = findViewById(R.id.TCPRTTbutton);
            TCPRTTButton.setEnabled(true);

            Button UDPButton = findViewById(R.id.UDPBandwidthbutton);
            UDPButton.setEnabled(true);

            Button UDPRTTButton = findViewById(R.id.UDPRTTbutton);
            UDPRTTButton.setEnabled(true);
        }
    }


    /**
     * Checks if the app has permission to write to device storage
     * <p>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity .
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.INTERNET);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(activity, PERMISSIONS_INTERNET, REQUEST_INTERNET);

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        Spinner function_spinner = findViewById(R.id.direction_spinner);
        CharSequence[] fun = getResources().getStringArray((R.array.function_array));
        ArrayAdapter<CharSequence> adfun = new ArrayAdapter<>(this,
                                                         android.R.layout.simple_spinner_item, fun);
        adfun.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        function_spinner.setAdapter(adfun);

        verifyStoragePermissions(this);

        Button TCPButton = findViewById(R.id.TCPBandwidthbutton);

        TCPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableAllButton();
                final EditText editText = findViewById(R.id.keyWordEditView);
                String keyword = editText.getText().toString();
                if(keyword.equals("")){
                    keyword="DEFAULT";
                }
                mAsyncTask = new AsyncCMD("TCPBANDWIDTHBUTTON", keyword);
                mAsyncTask.execute((Void) null);
                Log.d("TASK", "Task Eseguito");
            }
        });

        Button UDPButton = findViewById(R.id.UDPBandwidthbutton);
        UDPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableAllButton();

                final EditText editText = findViewById(R.id.keyWordEditView);
                String keyword = editText.getText().toString();
                if(keyword.equals("")){
                    keyword="DEFAULT";
                }
                mAsyncTask = new AsyncCMD("UDPBANDWIDTHBUTTON", keyword);
                mAsyncTask.execute((Void) null);
                Log.d("TASK", "Task Eseguito");
            }
        });

        Button TCPRTTButton = findViewById(R.id.TCPRTTbutton);
        TCPRTTButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableAllButton();

                final EditText editText = findViewById(R.id.keyWordEditView);
                String keyword = editText.getText().toString();
                if(keyword.equals("")){
                    keyword="DEFAULT";
                }

                mAsyncTask = new AsyncCMD("TCPRTTBUTTON", keyword);
                mAsyncTask.execute((Void) null);
            }
        });

        Button UDPRTTButton = findViewById(R.id.UDPRTTbutton);
        UDPRTTButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disableAllButton();
                final EditText editText = findViewById(R.id.keyWordEditView);

                String keyword = editText.getText().toString();
                if(keyword.equals("")){
                    keyword="DEFAULT";
                }

                mAsyncTask = new AsyncCMD("UDPRTTBUTTON", keyword);
                mAsyncTask.execute((Void) null);
            }
        });
    }


    private void disableAllButton(){
        Log.d("TEST", "DISABLE ALL BUTTON");
        Button TCPButton = findViewById(R.id.TCPBandwidthbutton);
        TCPButton.setEnabled(false);

        Button UDPButton = findViewById(R.id.UDPBandwidthbutton);
        UDPButton.setEnabled(false);

        Button TCPRTTButton = findViewById(R.id.TCPRTTbutton);
        TCPRTTButton.setEnabled(false);

        Button UDPRTTButton = findViewById(R.id.UDPRTTbutton);
        UDPRTTButton.setEnabled(false);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem it = menu.findItem(R.id.action_settings);
        it.setVisible(false);
        super.onCreateOptionsMenu(menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_results) {
            startActivity(new Intent(this, ResultsActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
