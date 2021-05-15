package com.whjstech.ylb;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DisplayDataActivity extends AppCompatActivity {

    TextView fileClass;
    TextView createFileTime;
    TextView idNumber;
    TextView hsVersion;

    TextView setValue0;
    TextView setValue1;
    TextView setValue2;
    TextView setValue3;
    TextView setValue4;
    TextView setValue5;

    TextView sampleValue0;
    TextView sampleValue1;
    TextView sampleValue2;
    TextView sampleValue3;
    TextView sampleValue4;
    TextView sampleValue5;

    TextView upLimit;
    TextView voltage;
    TextView interval;
    TextView pointsNumber;

    TextView maxValueTime;
    TextView maxValue;
    TextView minValueTime;
    TextView minValue;

    LineChartView lineChartView;

    private String name;
    private String path;
    private long size;
    private MyFile myFile;
    private FileContent fileContent;
    private List<MyPoint> myPointList;

    public static final int SET_TEXT_VIEW = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_data);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // 全屏显示，隐藏状态栏和导航栏，拉出状态栏和导航栏显示一会儿后消失。
                this.getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            } else {
                // 全屏显示，隐藏状态栏
                this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
            }
        }

        Log.d("log", "onCreate: ok");
        fileClass = (TextView)findViewById(R.id.tv_file_class);
        createFileTime = (TextView)findViewById(R.id.tv_create_file_time);
        idNumber = (TextView)findViewById(R.id.tv_id_number);
        hsVersion = (TextView)findViewById(R.id.tv_hs_version);

        setValue0 = (TextView)findViewById(R.id.tv_set_value_0);
        setValue1 = (TextView)findViewById(R.id.tv_set_value_1);
        setValue2 = (TextView)findViewById(R.id.tv_set_value_2);
        setValue3 = (TextView)findViewById(R.id.tv_set_value_3);
        setValue4 = (TextView)findViewById(R.id.tv_set_value_4);
        setValue5 = (TextView)findViewById(R.id.tv_set_value_5);

        sampleValue0 = (TextView)findViewById(R.id.tv_sample_value_0);
        sampleValue1 = (TextView)findViewById(R.id.tv_sample_value_1);
        sampleValue2 = (TextView)findViewById(R.id.tv_sample_value_2);
        sampleValue3 = (TextView)findViewById(R.id.tv_sample_value_3);
        sampleValue4 = (TextView)findViewById(R.id.tv_sample_value_4);
        sampleValue5 = (TextView)findViewById(R.id.tv_sample_value_5);

        upLimit = (TextView)findViewById(R.id.tv_up_limit);
        voltage = (TextView)findViewById(R.id.tv_voltage);
        interval = (TextView)findViewById(R.id.tv_interval);
        pointsNumber = (TextView)findViewById(R.id.tv_points_number);

        maxValueTime = (TextView)findViewById(R.id.tv_max_value_time);
        maxValue = (TextView)findViewById(R.id.tv_max_value);
        minValueTime = (TextView)findViewById(R.id.tv_min_value_time);
        minValue = (TextView)findViewById(R.id.tv_min_value);

        lineChartView = (LineChartView)findViewById(R.id.line_chart_view);

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        path = intent.getStringExtra("path");
        size = intent.getLongExtra("size",0);

        obtainContent();



        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = SET_TEXT_VIEW;
                handler.sendMessage(message);
            }
        }).start();

    }

    private Handler handler = new Handler(){
        public void handleMessage(Message message){
            switch (message.what){
                case SET_TEXT_VIEW:
                    setTextView();
                    resolveRecordPoints();
                    lineChartView.initData(myPointList);
                    break;
                default:
                    break;
            }
        }
    };

    private void obtainContent(){
        myFile = new MyFile(name,path,size);
        fileContent = myFile.getFileContent();
    }

    private void resolveRecordPoints(){
        myPointList  = new ArrayList<>();
        myPointList.clear();
        int n = fileContent.pointsNumber;
        myPointList.add(new MyPoint(fileContent.startRecordTime,calculateValue(fileContent.recordPoints[0])));
        for (int i = 1; i < n; i++){
            long t =myPointList.get(i-1).getDate().getTime() + fileContent.interval*1000;
            Date date = new Date(t);
            float value = calculateValue(fileContent.recordPoints[i]);
            myPointList.add(new MyPoint(date,value));
        }
    }

    private void setTextView(){
        if(fileContent == null){
            return;
        }
        fileClass.setText(fileContent.fileClass);
        createFileTime.setText(date2String(fileContent.createFileTime));
        idNumber.setText(String.valueOf(fileContent.idNumber));
        hsVersion.setText(fileContent.hsVersion);

        setValue0.setText(setValue2String(fileContent.setValue[0]));
        setValue1.setText(setValue2String(fileContent.setValue[1]));
        setValue2.setText(setValue2String(fileContent.setValue[2]));
        setValue3.setText(setValue2String(fileContent.setValue[3]));
        setValue4.setText(setValue2String(fileContent.setValue[4]));
        setValue5.setText(setValue2String(fileContent.setValue[5]));

        sampleValue0.setText(String.valueOf(fileContent.sampleValue[0]));
        sampleValue1.setText(String.valueOf(fileContent.sampleValue[1]));
        sampleValue2.setText(String.valueOf(fileContent.sampleValue[2]));
        sampleValue3.setText(String.valueOf(fileContent.sampleValue[3]));
        sampleValue4.setText(String.valueOf(fileContent.sampleValue[4]));
        sampleValue5.setText(String.valueOf(fileContent.sampleValue[5]));

        upLimit.setText(setValue2String(fileContent.upperLimitValue)+" kPa");
        voltage.setText(String.valueOf((float) fileContent.voltage/100)+" V");
        interval.setText(String.valueOf(fileContent.interval)+" s");
        pointsNumber.setText(String.valueOf(fileContent.pointsNumber));

        maxValueTime.setText(date2String(fileContent.maxValueTime));
        maxValue.setText(String.valueOf(calculateValue(fileContent.maxValue)) + " kPa");
        minValueTime.setText(date2String(fileContent.minValueTime));
        minValue.setText(String.valueOf(calculateValue(fileContent.minValue)) + " kPa");
    }

    public String setValue2String(int setValue){
        int n = (int)Math.pow(10,fileContent.dotPositon);
        return String.valueOf(setValue/n);
    }

    public float calculateValue(int value){
        int n = (int)Math.pow(10,fileContent.dotPositon);
        return (float)value/n;
    }

    public String date2String(Date date){
        int year = date.getYear()+1900;
        int month = date.getMonth()+1;
        int day = date.getDate();
        int hour = date.getHours();
        int minute = date.getMinutes();
        int second = date.getSeconds();
        String str = new String(year + "/"+addZero(month)+"/"+addZero(day)+" "
                +addZero(hour)+":"+addZero(minute)+":"+addZero(second));
        return str;
    }

    public String addZero(int i){
        String str;
        if(i/10 == 0){
            str = "0"+String.valueOf(i);
        }else {
            str = String.valueOf(i);
        }
        return str;
    }
}

