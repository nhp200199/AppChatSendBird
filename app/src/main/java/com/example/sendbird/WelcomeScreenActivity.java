package com.example.sendbird;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class WelcomeScreenActivity extends AppCompatActivity implements View.OnClickListener{

    private ViewPager viewPager;
    private TextView tv_title;
    private TextView tv_description;
    private TextView tv_NEXT;
    private ImageView img_intro;
    private TabLayout tabIndicator;
    private LinearLayout linearLayout_btns;
    private RelativeLayout relativeLayout;
    private Button btn_register;
    private Button btn_login;


    private WelcomeScreenPagerAdapter adapter;
    private List<ScreenItem> screenItems;
    private int postition = 0;
    private Animation btn_anim;
    private SharedPreferences sharedPreferences;
    private boolean isFirstTime = true;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        directToLoginScreen();

        setContentView(R.layout.activity_welcome_screen);

        connectViews();

        screenItems = new ArrayList<ScreenItem>();
        screenItems.add(new ScreenItem("Chào mừng đến với PACPAC !", getString(R.string.welcome_page_1_text), R.drawable.introo1));
        screenItems.add(new ScreenItem("Chúng tôi mong muốn gửi đến bạn một nơi hoàn hảo nhất để nhắn tin với bạn bè.", getString(R.string.welcome_page_2_text), R.drawable.introo2));
        screenItems.add(new ScreenItem("Nào hãy đăng ký ngay một tài khoản.", getString(R.string.welcome_page_3_text), R.drawable.pacpac));

        adapter = new WelcomeScreenPagerAdapter(this, screenItems);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == screenItems.size() - 1) {
                    loadLastScreen();
                } else {
                    // clear buttons
                    linearLayout_btns.setVisibility(View.GONE);

                    relativeLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabIndicator.setupWithViewPager(viewPager);
        tabIndicator.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition() == screenItems.size()-1){
                    loadLastScreen();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        tv_NEXT.setOnClickListener(this);
        btn_register.setOnClickListener(this);
        btn_login.setOnClickListener(this);

        sharedPreferences = getSharedPreferences("checkFirstTime", MODE_PRIVATE);

    }

    private void directToLoginScreen() {
        if(!isFirstTime()){
            Intent intent = new Intent(this, LoginScreenActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void loadLastScreen() {
        relativeLayout.setVisibility(View.GONE);

        linearLayout_btns.setVisibility(View.VISIBLE);
        linearLayout_btns.setAnimation(btn_anim);

    }

    private void connectViews() {
        viewPager  = (ViewPager) findViewById(R.id.intro_pager);
        tv_description = (TextView) findViewById(R.id.tv_intro_description);
        tv_title = (TextView) findViewById(R.id.tv_intro_title);
        tv_NEXT = (TextView) findViewById(R.id.tv_Next);
        img_intro = (ImageView) findViewById(R.id.img_intro);
        tabIndicator = (TabLayout) findViewById(R.id.tl_swipe);
        linearLayout_btns = findViewById(R.id.layout_btns);
        relativeLayout = findViewById(R.id.rlt_layout);
        btn_login = findViewById(R.id.btn_login);
        btn_register = findViewById(R.id.btn_register);

        btn_anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.btn_anim);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_Next:

                postition = viewPager.getCurrentItem();
                if(postition < screenItems.size()){
                    postition++;
                    viewPager.setCurrentItem(postition);
                }

                if(postition == screenItems.size()-1){
                    loadLastScreen();
                }
                break;

            case R.id.btn_login:
                isFirstTime =false;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isFirstTime", isFirstTime);
                editor.apply();

                Intent intent = new Intent(this, LoginScreenActivity.class);
                startActivity(intent);
                finish();
                break;

            case R.id.btn_register:
                isFirstTime =false;
                SharedPreferences.Editor editor2 = sharedPreferences.edit();
                editor2.putBoolean("isFirstTime", isFirstTime);
                editor2.apply();

                Intent intent1 = new Intent(this, RegisterActivity.class);
                startActivity(intent1);
                finish();
                break;
        }
    }
    private boolean isFirstTime() {
        SharedPreferences sharedPreferences = getSharedPreferences("checkFirstTime", MODE_PRIVATE);
        boolean isFirstTime = sharedPreferences.getBoolean("isFirstTime", true);
        return isFirstTime;

    }

}
