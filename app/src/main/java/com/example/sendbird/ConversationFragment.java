package com.example.sendbird;


import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import com.sendbird.android.FileMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.Member;
import com.sendbird.android.SendBird;
import com.sendbird.android.UserMessage;
import com.sendbird.android.shadow.com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConversationFragment extends Fragment {
    public static final String TAG = ConversationFragment.class.getSimpleName();
    public static final String CHANNEL_HANDLER = "Conversation Channel Handler";
    public static final String URL_GET_CONVERSATIONS = "http://192.168.100.12:8080/SendBird/getConversationItem.php";
    private ConversationAdapter adapter;
    private RecyclerView conversationContainer;
    private ArrayList<ConversationItem> conversationItems;

    private String userID;
    public LinearLayoutManager mLinearLayoutManager;

    public ConversationFragment() {

    }
    public static ConversationFragment newInstance(){
        ConversationFragment fragment = new ConversationFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if(bundle.containsKey("id")){
            userID = bundle.getString("id");
        }
        if(bundle.containsKey("list_data")){
            conversationItems = bundle.getParcelableArrayList("list_data");
        }
        else{
            conversationItems = new ArrayList<ConversationItem>();
        }
        Log.d(TAG, "onCreateView");
        
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_conversation, container, false);

        adapter = new ConversationAdapter(getActivity(), conversationItems);

        conversationContainer = v.findViewById(R.id.conversations_container);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setStackFromEnd(true);
        mLinearLayoutManager.setReverseLayout(true);
        conversationContainer.setLayoutManager(mLinearLayoutManager);
        conversationContainer.setAdapter(adapter);

        adapter.setListener(new ConversationAdapter.Listener() {
            public void onClick(int position) {
                Intent intent = new Intent(getActivity(), ChatWindowActivity.class);
                intent.putExtra(ChatWindowActivity.EXTRA_COVERSATION_ID, conversationItems.get(position).getUid());
                intent.putExtra(ChatWindowActivity.EXTRA_COVERSATION_CHANNEL, conversationItems.get(position).getChannelId());
                intent.putExtra(ChatWindowActivity.EXTRA_COVERSATION_NAME, conversationItems.get(position).getName());
                intent.putExtra(ChatWindowActivity.EXTRA_COVERSATION_AVA, conversationItems.get(position).getAvatar());
                getActivity().startActivity(intent);
            }
        });

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        SendBird.addChannelHandler(CHANNEL_HANDLER, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
            }

            @Override
            public void onChannelChanged(BaseChannel channel) {
                GroupChannel groupChannel = (GroupChannel) channel;
                if(!groupChannel.getCustomType().equals("group")){
                    BaseMessage baseMessage = groupChannel.getLastMessage();
                    if(!baseMessage.getCustomType().equals("notify")) {
                        for (int i = 0; i < conversationItems.size(); i++) {
                            if (conversationItems.get(i).getChannelId().equals(channel.getUrl())) {
                                updateChannel(i, groupChannel, baseMessage);
                                return;
                            }

                        }
                        addChannel(groupChannel, baseMessage);
                    }
                    else if(baseMessage.getCustomType().equals("notify") && baseMessage.getMessage().equals("delete")){

                        int index = -1;
                        for (int i = 0; i< conversationItems.size();i++) {
                            if(conversationItems.get(i).getChannelId().equals(baseMessage.getChannelUrl())){
                                index = i;
                                break;
                            }
                        }if(index!=-1){
                            conversationItems.remove(index);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            private void updateChannel(int pos,GroupChannel channel, BaseMessage baseMessage) {
                conversationItems.remove(pos);
                addChannel(channel, baseMessage);
            }
            private void addChannel(GroupChannel channel, BaseMessage baseMessage){
                final List<Member> members = channel.getMembers();
                int index = -1;
                String uid = "", senderId = baseMessage.getSender().getUserId();
                for (int i = 0 ; i < members.size(); i++) {
                    if(!members.get(i).getUserId().equals(userID)){
                        uid = members.get(i).getUserId();
                        index = i;
                        break;
                    }
                }
                Member friend = members.get(index);

                String channelId = channel.getUrl();
                String name = baseMessage.getSender().getNickname();
                String avatar = baseMessage.getSender().getProfileUrl();
                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(baseMessage.getCreatedAt()));
                String message = "";

                if(senderId.equals(userID))
                {
                    name = friend.getNickname();
                    avatar = friend.getProfileUrl();
                    message = "Bạn: ";
                }
                if(baseMessage instanceof UserMessage){
                    message += baseMessage.getMessage();
                    ConversationItem item = new ConversationItem(uid,channelId, name, avatar, time, message);
                    conversationItems.add(item);
                }else if(baseMessage instanceof FileMessage){
                    message += ((FileMessage) baseMessage).getUrl();
                    ConversationItem item = new ConversationItem(uid,channelId, name, avatar, time, message);
                    conversationItems.add(item);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("list_data", conversationItems);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState!=null){
            conversationItems = savedInstanceState.getParcelableArrayList("list_data");
            adapter.notifyDataSetChanged();
        }

    }

    private void retrieveConversations() {
        final Gson gson =new Gson();
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        StringRequest request =new StringRequest(Request.Method.POST,
                URL_GET_CONVERSATIONS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i< jsonArray.length(); i++){
                                ConversationItem message = gson.fromJson(jsonArray.getJSONObject(i).toString(), ConversationItem.class);
                                conversationItems.add(message);
                            }
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), response, Toast.LENGTH_LONG).show();
                        }
                    }
                }
                ,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params =new HashMap<>();
                params.put("id",userID);
                return params;
            }
        };
        int socketTimeout = 20000;//20s timeout
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        requestQueue.add(request);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        retrieveConversations();
    }

    @Override
    public void onPause() {
        super.onPause();

        getArguments().putParcelableArrayList("list_data", conversationItems);
    }
}
