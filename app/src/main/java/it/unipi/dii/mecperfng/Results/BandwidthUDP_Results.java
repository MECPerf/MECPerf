package it.unipi.dii.mecperfng.Results;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DecimalFormat;
import java.util.ArrayList;

import it.unipi.dii.mecperfng.R;

public class BandwidthUDP_Results extends AppCompatActivity {

    private BarChart bandwidthUdpBarChart;
    private BarData bandwidthUdpbarData;
    private BarDataSet bandwidthUdpBarDataSet;
    private ArrayList bandwidthUdpBarEntries;
    private TextView labelDirection;
    private ImageView comeBack;
    private ListView listDetails;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bandwidth_u_d_p__results);

        //per prelevare le stringhe passate dall'activity precedente
        final Bundle oldBundle = getIntent().getExtras();


        listDetails = findViewById(R.id.listDetails);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                R.layout.list_single_textview, R.id.textViewMeasure, oldBundle.getStringArrayList("bandwidthUdp_measurements"));

        listDetails.setAdapter(adapter);


        //prendo il riferimento al titolo della sezione
        labelDirection = findViewById(R.id.bandwidthUdpDirectionLabelGraph);
        labelDirection.setText(oldBundle.getString("bandwidthUdp_direction"));

        //prendo il riferimento alla freccia che mi consente di tornare indietro e al click faccio avviare la standar onBackPressed()
        comeBack = findViewById(R.id.comeBackToGeneralResults);
        comeBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //prendo il riferimento del grafico
        bandwidthUdpBarChart = findViewById(R.id.bandwidthUdpBarChart);

        //creo le entrate
        getEntries();

        //assegno le entrate al data set
        bandwidthUdpBarDataSet = new BarDataSet(bandwidthUdpBarEntries,"TESTS(BANDWIDTH-UDP)");
        bandwidthUdpBarDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        bandwidthUdpBarDataSet.setValueTextColor(Color.BLACK);
        bandwidthUdpBarDataSet.setValueTextSize(16f);
        bandwidthUdpBarDataSet.setColor(getResources().getColor(R.color.myPrimaryColor));

        bandwidthUdpbarData = new BarData(bandwidthUdpBarDataSet);


        bandwidthUdpBarChart.setData(bandwidthUdpbarData);
        bandwidthUdpBarChart.getAxisLeft().setEnabled(true);
        bandwidthUdpBarChart.getAxisLeft().setTextColor(Color.BLACK); //imposto nero il colore del testo nell'asse y sinistro
        bandwidthUdpBarChart.getXAxis().setTextColor(Color.BLACK);//imposto nero il colore del testo nell'asse x (sopra il grafico)
        bandwidthUdpBarChart.getAxisRight().setTextColor(Color.BLACK);//imposto nero il colore del testo nell'asse y destro
        bandwidthUdpBarChart.getLegend().setTextColor(Color.BLACK); //imposto a nero la legenda
        bandwidthUdpBarChart.setGridBackgroundColor(Color.BLACK);
        bandwidthUdpBarChart.getDescription().setEnabled(false); //disattivo label asse x
        //solo quando nella finestra sono visibili 5 barre mostro il label della bandwidth
        bandwidthUdpBarChart.setMaxVisibleValueCount(5);

        //modifico il formato dei valori di ogni barra (voglio l'unità di misura ms accanto ad ogni valore)
        bandwidthUdpBarDataSet.setValueFormatter(new MyValueFormatter());

    }



    private void getEntries(){

        //creo un nuovo array che conterrà le misurazioni
        bandwidthUdpBarEntries = new ArrayList<>();
        Bundle bundle = getIntent().getExtras();
        //prelevo la stringa di interi che in ms sono le varie latenze della misura fatta
        ArrayList<String> bandwidth_measurements = bundle.getStringArrayList("bandwidthUdp_bandwidth");

        for(int i=0; i<bandwidth_measurements.size(); i++) {
            bandwidthUdpBarEntries.add(new BarEntry((i), Float.valueOf(bandwidth_measurements.get(i))));
        }

    }

    public class MyValueFormatter extends ValueFormatter {

        private DecimalFormat mFormat;

        public MyValueFormatter() {
            mFormat = new DecimalFormat("###,###,##0.000"); // use one decimal
        }

        @Override
        public String getFormattedValue(float value) {
            return mFormat.format(value) + "mbps"; // indico l'unità di misura di ogni valore sopra la barra
        }

    }



}














