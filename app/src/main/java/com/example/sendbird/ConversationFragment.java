package com.example.sendbird;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.SendBird;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConversationFragment extends Fragment {
    private ConversationAdapter adapter;
    private FindPeopleAdapter findPeopleAdapter;
    private RecyclerView conversationContainer;
    private ArrayList<ConversationItem> conversationItems;

    private String userID;

    public ConversationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        if(bundle.containsKey("id")){
            userID = bundle.getString("id");
        }
        
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_conversation, container, false);

        conversationItems = new ArrayList<ConversationItem>();
        adapter = new ConversationAdapter(getActivity(), conversationItems);

        conversationContainer = v.findViewById(R.id.conversations_container);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        conversationContainer.setLayoutManager(linearLayoutManager);
        conversationContainer.setAdapter(adapter);

        adapter.setListener(new ConversationAdapter.Listener() {
            public void onClick(int position) {
                Intent intent = new Intent(getActivity(), ChatWindowActivity.class);
                intent.putExtra(ChatWindowActivity.EXTRA_COVERSATION_ID, conversationItems.get(position).getChannelId());
                intent.putExtra(ChatWindowActivity.EXTRA_COVERSATION_NAME, conversationItems.get(position).getName());
                intent.putExtra(ChatWindowActivity.EXTRA_COVERSATION_AVA, conversationItems.get(position).getAvatar());
                getActivity().startActivity(intent);
            }
        });

        conversationItems.add(new ConversationItem("Phuc", "", "20-11", "Hello"));
        conversationItems.add(new ConversationItem("fsadfsdfsd", "", "20-11", "Hello"));
        conversationItems.add(new ConversationItem("fsadfsdfsdfsdfsd", "", "20-11", "Hello"));
        conversationItems.add(new ConversationItem("fsadfsdfsdfs", "", "20-11", "Hello"));

        adapter.notifyDataSetChanged();

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        retrieveConversations();
    }

    private void retrieveConversations() {
    }
}
