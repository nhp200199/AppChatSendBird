package com.example.sendbird;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

public class WelcomeScreenPagerAdapter extends PagerAdapter {
    Context mContext;
    List<ScreenItem> screenItems;

    public WelcomeScreenPagerAdapter(Context mContext, List<ScreenItem> screenItems) {
        this.mContext = mContext;
        this.screenItems = screenItems;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService((Context.LAYOUT_INFLATER_SERVICE));
        View v = inflater.inflate(R.layout.layout_screen, null);

        ImageView imageView = v.findViewById(R.id.img_intro);
        TextView tv_tille = v.findViewById(R.id.tv_intro_title);
        TextView tv_des = v.findViewById(R.id.tv_intro_description);

        tv_des.setText(screenItems.get(position).getDescription());
        tv_tille.setText(screenItems.get(position).getTitle());
        imageView.setImageResource(screenItems.get(position).getImageRsc());

        container.addView(v);

        return v;
    }

    @Override
    public int getCount() {
        return screenItems.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object );
    }
}
