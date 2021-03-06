package com.example.sendbird;


import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FriendListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DirectoryFragment extends Fragment {

    private TabLayout tabs;
    private ViewPager viewPager;

    private SectionsPagerAdapter adapter;


    public DirectoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.fragment_friends, container, false);


        tabs  = v.findViewById(R.id.tabs);
        viewPager = v.findViewById(R.id.pager);
        adapter = new SectionsPagerAdapter((getChildFragmentManager()));

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);

        tabs.setupWithViewPager(viewPager);
        return v;
    }


    private class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public int getCount() {
            return 2;
        }
        @Override
        public Fragment getItem(int position) {
            Bundle bundle = getArguments();
            Fragment fragment;
            switch (position) {
                case 0:
                    fragment = new OnlineFriendsFragment();
                    bundle.putParcelableArrayList("onl friends", bundle.getParcelableArrayList("onl friends"));
                    fragment.setArguments(bundle);
                    return fragment;
                case 1:
                    bundle.putParcelableArrayList("all friends", bundle.getParcelableArrayList("all friends"));
                    fragment = new AllFriendsFragment();
                    fragment.setArguments(bundle);
                    return fragment;

            }
            return null;
        }
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Đang hoạt động";
                case 1:
                    return "Tất cả";

            }
            return null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
