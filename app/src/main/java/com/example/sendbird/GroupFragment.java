package com.example.sendbird;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupFragment extends Fragment{
    public static final String RETRIEVE_GROUPS_URL = "http://192.168.100.12:8080/SendBird/getGroupConversation.php";
    public static final String CHANNEL_HANDLER = "Group Channel Handler";

    private RecyclerView groupContainer;
    private TextView tv_none;
    private ArrayList<ConversationItem> conversationItems;
    private ConversationAdapter adapter;
    private String userId;
    public GroupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_group, container, false);
        userId = getArguments().getString("id");

        tv_none = v.findViewById(R.id.tv_no_group);
        groupContainer = v.findViewById(R.id.groups_container);

        conversationItems = new ArrayList<ConversationItem>();
        adapter = new ConversationAdapter(getContext(), conversationItems);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        groupContainer.setLayoutManager(linearLayoutManager);
        groupContainer.setAdapter(adapter);

        adapter.notifyDataSetChanged();

        adapter.setListener(new ConversationAdapter.Listener() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent(getActivity(), ChatWindowActivity.class);
                intent.putExtra(ChatWindowActivity.EXTRA_COVERSATION_CHANNEL, conversationItems.get(position).getChannelId());
                intent.putExtra(ChatWindowActivity.EXTRA_COVERSATION_AVA, conversationItems.get(position).getAvatar());
                intent.putExtra(ChatWindowActivity.EXTRA_COVERSATION_NAME, conversationItems.get(position).getName());
                getActivity().startActivity(intent);
            }
        });

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        retrieveGroups();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("Tag", "Group Fragment: Start");
        SendBird.addChannelHandler(CHANNEL_HANDLER, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
            }

            @Override
            public void onChannelChanged(BaseChannel channel) {
                GroupChannel groupChannel = (GroupChannel) channel;
                if(groupChannel.getCustomType().equals("group")){
                    BaseMessage baseMessage = groupChannel.getLastMessage();
                    if(!baseMessage.getCustomType().equals("notify")){
                        for(int i = 0; i < conversationItems.size(); i++){
                            if(conversationItems.get(i).getChannelId().equals(channel.getUrl())){
                                updateChannel(i ,groupChannel, baseMessage);
                                return;
                            }

                        }
                        addChannel(groupChannel, baseMessage);
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
                    if(!members.get(i).getUserId().equals(userId)){
                        uid = members.get(i).getUserId();
                        index = i;
                        break;
                    }
                }
                Member friend = members.get(index);

                String channelId = channel.getUrl();
                String name = channel.getName();
                String avatar = channel.getCoverUrl();
                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(baseMessage.getCreatedAt()));
                String message = "";

                if(senderId.equals(userId)) {
                    message = "Bạn: ";
                }
                else {
                    message = baseMessage.getSender().getNickname() + ": ";
                }
                if(baseMessage instanceof UserMessage){
                    message += baseMessage.getMessage();
                    ConversationItem item = new ConversationItem(channelId, name, avatar, time, message);
                    conversationItems.add(item);
                }else if(baseMessage instanceof FileMessage){
                    message += ((FileMessage) baseMessage).getUrl();
                    ConversationItem item = new ConversationItem(channelId, name, avatar, time, message);
                    conversationItems.add(item);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void retrieveGroups() {
        final Gson gson =new Gson();
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        StringRequest request =new StringRequest(Request.Method.POST,
                RETRIEVE_GROUPS_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //TODO: display conversations
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

                        //check if there is a group
                        if (conversationItems.size() == 0) {
                            tv_none.setVisibility(View.VISIBLE);
                            groupContainer.setVisibility(View.INVISIBLE);
                        } else {
                            tv_none.setVisibility(View.INVISIBLE);
                            groupContainer.setVisibility(View.VISIBLE);
                        }
                    }
                }
                ,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_LONG).show();
                        ProgressDialog.dismissProgressDialog();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params =new HashMap<>();
                params.put("id", userId);
                return params;
            }
        };
        int socketTimeout = 20000;//20s timeout
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        requestQueue.add(request);
    }
    @Override
    public void onStop() {
        super.onStop();
        Log.d("Tag", "Group Fragment: onStop");
        //conversationItems.clear();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Tag", "Group Fragment: Destroy");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("Tag", "Group Fragment: Destroy View");
    }
}
