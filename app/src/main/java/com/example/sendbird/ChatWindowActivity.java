package com.example.sendbird;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
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
import com.sendbird.android.ApplicationUserListQuery;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.FileMessageParams;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.Member;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserListQuery;
import com.sendbird.android.UserMessage;
import com.sendbird.android.UserMessageParams;
import com.sendbird.android.shadow.com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatWindowActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String GET_MESSAGES_URL = "https://pacpac-chat.000webhostapp.com/getMessageItem.php";
    public static final String EXTRA_COVERSATION_ID = "ConversationID";
    public static final String EXTRA_COVERSATION_NAME = "ConversationName";
    public static final String EXTRA_COVERSATION_AVA = "ConversationAva";
    public static final String EXTRA_COVERSATION_CHANNEL = "ConversationChannel";
    public static final int ACTION_GET_PICTURE = 113;
    private int STORAGE_PERMISSION_CODE = 1;
    private int MEDIA_PERMISSION_CODE = 2;
    public static final String LIST_STATE = "list state";
    public static final String CHANNEL_HANDlER = "ChatWindow Channel Handler";
    public static final String SEND_TO_DATABASE = "https://pacpac-chat.000webhostapp.com/SaveMessage.php";

    private String channelId, name, avatar, userId;
    private String checker = "", myUrl = "";
    private Uri fileUri;
    private GroupChannel mGoupChannel;

    private TextView tv_username;
    private CircleImageView civ_avatar;
    private ImageView img_back;
    private ImageButton ib_send;
    private EditText edt_message;
    private RecyclerView message_container;
    private TextView tv_online_status;
    private ImageView img_online_status;
    private ImageButton ib_send_picture;
    private ImageButton ib_send_file;
    private ImageButton ib_menu;
    private TextView mCurrentEventText;
    private LinearLayout mMediaLinearLayout;
    private ImageButton mImageButtonVideo;
    private ImageButton mImageButtonCall;

    private ArrayList<ChatItem> chatItems;
    private ChatAdapter adapter;

    private Parcelable rcv_state;
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);
        connectViews();

        SharedPreferences sharedPreferences = getSharedPreferences("user infor", MODE_PRIVATE);
        userId = sharedPreferences.getString("id", null);

        LinearLayout linearLayout = findViewById(R.id.linearInfor);

        //disable send button
        ib_send.setEnabled(false);
        ImageViewCompat.setImageTintList(ib_send, ColorStateList.valueOf(ContextCompat.getColor(ChatWindowActivity.this, R.color.Grey)));

        loadUserInfo();

        edt_message.addTextChangedListener(textWatcher);

        chatItems = new ArrayList<ChatItem>();

        adapter = new ChatAdapter(this, chatItems, userId);
        linearLayoutManager.setStackFromEnd(false);
        message_container.setLayoutManager(linearLayoutManager);
        message_container.setAdapter(adapter);
        adapter.setListener(new ChatAdapter.Listener() {
            @Override
            public void onClick(int position) {
                if (chatItems.get(position).getType().equals("image")) {
                    String imageUrl = chatItems.get(position).getMessage();
                    Intent intent = new Intent(ChatWindowActivity.this, FullViewActivity.class);
                    intent.putExtra(FullViewActivity.EXTRA_IMAGE_URL, imageUrl);
                    startActivity(intent);
                }
                else{
                    TextView dateTime = message_container.findViewHolderForAdapterPosition(position).itemView.findViewById(R.id.tv_date_time);
                    if(!chatItems.get(position).isClicked()){
                        SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        SimpleDateFormat out = new SimpleDateFormat("dd MMM HH:mm");
                        dateTime.setVisibility(View.VISIBLE);
                        try {
                            Date date= in.parse(chatItems.get(position).getDate());
                            dateTime.setText(out.format(date));
                        } catch (ParseException e) {
                            e.printStackTrace();
                            Toast.makeText(ChatWindowActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                        chatItems.get(position).toggleClicked();
                    }
                    else{
                        dateTime.setVisibility(View.GONE);
                        chatItems.get(position).toggleClicked();
                    }

                }
            }
        });

        img_back.setOnClickListener(this);
        ib_send.setOnClickListener(this);
        edt_message.setOnClickListener(this);
        ib_menu.setOnClickListener(this);
        ib_send_file.setOnClickListener(this);
        ib_send_picture.setOnClickListener(this);
        mImageButtonVideo.setOnClickListener(this);
        mImageButtonCall.setOnClickListener(this);

        linearLayout.setOnClickListener(this);

        loadChatHistory();
    }


    private void loadUserInfo() {
        if (getIntent().hasExtra(EXTRA_COVERSATION_CHANNEL)) {
            name = getIntent().getStringExtra(EXTRA_COVERSATION_NAME);
            avatar = getIntent().getStringExtra(EXTRA_COVERSATION_AVA);
            channelId = getIntent().getStringExtra(EXTRA_COVERSATION_CHANNEL);
            Toast.makeText(ChatWindowActivity.this, channelId, Toast.LENGTH_SHORT).show();
            GroupChannel.getChannel(channelId, new GroupChannel.GroupChannelGetHandler() {
                @Override
                public void onResult(GroupChannel groupChannel, SendBirdException e) {
                    if(e!=null){
                        Log.e("ERROR", e.getMessage());
                        Toast.makeText(ChatWindowActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                    else{
                        mGoupChannel = groupChannel;
                        if (!mGoupChannel.getCustomType().equals("group")){
                            mMediaLinearLayout.setVisibility(View.VISIBLE);
                        }
                        if(mGoupChannel.isDistinct())
                            updateCurrentUserState();
                    }

                }
            });

            tv_username.setText(name);
            Glide.with(this)
                    .load(avatar)
                    .thumbnail(0.5f)
                    .into(civ_avatar);
        }
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.toString().trim().length() > 0) {
                setTypingStatus(true);

                ib_send.setImageResource(R.drawable.ic_send);
                ImageViewCompat.setImageTintList(ib_send, ColorStateList.valueOf(ContextCompat.getColor(ChatWindowActivity.this, R.color.blue)));

                ib_send.setEnabled(true);
                ib_send_picture.setVisibility(View.GONE);
                ib_send_file.setVisibility(View.GONE);
                ib_menu.setVisibility(View.VISIBLE);
            } else {
                setTypingStatus(false);

                ib_send.setImageResource(R.drawable.ic_send);
                ImageViewCompat.setImageTintList(ib_send, ColorStateList.valueOf(ContextCompat.getColor(ChatWindowActivity.this, R.color.Grey)));

                ib_send.setEnabled(false);
                ib_send_picture.setVisibility(View.VISIBLE);
                ib_send_file.setVisibility(View.VISIBLE);
                ib_menu.setVisibility(View.GONE);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void setTypingStatus(boolean typing) {
        if(typing)
            mGoupChannel.startTyping();
        else mGoupChannel.endTyping();
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(ChatWindowActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == STORAGE_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                getImageFromGallery();
            else{
                Toast.makeText(ChatWindowActivity.this, "Bạn cần cấp quyền để chia sẻ hình ảnh", Toast.LENGTH_SHORT).show();
            }
        } else if(requestCode == MEDIA_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
            && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED){
                Intent videoCall = new Intent(ChatWindowActivity.this, VideoCallActivity.class);
                videoCall.putExtra("senderID", userId);
                videoCall.putExtra("receiverID", getIntent().getStringExtra(EXTRA_COVERSATION_ID));
                videoCall.putExtra("receiverName", name);
                videoCall.putExtra("receiveImg", avatar);
                startActivity(videoCall);
            }else{
                Toast.makeText(ChatWindowActivity.this, "Bạn cần cấp quyền để thực hiện cuộc gọi", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void connectViews() {
        mMediaLinearLayout = findViewById(R.id.media);
        tv_username = findViewById(R.id.tv_friend_name);
        civ_avatar = findViewById(R.id.civ_friend_avatar);
        img_back = findViewById(R.id.img_back);
        ib_send = findViewById(R.id.ib_send_message);
        edt_message = findViewById(R.id.edt_chat_box);
        message_container = findViewById(R.id.rcv_message_container);
        tv_online_status = findViewById(R.id.tv_status);
        img_online_status = findViewById(R.id.img_status);
        ib_send_picture = findViewById(R.id.imgbtn_send_picture);
        ib_send_file = findViewById(R.id.imgbtn_send_file);
        ib_menu = findViewById(R.id.imgbtn_media_menu);
        mImageButtonCall = findViewById(R.id.imgBtn_Call);
        mImageButtonVideo = findViewById(R.id.imgBtn_Videocall);

        mCurrentEventText = (TextView)findViewById(R.id.text_group_chat_current_event);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                onBackPressed();

                break;

            case R.id.ib_send_message:
                checker = "text";
                sendMessage();

                //scroll rcv to the last item when send button is click
                message_container.smoothScrollToPosition(message_container.getAdapter().getItemCount());
                edt_message.setText("");
                break;

            case R.id.edt_chat_box:
                ib_send_picture.setVisibility(View.GONE);
                ib_send_file.setVisibility(View.GONE);
                ib_menu.setVisibility(View.VISIBLE);
                break;

            case R.id.rcv_message_container:
                Log.d("message", "clicked");

                break;

            case R.id.imgbtn_send_picture:
                checker = "image";
                if(ContextCompat.checkSelfPermission(ChatWindowActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    getImageFromGallery();
                }else{
                    requestStoragePermission();
                }
                break;

            case R.id.imgbtn_media_menu:
                ib_send_picture.setVisibility(View.VISIBLE);
                ib_send_file.setVisibility(View.VISIBLE);
                ib_menu.setVisibility(View.GONE);
                break;
            case R.id.linearInfor:
                if(mGoupChannel.getCustomType().equals("group")){

                }
                else{
                    Intent intent = new Intent(this, PersonProfileActivity.class);
                    intent.putExtra(PersonProfileActivity.EXTRA_ID, getIntent().getStringExtra(EXTRA_COVERSATION_ID));
                    startActivity(intent);
                }
                break;
            case R.id.imgBtn_Call:
                if(ContextCompat.checkSelfPermission(ChatWindowActivity.this,
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
                    Intent audioCall = new Intent(ChatWindowActivity.this, AudioCallActivity.class);
                    audioCall.putExtra("channelId", channelId);
                    audioCall.putExtra("senderID", userId);
                    audioCall.putExtra("receiverID", getIntent().getStringExtra(EXTRA_COVERSATION_ID));
                    audioCall.putExtra("receiverName", name);
                    audioCall.putExtra("receiveImg", avatar);
                    startActivity(audioCall);
                }else{
                    retrievePermissions();
                }
                break;
            case R.id.imgBtn_Videocall:
                if(ContextCompat.checkSelfPermission(ChatWindowActivity.this,
                        Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(ChatWindowActivity.this,
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
                    Intent videoCall = new Intent(ChatWindowActivity.this, VideoCallActivity.class);
                    videoCall.putExtra("channelId", channelId);
                    videoCall.putExtra("senderID", userId);
                    videoCall.putExtra("receiverID", getIntent().getStringExtra(EXTRA_COVERSATION_ID));
                    videoCall.putExtra("receiverName", name);
                    videoCall.putExtra("receiveImg", avatar);
                    startActivity(videoCall);
                }else{
                    retrievePermissions();
                }

                break;
        }
    }

    private void retrievePermissions() {
        ActivityCompat.requestPermissions(ChatWindowActivity.this,
                new String[]{Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO },
                MEDIA_PERMISSION_CODE);
    }


    private void sendMessage() {
        UserMessageParams params = new UserMessageParams()
                .setMessage(edt_message.getText().toString())
                .setCustomType("text");

        mGoupChannel.sendUserMessage(params, new BaseChannel.SendUserMessageHandler() {
            @Override
            public void onSent(UserMessage userMessage, SendBirdException e) {
                sendToDatabase(userMessage);
                displaySend(userMessage);
            }
        });
    }

    private void sendToDatabase(final BaseMessage userMessage) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest request =new StringRequest(Request.Method.POST,
                SEND_TO_DATABASE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("fail"))
                            Toast.makeText(ChatWindowActivity.this, "Đã xảy ra lỗi", Toast.LENGTH_LONG).show();
                    }
                }
                ,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ChatWindowActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                String time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                        .format(new Date(userMessage.getCreatedAt()));

                Map<String,String> params =new HashMap<>();
                params.put("fromId", userId);
                params.put("channelId", channelId);
                params.put("message", userMessage.getCustomType().equals("text")? userMessage.getMessage():((FileMessage) userMessage).getUrl());
                params.put("date", time);
                params.put("type",userMessage.getCustomType().equals("text")? "text":"image");
                return params;
            }
        };
        int socketTimeout = 20000;//20s timeout
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        requestQueue.add(request);
    }


    /*
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText ||v instanceof ImageButton) {
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

     */

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Tag", "Chat Window Started");

    }



    private void loadChatHistory() {
        final Gson gson =new Gson();
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest request =new StringRequest(Request.Method.POST,
                GET_MESSAGES_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i< jsonArray.length(); i++){
                                ChatItem message = gson.fromJson(jsonArray.getJSONObject(i).toString(), ChatItem.class);
                                chatItems.add(message);
                            }
                            adapter.notifyDataSetChanged();

                            if(chatItems.size()>0){
                                message_container.smoothScrollToPosition(message_container.getAdapter().getItemCount() - 1);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ChatWindowActivity.this, response, Toast.LENGTH_LONG).show();
                        }
                    }
                }
                ,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ChatWindowActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params =new HashMap<>();
                params.put("channelId",channelId);
                return params;
            }
        };
        int socketTimeout = 20000;//20s timeout
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        requestQueue.add(request);
    }


    @Override
    protected void onPause() {
        super.onPause();

        SendBird.removeChannelHandler(CHANNEL_HANDlER);
        mGoupChannel.endTyping();
        adapter.notifyDataSetChanged();
    }

    private void updateCurrentUserState() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                List<String> userId = new ArrayList<>();
                userId.add(getIntent().getStringExtra(EXTRA_COVERSATION_ID));
                ApplicationUserListQuery query = SendBird.createApplicationUserListQuery();
                query.setUserIdsFilter(userId);
                query.next(new UserListQuery.UserListQueryResultHandler() {
                    @Override
                    public void onResult(List<User> list, SendBirdException e) {
                        if(list.get(0).getConnectionStatus().name().equals("ONLINE")){
                            tv_online_status.setVisibility(View.VISIBLE);
                            tv_online_status.setText("Đang hoạt động");
                            img_online_status.setVisibility(View.VISIBLE);
                        }
                        else{
                            displayLastSeen(list.get(0).getLastSeenAt());
                            img_online_status.setVisibility(View.INVISIBLE);
                        }
                    }
                });
                handler.postDelayed(this, 60000);
            }
        });


    }

    private void displayLastSeen(long lastSeen) {
        long now = System.currentTimeMillis();
        int second = (int) (now  - lastSeen)/1000;
        tv_online_status.setText("Hoạt động " + String.valueOf((second/60) + 1) + " phút trước");
        tv_online_status.setVisibility(View.VISIBLE);

    }

    private void getImageFromGallery() {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, ACTION_GET_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTION_GET_PICTURE && resultCode == RESULT_OK && data != null & data.getData() != null) {
            fileUri = data.getData();
            File file = new File(RealPathUlti.getRealPathFromURI_API19(ChatWindowActivity.this, fileUri));

            FileMessageParams params = new FileMessageParams()
                    .setFile(file)
                    .setCustomType("image")
                    .setFileSize(500);
            mGoupChannel.sendFileMessage(params, new BaseChannel.SendFileMessageHandler() {
                @Override
                public void onSent(FileMessage fileMessage, SendBirdException e) {
                    sendToDatabase(fileMessage);
                    displaySend(fileMessage);
                }
            });

        }
    }

    private String getRealPathFromURI(Uri fileUri) {
        String result;
        Cursor cursor = getContentResolver().query(fileUri, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = fileUri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

//        rcv_state = linearLayoutManager.onSaveInstanceState();
//        outState.putParcelable(LIST_STATE, rcv_state);


    }
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);

        // Retrieve list state and list/item positions
