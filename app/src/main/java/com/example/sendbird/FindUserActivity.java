package com.example.sendbird;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FindUserActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String FIND_USER_URL = "http://192.168.100.11:8080/SendBird/FindUser.php";

    private ListView friend_container;
    private EditText edt_find_friend;
    private Button btnSearch;
    private LinearLayout results_container;
    private ProgressBar pbProcessing;
    private TextView tvNoResult;
    private ArrayList<ContactItem> mFriendList;
    private ContactItemAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_user);
        connectViews();

        btnSearch.setOnClickListener(this);
    }

    private void connectViews() {
        friend_container = findViewById(R.id.friends_container);
        edt_find_friend = findViewById(R.id.edt_find_user);
        btnSearch = findViewById(R.id.btn_search);
        results_container = findViewById(R.id.search_results_container);
        tvNoResult = findViewById(R.id.tv_no_result);
        pbProcessing = findViewById(R.id.rotateloading);

        mFriendList = new ArrayList<ContactItem>();
        mAdapter = new ContactItemAdapter(FindUserActivity.this, 0,mFriendList);
        friend_container.setAdapter(mAdapter);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.edt_find_user:

            case R.id.btn_search:
                if(edt_find_friend.getText().toString().trim().equals(""))
                    return;
                else
                {
                    hideView(results_container);
                    hideView(tvNoResult);
                    displayView(pbProcessing);

                    mFriendList.clear();
                    mAdapter.notifyDataSetChanged();

                    findFriends(edt_find_friend.getText().toString().trim());
                    break;
                }

        }
    }

    private void displayView(View v) {
        v.setVisibility(View.VISIBLE);
    }

    private void findFriends(final String name) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, FIND_USER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        hideView(pbProcessing);

                        if (response.equals("fail")) {
                            displayView(tvNoResult);
                        } else {
                            displayView(results_container);

                            try {
                                JSONArray Contacts = new JSONArray(response);
                                for (int i = 0; i < Contacts.length(); i++) {
                                    try {
                                        JSONObject jsonObject = Contacts.getJSONObject(i);

                                        ContactItem friend_item = new ContactItem();
                                        friend_item.setName(jsonObject.getString("name"));
                                        friend_item.setUid(jsonObject.getString("id"));
                                        friend_item.setAvatar(jsonObject.getString("avatar"));

                                        mFriendList.add(friend_item);
                                        mAdapter.notifyDataSetChanged();

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error", error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("name",name);
                return params;
            }
        };
        requestQueue.add(request);
    }

    private void hideView(View v) {
        v.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();

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
                                    Toast.makeText(FindUserActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    if (message.equals("delete")) {
                        SendBird.deleteFriend(senderId, new SendBird.DeleteFriendHandler() {
                            @Override
                            public void onResult(SendBirdException e) {
                                if (e != null)
                                    Toast.makeText(FindUserActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
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
}
