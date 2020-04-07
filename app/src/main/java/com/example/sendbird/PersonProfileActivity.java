package com.example.sendbird;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.sendbird.android.FriendListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.shadow.com.google.gson.JsonElement;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {
    public static final String REQUEST_STATUS_URL="http://192.168.100.11:8080/SendBird/GetRequestStatus.php";
    public static final String SEND_REQUEST_URL="http://192.168.100.11:8080/SendBird/SendRequest.php";
    public static final String ACCEPT_REQUEST_URL="http://192.168.100.11:8080/SendBird/GetRequestStatus.php";
    public static final String CANCEL_REQUEST_URL="http://192.168.100.11:8080/SendBird/CancelRequest.php";
    public static final String REMOVE_CONTACT_URL="http://192.168.100.11:8080/SendBird/GetRequestStatus.php";
    public static final String EXTRA_ID = "FriendId";

    boolean flag = false;

    private String friendId, userId, currentState, requestId;
    private SharedPreferences sharedPreferences;

    private ImageView img_cover;
    private ImageView img_back;
    private ImageView img_gender;
    private CircleImageView civ_avatar;
    private TextView tv_userName;
    private TextView tv_birthday;
    private TextView tv_gender;
    private TextView tv_email;
    private Button btn_add_friend, btn_cancel_request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);
        connectViews();
        sharedPreferences = getSharedPreferences("user infor", MODE_PRIVATE);
        userId = sharedPreferences.getString("id", "");
        SendBird.init(RegisterActivity.appID, this);
        SendBird.connect(userId, new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
            }
        });

        if(getIntent().hasExtra(EXTRA_ID)){
            friendId = getIntent().getStringExtra(EXTRA_ID);
            retrieveUserInfo();
            retrieveRequestStatus();
        }
        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        btn_add_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_add_friend.setEnabled(false);

                if(currentState.equals("new"))
                {
                    sendRequest();
                }
                else if(currentState.equals("request sent")){
                    cancelRequest();

                }
                else if(currentState.equals("request received")){
                    acceptRequest();
                }

                else if(currentState.equals("friends")){
                    displayRemoveFriendConfirmDialog();
                    btn_add_friend.setEnabled(true);
                }
            }
        });

    }

    private void connectViews() {
        img_cover = findViewById(R.id.img_cover);
        img_back = findViewById(R.id.back);
        civ_avatar = findViewById(R.id.civ_avatar);
        tv_birthday= findViewById(R.id.tv_birthday);
        tv_gender = findViewById(R.id.tv_gender);
        tv_email = findViewById(R.id.tv_email);
        img_gender = findViewById(R.id.img_gender);
        tv_userName = findViewById(R.id.tv_username);
        btn_add_friend = findViewById(R.id.btn_request_add_friend);
        btn_cancel_request = findViewById(R.id.btn_decline_request);

        currentState = "new";
    }

    private void retrieveUserInfo() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest request =new StringRequest(Request.Method.POST,
                UserProfileActivity.USER_PROFILE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            Log.d("TAG", object.toString());
                            if(object.getString("gender").equals("Nam")){
                                img_gender.setImageResource(R.drawable.ic_male);
                            }
                            else img_gender.setImageResource(R.drawable.ic_female);

                            tv_email.setText(object.getString("email"));
                            tv_birthday.setText(object.getString("DoB"));
                            tv_userName.setText(object.getString("nickname"));
                            tv_gender.setText(object.getString("gender"));

                            Glide.with(PersonProfileActivity.this)
                                    .load(object.getString("avatar"))
                                    .thumbnail(0.5f)
                                    .apply(RequestOptions.skipMemoryCacheOf(true))
                                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                                    .into(civ_avatar);
                            Glide.with(PersonProfileActivity.this)
                                    .load(object.getString("cover"))
                                    .thumbnail(0.5f)
                                    .apply(RequestOptions.skipMemoryCacheOf(true))
                                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                                    .into(img_cover);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(PersonProfileActivity.this, response, Toast.LENGTH_LONG).show();
                        }
                    }
                }
                ,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(PersonProfileActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params =new HashMap<>();
                params.put("id", friendId);
                return params;
            }
        };
        int socketTimeout = 20000;//20s timeout
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        requestQueue.add(request);
    }
    private void retrieveRequestStatus() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest request =new StringRequest(Request.Method.POST,
                REQUEST_STATUS_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String type = "";
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            type = jsonObject.getString("type");
                            requestId = jsonObject.getString("id");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(PersonProfileActivity.this, response, Toast.LENGTH_LONG).show();
                        if(type.equals("sent")){
                            currentState = "request received";

                            btn_add_friend.setText("Đồng ý");
                            btn_cancel_request.setVisibility(View.VISIBLE);
                            btn_cancel_request.setEnabled(true);

                            btn_cancel_request.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    cancelRequest();
                                }
                            });
                        }
                        else if(type.equals("received")){
                            currentState = "request sent";

                            btn_add_friend.setText("Hủy kết bạn");
                        }
                        else{
                            FriendListQuery friendListQuery =  SendBird.createFriendListQuery();
                            friendListQuery.next(new FriendListQuery.FriendListQueryResultHandler() {
                                @Override
                                public void onResult(List<User> list, SendBirdException e) {
                                    if(e!=null){
                                        Toast.makeText(PersonProfileActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        boolean isFriend = false;
                                        for(User friend: list)
                                        {
                                            if(friend.getFriendName().equals(friendId))
                                            {
                                                isFriend = true;
                                                currentState = "friends";

                                                btn_add_friend.setText("Hủy");
                                                btn_add_friend.setEnabled(true);

                                                btn_cancel_request.setVisibility(View.INVISIBLE);
                                                btn_cancel_request.setEnabled(false);
                                                break;
                                            }
                                        }
                                        if(!isFriend){
                                            currentState = "new";

                                            btn_add_friend.setText("Kết bạn");

                                            btn_cancel_request.setVisibility(View.INVISIBLE);
                                            btn_cancel_request.setEnabled(false);
                                        }

                                    }
                                }
                            });
                        }
                    }
                }
                ,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(PersonProfileActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params =new HashMap<>();
                params.put("userId", "1");
                params.put("friendId", friendId);
                return params;
            }
        };
        int socketTimeout = 20000;//20s timeout
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        requestQueue.add(request);

    }

    private void sendRequest() {
        ProgressDialog.startProgressDialog(PersonProfileActivity.this, "Đang gửi lời mời");
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest request =new StringRequest(Request.Method.POST,
                SEND_REQUEST_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(PersonProfileActivity.this, "Đã gửi lời mời tới " + tv_userName.getText().toString(), Toast.LENGTH_LONG).show();
                        ProgressDialog.dismissProgressDialog();
                        requestId  = response;

                        currentState = "request sent";

                        btn_add_friend.setEnabled(true);
                        btn_add_friend.setText("Hủy kết bạn");
                    }
                }
                ,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(PersonProfileActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                        ProgressDialog.dismissProgressDialog();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params =new HashMap<>();
                params.put("userId", "1");
                params.put("friendId", friendId);
                return params;
            }
        };
        int socketTimeout = 20000;//20s timeout
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        requestQueue.add(request);
    }

    private void cancelRequest() {
        ProgressDialog.startProgressDialog(PersonProfileActivity.this, "Đang xử lý");
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest request =new StringRequest(Request.Method.POST,
                CANCEL_REQUEST_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ProgressDialog.dismissProgressDialog();
                        if(response.equals("success")){
                            Toast.makeText(PersonProfileActivity.this, "Đã hủy yêu cầu", Toast.LENGTH_LONG).show();

                            currentState = "new";

                            btn_add_friend.setText("Kết bạn");
                            btn_add_friend.setEnabled(true);

                            btn_cancel_request.setVisibility(View.INVISIBLE);
                            btn_cancel_request.setEnabled(false);
                        }
                        else {
                            Toast.makeText(PersonProfileActivity.this, "Lời mời kết bạn đã bị xóa hoặc không tồn tại", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                ,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(PersonProfileActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                        ProgressDialog.dismissProgressDialog();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params =new HashMap<>();
                params.put("requestId", requestId);
                return params;
            }
        };
        int socketTimeout = 20000;//20s timeout
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        requestQueue.add(request);
    }

    private void acceptRequest() {
        /*
        ProgressDialog.startProgressDialog(PersonProfileActivity.this, "Đang xử lý");
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest request =new StringRequest(Request.Method.POST,
                ACCEPT_REQUEST_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(PersonProfileActivity.this, "Các bạn đã là bạn bè của nhau", Toast.LENGTH_LONG).show();
                        ProgressDialog.dismissProgressDialog();

                        currentState = "friends";

                        btn_add_friend.setText("Hủy");
                        btn_add_friend.setEnabled(true);

                        btn_cancel_request.setVisibility(View.INVISIBLE);
                        btn_cancel_request.setEnabled(false);
                    }
                }
                ,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(PersonProfileActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                        ProgressDialog.dismissProgressDialog();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params =new HashMap<>();
                params.put("requestId", requestId);
                return params;
            }
        };
        int socketTimeout = 20000;//20s timeout
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        requestQueue.add(request);

         */

    }

    private void displayRemoveFriendConfirmDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(PersonProfileActivity.this)
                .setMessage("Bạn có muốn hủy kết bạn với " + tv_userName.getText().toString() + "?")
                .setTitle("Thông báo")
                .setPositiveButton("Không", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();

                    }
                })
                .setNegativeButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        removeContact();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void removeContact(){

    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}
