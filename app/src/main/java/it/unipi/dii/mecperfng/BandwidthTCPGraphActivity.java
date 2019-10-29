package it.unipi.dii.mecperfng;

/*
This code was implemented by Enrico Alberti.
The use of this code is permitted by BSD licenses
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class BandwidthTCPGraphActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph_activity);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        String x = getIntent().getStringExtra("EXTRA_X");
        String y = getIntent().getStringExtra("EXTRA_Y");


        String[] splitted_x = x.split("#");
        String[] splitted_y = y.split("#");


        //TODO sostituire i float con i DOuble
        //TODO in realtà influenzano solo le cifre decimali, quindi si può lasciare così

        float[] xvalues = new float[splitted_x.length];
        float[] yvalues = new float[splitted_y.length];

        for(int i =0; i<splitted_x.length; i++){
            xvalues[i] = Float.parseFloat(splitted_x[i]);
        }

        for(int i =0; i<splitted_y.length; i++){
            yvalues[i] = Float.parseFloat(splitted_y[i]);
        }


        //DLN

        final LinearLayout buttonGraph = new LinearLayout(this);
        buttonGraph.setOrientation(LinearLayout.VERTICAL);

        setContentView(buttonGraph);

        plot2d graph = new plot2d(getApplicationContext(), xvalues, yvalues, 1);

        buttonGraph.addView(graph);
        setContentView(buttonGraph);
        buttonGraph.invalidate();

        //END DLN

    }



    //DLN
    //This class was implemented by Ankit Srivastava.
    public class plot2d extends View {

        private Paint paint;
        private float[] xvalues,yvalues;
        private float maxx,maxy,minx,miny,locxAxis,locyAxis;
        private int vectorLength;
        private int axes = 1;

        // ---------------------------------------------------------------------------------------------
        // -------------------------- Constructor of plot class ----------------------------------------
        // ---------------------------------------------------------------------------------------------
        public plot2d(Context context, float[] xvalues, float[] yvalues, int axes) {
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
                paint.setColor(Color.RED);
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

        private int[] toPixel(float pixels, float min, float max, float[] value) {

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
        private void getAxes(float[] xvalues, float[] yvalues) {

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

        private int toPixelInt(float pixels, float min, float max, float value) {

            double p;
            int pint;
            p = .1*pixels+((value-min)/(max-min))*.8*pixels;
            pint = (int)p;
            return (pint);
        }

        // ---------------------------------------------------------------------------------------------
        // ----------------------- Get value for pixel rapresentation ----------------------------------
        // ---------------------------------------------------------------------------------------------
        private float getMax(float[] v) {
            float largest = v[0];
            for (int i = 0; i < v.length; i++)
                if (v[i] > largest)
                    largest = v[i];
            return largest;
        }

        private float getMin(float[] v) {
            float smallest = v[0];
            for (int i = 0; i < v.length; i++)
                if (v[i] < smallest)
                    smallest = v[i];
            return smallest;
        }

    }

    //END DLN
}
