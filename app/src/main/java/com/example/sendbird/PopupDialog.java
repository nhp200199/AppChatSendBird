package com.example.sendbird;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PopupDialog extends AppCompatDialogFragment {
    public static final String URL_CHANGE_PASSWORD = "http://192.168.100.5:8080/SendBird/EditPassword.php";
    public static final String URL_CHANGE_USERNAME = "http://192.168.100.5:8080/SendBird/EditUsername.php";

    private String userID;
    private String mTitle;
    private int mStyle;

    private Context mContext;
    private DialogListener mListener;

    private EditText mEditUsername;
    private EditText mEdtOldPassword;
    private EditText mEdtNewPassword;
    private EditText mEdtConfirmPassword;
    private TextView tvAlertPassword;
    private TextView tvAlertConfirmPassword;

    private AlertDialog.Builder mBuilder;

    interface DialogListener{
        void onNameChanged(String newName);
    }

    public PopupDialog(String title, int style){
        mTitle = title;
        mStyle = style;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(@NonNull Dialog dialog, int style) {
        super.setupDialog(dialog, style);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        mBuilder = new AlertDialog.Builder(getActivity());

        userID = getArguments().getString("id");

        LayoutInflater inflater  = getActivity().getLayoutInflater();
        View view = inflater.inflate(mStyle, null);


        mBuilder.setView(view)
                .setTitle(mTitle)
                .setCancelable(true)
                .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Lưu", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ProgressDialog.startProgressDialog(getActivity(), "Vui lòng đợi nha ...");

                        if(view.findViewById(R.id.edt_userName)==null){
                            String oldPassword = mEdtOldPassword.getText().toString().trim();
                            String newPassword = mEdtNewPassword.getText().toString().trim();
                            changePassword(oldPassword, newPassword);

                        }else{
                            String newUsername = mEditUsername.getText().toString().trim();
                            changeUsername(newUsername);
                        }
                    }

                    private void changePassword(String oldPassword,String newPassword) {
                        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
                        StringRequest request =new StringRequest(Request.Method.POST,
                                URL_CHANGE_PASSWORD,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        ProgressDialog.dismissProgressDialog();
                                        if(response.equals("success")){
                                            Toast.makeText(mContext, "Mật khẩu đã được đổi", Toast.LENGTH_LONG).show();
                                        }else if(response.equals("incorrect password")){
                                            Toast.makeText(mContext, "Cần nhập đúng mật khẩu cũ", Toast.LENGTH_LONG).show();
                                        }else{
                                            Toast.makeText(mContext, "Đã có lỗi xảy ra, vui lòng thử lại sau", Toast.LENGTH_LONG).show();
                                        }

                                    }
                                }
                                ,
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        ProgressDialog.dismissProgressDialog();
                                        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }){
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String,String> params =new HashMap<>();
                                params.put("id", userID);
                                //params.put("oldPassword", oldPassword);
                                params.put("oldPassword", String.valueOf(oldPassword.trim().hashCode()));
                                params.put("newPassword", String.valueOf(newPassword.trim().hashCode()));

                                return params;
                            }
                        };
                        int socketTimeout = 20000;//20s timeout
                        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                        requestQueue.add(request);
                    }

                    private void changeUsername(String newUsername) {
                        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
                        StringRequest request =new StringRequest(Request.Method.POST,
                                URL_CHANGE_USERNAME,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        ProgressDialog.dismissProgressDialog();
                                        String curAva = getArguments().getString("avatar");
                                        if(response.equals("success")){
                                            SendBird.updateCurrentUserInfo(newUsername, curAva, new SendBird.UserInfoUpdateHandler() {
                                                @Override
                                                public void onUpdated(SendBirdException e) {
                                                    if(e==null){
                                                        mListener.onNameChanged(newUsername);
                                                        Toast.makeText(mContext, "Tên người dùng đã được đổi", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });

                                        }else{
                                            Toast.makeText(mContext, "Đã có lỗi xảy ra, vui lòng thử lại sau", Toast.LENGTH_LONG).show();
                                        }

                                    }
                                }
                                ,
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        ProgressDialog.dismissProgressDialog();
                                        Log.d("Tag", error.getMessage());
                                    }
                                }){
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String,String> params =new HashMap<>();
                                params.put("id", userID);
                                params.put("newUsername", newUsername);

                                return params;
                            }
                        };
                        int socketTimeout = 20000;//20s timeout
                        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                        requestQueue.add(request);
                    }
                });

        connectView(view);
        return mBuilder.create();
    }


    private void connectView(View view) {
        if(view.findViewById(R.id.edt_userName)==null){
            mEdtOldPassword = view.findViewById(R.id.edt_old_password);
            mEdtNewPassword = view.findViewById(R.id.edt_password);
            mEdtConfirmPassword = view.findViewById(R.id.edt_confirm_password);

            tvAlertConfirmPassword = view.findViewById(R.id.tv_confirm_reminder);
            tvAlertPassword = view.findViewById(R.id.tv_password_reminder);

            setPasswordTextChanges();
        }else{
            mEditUsername = view.findViewById(R.id.edt_userName);

            setUsernameTextChanges();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
    }

    private void setPasswordTextChanges() {

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String oldPassword = mEdtOldPassword.getText().toString().trim();
                String newPassword = mEdtNewPassword.getText().toString().trim();
                String confirmPassword = mEdtConfirmPassword.getText().toString().trim();
                boolean isValid = true;

                if(oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()){
                    ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }else{
                    if(oldPassword.equals(newPassword)){
                        tvAlertPassword.setVisibility(View.VISIBLE);
                        isValid = false;
                    }
                    else{
                        tvAlertPassword.setVisibility(View.GONE);
                    }
                    if(!newPassword.equals(confirmPassword)){
                        tvAlertConfirmPassword.setVisibility(View.VISIBLE);
                        isValid = false;
                    }else{
                        tvAlertConfirmPassword.setVisibility(View.GONE);
                    }
                    if(!checkString(newPassword) || newPassword.length() < 8)
                        isValid =false;
                    if(isValid)
                        ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
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

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        mEdtOldPassword.addTextChangedListener(textWatcher);
        mEdtNewPassword.addTextChangedListener(textWatcher);
        mEdtConfirmPassword.addTextChangedListener(textWatcher);
    }

    private void setUsernameTextChanges() {
        String curUsername = getArguments().getString("username");


        mEditUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String newUsername = mEditUsername.getText().toString().trim();
                if(!newUsername.equals(curUsername) && !newUsername.equals("")){
                    ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }else{
                    ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mListener = (DialogListener) context;
    }
}
