package com.example.sendbird;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sendbird.android.SendBird;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String appID = "9DCA326D-1743-490F-90B8-2AA2B29CE71B";
    public static final String REGISTER_URL = "http://192.168.100.11:8080/SendBird/AccountRegister.php";

    private EditText edt_username;
    private EditText edt_password;
    private EditText edt_password_repeated;
    private EditText edt_email;
    private EditText edt_gender;
    private Button btn_register;
    private ImageView img_facebook;
    private ImageView img_back;
    private TextView tv_password_reminder;
    private TextView tv_gender_reminder;
    private TextView tv_email_reminder;

    private boolean isPasswordFault = false;
    private boolean isGenderFault = false;
    private boolean isEmailFault = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_account);
        connectViews();
        SendBird.init(appID, this);

        btn_register.setEnabled(false);

        //enable register button when all things is entered
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String userName = edt_username.getText().toString().trim();
                String userPassword = edt_password.getText().toString().trim();
                String gender = edt_gender.getText().toString().trim();
                String userMail = edt_email.getText().toString().trim();
                String repeatedPassword = edt_password_repeated.getText().toString().trim();


                if(userName.isEmpty() || userPassword.isEmpty() || gender.isEmpty() || userMail.isEmpty() || repeatedPassword.isEmpty()){
                    btn_register.setEnabled(false);
                }
                else  btn_register.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        edt_username.addTextChangedListener(textWatcher);
        edt_password.addTextChangedListener(textWatcher);
        edt_password_repeated.addTextChangedListener(textWatcher);
        edt_gender.addTextChangedListener(textWatcher);
        edt_email.addTextChangedListener(textWatcher);

        //some TextWatcher to check condition of input
        TextWatcher textWatcher1 = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = edt_password.getText().toString().trim();
                String confirmedPassword = edt_password_repeated.getText().toString().trim();

                if(!password.equals(confirmedPassword))
                {
                    isPasswordFault = true;
                    tv_password_reminder.setVisibility(View.VISIBLE);
                }

                else{
                    isPasswordFault = false;
                    tv_password_reminder.setVisibility(View.GONE);
                }


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        edt_password_repeated.addTextChangedListener(textWatcher1);

        TextWatcher textWatcher2 = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String gender = edt_gender.getText().toString().trim();

                if(gender.equals("Nam") || gender.equals("Nữ")){
                    isGenderFault = false;
                    tv_gender_reminder.setVisibility(View.GONE);
                }

                else{
                    isGenderFault = true;
                    tv_gender_reminder.setVisibility(View.VISIBLE);
                }


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        edt_gender.addTextChangedListener(textWatcher2);

        TextWatcher textWatcher3 = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(isValidEmail((CharSequence)s)){
                    isEmailFault = false;
                    tv_email_reminder.setVisibility(View.GONE);
                }
                else {
                    isEmailFault = true;
                    tv_email_reminder.setVisibility(View.VISIBLE);
                }
            }
        };
        edt_email.addTextChangedListener(textWatcher3);

    }

    private void connectViews() {
        edt_email = findViewById(R.id.edt_email);
        edt_gender = findViewById(R.id.edt_genger);
        edt_password = findViewById(R.id.edt_password);
        edt_password_repeated = findViewById(R.id.edt_confirm_password);
        edt_username  = findViewById(R.id.edt_username);
        btn_register = findViewById(R.id.btn_register);
        img_facebook = findViewById(R.id.img_facebook);
        img_back = findViewById(R.id.img_back);
        tv_password_reminder = findViewById(R.id.tv_password_reminder);
        tv_gender_reminder = findViewById(R.id.tv_gender_reminder);
        tv_email_reminder = findViewById(R.id.tv_email_reminder);

        btn_register.setOnClickListener(RegisterActivity.this);
        img_facebook.setOnClickListener(RegisterActivity.this);
        img_back.setOnClickListener(RegisterActivity.this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.btn_register:
                if(isEmailFault | isGenderFault | isPasswordFault)
                    showErrorPopUp();
                else createNewAccount();
                break;

            case R.id.img_facebook:
                break;

            case R.id.img_back:
                onBackPressed();
                break;
        }
    }

    private void createNewAccount() {
        String progressMessage = "The account is being created, please wait ...";
        ProgressDialog.startProgressDialog(RegisterActivity.this, progressMessage);

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest request =new StringRequest(Request.Method.POST,
                REGISTER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("success")){
                            ProgressDialog.dismissProgressDialog();
                            startActivity(new Intent(RegisterActivity.this, LoginScreenActivity.class));
                            finish();
                        }else{
                            ProgressDialog.dismissProgressDialog();
                            Toast.makeText(RegisterActivity.this, response, Toast.LENGTH_LONG).show();
                        }
                    }
                }
                ,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ProgressDialog.dismissProgressDialog();
                        Toast.makeText(RegisterActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params =new HashMap<>();
                params.put("nickname",edt_username.getText().toString().trim());
                params.put("DoB","1999-01-20");
                params.put("password",edt_password.getText().toString().trim());
                params.put("email",edt_email.getText().toString().trim());
                params.put("gender",edt_gender.getText().toString().trim());
                return params;
            }
        };
        requestQueue.add(request);
    }

    private void showErrorPopUp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this)
                .setTitle("Lỗi")
                .setMessage("Thông tin đăng kí chưa hợp lệ, vui lòng kiểm tra lại")
                ;
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    public static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }
}
