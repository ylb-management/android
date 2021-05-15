package com.whjstech.ylb;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

public class NormalLoginActivity extends AppCompatActivity {
    private ArrayList listArea = new ArrayList(Arrays.asList("请选择地区"));
    private ArrayList listStation = new ArrayList(Arrays.asList("请选择站点"));
    private ArrayList listId = new ArrayList(Arrays.asList("请选择表号"));
    private ArrayList addtext = new ArrayList();
    private String Area;
    private String Station;
    private String Id;
    private Spinner spinnerArea;
    private Spinner spinnerStation;
    private Spinner spinnerId;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_login);


        ArrayAdapter<ArrayList> adapterArea = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, listArea);
        ArrayAdapter<ArrayList> adapterStation = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, listStation);
        ArrayAdapter<ArrayList> adapterId = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, listId);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String url = bundle.getString("url");
        String user = bundle.getString("user");
        String password = bundle.getString("password");


        //从MySQL获取地区名
        new Thread(() -> {
            Connection cn = null;
            Statement st = null;
            ResultSet rs = null;
            try {
                cn = DriverManager.getConnection(url, user, password);
                st = cn.createStatement();
                rs = st.executeQuery("select distinct 区名 from ylb_data");
                while (rs.next()) {
                    listArea.add(rs.getString("区名"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("查找区名失败");
            }finally {
                //释放资源(按顺序从后到前:查询结果集-数据库操作对象-连接对象)
                if (rs != null){
                    try{
                        rs.close();
                    }catch(SQLException e){
                        e.printStackTrace();
                    }
                }
                if (st != null){
                    try{
                        st.close();
                    }catch(SQLException e){
                        e.printStackTrace();
                    }
                }
                if (cn != null){
                    try{
                        cn.close();
                    }catch(SQLException e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        //地区名动态数组放在下拉框里面
        adapterArea.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinnerArea = findViewById(R.id.sp_Area);
        spinnerArea.setAdapter(adapterArea);
        spinnerArea.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("HandlerLeak")
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Area = spinnerArea.getSelectedItem().toString();
                listStation.clear();
                listStation.add("请选择站点");
                new Thread(() -> {
                    Connection cn = null;
                    Statement st = null;
                    ResultSet rs = null;
                    try {
                        cn = DriverManager.getConnection(url, user, password);
                        st = cn.createStatement();
                        rs = st.executeQuery("select distinct 站名 from ylb_data where 区名 = '" + Area + "'");
                        while (rs.next()) {
                            addtext.add(rs.getString("站名"));
                        }
                        Message message = new Message();
                        message.obj = addtext;
                        mHandler.sendMessage(message);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        System.out.println("查找站名失败");
                    }finally {
                        //释放资源(按顺序从后到前:查询结果集-数据库操作对象-连接对象)
                        if (rs != null){
                            try{
                                rs.close();
                            }catch(SQLException e){
                                e.printStackTrace();
                            }
                        }
                        if (st != null){
                            try{
                                st.close();
                            }catch(SQLException e){
                                e.printStackTrace();
                            }
                        }
                        if (cn != null){
                            try{
                                cn.close();
                            }catch(SQLException e){
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
                mHandler = new Handler(){
                    @Override
                    public void handleMessage(@NonNull Message msg) {
                        super.handleMessage(msg);
                        addtext = (ArrayList) msg.obj;
                        listStation.addAll(addtext);
                        addtext.clear();
                        spinnerStation.setSelection(0);
                        spinnerId.setSelection(0);
                    }
                };
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        adapterStation.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinnerStation = findViewById(R.id.sp_Station);
        spinnerStation.setAdapter(adapterStation);
        spinnerStation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("HandlerLeak")
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Station = spinnerStation.getSelectedItem().toString();
                listId.clear();
                listId.add("请选择表号");
                new Thread(() -> {
                    Connection cn = null;
                    Statement st = null;
                    ResultSet rs = null;
                    try {
                        cn = DriverManager.getConnection(url, user, password);
                        st = cn.createStatement();
                        rs = st.executeQuery("select distinct 表号 from ylb_data where 区名 = '" + Area + "' and 站名 = '"+ Station +"'");
                        while (rs.next()) {
                            addtext.add(rs.getString("表号"));
                        }
                        Message message = new Message();
                        message.obj = addtext;
                        mHandler.sendMessage(message);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        System.out.println("查找表号失败");
                    }finally {
                        //释放资源(按顺序从后到前:查询结果集-数据库操作对象-连接对象)
                        if (rs != null){
                            try{
                                rs.close();
                            }catch(SQLException e){
                                e.printStackTrace();
                            }
                        }
                        if (st != null){
                            try{
                                st.close();
                            }catch(SQLException e){
                                e.printStackTrace();
                            }
                        }
                        if (cn != null){
                            try{
                                cn.close();
                            }catch(SQLException e){
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
                mHandler = new Handler(){
                    @Override
                    public void handleMessage(@NonNull Message msg) {
                        super.handleMessage(msg);
                        addtext = (ArrayList) msg.obj;
                        listId.addAll(addtext);
                        addtext.clear();
                        spinnerId.setSelection(0);
                    }
                };
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        adapterId.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinnerId = findViewById(R.id.sp_Id);
        spinnerId.setAdapter(adapterId);
        spinnerId.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Id = spinnerId.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Button mBtnSearchId = findViewById(R.id.btn_SearchId);
        mBtnSearchId.setOnClickListener(view -> {
            if(spinnerArea.getSelectedItem().toString().equals("请选择地区")){
                Toast.makeText(NormalLoginActivity.this, "请选择地区", Toast.LENGTH_SHORT).show();
            }else if(spinnerStation.getSelectedItem().toString().equals("请选择站点")){
                Toast.makeText(NormalLoginActivity.this, "请选择站点", Toast.LENGTH_SHORT).show();
            }else if(spinnerId.getSelectedItem().toString().equals("请选择表号")){
                Toast.makeText(NormalLoginActivity.this, "请选择表号", Toast.LENGTH_SHORT).show();
            }else{
                Intent intent1 = new Intent(NormalLoginActivity.this,ListFileActivity.class);
//                Bundle bundle1 = new Bundle();
//                bundle1.putString("url",url);
//                bundle1.putString("user", user);
//                bundle1.putString("password", password);
//                bundle1.putString("Id",Id);
//                intent1.putExtras(bundle1);
                startActivity(intent1);
            }
        });
    }
}