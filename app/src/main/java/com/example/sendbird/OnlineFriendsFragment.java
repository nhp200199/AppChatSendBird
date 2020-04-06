package com.example.sendbird;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;



/**
 * A simple {@link Fragment} subclass.
 */
public class OnlineFriendsFragment extends Fragment {

    private ListView friendsContainer;

    private ContactItemAdapter adapter;
    private List<ContactItem> items;

    private String currentID;

    public OnlineFriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_online_friends, container, false);

        friendsContainer = v.findViewById(R.id.friends_container);
        items = new ArrayList<ContactItem>();
        adapter = new ContactItemAdapter(getActivity(), 1, items);

        friendsContainer.setAdapter(adapter);
        /*
        friendsContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ChatWindowActivity.class);
                intent.putExtra(ChatWindowActivity.EXTRA_COVERSATION_ID, items.get(position).getUid());
                intent.putExtra(ChatWindowActivity.EXTRA_COVERSATION_NAME, items.get(position).getName());
                intent.putExtra(ChatWindowActivity.EXTRA_COVERSATION_AVA, items.get(position).getAvatar());

                startActivity(intent);
            }
        });

         */

        Log.d("TAG", "AllFriendsFragment OnCreateView");

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }
}
