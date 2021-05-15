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

public class DeleteActivity extends AppCompatActivity {

    private EditText mEtDeleteArea;
    private EditText mEtDeleteStation;
    private EditText mEtDeleteId;
    private Button mBtDelete;
    String area;
    String station;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String url = bundle.getString("url");
        String user = bundle.getString("user");
        String password = bundle.getString("password");

        mEtDeleteArea = findViewById(R.id.et_DeleteArea);
        mEtDeleteStation = findViewById(R.id.et_DeleteStation);
        mEtDeleteId = findViewById(R.id.et_DeleteId);
        mBtDelete = findViewById(R.id.btn_delete);

        mBtDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Connection cn = null;
                        try {
                            cn = DriverManager.getConnection(url, user, password);
                            Statement st = cn.createStatement();
                            area = mEtDeleteArea.getText().toString();
                            station = mEtDeleteStation.getText().toString();
                            id = mEtDeleteId.getText().toString();
                            String sql = "delete from ylb_data where 表号 = '" + id + "'";
                            st.execute(sql);
                            System.out.println("删除成功");
                        } catch (SQLException e) {
                            e.printStackTrace();
                            System.out.println("删除失败");
                        }
                    }
                }).start();
            }
        });
    }
}