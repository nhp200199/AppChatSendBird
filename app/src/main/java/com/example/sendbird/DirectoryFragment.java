package com.example.sendbird;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

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
            switch (position) {
                case 0:
                    return new OnlineFriendsFragment();
                case 1:
                    return new AllFriendsFragment();

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

}
