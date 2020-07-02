package com.example.sendbird;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sendbird.android.ApplicationUserListQuery;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FriendListQuery;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelListQuery;
import com.sendbird.android.Member;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

import org.conscrypt.Conscrypt;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String TAG = MainActivity.class.getSimpleName();

    private CircleImageView civ_userImg;
    private FrameLayout fragment_container;
    private BottomNavigationView navigationView;
    private TextView tv_title;
    private ImageView img_add_user;
    private ImageView img_add_group;

    private String userID;
    private SharedPreferences sharedPreferences;

    private ArrayList<ContactItem> allFriends;
    private ArrayList<ContactItem> onlineFriends;

    private ConversationFragment mConversationFragment;

    Bundle bundle = new Bundle();
    static {
        // add Conscrypt in list of security providers for device
        Security.addProvider(Conscrypt.newProvider());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        connectViews();
        sharedPreferences = getSharedPreferences("user infor", MODE_PRIVATE);
        userID = sharedPreferences.getString("id", null);

        SendBird.init(RegisterActivity.appID, MainActivity.this);
        SendBird.connect(userID, new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                if(e != null)
                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
//        List<String> ids= new ArrayList<String>();
//       ids.add("75");
//        ids.add("77");
//        SendBird.deleteFriends(ids, new SendBird.DeleteFriendsHandler() {
//            @Override
//            public void onResult(SendBirdException e) {
//
//            }
//        });


        allFriends = new ArrayList<>();
        onlineFriends = new ArrayList<>();
        SendBird.createFriendListQuery().next(new FriendListQuery.FriendListQueryResultHandler() {
            @Override
            public void onResult(List<User> list, SendBirdException e) {
                if(e!=null){
                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
                else {
                    if(list.size() ==0)
                        return;
                    else{
                        List<String> userId = new ArrayList<>();
                        for(final User a: list){
                            GroupChannelListQuery query = GroupChannel.createMyGroupChannelListQuery();
                            userId.add(a.getUserId());
                            query.setUserIdsExactFilter(userId);
                            query.setIncludeEmpty(true);
                            query.next(new GroupChannelListQuery.GroupChannelListQueryResultHandler() {
                                @Override
                                public void onResult(final List<GroupChannel> list, SendBirdException e) {
                                    if(e!=null)
                                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                    else{
                                        ContactItem contactItem = new ContactItem(a.getUserId(), a.getNickname(), a.getProfileUrl());
                                        contactItem.setChannel(list.get(0).getUrl());
                                        allFriends.add(contactItem);

                                        list.get(0).refresh(new GroupChannel.GroupChannelRefreshHandler() {
                                            @Override
                                            public void onResult(SendBirdException e) {
                                                List<Member> members =list.get(0).getMembers();
                                                for(Member member: members){
                                                    if(!member.getUserId().equals(userID) && member.getConnectionStatus().equals(User.ConnectionStatus.ONLINE)){
                                                        ContactItem contactItem = new ContactItem(a.getUserId(), a.getNickname(), a.getProfileUrl());
                                                        contactItem.setChannel(list.get(0).getUrl());
                                                        onlineFriends.add(contactItem);
                                                    }
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                            userId.clear();
                        }
                        bundle.putParcelableArrayList("all friends", allFriends);
                        bundle.putParcelableArrayList("onl friends", onlineFriends);

                    }
                }

            }
        });


        bundle.putString("id", userID);
        mConversationFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, mConversationFragment)
                .addToBackStack(null)
                .commit();

        civ_userImg.setOnClickListener(this);
        img_add_user.setOnClickListener(this);
        img_add_group.setOnClickListener(this);

        navigationView.setOnNavigationItemSelectedListener(navListener);
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
                            Glide.with(MainActivity.this)
                                    .load(object.getString("avatar"))
                                    .placeholder(R.drawable.couple)
                                    .thumbnail(0.5f)
                                    .apply(RequestOptions.skipMemoryCacheOf(true))
                                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                                    .into(civ_userImg);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, response, Toast.LENGTH_LONG).show();
                        }
                    }
                }
                ,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params =new HashMap<>();
                params.put("id", userID);
                return params;
            }
        };
        int socketTimeout = 20000;//20s timeout
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        requestQueue.add(request);

    }

    private BottomNavigationView.OnNavigationItemSelectedListener  navListener= new BottomNavigationView.OnNavigationItemSelectedListener(){
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

            Fragment selectedFragment = null;

            switch (menuItem.getItemId()){

                case R.id.nav_home:
                    tv_title.setText("Chat");
                    img_add_user.setVisibility(View.GONE);
                    img_add_group.setVisibility(View.GONE);

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, mConversationFragment)
                            .addToBackStack(null)
                            .commit();
                    return true;

                case R.id.nav_directory:
                    selectedFragment = new DirectoryFragment();

                    img_add_user.setVisibility(View.VISIBLE);
                    img_add_group.setVisibility(View.GONE);
                    tv_title.setText("Danh bạ");
                    break;

                case R.id.nav_group:

                    selectedFragment = new GroupFragment();

                    img_add_user.setVisibility(View.GONE);
                    img_add_group.setVisibility(View.VISIBLE);
                    tv_title.setText("Group");
                    break;
            }
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("all friends", allFriends);
            bundle.putParcelableArrayList("onl friends", onlineFriends);
            bundle.putString("id", userID);
            selectedFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment)
                    .addToBackStack(null)
                    .commit();
            return true;
        }
    };


    private void connectViews() {
        civ_userImg = findViewById(R.id.civ_avatar);
        fragment_container = findViewById(R.id.fragment_container);
        navigationView = findViewById(R.id.bnv_menu);
        tv_title = findViewById(R.id.tv_title);
        img_add_group = findViewById(R.id.img_add_group);
        img_add_user = findViewById(R.id.img_add_user);

        mConversationFragment = ConversationFragment.newInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        retrieveUserInfo();

        updateCurrentUserState("online");
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        updateCurrentUserState("offline");
        SendBird.removeChannelHandler(PersonProfileActivity.CHANNEL_HANDLER_ID);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG ,"onDestroy");

        updateCurrentUserState("offline");
    }

    private void updateCurrentUserState(String state){
        String currentDate, currentTime;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdfCurrentDate = new SimpleDateFormat("dd/MMM/YYYY");
        SimpleDateFormat sdfCurrentTime = new SimpleDateFormat("hh:mm");

        currentDate = sdfCurrentDate.format(calendar.getTime());
        currentTime = sdfCurrentTime.format(calendar.getTime());

        HashMap<String, Object> userStateMap = new HashMap<>();
        userStateMap.put("status", state);
        userStateMap.put("date", currentDate);
        userStateMap.put("time", currentTime);


    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.civ_avatar:

                Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                startActivity(intent);
                finish();
                break;

            case R.id.img_add_group:
                Intent intent1 = new Intent(MainActivity.this, CreateGroupActivity.class);
                startActivity(intent1);

                break;

            case R.id.img_add_user:

                Intent intent3 = new Intent(MainActivity.this, FriendRequestActivity.class);
                startActivity(intent3);

                break;

        }
    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    @Override
    protected void onResume(){
        SendBird.addChannelHandler(PersonProfileActivity.CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                String senderId = baseMessage.getSender().getUserId();
                String type = baseMessage.getCustomType();
                String message = baseMessage.getMessage();
                if(type.equals("notify")){
                    if(message.equals("add")){
                        List<String> friend = new ArrayList<>();
                        friend.add(baseMessage.getSender().getUserId());

                        SendBird.addFriends(friend, new SendBird.AddFriendsHandler() {
                            @Override
                            public void onResult(List<User> list, SendBirdException e) {
                                if(e!=null)
                                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    if(message.equals("delete")){
                        SendBird.deleteFriend(senderId, new SendBird.DeleteFriendHandler() {
                            @Override
                            public void onResult(SendBirdException e) {
                                if(e!=null)
                                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

            }
        });


        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}