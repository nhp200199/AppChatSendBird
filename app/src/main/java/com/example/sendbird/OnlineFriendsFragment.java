package com.example.sendbird;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sendbird.android.FriendListQuery;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelListQuery;
import com.sendbird.android.Member;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

import java.util.ArrayList;
import java.util.List;



/**
 * A simple {@link Fragment} subclass.
 */
public class OnlineFriendsFragment extends Fragment {

    private ListView friendsContainer;

    private ContactItemAdapter adapter;
    private List<ContactItem> items;

    private String userID;
    private SharedPreferences sharedPreferences;

    public OnlineFriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_online_friends, container, false);
        sharedPreferences = getActivity().getSharedPreferences("user infor", Context.MODE_PRIVATE);
        userID = sharedPreferences.getString("id", null);

        friendsContainer = v.findViewById(R.id.friends_container);
        Bundle bundle = getArguments();
        items = bundle.getParcelableArrayList("onl friends");
        adapter = new ContactItemAdapter(getActivity(), 1, items);

        friendsContainer.setAdapter(adapter);
        friendsContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ChatWindowActivity.class);
                intent.putExtra(ChatWindowActivity.EXTRA_COVERSATION_ID, items.get(position).getUid());
                intent.putExtra(ChatWindowActivity.EXTRA_COVERSATION_NAME, items.get(position).getName());
                intent.putExtra(ChatWindowActivity.EXTRA_COVERSATION_AVA, items.get(position).getAvatar());
                intent.putExtra(ChatWindowActivity.EXTRA_COVERSATION_CHANNEL, items.get(position).getChannel());

                Toast.makeText(getActivity(), items.get(position).getChannel(), Toast.LENGTH_SHORT).show();

                startActivity(intent);
            }
        });


        Log.d("TAG", "AllFriendsFragment OnCreateView");

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        checkFriendsState();
    }

    private void checkFriendsState() {

        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                items.clear();
                adapter.notifyDataSetChanged();
                SendBird.createFriendListQuery().next(new FriendListQuery.FriendListQueryResultHandler() {
                    @Override
                    public void onResult(List<User> list, SendBirdException e) {
                        if(e!=null){
                            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
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
                                                Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
                                            else{
                                                list.get(0).refresh(new GroupChannel.GroupChannelRefreshHandler() {
                                                    @Override
                                                    public void onResult(SendBirdException e) {
                                                        List<Member> members =list.get(0).getMembers();
                                                        for(Member member: members){
                                                            if(!member.getUserId().equals(userID) && member.getConnectionStatus().equals(User.ConnectionStatus.ONLINE)){
                                                                ContactItem contactItem = new ContactItem(a.getUserId(), a.getNickname(), a.getProfileUrl());
                                                                contactItem.setChannel(list.get(0).getUrl());
                                                                items.add(contactItem);
                                                                adapter.notifyDataSetChanged();
                                                                return;
                                                            }
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                    userId.clear();
                                }
                            }
                        }

                    }
                });
                handler.postDelayed(this, 60000);
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();

    }
}
