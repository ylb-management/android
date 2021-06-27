package com.whjstech.ylb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.chinamobile.iot.onenet.OneNetApi;
import com.chinamobile.iot.onenet.OneNetApiCallback;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

public class DisplayOnenetDataActivity extends AppCompatActivity {

    private String deviceId;
    private String deviceData;
    private String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/压力数据/";
    private ArrayList idArray = new ArrayList();
    private ArrayList dataArray = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_onenet_data);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String Id = bundle.getString("Id");
        rootPath = rootPath + Id + "/";

        ProgressDialog progressDialog = new ProgressDialog(DisplayOnenetDataActivity.this);
        progressDialog.setMessage("查询中...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Thread(() -> {
            Connection cn = null;
            Statement st = null;
            ResultSet rs = null;
            try {
                cn = DriverManager.getConnection(MainActivity.url, MainActivity.user, MainActivity.password);
                st = cn.createStatement();
                rs = st.executeQuery("select 设备号 from ylb_data where 表号='" + Id + "'");
                while (rs.next()) {
                    deviceId = rs.getString("设备号");
                }
                OneNetApi.queryMultiDataStreams(deviceId, new Callback());
                progressDialog.dismiss();
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("查找Id失败");
            } finally {
                //释放资源(按顺序从后到前:查询结果集-数据库操作对象-连接对象)
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if (st != null) {
                    try {
                        st.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if (cn != null) {
                    try {
                        cn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            progressDialog.dismiss();
        }).start();

        Button mButton = findViewById(R.id.btn_plot);
        mButton.setOnClickListener(view -> {
            idArray = findArrayListInLog("\"id\"");
            System.out.println(idArray.toString());
            dataArray = findArrayListInLog("\"current_value\"");
            System.out.println(dataArray);
            if (idArray.isEmpty()) {
                Utils.showToastMsg(DisplayOnenetDataActivity.this, "查找不到有效的数据流id,请重试!");
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(DisplayOnenetDataActivity.this);
                ArrayAdapter<ArrayList> adapterid = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, idArray);
                builder.setTitle("请选择数据流id").setAdapter(adapterid, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which + 1 > dataArray.size()) {
                            Utils.showToastMsg(DisplayOnenetDataActivity.this, "该数据流下无有效数据,请重试!");
                        } else {
                            if (dataArray.get(which) == null || dataArray.get(which).equals("")) {
                                Utils.showToastMsg(DisplayOnenetDataActivity.this, "该数据流下无有效数据,请重试!");
                            } else {
                                System.out.println(which);
                                writeDat(idArray.get(which).toString(), dataArray.get(which).toString());
                                Utils.showToastMsg(DisplayOnenetDataActivity.this, "保存成功");
                                Intent intent1 = new Intent(DisplayOnenetDataActivity.this, ListFileActivity.class);
                                startActivity(intent1);
                            }
                        }
                    }
                }).show();
            }
        });
    }

    private class Callback implements OneNetApiCallback {
        @Override
        public void onSuccess(String response) {
            displayLog(response);
        }

        @Override
        public void onFailed(Exception e) {
        }
    }

    private void displayLog(String response) {
        if ((response.startsWith("{") && response.endsWith("}")) || (response.startsWith("[") && response.endsWith("]"))) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonParser jsonParser = new JsonParser();
            response = gson.toJson(jsonParser.parse(response));
            initViews(response);
        }
    }

    private void initViews(String log) {
        TextView mTextView = findViewById(R.id.resp_display);
        if (log != null) {
            mTextView.setText(log);
            deviceData = log;
        }
    }

    //将16进制字符串保存为.dat文件
    private void writeDat(String fileName, String value) {
        try {
            File file = new File(rootPath);
            file.mkdirs();//创建文件夹
            File fil = new File(rootPath);
            fil.createNewFile();
            DataOutputStream fileOut = new DataOutputStream(new FileOutputStream(rootPath + fileName + ".DAT"));
            byte[] bytes = hexStr2Byte(value);
            fileOut.write(bytes);
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //从日志中找出指定字符串
    private ArrayList findArrayListInLog(String inPut) {
        int index = 0;
        ArrayList outPut = new ArrayList();
        StringBuffer item = new StringBuffer();
        while (deviceData.indexOf(inPut, index) != -1) {
            index = deviceData.indexOf(inPut, index) + inPut.length() + 3;
            while (deviceData.charAt(index) != '\"') {
                item.append(deviceData.charAt(index));
                index++;
            }
            String str = item.toString();
            outPut.add(str);
            item.setLength(0);
        }
        return outPut;
    }

    private byte[] hexStr2Byte(String hex) {
        ByteBuffer bf = ByteBuffer.allocate(hex.length() / 2);
        for (int i = 0; i < hex.length(); i++) {
            String hexStr = hex.charAt(i) + "";
            i++;
            hexStr += hex.charAt(i);
            byte b = (byte) Integer.parseInt(hexStr, 16);
            bf.put(b);
        }
        return bf.array();
    }
}