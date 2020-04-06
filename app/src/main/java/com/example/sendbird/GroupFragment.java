package com.example.sendbird;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupFragment extends Fragment{

    private RecyclerView groupContainer;
    private TextView tv_none;
    private ArrayList<ConversationItem> conversationItems;
    private ConversationAdapter adapter;

    public GroupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_group, container, false);

        tv_none = v.findViewById(R.id.tv_no_group);
        groupContainer = v.findViewById(R.id.groups_container);

        conversationItems = new ArrayList<ConversationItem>();
        conversationItems.add(new ConversationItem("1", "hello", "", "", "chao cau"));
        conversationItems.add(new ConversationItem("1", "Dung", "", "", "chao cau"));
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
                intent.putExtra(ChatWindowActivity.EXTRA_COVERSATION_ID, conversationItems.get(position).getChannelId());
                intent.putExtra(ChatWindowActivity.EXTRA_COVERSATION_AVA, conversationItems.get(position).getAvatar());
                intent.putExtra(ChatWindowActivity.EXTRA_COVERSATION_NAME, conversationItems.get(position).getName());
                getActivity().startActivity(intent);
            }
        });

        //check if there is a group
        if (conversationItems.size() == 0) {
            tv_none.setVisibility(View.VISIBLE);
            groupContainer.setVisibility(View.INVISIBLE);
        } else {
            tv_none.setVisibility(View.INVISIBLE);
            groupContainer.setVisibility(View.VISIBLE);
        }

        return v;
    }


}
