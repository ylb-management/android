package com.whjstech.ylb;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AdminLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        Button mBtnCreate = (Button)findViewById(R.id.btn_create);
        Button mBtnDelete = (Button)findViewById(R.id.btn_delete);
        Button mBtnUpdate = (Button)findViewById(R.id.btn_update);
        Button mBtnRead = (Button)findViewById(R.id.btn_read);

        mBtnCreate.setOnClickListener(new ButtonListener());
        mBtnDelete.setOnClickListener(new ButtonListener());
        mBtnUpdate.setOnClickListener(new ButtonListener());
        mBtnRead.setOnClickListener(new ButtonListener());
    }

    private class ButtonListener implements View.OnClickListener {
        Intent intent = getIntent();
        Bundle bundle2 = intent.getExtras();
        String url = bundle2.getString("url");
        String user = bundle2.getString("user");
        String password = bundle2.getString("password");
        Intent intent1;
        public void onClick(View v){
            switch (v.getId()){
                case R.id.btn_create:
                    intent1 = new Intent(AdminLoginActivity.this, CreateActivity.class);
                    break;
                case R.id.btn_delete:
                    intent1 = new Intent(AdminLoginActivity.this, DeleteActivity.class);
                    break;
                case R.id.btn_update:
                    intent1 = new Intent(AdminLoginActivity.this, UpdateActivity.class);
                    break;
                case R.id.btn_read:
                    intent1 = new Intent(AdminLoginActivity.this, ReadActivity.class);
                    break;
            }
            Bundle bundle3 = new Bundle();
            bundle3.putString("url",url);
            bundle3.putString("user", user);
            bundle3.putString("password", password);
            intent1.putExtras(bundle3);
            startActivity(intent1);
        }
    }
}