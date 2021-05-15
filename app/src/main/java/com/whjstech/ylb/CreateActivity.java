package com.whjstech.ylb;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateActivity extends AppCompatActivity {

    private EditText mEtCreateArea;
    private EditText mEtCreateStation;
    private EditText mEtCreateId;
    private EditText mEtCreateIMEI;
    private EditText mEtCreateIMSI;
    private EditText mEtCreateNote;
    private Button mBtCreate;
    String area;
    String station;
    String id;
    String imei;
    String imsi;
    String note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String url = bundle.getString("url");
        String user = bundle.getString("user");
        String password = bundle.getString("password");

        mEtCreateArea = findViewById(R.id.et_CreateArea);
        mEtCreateStation = findViewById(R.id.et_CreateStation);
        mEtCreateId = findViewById(R.id.et_CreateId);
        mEtCreateIMEI = findViewById(R.id.et_CreateIMEI);
        mEtCreateIMSI = findViewById(R.id.et_CreateIMSI);
        mEtCreateNote = findViewById(R.id.et_CreateNote);
        mBtCreate = findViewById(R.id.btn_Create);


        mBtCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Connection cn = DriverManager.getConnection(url, user, password);
                            Statement st = cn.createStatement();
                            area = mEtCreateArea.getText().toString();
                            station = mEtCreateStation.getText().toString();
                            id = mEtCreateId.getText().toString();
                            imei = mEtCreateIMEI.getText().toString();
                            imsi = mEtCreateIMSI.getText().toString();
                            note = mEtCreateNote.getText().toString();
                            String sql = "insert into ylb_data (区名, 站名, 表号, IMEI, IMSI, 备注) values('"  + area + "','" + station + "','" + id + "','" + imei + "','" + imsi + "','" + note +"')";
                            st.execute(sql);
                            System.out.println("添加成功");
                        } catch (SQLException e) {
                            e.printStackTrace();
                            System.out.println("添加失败");
                        }

                    }
                }).start();
            }
        });
    }
}