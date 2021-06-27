package com.whjstech.ylb;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import com.chinamobile.iot.onenet.OneNetApi;
import com.whjstech.ylb.Preferences;

public class MainActivity extends AppCompatActivity {

    private static final String[] permissionsArray = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    //还需申请的权限列表
    private ArrayList<String> permissionsList = new ArrayList<String>();

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private EditText mEtUserName;
    private EditText mEtPassWord;
    private CheckBox savePassword;
    public static String url = "jdbc:mysql://rm-bp1h4c20303wf20fa0o.mysql.rds.aliyuncs.com/ylb?useSSL=true";
    public static String user;
    public static String password;
    private ArrayList listArea = new ArrayList(Arrays.asList("请选择地区"));
    private String flag = "普通用户";
    private Preferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mayRequestPermissions();//请求权限

        mPreferences = Preferences.getInstance(this);
        String apiKey = "mb8xiFTmjQHfnetcYlUWmAxRVPc=";
        OneNetApi.setAppKey(apiKey.trim());//设置apiKey
        mPreferences.putString(Preferences.API_KEY, apiKey);

        ImageView imageView = findViewById(R.id.iv_ylb);
        imageView.setImageResource(R.drawable.recorder);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        mEtUserName = findViewById(R.id.username);
        mEtPassWord = findViewById(R.id.password);
        savePassword = findViewById(R.id.cb_SavePassword);
        boolean isSave = pref.getBoolean("savePassword", false);
        if (isSave) {
            user = pref.getString("user", "");
            password = pref.getString("password", "");
            mEtUserName.setText(user);
            mEtPassWord.setText(password);
            savePassword.setChecked(true);
        }

        RadioGroup mRg = findViewById(R.id.rg);
        mRg.setOnCheckedChangeListener((radioGroup, i) -> {
            RadioButton radioButton = radioGroup.findViewById(i);
            flag = radioButton.getText().toString();
        });

        mEtUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                user = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mEtPassWord.setText(null);
            }
        });

        mEtPassWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                password = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        Button mBtnLogin = findViewById(R.id.btnLogin);
        mBtnLogin.setOnClickListener(v -> {
            if (!Utils.isFastClick()) {
                if (user == null || user.equals("")) {
                    Utils.showToastMsg(MainActivity.this, "请输入用户名");
                } else if (password == null || password.equals("")) {
                    Utils.showToastMsg(MainActivity.this, "请输入密码");
                } else if (flag.equals("普通用户")) {
                    ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setMessage("登陆中...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    new Thread(() -> {
                        Connection cn = null;
                        try {
                            Class.forName("com.mysql.jdbc.Driver");
                            cn = DriverManager.getConnection(url, user, password);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (SQLException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            Looper.prepare();
                            Utils.showToastMsg(MainActivity.this, "账号或密码错误");
                            mEtUserName.setText(null);
                            mEtPassWord.setText(null);
                            Looper.loop();
                        } finally {
                            //释放资源(按顺序从后到前:查询结果集-数据库操作对象-连接对象)
                            if (cn != null) {
                                try {
                                    cn.close();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        progressDialog.dismiss();
                        Intent intent = new Intent(MainActivity.this, NormalLoginActivity.class);
                        startActivity(intent);
                        Looper.prepare();
                        Utils.showToastMsg(MainActivity.this, "登陆成功");
                        editor = pref.edit();
                        if (savePassword.isChecked()) {
                            editor.putBoolean("savePassword", true);
                            editor.putString("user", user);
                            editor.putString("password", password);
                        } else {
                            editor.clear();
                        }
                        editor.apply();
                        Looper.loop();
                    }).start();
                } else if (flag.equals("管理员用户")) {
                    new Thread(() -> {
//                    Connection cn = null;
//                    Statement st = null;
                        try {
                            Class.forName("com.mysql.jdbc.Driver");
                            DriverManager.getConnection(url, user, password);
                            Intent intent = new Intent(MainActivity.this, AdminLoginActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("url", url);
                            bundle.putString("user", user);
                            bundle.putString("password", password);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (SQLException e) {
                            e.printStackTrace();
                            System.out.println("登陆失败");
                            //Toast.makeText(MainActivity.this, "登陆失败", Toast.LENGTH_SHORT).show();
                        }
                    }).start();
                }
            }
        });
    }

    private void mayRequestPermissions() {
        int permissions = 0;
        if (Build.VERSION.SDK_INT >= 23) {
            for (String permission : permissionsArray) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsList.add(permission);
                    permissions++;
                }
            }
            if (permissions != 0) {
                ActivityCompat.requestPermissions(this, permissionsList.toArray(new String[permissionsList.size()]), 1);
            }
        }
    }
}