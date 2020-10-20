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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DecimalFormat;
import java.util.ArrayList;

import it.unipi.dii.mecperfng.R;

public class Rtt_Results extends AppCompatActivity {

    private BarChart rttBarChart;
    private BarData barData;
    private BarDataSet barDataSet;
    private ArrayList barEntries;
    private TextView labelY, labelDirection;
    private ImageView comeBack;
    private ListView listDetails;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtt__results);

        //per prelevare le stringhe passate dall'activity precedente
        final Bundle oldBundle = getIntent().getExtras();

        listDetails = findViewById(R.id.listDetailsRtt);
        //measureListView.setEnabled(false);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                R.layout.list_single_textview, R.id.textViewMeasure, oldBundle.getStringArrayList("rtt_measurements"));

        listDetails.setAdapter(adapter);

        //prendo il riferimento del label dell'asse y
        labelY = findViewById(R.id.rttYProtocol);
        //se il protocollo è TCP setto il label a RTTTCP(ms) oppure se è UDP lo setto come RTTUDP(ms)
        labelY.setText("RTT"+oldBundle.getString("rtt_protocol")+"(ms)");

        //prendo il riferimento al titolo della sezione
        labelDirection = findViewById(R.id.rttDirectionLabelGraph);
        labelDirection.setText(oldBundle.getString("rtt_direction"));

        //prendo il riferimento alla freccia che mi consente di tornare indietro e al click faccio avviare la standar onBackPressed()
        comeBack = findViewById(R.id.comeBackToGeneralResults);
        comeBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //prendo il riferimento del grafico
        rttBarChart = findViewById(R.id.rttBarChart);

        //creo le entrate
        getEntries();

        //assegno le entrate al data set
        barDataSet = new BarDataSet(barEntries,"TESTS(RTT-"+oldBundle.getString("rtt_protocol")+")");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(16f);
        barDataSet.setColor(getResources().getColor(R.color.myPrimaryColor));

        barData = new BarData(barDataSet);

        rttBarChart.setData(barData);
        rttBarChart.getAxisLeft().setEnabled(true);
        rttBarChart.getAxisLeft().setTextColor(Color.BLACK); //imposto nero il colore del testo nell'asse y sinistro
        rttBarChart.getXAxis().setTextColor(Color.BLACK);//imposto nero il colore del testo nell'asse x (sopra il grafico)
        rttBarChart.getAxisRight().setTextColor(Color.BLACK);//imposto nero il colore del testo nell'asse y destro
        rttBarChart.getLegend().setTextColor(Color.BLACK); //imposto a nero la legenda
        rttBarChart.getDescription().setEnabled(false); //disattivo label asse x
        //solo quando nella finestra sono visibili 17 barre mostro il label della latenza
        rttBarChart.setMaxVisibleValueCount(17);

        //modifico il formato dei valori di ogni barra (voglio l'unità di misura ms accanto ad ogni valore)
        barDataSet.setValueFormatter(new MyValueFormatter());

    }

    private void getEntries(){

        //creo un nuovo array che conterrà le misurazioni
        barEntries = new ArrayList<>();
        Bundle bundle = getIntent().getExtras();
        //prelevo la stringa di interi che in ms sono le varie latenze della misura fatta
        ArrayList<Integer> latency_measurements = bundle.getIntegerArrayList("rtt_latency");

        for(int i=0; i<latency_measurements.size(); i++)
            barEntries.add(new BarEntry((i), latency_measurements.get(i)));


    }

    public class MyValueFormatter extends ValueFormatter {

        private DecimalFormat mFormat;

        public MyValueFormatter() {
            mFormat = new DecimalFormat("###,###,##0.0"); // use one decimal
        }

        @Override
        public String getFormattedValue(float value) {
            return mFormat.format(value) + "ms"; // indico l'unità di misura di ogni valore sopra la barra
        }

    }



}














