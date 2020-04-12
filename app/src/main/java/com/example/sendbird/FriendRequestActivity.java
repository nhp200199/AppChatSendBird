package com.example.sendbird;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelParams;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.shadow.com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequestActivity extends AppCompatActivity  {
    public static final String GET_REQUESTS_URL = "http://192.168.100.5:8080/SendBird/GetRequests.php";
    public static final String GET_RECENT_SEARCH_URL = "http://192.168.100.5:8080/SendBird/GetRecentSearch.php";
    private EditText edt_find_user;
    private ImageView img_back;
    private RecyclerView requests_container;
    private RecyclerView recent_search_container;

    private String userId;
    private String friendId;
    private ArrayList<RequestItem> requestItems;
    private RequestAdapter requestAdapter;
    private ArrayList<FriendItem> friendItems;
    private FriendAdapter friendAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);
        connectViews();

        SharedPreferences sharedPreferences = getSharedPreferences("user infor", MODE_PRIVATE);
        userId = sharedPreferences.getString("id", "");
        SendBird.connect(userId, new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                if(e != null)
                    Toast.makeText(FriendRequestActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        requestItems = new ArrayList<RequestItem>();
        requestAdapter = new RequestAdapter(requestItems, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        requests_container.setLayoutManager(linearLayoutManager);
        requests_container.setAdapter(requestAdapter);
        requestAdapter.setListener(new RequestAdapter.Listener() {
            @Override
            public void onClick(int viewType, int position) {
                switch (viewType){
                    case R.id.btn_cancel_request:
                        declineRequest(position);
                        break;
                    case R.id.img_icon_decline:
                        declineRequest(position);
                        break;
                    case R.id.img_icon_accept:
                        acceptRequest(position);
                        break;
                }
            }
        });


        friendItems = new ArrayList<FriendItem>();
        friendAdapter = new FriendAdapter(this, friendItems);
        recent_search_container.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        recent_search_container.setAdapter(friendAdapter);
        friendAdapter.setListener(new ConversationAdapter.Listener() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent(FriendRequestActivity.this, PersonProfileActivity.class);
                intent.putExtra(PersonProfileActivity.EXTRA_ID, friendItems.get(position).getId());
                startActivity(intent);
            }
        });

        loadCurrentSearch();

    }

    private void acceptRequest(final int position) {
        /*ProgressDialog.startProgressDialog(FriendRequestActivity.this, "Đang xử lý");

        final List<String> channelUserIds = new ArrayList<String>();
        channelUserIds.add(friendId);
        SendBird.addFriends(channelUserIds, new SendBird.AddFriendsHandler() {
            @Override
            public void onResult(List<User> list, SendBirdException e) {
                if(e != null){
                    Toast.makeText(FriendRequestActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
                else{
                    channelUserIds.add(userId);
                    GroupChannelParams params = new GroupChannelParams()
                            .setPublic(false)
                            .setEphemeral(true)
                            .setDistinct(true)
                            .addUserIds(channelUserIds);
                    GroupChannel.createChannel(params, new GroupChannel.GroupChannelCreateHandler() {
                        @Override
                        public void onResult(GroupChannel groupChannel, SendBirdException e) {
                            String mChannelId = groupChannel.getUrl();
                            createChannelInDatabase(position, mChannelId);

                            //remove request in recycler view
                            requestItems.remove(position);
                            requestAdapter.notifyDataSetChanged();
                        }
                    });

                }
            }
        });*/
    }

    private void createChannelInDatabase(final int pos, final String channelId) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest request =new StringRequest(Request.Method.POST,
                PersonProfileActivity.ACCEPT_REQUEST_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(FriendRequestActivity.this, response, Toast.LENGTH_LONG).show();
                        ProgressDialog.dismissProgressDialog();
                    }
                }
                ,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(FriendRequestActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                        ProgressDialog.dismissProgressDialog();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params =new HashMap<>();
                params.put("requestId", requestItems.get(pos).getId());
                params.put("channelId", channelId);
                params.put("userId", userId);
                params.put("friendId", requestItems.get(pos).getFromId());
                return params;
            }
        };
        int socketTimeout = 20000;//20s timeout
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        requestQueue.add(request);
    }

    private void declineRequest(final int position) {
        ProgressDialog.startProgressDialog(FriendRequestActivity.this, "Đang xử lý");
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest request =new StringRequest(Request.Method.POST,
                PersonProfileActivity.CANCEL_REQUEST_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        ProgressDialog.dismissProgressDialog();
                        if(response.equals("success")){
                            Toast.makeText(FriendRequestActivity.this, "Đã hủy yêu cầu", Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(FriendRequestActivity.this, "Lời mời kết bạn đã bị xóa hoặc không tồn tại", Toast.LENGTH_LONG).show();
                        }

                        requestItems.remove(position);
                        requestAdapter.notifyDataSetChanged();
                    }
                }
                ,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(FriendRequestActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                        ProgressDialog.dismissProgressDialog();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params =new HashMap<>();
                params.put("requestId", requestItems.get(position).getId());
                return params;
            }
        };
        int socketTimeout = 20000;//20s timeout
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        requestQueue.add(request);
    }



    private void loadCurrentSearch() {
        friendItems.add(new FriendItem("1", "Phuc", ""));
        friendItems.add(new FriendItem("65", "Dung", ""));
        friendItems.add(new FriendItem("69", "Cuong", ""));
        friendItems.add(new FriendItem("70", "Toan", ""));
        friendAdapter.notifyDataSetChanged();

        TextView tv_recent_search = findViewById(R.id.tv_recent_search);
        tv_recent_search.setVisibility(View.VISIBLE);
        recent_search_container.setVisibility(View.VISIBLE);
    }

    private void connectViews() {
        edt_find_user = findViewById(R.id.edt_find_user);
        img_back = findViewById(R.id.img_back);
        requests_container = findViewById(R.id.requests_container);
        recent_search_container  = findViewById(R.id.recent_search_container);

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        edt_find_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendRequestActivity.this, FindUserActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        retrieveRequests(userId);
    }

    private void removeItem(){}

    private void retrieveRequests(final String Uid) {
        final Gson gson =new Gson();
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest request =new StringRequest(Request.Method.POST,
                GET_REQUESTS_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Tag", response);
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            TextView tv_requests = findViewById(R.id.tv_request_list);
                            tv_requests.setVisibility(View.VISIBLE);
                            for (int i = 0; i< jsonArray.length(); i++){
                                RequestItem message = gson.fromJson(jsonArray.getJSONObject(i).toString(), RequestItem.class);
                                requestItems.add(message);
                            }
                            requestAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(FriendRequestActivity.this, response, Toast.LENGTH_LONG).show();
                        }
                    }
                }
                ,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(FriendRequestActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params =new HashMap<>();
                params.put("id",Uid);
                return params;
            }
        };
        int socketTimeout = 20000;//20s timeout
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        requestQueue.add(request);
    }

    @Override
    protected void onStop() {
        super.onStop();
        requestItems.clear();
        requestAdapter.notifyDataSetChanged();
    }
}
