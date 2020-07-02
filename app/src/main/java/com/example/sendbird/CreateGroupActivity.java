package com.example.sendbird;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.FriendListQuery;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelParams;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserMessage;
import com.sendbird.android.UserMessageParams;
import com.sendbird.android.shadow.com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateGroupActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String CREATE_GROUP_URL = "https://pacpac-chat.000webhostapp.com/CreateGroup.php";
    private EditText edt_group_name;
    private Button btn_create;
    private ListView lv_friends;
    private TextView tv_members;
    private ImageView img_back;

    private ContactItemAdapter adapter;
    private ArrayList<ContactItem> friendList;

    private FriendAdapter friendAdapter;
    private ArrayList<FriendItem> itemFriendsSelected;
    private RecyclerView friendsSelectedContainer;

    private List<Integer> integerList;
    public String mChannelId, mUserId;

    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        connectViews();

        sharedPreferences = getSharedPreferences("user infor", MODE_PRIVATE);
        mUserId = sharedPreferences.getString("id", null);

        lv_friends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int i = member_exists(position, integerList);
                if(member_exists(position, integerList) > -1){
                    itemFriendsSelected.remove(member_exists(position, integerList));
                    integerList.remove(integerList.indexOf(position));
                    //Log.d("Find", String.valueOf(i));
                    //Toast.makeText(CreateGroupActivity.this, "Find: " + i, Toast.LENGTH_SHORT).show();
                }
                else {
                    itemFriendsSelected.add(new FriendItem(friendList.get(position).getUid(), friendList.get(position).getName(), friendList.get(position).getAvatar()));

                    friendsSelectedContainer.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            friendsSelectedContainer.smoothScrollToPosition(friendsSelectedContainer.getAdapter().getItemCount() - 1);
                        }
                    }, 100);

                    integerList.add(position);
                }
                friendAdapter.notifyDataSetChanged();
            }
        });

        btn_create.setOnClickListener(this);
        edt_group_name.addTextChangedListener(textWatcher);
        img_back.setOnClickListener(this);
        
    }
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(s.toString().trim().isEmpty())
                btn_create.setEnabled(false);
            else btn_create.setEnabled(true);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void connectViews() {
        edt_group_name = findViewById(R.id.edt_group_name);
        btn_create = findViewById(R.id.btn_create_group);
        lv_friends = findViewById(R.id.friends_container);
        tv_members = findViewById(R.id.tv_members);
        friendsSelectedContainer = findViewById(R.id.friends_selected_container);
        img_back = findViewById(R.id.img_back);

        friendList = new ArrayList<ContactItem>();
        retrieveFriendList();

        adapter = new ContactItemAdapter(this, 1, friendList);
        lv_friends.setAdapter(adapter);

        itemFriendsSelected = new ArrayList<FriendItem>();

        friendAdapter = new FriendAdapter(this, itemFriendsSelected);
        friendAdapter.setListener(new ConversationAdapter.Listener() {
            @Override
            public void onClick(int position) {
                itemFriendsSelected.remove(position);
                friendAdapter.notifyDataSetChanged();
                integerList.remove(position);
            }
        });

        friendsSelectedContainer.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        friendsSelectedContainer.setAdapter(friendAdapter);

        integerList = new ArrayList<Integer>();
    }

    private void retrieveFriendList() {
        SendBird.createFriendListQuery().next(new FriendListQuery.FriendListQueryResultHandler() {
            @Override
            public void onResult(List<User> list, SendBirdException e) {
                for(User a: list){
                    friendList.add(new ContactItem(a.getUserId(), a.getNickname(), a.getProfileUrl()));
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_create_group:
                if(itemFriendsSelected.size() == 0)
                    Toast.makeText(CreateGroupActivity.this, "Chưa chọn thành viên", Toast.LENGTH_SHORT).show();
                else
                {
                    List<String> channelUserIds = new ArrayList<String>();
                    for (FriendItem item: itemFriendsSelected){
                        channelUserIds.add(item.getId());
                    }
                    channelUserIds.add(mUserId);
                    createGroup(channelUserIds);
                    
                }
                break;

            case R.id.img_back:
                onBackPressed();
        }

    }

    private void createGroup(final List<String> channelUserIds) {
        ProgressDialog.startProgressDialog(CreateGroupActivity.this, "Đang xử lý");

        GroupChannelParams params = new GroupChannelParams()
                .setPublic(false)
                .setEphemeral(true)
                .setDistinct(false)
                .setCustomType("group")
                .setName(edt_group_name.getText().toString().trim())
                .setCoverUrl("https://pacpac-chat.000webhostapp.com/upload/Default.jpg")
                .addUserIds(channelUserIds);
        GroupChannel.createChannel(params, new GroupChannel.GroupChannelCreateHandler() {
            @Override
            public void onResult(GroupChannel groupChannel, SendBirdException e) {
                if(e!=null){
                    ProgressDialog.dismissProgressDialog();
                    Toast.makeText(CreateGroupActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
                else {
                    mChannelId = groupChannel.getUrl();
                    createChannelInDatabase(mChannelId, channelUserIds);
                    sendIniMessage(groupChannel);
                }

            }

            private void createChannelInDatabase(final String channelId, List<String>Ids) {
                final String idListString = new Gson().toJson(Ids);

                RequestQueue requestQueue = Volley.newRequestQueue(CreateGroupActivity.this);
                StringRequest request =new StringRequest(Request.Method.POST,
                        CREATE_GROUP_URL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Toast.makeText(CreateGroupActivity.this, response, Toast.LENGTH_LONG).show();
                                ProgressDialog.dismissProgressDialog();
                            }
                        }
                        ,
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(CreateGroupActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                                ProgressDialog.dismissProgressDialog();
                            }
                        }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String,String> params =new HashMap<>();
                        params.put("channelId", channelId);
                        params.put("Ids", idListString);
                        params.put("name", edt_group_name.getText().toString().trim());
                        return params;
                    }
                };
                int socketTimeout = 20000;//20s timeout
                RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                requestQueue.add(request);
            }
        });

    }

    private void sendIniMessage(GroupChannel groupChannel) {
        UserMessageParams params = new UserMessageParams()
                .setMessage("GROUP CREATED")
                .setCustomType("text");

        groupChannel.sendUserMessage(params, new BaseChannel.SendUserMessageHandler() {
            @Override
            public void onSent(UserMessage userMessage, SendBirdException e) {
                sendToDatabase(userMessage);
            }
        });
    }

    private int member_exists(int postitionSelected, List<Integer> positionArray) {
        if(positionArray == null)
            return -1;
        for (int i = 0; i < positionArray.size(); i++) {
            if(postitionSelected == positionArray.get(i))
            {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected  void onResume() {
        SendBird.addChannelHandler(PersonProfileActivity.CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                String senderId = baseMessage.getSender().getUserId();
                String type = baseMessage.getCustomType();
                String message = baseMessage.getMessage();
                if (type.equals("notify")) {
                    if (message.equals("add")) {
                        List<String> friend = new ArrayList<>();
                        friend.add(baseMessage.getSender().getUserId());

                        SendBird.addFriends(friend, new SendBird.AddFriendsHandler() {
                            @Override
                            public void onResult(List<User> list, SendBirdException e) {
                                if (e != null)
                                    Toast.makeText(CreateGroupActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    if (message.equals("delete")) {
                        SendBird.deleteFriend(senderId, new SendBird.DeleteFriendHandler() {
                            @Override
                            public void onResult(SendBirdException e) {
                                if (e != null)
                                    Toast.makeText(CreateGroupActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

            }
        });
        super.onResume();

    }

    @Override
    protected void onPause() {
        SendBird.removeChannelHandler(PersonProfileActivity.CHANNEL_HANDLER_ID);
        super.onPause();
    }
    private void sendToDatabase(final BaseMessage userMessage) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest request =new StringRequest(Request.Method.POST,
                ChatWindowActivity.SEND_TO_DATABASE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("fail"))
                            Toast.makeText(CreateGroupActivity.this, "Đã xảy ra lỗi", Toast.LENGTH_LONG).show();
                    }
                }
                ,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(CreateGroupActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                String time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                        .format(new Date(userMessage.getCreatedAt()));

                Map<String,String> params =new HashMap<>();
                params.put("fromId", mUserId);
                params.put("channelId", mChannelId);
                params.put("message", "GROUP CREATED");
                params.put("date", time);
                params.put("type","text");
                return params;
            }
        };
        int socketTimeout = 20000;//20s timeout
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        requestQueue.add(request);
    }
}
