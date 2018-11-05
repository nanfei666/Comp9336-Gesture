package com.example.apple.gesture_communication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.jjoe64.graphview.series.Series;

import org.achartengine.model.XYValueSeries;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class Gesture_communication extends AppCompatActivity implements SensorEventListener{
    private SensorManager mySensormanager;
    private Sensor Light_Sensor;
    private Sensor Proximity_Sensor;
    private LineGraphSeries<DataPointInterface> Series;
    private double graph2LastXValue = 5d;
    private int lastx=0;
    private Handler mHandler = new Handler();
    private float compared_value=0;
    GraphView graph;
    long startTime;
    private boolean flage;
    private boolean mflage;
//    private int Down_Up_counter=0;
    private List<String>Down_Up_counter;
    private List<Float>luminance;
    private int Down_up_cont;
    private boolean Proximity;
    private Timer timer = new Timer();
    private Handler handler;
    private TextView tv;
    private TextView tv_2;
    private int changetime;
    private float average;
    private double threhold =0.78;



    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture_communication);
        tv=(TextView)findViewById(R.id.counter);
        tv_2=(TextView)findViewById(R.id.TV_2);
        tv.setGravity(Gravity.CENTER);
        Down_Up_counter = new ArrayList<>();
        luminance = new ArrayList<>();

        mySensormanager= (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        Light_Sensor = mySensormanager.getDefaultSensor(Sensor.TYPE_LIGHT);
        Proximity_Sensor = mySensormanager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        graph = (GraphView) findViewById(R.id.graph);
        //Series = new PointsGraphSeries<>();
        Series = new LineGraphSeries<>();
        Viewport viewport = graph.getViewport();

        viewport.setMaxY(300);
        viewport.setMinY(0);
        viewport.setMaxX(100);
        viewport.setMinX(0);

        graph.getViewport().setYAxisBoundsManual(true);

        graph.getViewport().setXAxisBoundsManual(true);

        graph.setTitle("time series of light sensor data");
        graph.getGridLabelRenderer().setVerticalAxisTitle("sensor values");
        graph.getViewport().setScrollable(true);

        graph.addSeries(Series);

        handler = new Handler(){
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                int arg1 = msg.arg1;
                int arg2 =msg.arg2;
                String name=(String)msg.obj;
                tv_2.setText(name+arg1);
                tv.setText("Count: "+arg2);
            }
        };
        five_second_counter();


    }

    private void addEntry(final Float Y){

        Series.appendData(new DataPoint(lastx++,Y),true,100);
        TextView v = (TextView) findViewById(R.id.textView);
        v.setText("luminance: "+Float.toString(Y));


    }

    @Override
    public final void onAccuracyChanged(Sensor mSensor, int accuracy){

    }

    @Override
    public final void onSensorChanged(SensorEvent event){
        switch (event.sensor.getType()) {

            case Sensor.TYPE_LIGHT:
                changetime++;
            float acc = event.accuracy;
            final float Y = event.values[0];

            if(luminance.size()<22){
                luminance.add(Y);
                }
            else {
                    float sum =0;
                    for(int i=0;i<=21;i++){
                         sum = sum+luminance.get(i);
                    }
                    average = sum/22;
                    System.out.println(average);
                    luminance.remove(0);
                    luminance.add(Y);
                    if(Y<average*threhold){
                        flage=true;

                    }
                    if(flage&&Y>average*threhold){
                        flage=false;
                        //Proximity=false;
                        Down_Up_counter.add("count one");

                    }


            }
            addEntry(Y);



        }

        }




    protected void onResume(){
        super.onResume();
        mySensormanager.registerListener(this,Light_Sensor,SensorManager.SENSOR_DELAY_FASTEST);
        mySensormanager.registerListener(this,Proximity_Sensor,SensorManager.SENSOR_DELAY_FASTEST);

    }

    protected void onPause(){
        super.onPause();
        mySensormanager.unregisterListener(this);
    }



    @Override
    protected void onStop(){
        mflage = false;
//        stopTimer();
        super.onStop();
    }
    private void five_second_counter(){
        new Thread(new Runnable() {
            int count =1;
            @Override
            public void run() {
                while (count<6){
                    Down_up_cont=Down_Up_counter.size();
                    Message message=Message.obtain();

                    message.arg1 = count;
                    message.arg2 = Down_up_cont;

                    message.obj="Timer: ";

                    handler.sendMessage(message);
                    count++;
                    try{
                        Thread.sleep(1000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
                while (count>=6){
                    Down_Up_counter.clear();
                    count=1;
                    run();
                }
            }
        }).start();

    }


}
