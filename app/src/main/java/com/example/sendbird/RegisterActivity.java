package com.example.sendbird;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String appID = "9DCA326D-1743-490F-90B8-2AA2B29CE71B";
    public static final String REGISTER_URL = "http://192.168.100.5:8080/SendBird/AccountRegister.php";

    private EditText edt_username;
    private EditText edt_password;
    private EditText edt_password_repeated;
    private EditText edt_email;
    private EditText edt_gender;
    private Button btn_register;
    private Button btn_login;
    private TextView tv_password_reminder;
    private TextView tv_gender_reminder;
    private TextView tv_email_reminder;
    private TextView tv_password;
    private TextView tv_DoB;

    private boolean isRepeatedPasswordFault;
    private boolean isGenderFault;
    private boolean isEmailFault;
    private boolean isPasswordFault;

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


                if(userName.isEmpty() || userPassword.isEmpty() || gender.isEmpty() || userMail.isEmpty() || repeatedPassword.isEmpty()||tv_DoB.getText().toString().equals("")){
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
                    isRepeatedPasswordFault = true;
                    tv_password_reminder.setVisibility(View.VISIBLE);
                }

                else{
                    isRepeatedPasswordFault = false;
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

        TextWatcher textWatcher4 = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String password = edt_password.getText().toString().trim();
                if(checkString(password) && password.length() >= 8){
                    isPasswordFault = false;
                    tv_password.setVisibility(View.GONE);
                }
                else{
                    isPasswordFault = true;
                    tv_password.setVisibility(View.VISIBLE);
                }
            }

            private boolean checkString(String str) {
                char ch;
                boolean capitalFlag = false;
                boolean lowerCaseFlag = false;
                boolean numberFlag = false;
                for(int i=0;i < str.length();i++) {
                    ch = str.charAt(i);
                    if( Character.isDigit(ch)) {
                        numberFlag = true;
                    }
                    else if (Character.isUpperCase(ch)) {
                        capitalFlag = true;
                    } else if (Character.isLowerCase(ch)) {
                        lowerCaseFlag = true;
                    }
                    if(numberFlag && capitalFlag && lowerCaseFlag)
                        return true;
                }
                return false;
            }
        };
        edt_password.addTextChangedListener(textWatcher4);

    }

    private void connectViews() {
        edt_email = findViewById(R.id.edt_email);
        edt_gender = findViewById(R.id.edt_genger);
        edt_password = findViewById(R.id.edt_password);
        edt_password_repeated = findViewById(R.id.edt_confirm_password);
        edt_username  = findViewById(R.id.edt_username);
        btn_register = findViewById(R.id.btn_register);
        tv_password_reminder = findViewById(R.id.tv_password_reminder);
        tv_gender_reminder = findViewById(R.id.tv_gender_reminder);
        tv_email_reminder = findViewById(R.id.tv_email_reminder);
        tv_password = findViewById(R.id.tv_password);
        tv_DoB = findViewById(R.id.tv_DoB);
        btn_login = findViewById(R.id.btn_login);
        tv_DoB.setOnClickListener(this);
        btn_register.setOnClickListener(RegisterActivity.this);
        btn_login.setOnClickListener(RegisterActivity.this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.btn_register:
                if(isEmailFault | isGenderFault | isRepeatedPasswordFault|| isPasswordFault)
                    showErrorPopUp();
                else createNewAccount();
                break;

            case R.id.btn_login:
                startActivity(new Intent(RegisterActivity.this, LoginScreenActivity.class));
                finish();
                break;

            case R.id.tv_DoB:
                showDatePickerDialog();
                break;
        }
    }
    private void showDatePickerDialog() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                android.R.style.ThemeOverlay_DeviceDefault_Accent_DayNight,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month = month+1;
                        String date = year +"/" + month + "/" + dayOfMonth;
                        tv_DoB.setText(date);
                    }
                }, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
        datePickerDialog.show();
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
                params.put("DoB",tv_DoB.getText().toString());
                params.put("password",String.valueOf(edt_password.getText().toString().trim().hashCode()));
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
