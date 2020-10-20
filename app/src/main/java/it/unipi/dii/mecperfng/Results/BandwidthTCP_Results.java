package it.unipi.dii.mecperfng.Results;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import it.unipi.dii.mecperfng.Model.TcpPackResult;
import it.unipi.dii.mecperfng.R;

public class BandwidthTCP_Results extends AppCompatActivity {

    private BarChart bandwidthTcpBarChart;
    private BarData bandwidthTcpbarData;
    private BarDataSet bandwidthTcpBarDataSet;
    private ArrayList bandwidthTcpBarEntries;
    private TextView labelDirection, avgLabel, labelDetails;
    private ImageView comeBack, showDetails;
    private boolean detailsOpened; //se true indica che i dettagli sono mostrati
    private Guideline guidelineUp, guidelineDown;
    private ListView listDetails;
    private TcpPackResult tcpPackResult;

    //loading
    private ProgressBar progressTcpBar;
    private LinearLayout loadingTcpScreen;

    //cdf
    private List<Double> x;
    private List<Double> y;
    String x1,y1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bandwidth_t_c_p__results);

        //per prelevare le stringhe passate dall'activity precedente
        final Bundle oldBundle = getIntent().getExtras();

        progressTcpBar = findViewById(R.id.progressTcpBar);
        loadingTcpScreen = findViewById(R.id.loadingTcpScreen);

        guidelineUp = findViewById(R.id.guidelineUpDetails);
        guidelineDown = findViewById(R.id.guidelineDownDetails);

        detailsOpened = false; //inizialmente i dettagli non sono mostrati
        labelDetails = findViewById(R.id.detailsLabel);
        showDetails = findViewById(R.id.showMoreDetails);
        labelDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //se il menù non è aperto aprilo
                Log.d("MENUDETAILS",String.valueOf(detailsOpened));
                if(!detailsOpened){
                    detailsOpened = true;
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) guidelineDown.getLayoutParams();
                    params.guidePercent = 1f;
                    guidelineDown.setLayoutParams(params);
                    showDetails.setBackgroundResource(R.drawable.ic_show_more);
                } else {
                    detailsOpened = false;
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) guidelineDown.getLayoutParams();
                    params.guidePercent = 0.6f;
                    guidelineDown.setLayoutParams(params);
                    showDetails.setBackgroundResource(R.drawable.ic_show_less);
                }
            }
        });

        showDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //se il menù non è aperto aprilo
                if(!detailsOpened){
                    detailsOpened = true;
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) guidelineDown.getLayoutParams();
                    params.guidePercent = 1f;
                    guidelineDown.setLayoutParams(params);
                    showDetails.setBackgroundResource(R.drawable.ic_show_more);
                } else {
                    detailsOpened = false;
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) guidelineDown.getLayoutParams();
                    params.guidePercent = 0.6f;
                    guidelineDown.setLayoutParams(params);
                    showDetails.setBackgroundResource(R.drawable.ic_show_less);
                }
            }
        });


        listDetails = findViewById(R.id.listDetailsTcp);


        //prendo il riferimento al titolo della sezione
        labelDirection = findViewById(R.id.bandwidthTcpDirectionLabelGraph);

        //prendo il riferimento della label AVG
        avgLabel = findViewById(R.id.avgLabel);

        //prendo il riferimento alla freccia che mi consente di tornare indietro e al click faccio avviare la standar onBackPressed()
        comeBack = findViewById(R.id.bandwidthTCPComeBackToGeneralResults);
        comeBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        new TakeTcpBandwidthResultsIntoFileTask().execute();

    }

    private void showLoadingScreen(){

        progressTcpBar.setVisibility(View.VISIBLE);
        loadingTcpScreen.setVisibility(View.VISIBLE);

    }

    private void hideLoadingScreen(){
        progressTcpBar.setVisibility(View.GONE);
        loadingTcpScreen.setVisibility(View.GONE);
    }

    //------------------ async task per prelevare da file e far partire il tutto ----------------

    private class TakeTcpBandwidthResultsIntoFileTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {

            showLoadingScreen();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            FileInputStream fis = null;
            try {
                fis = getApplicationContext().openFileInput("tcpPack");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            ObjectInputStream is = null;
            try {
                is = new ObjectInputStream(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }
            tcpPackResult = null;
            try {
                tcpPackResult = (TcpPackResult) is.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                    R.layout.list_single_textview, R.id.textViewMeasure, tcpPackResult.getListMeasurements());

            listDetails.setAdapter(adapter);

            labelDirection.setText(tcpPackResult.getDirection());

            avgCountAnimation(tcpPackResult.getAvg());

            createGraphs();

        }
    }

    private void createGraphs(){
        //creo le entrate
        getEntries();

        //assegno le entrate al data set
        bandwidthTcpBarDataSet = new BarDataSet(bandwidthTcpBarEntries,"TESTS(BANDWIDTH-TCP)");
        bandwidthTcpBarDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        bandwidthTcpBarDataSet.setValueTextColor(Color.BLACK);
        bandwidthTcpBarDataSet.setValueTextSize(16f);
        bandwidthTcpBarDataSet.setColor(getResources().getColor(R.color.myPrimaryColor));

        bandwidthTcpbarData = new BarData(bandwidthTcpBarDataSet);

        bandwidthTcpBarChart = new BarChart(this);
        bandwidthTcpBarChart.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        LinearLayout containerBarChart = findViewById(R.id.containerTcpBarChart);
        containerBarChart.addView(bandwidthTcpBarChart);

        bandwidthTcpBarChart.setData(bandwidthTcpbarData);
        bandwidthTcpBarChart.getAxisLeft().setEnabled(true);
        bandwidthTcpBarChart.getAxisLeft().setTextColor(Color.BLACK); //imposto nero il colore del testo nell'asse y sinistro
        bandwidthTcpBarChart.getXAxis().setTextColor(Color.BLACK);//imposto nero il colore del testo nell'asse x (sopra il grafico)
        bandwidthTcpBarChart.getAxisRight().setTextColor(Color.BLACK);//imposto nero il colore del testo nell'asse y destro
        bandwidthTcpBarChart.getLegend().setTextColor(Color.BLACK); //imposto a nero la legenda
        bandwidthTcpBarChart.getDescription().setEnabled(false); //disattivo label asse x
        //solo quando nella finestra sono visibili 5 barre mostro il label della bandwidth
        bandwidthTcpBarChart.setMaxVisibleValueCount(5);

        //modifico il formato dei valori di ogni barra (voglio l'unità di misura ms accanto ad ogni valore)
        bandwidthTcpBarDataSet.setValueFormatter(new BandwidthTCP_Results.MyValueFormatter());


        //GRAFICO DEL CDF----------------------------------------------------------------------------
        List<Double> listBandwidthCDF = new ArrayList<Double>();

        for (String s : tcpPackResult.getListBandwidthForCdf()) {
            // Apply formatting to the string if necessary
            listBandwidthCDF.add(Double.valueOf(s));
        }
        CDFDiagram cdfDiagram = new CDFDiagram(listBandwidthCDF);
        cdfDiagram.showDiagram();

        hideLoadingScreen();

    }

    private void avgCountAnimation(String end){
            String justAvg = end.substring(0,end.indexOf("M")).replace(",",".");
            ValueAnimator animator = ValueAnimator.ofFloat(0,Float.parseFloat(justAvg));
            animator.setDuration(1500);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    avgLabel.setText(animation.getAnimatedValue().toString()+" MB/s");
                }
            });
            animator.start();
    }

    private void getEntries(){

        //creo un nuovo array che conterrà le misurazioni
        bandwidthTcpBarEntries = new ArrayList<>();
        Bundle bundle = getIntent().getExtras();
        //prelevo la stringa di interi che in ms sono le varie latenze della misura fatta
          List<String> bandwidth_measurements = tcpPackResult.getListBandwidth();

        for(int i=0; i<bandwidth_measurements.size(); i++) {
            bandwidthTcpBarEntries.add(new BarEntry((i), Float.valueOf(bandwidth_measurements.get(i))));
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


    //CDF-------------------------------------------------------------------------------------------
    private class CDFDiagram {
        private List<Double> istantBandwidth;

        public CDFDiagram(List<Double> tmp) {

            this.istantBandwidth = tmp;
        }

        public void showDiagram() {
            Map<Double, Double> orderedMap = new LinkedHashMap<>();
            double len_previous = 1;
            int size_istantBandwidth = 0;

            orderedMap.put(0.0, 0.0);
            for (Double tmp : istantBandwidth) {
                orderedMap.put(tmp, len_previous / istantBandwidth.size());
                len_previous++;
                size_istantBandwidth++;
            }

            x= new ArrayList<>();
            y= new ArrayList<>();


            int i=0;
            for (Map.Entry<Double, Double> entry : orderedMap.entrySet()) {

                x1 += entry.getKey().toString() + "#";
                y1 += entry.getValue().toString() + "#";
                x.add(entry.getKey());
                y.add(entry.getValue());
                i++;
            }

            new BandwidthTCPGraphActivity();
        }
        public class BandwidthTCPGraphActivity{

            public BandwidthTCPGraphActivity(){


            double[] xvalues = new double[x.size()];
            double[] yvalues = new double[y.size()];

            for(int i =0; i<x.size(); i++){
                xvalues[i] = x.get(i);
            }

            for(int i =0; i<y.size(); i++){
                yvalues[i] = y.get(i);
            }

                final LinearLayout graphArea = findViewById(R.id.graph_cdf_linear_layout);
                graphArea.setOrientation(LinearLayout.VERTICAL);

                Plot2d graph = new Plot2d(getApplicationContext(), xvalues,yvalues, 1);

                graphArea.addView(graph);

                graphArea.invalidate();

            }




            //DLN
            //This class was implemented by Ankit Srivastava.
            public class Plot2d extends View {

                private Paint paint;
                private double[] xvalues,yvalues;
                private double maxx,maxy,minx,miny,locxAxis,locyAxis;
                private int vectorLength;
                private int axes = 1;

                // ---------------------------------------------------------------------------------------------
                // -------------------------- Constructor of plot class ----------------------------------------
                // ---------------------------------------------------------------------------------------------
                public Plot2d(Context context, double[] xvalues, double[] yvalues, int axes) {
                    super(context);
                    this.xvalues=xvalues;
                    this.yvalues=yvalues;
                    this.axes=axes;
                    vectorLength = xvalues.length;
                    paint = new Paint();

                    getAxes(xvalues, yvalues);

                }

                // ---------------------------------------------------------------------------------------------
                // ------------------------------ Draw on canvas -----------------------------------------------
                // ---------------------------------------------------------------------------------------------
                @Override
                protected void onDraw(Canvas canvas) {

                    float canvasHeight = getHeight();
                    float canvasWidth = getWidth();

                    int[] xvaluesInPixels = toPixel(canvasWidth, minx, maxx, xvalues);
                    int[] yvaluesInPixels = toPixel(canvasHeight, miny, maxy, yvalues);
                    int locxAxisInPixels = toPixelInt(canvasHeight, miny, maxy, locxAxis);
                    int locyAxisInPixels = toPixelInt(canvasWidth, minx, maxx, locyAxis);

                    paint.setStrokeWidth(8);
                    canvas.drawARGB(255, 255, 255, 255);
                    for (int i = 0; i < vectorLength-1; i++) {
                        paint.setColor(getResources().getColor(R.color.myPrimaryColor));
                        canvas.drawLine(xvaluesInPixels[i],canvasHeight-yvaluesInPixels[i],
                                xvaluesInPixels[i+1],canvasHeight-yvaluesInPixels[i+1],paint);

                        //TODO provare (startX, StartY, x+1, y+1);
                    }

                    //DRAW AXIS
                    paint.setColor(Color.BLACK);
                    canvas.drawLine(0,canvasHeight-locxAxisInPixels,canvasWidth,
                            canvasHeight-locxAxisInPixels,paint);
                    canvas.drawLine(locyAxisInPixels,0,locyAxisInPixels,canvasHeight,paint);

                    //Automatic axes markings, modify n to control the number of axes labels
                    if (axes!=0){
                        float temp = 0.0f;
                        int n=5;
                        paint.setTextAlign(Paint.Align.CENTER);
                        paint.setTextSize(40.0f);
                        for (int i=1;i<=n;i++){
                            //temp = Math.round(10*(minx+(i-1)*(maxx-minx)/n))/10;
                            //TODO TEST
                            temp = Math.round((minx+(i-1)*(maxx-minx)/n));

                            String formatted_result = String.format("%.2f", temp/1024);

                            //TODO END TEST
                            canvas.drawText(""+formatted_result, (float)toPixelInt(canvasWidth, minx, maxx, temp),
                                    canvasHeight-locxAxisInPixels+40, paint);

                            //ASSE Y SEMPRE o 0.0 o 1.0
                        }

                        canvas.drawText("MB/s", (float)toPixelInt(canvasWidth, minx, maxx, maxx),
                                canvasHeight-locxAxisInPixels+40, paint);   //UNITA' di MISURA (Ultimo valore di X)

                        canvas.drawText(""+maxy, locyAxisInPixels-40,
                                canvasHeight-(float)toPixelInt(canvasHeight, miny, maxy, maxy), paint); //ULTIMA DI Y (1.0)

                        canvas.drawText("0.5", locyAxisInPixels-40,canvasHeight/2, paint);
                    }


                }

                private int[] toPixel(double pixels, double min, double max, double[] value) {

                    double[] p = new double[value.length];
                    int[] pint = new int[value.length];

                    for (int i = 0; i < value.length; i++) {
                        p[i] = .1*pixels+((value[i]-min)/(max-min))*.8*pixels;
                        pint[i] = (int)p[i];
                    }

                    return (pint);
                }

                // ---------------------------------------------------------------------------------------------
                // ------------------------------ Autoscaling axes ---------------------------------------------
                // ---------------------------------------------------------------------------------------------
                private void getAxes(double[] xvalues, double[] yvalues) {

                    minx=getMin(xvalues);
                    miny=getMin(yvalues);
                    maxx=getMax(xvalues);
                    maxy=getMax(yvalues);

                    Log.d("TEST", "MAX_X: " + maxx);


                    if (minx>=0)
                        locyAxis=minx;
                    else if (minx<0 && maxx>=0)
                        locyAxis=0;
                    else
                        locyAxis=maxx;

                    if (miny>=0)
                        locxAxis=miny;
                    else if (miny<0 && maxy>=0)
                        locxAxis=0;
                    else
                        locxAxis=maxy;

                }

                private int toPixelInt(double pixels, double min, double max, double value) {

                    double p;
                    int pint;
                    p = .1*pixels+((value-min)/(max-min))*.8*pixels;
                    pint = (int)p;
                    return (pint);
                }

                // ---------------------------------------------------------------------------------------------
                // ----------------------- Get value for pixel rapresentation ----------------------------------
                // ---------------------------------------------------------------------------------------------
                private double getMax(double[] v) {
                    double largest = v[0];
                    for (int i = 0; i < v.length; i++)
                        if (v[i] > largest)
                            largest = v[i];
                    return largest;
                }

                private double getMin(double[] v) {
                    double smallest = v[0];
                    for (int i = 0; i < v.length; i++)
                        if (v[i] < smallest)
                            smallest = v[i];
                    return smallest;
                }

            }

            //END DLN
        }
    }


}