//        rcv_state = state.getParcelable(LIST_STATE);
    }
    @Override
    protected void onResume() {
        super.onResume();

//        if (rcv_state != null) {
//            linearLayoutManager.onRestoreInstanceState(rcv_state);
//        }

        SendBird.addChannelHandler(CHANNEL_HANDlER, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                String channel = baseChannel.getUrl();
                String sender = baseMessage.getSender().getUserId();
                if(channel.equals(channelId)){
                    displayReceive(baseMessage);
                    message_container.smoothScrollToPosition(message_container.getAdapter().getItemCount() - 1);
                }
            }

            @Override
            public void onTypingStatusUpdated(GroupChannel channel) {
                if (channel.getUrl().equals(channelId)) {
                    List<Member> typingUsers = channel.getTypingMembers();
                    displayTyping(typingUsers);
                }
            }
        });
    }

    private void displayTyping(List<Member> typingUsers) {
        if (typingUsers.size() > 0) {
            mCurrentEventText.setVisibility(View.VISIBLE);
            String string;

            if (typingUsers.size() == 1) {
                string = String.format(getString(R.string.user_typing), typingUsers.get(0).getNickname());
            } else if (typingUsers.size() == 2) {
                string = String.format(getString(R.string.two_users_typing), typingUsers.get(0).getNickname(), typingUsers.get(1).getNickname());
            } else {
                string = getString(R.string.users_typing);
            }
            mCurrentEventText.setText(string);
        } else {
            mCurrentEventText.setVisibility(View.GONE);
        }
    }

    private void displayReceive(BaseMessage baseMessage) {
        String time  = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(baseMessage.getCreatedAt()));
        if(baseMessage instanceof UserMessage){
            ChatItem chatItem = new ChatItem(String.valueOf(baseMessage.getMessageId()),baseMessage.getSender().getUserId(),
                    baseMessage.getSender().getNickname(), baseMessage.getSender().getProfileUrl(),
                    baseMessage.getMessage(), baseMessage.getCustomType(), time);

            chatItems.add(chatItem);
            adapter.notifyDataSetChanged();
        }
        else if(baseMessage instanceof FileMessage){
            ChatItem chatItem = new ChatItem(String.valueOf(baseMessage.getMessageId()),baseMessage.getSender().getUserId(),
                    baseMessage.getSender().getNickname(), baseMessage.getSender().getProfileUrl(),
                    ((FileMessage) baseMessage).getUrl(), baseMessage.getCustomType(), time);

            chatItems.add(chatItem);
            adapter.notifyDataSetChanged();
        }
    }

    private void displaySend(BaseMessage baseMessage) {
        String time  = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(baseMessage.getCreatedAt()));
        if(baseMessage instanceof UserMessage){
            ChatItem chatItem = new ChatItem(String.valueOf(baseMessage.getMessageId()),baseMessage.getSender().getUserId(),
                    baseMessage.getMessage(), baseMessage.getCustomType(), time);

            chatItems.add(chatItem);
            adapter.notifyDataSetChanged();
        }
        else if(baseMessage instanceof FileMessage){
            ChatItem chatItem = new ChatItem(String.valueOf(baseMessage.getMessageId()),baseMessage.getSender().getUserId(),
                    ((FileMessage) baseMessage).getUrl(), baseMessage.getCustomType(), time);

            chatItems.add(chatItem);
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("TAG", "onDestroy");
    }
}
