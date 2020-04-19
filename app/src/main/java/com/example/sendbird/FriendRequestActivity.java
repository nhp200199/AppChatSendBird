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
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelParams;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserMessage;
import com.sendbird.android.UserMessageParams;
import com.sendbird.android.shadow.com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendRequestActivity extends AppCompatActivity  {
    public static final String GET_REQUESTS_URL = "http://192.168.100.11:8080/SendBird/GetRequests.php";
    public static final String GET_RECENT_SEARCH_URL = "http://192.168.100.11:8080/SendBird/GetRecentSearch.php";
    public static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_FRIEND_REQUEST";
    private EditText edt_find_user;
    private ImageView img_back;
    private RecyclerView requests_container;
    private RecyclerView recent_search_container;

    private String userId;
    private String channel;
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



    }

    private void acceptRequest(final int position) {
        ProgressDialog.startProgressDialog(FriendRequestActivity.this, "Đang xử lý");

        final List<String> channelUserIds = new ArrayList<String>();
        channelUserIds.add(requestItems.get(position).getFromId().trim());
        SendBird.addFriends(channelUserIds, new SendBird.AddFriendsHandler() {
            @Override
            public void onResult(List<User> list, SendBirdException e) {
                if(e != null){
                    Toast.makeText(FriendRequestActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    ProgressDialog.dismissProgressDialog();
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

                        }
                    });

                }
            }
        });
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

                        channel = response;
                        //remove request in recycler view
                        requestItems.remove(pos);
                        requestAdapter.notifyDataSetChanged();
                        notifyFriend("notify", "add",channelId);
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



    private void loadCurrentSearch(final String Uid) {
        final Gson gson =new Gson();
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest request =new StringRequest(Request.Method.POST,
                GET_RECENT_SEARCH_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Tag", response);
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            TextView tv_recent_search = findViewById(R.id.tv_recent_search);
                            tv_recent_search.setVisibility(View.VISIBLE);
                            recent_search_container.setVisibility(View.VISIBLE);
                            for (int i = 0; i< jsonArray.length(); i++){
                                FriendItem message = gson.fromJson(jsonArray.getJSONObject(i).toString(), FriendItem.class);
                                friendItems.add(message);
                            }
                            friendAdapter.notifyDataSetChanged();
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

        friendItems.clear();
        friendAdapter.notifyDataSetChanged();
        retrieveRequests(userId);
        loadCurrentSearch(userId);
    }

    private void notifyFriend(final String type, final String message, String mChannelId) {
        GroupChannel.getChannel(mChannelId, new GroupChannel.GroupChannelGetHandler() {
            @Override
            public void onResult(GroupChannel groupChannel, SendBirdException e) {
                if(e == null){
                    UserMessageParams userMessageParams = new UserMessageParams()
                            .setCustomType(type)
                            .setMessage(message);

                    groupChannel.sendUserMessage(userMessageParams, new BaseChannel.SendUserMessageHandler() {
                        @Override
                        public void onSent(UserMessage userMessage, SendBirdException e) {
                            if(e != null){
                                Log.d("Tag", e.getMessage());
                                Toast.makeText(FriendRequestActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                }
                else{
                    Toast.makeText(FriendRequestActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });
    }

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

    @Override
    protected void onPause() {
        super.onPause();
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
        SendBird.removeChannelHandler(PersonProfileActivity.CHANNEL_HANDLER_ID);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                if(baseChannel.getUrl().equals(channel)){
                    String message = baseMessage.getMessage();
                    String type =baseMessage.getCustomType();

                    if(type.equals("notify")){
                        if(message.equals("add")){
                            List<String> id = new ArrayList<>();
                            id.add(baseMessage.getSender().getUserId());
                            SendBird.addFriends(id, new SendBird.AddFriendsHandler() {
                                @Override
                                public void onResult(List<User> list, SendBirdException e) {
                                    if(e!=null){
                                        Toast.makeText(FriendRequestActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }

                    }
                }
            }
        });
        SendBird.addChannelHandler(PersonProfileActivity.CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                String senderId = baseMessage.getSender().getUserId();
                String type = baseMessage.getCustomType();
                String message = baseMessage.getMessage();
                if(type.equals("notify")){
                    if(message.equals("add")){
                        int index = -1;
                        for (RequestItem item: requestItems){
                            if(item.getFromId().equals(baseMessage.getSender().getUserId())){
                                index = requestItems.indexOf(item);
                                break;
                            }
                        }
                        requestItems.remove(index);
                        requestAdapter.notifyDataSetChanged();

                        List<String> friend = new ArrayList<>();
                        friend.add(baseMessage.getSender().getUserId());

                        SendBird.addFriends(friend, new SendBird.AddFriendsHandler() {
                            @Override
                            public void onResult(List<User> list, SendBirdException e) {
                                if(e!=null)
                                    Toast.makeText(FriendRequestActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    if(message.equals("delete")){
                        SendBird.deleteFriend(senderId, new SendBird.DeleteFriendHandler() {
                            @Override
                            public void onResult(SendBirdException e) {
                                if(e!=null)
                                    Toast.makeText(FriendRequestActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

            }
        });
    }

}
