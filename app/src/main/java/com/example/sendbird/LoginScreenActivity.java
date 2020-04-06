package com.example.sendbird;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginScreenActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String LOGIN_URL = "http://192.168.100.11:8080/SendBird/AccountLogin.php";
    private String userID = "";

    private EditText edt_username;
    private EditText edt_password;
    private Button btn_login;
    private Button btn_register;

    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences =getSharedPreferences("user infor", MODE_PRIVATE);
        SendBird.init(RegisterActivity.appID, this);
        if(sharedPreferences.contains("id")){
            Log.d("SendBird", sharedPreferences.getString("id", ""));
            userID = sharedPreferences.getString("id", "");
            Thread timer = new Thread(){
                @Override
                public void run() {
                    try {
                        SendBird.connect(userID, new SendBird.ConnectHandler() {
                            @Override
                            public void onConnected(User user, SendBirdException e) {
                                if(e!=null){
                                    Toast.makeText(LoginScreenActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    goToMainAcitvity();
                                }
                            }
                        });
                        sleep(3000);
                        super.run();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                }
            };
            timer.start();
        }
        setContentView(R.layout.activity_login);
        connectView();
        if(getIntent().hasExtra("email")){
            edt_username.setText(getIntent().getStringExtra("email"));
        }

        btn_login.setEnabled(false);
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String userName = edt_username.getText().toString().trim();
                String userPassword = edt_password.getText().toString().trim();

                if(userName.isEmpty() || userPassword.isEmpty()){
                    btn_login.setEnabled(false);
                }
                else  btn_login.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        edt_username.addTextChangedListener(textWatcher);
        edt_password.addTextChangedListener(textWatcher);

        btn_login.setOnClickListener(this);
        btn_register.setOnClickListener(this);
    }

    private void connectView() {
        edt_password = findViewById(R.id.edt_password);
        edt_username = findViewById(R.id.edt_username);
        btn_login = findViewById(R.id.btn_login);
        btn_register = findViewById(R.id.btn_register);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_login:

                LogIn();
                break;

            case R.id.btn_register:

                Intent intent1 = new Intent(this, RegisterActivity.class);
                startActivity(intent1);
                break;
        }
    }

    private void LogIn() {
        String progressMessage = "Hold on ...";
        ProgressDialog.startProgressDialog(LoginScreenActivity.this, progressMessage);

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest request =new StringRequest(Request.Method.POST,
                LOGIN_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if(object.getInt("is_verified") == 0){
                                ProgressDialog.dismissProgressDialog();
                                Toast.makeText(LoginScreenActivity.this,
                                        "Tài khoản của bạn chưa được xác thực, vui lòng xác thực ở email trước khi đăng nhập",
                                        Toast.LENGTH_SHORT).show();
                            }

                            else{
                            String userID = String.valueOf(object.getInt("id"));
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("id", userID);
                            editor.commit();

                            SendBird.connect(userID, new SendBird.ConnectHandler() {
                                @Override
                                public void onConnected(User user, SendBirdException e) {
                                    if(e != null){
                                        Toast.makeText(LoginScreenActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                                    }
                                    else{
                                        ProgressDialog.dismissProgressDialog();

                                        startActivity(new Intent(LoginScreenActivity.this, MainActivity.class));
                                        finish();
                                    }
                                }
                            });
                        }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ProgressDialog.dismissProgressDialog();
                            Toast.makeText(LoginScreenActivity.this, response, Toast.LENGTH_LONG).show();
                        }
                    }
                }
                ,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ProgressDialog.dismissProgressDialog();
                        Toast.makeText(LoginScreenActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params =new HashMap<>();
                params.put("email",edt_username.getText().toString().trim());
                params.put("password",edt_password.getText().toString().trim());
                return params;
            }
        };
        int socketTimeout = 20000;//20s timeout
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        requestQueue.add(request);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void goToMainAcitvity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
