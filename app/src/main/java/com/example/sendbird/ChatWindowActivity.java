package com.example.sendbird;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserListQuery;
import com.sendbird.android.shadow.com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatWindowActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String GET_MESSAGES_URL = "http://192.168.100.11:8080/SendBird/getMessageItem.php";
    public static final String EXTRA_COVERSATION_ID = "ConversationID";
    public static final String EXTRA_COVERSATION_NAME = "ConversationName";
    public static final String EXTRA_COVERSATION_AVA = "ConversationAva";
    public static final String EXTRA_COVERSATION_CHANNEL = "ConversationChannel";
    public static final int ACTION_GET_PICTURE = 113;
    public static final String LIST_STATE = "list state";

    private String channelId, name, avatar, currentUID;
    private String checker = "", myUrl = "";
    private Uri fileUri;

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

    private ArrayList<ChatItem> chatItems;
    private ChatAdapter adapter;

    private Parcelable rcv_state;
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);
        connectViews();

        //disable send button
        ib_send.setEnabled(false);
        ImageViewCompat.setImageTintList(ib_send, ColorStateList.valueOf(ContextCompat.getColor(ChatWindowActivity.this, R.color.Grey)));

        loadUserInfo();

        edt_message.addTextChangedListener(textWatcher);

        chatItems = new ArrayList<ChatItem>();

        adapter = new ChatAdapter(this, chatItems, "1");
        linearLayoutManager.setStackFromEnd(false);
        message_container.setLayoutManager(linearLayoutManager);
        message_container.setAdapter(adapter);
        adapter.setListener(new ChatAdapter.Listener() {
            @Override
            public void onClick(int position) {
                if (chatItems.get(position).getType().equals("image")) {
                    String imageUrl = chatItems.get(position).getMessage();
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

    }

    private void loadUserInfo() {
        if (getIntent().hasExtra(EXTRA_COVERSATION_ID)) {
            name = getIntent().getStringExtra(EXTRA_COVERSATION_NAME);
            avatar = getIntent().getStringExtra(EXTRA_COVERSATION_AVA);
            channelId = getIntent().getStringExtra(EXTRA_COVERSATION_CHANNEL);

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
                ib_send.setImageResource(R.drawable.ic_send);
                ImageViewCompat.setImageTintList(ib_send, ColorStateList.valueOf(ContextCompat.getColor(ChatWindowActivity.this, R.color.blue)));

                ib_send.setEnabled(true);
                ib_send_picture.setVisibility(View.GONE);
                ib_send_file.setVisibility(View.GONE);
                ib_menu.setVisibility(View.VISIBLE);
            } else {
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

    private void connectViews() {

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
                message_container.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        message_container.smoothScrollToPosition(message_container.getAdapter().getItemCount() - 1);
                    }
                }, 100);

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
                getImageFromGallery();
                break;

            case R.id.imgbtn_send_file:
                break;

            case R.id.imgbtn_media_menu:
                ib_send_picture.setVisibility(View.VISIBLE);
                ib_send_file.setVisibility(View.VISIBLE);
                ib_menu.setVisibility(View.GONE);

                break;
        }
    }


    private void sendMessage() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf_date = new SimpleDateFormat("dd/MMM/YYYY");
        SimpleDateFormat sdf_time = new SimpleDateFormat("hh:mm");
        String date = sdf_date.format(calendar.getTime());
        String time = sdf_time.format(calendar.getTime());
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
        updateCurrentUserState();
        loadChatHistory();

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
                params.put("channelId","1");
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
    }

    private void updateCurrentUserState() {
        /*String currentDate, currentTime;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdfCurrentDate = new SimpleDateFormat("dd/MMM/YYYY");
        SimpleDateFormat sdfCurrentTime = new SimpleDateFormat("hh:mm");

        currentDate = sdfCurrentDate.format(calendar.getTime());
        currentTime = sdfCurrentTime.format(calendar.getTime());

        HashMap<String, Object> userStateMap = new HashMap<>();
        userStateMap.put("status", state);
        userStateMap.put("date", currentDate);
        userStateMap.put("time", currentTime);*/

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

            if (checker.equals("image")) {
            }

        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        rcv_state = linearLayoutManager.onSaveInstanceState();
        outState.putParcelable(LIST_STATE, rcv_state);


    }
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);

        // Retrieve list state and list/item positions
        rcv_state = state.getParcelable(LIST_STATE);
    }
    @Override
    protected void onResume() {
        super.onResume();

        if (rcv_state != null) {
            linearLayoutManager.onRestoreInstanceState(rcv_state);
        }
    }
}
