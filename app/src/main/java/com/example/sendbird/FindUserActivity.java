package com.example.sendbird;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sendbird.android.ApplicationUserListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserListQuery;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindUserActivity extends AppCompatActivity implements View.OnClickListener{
    private String userID;

    private RecyclerView friend_container;
    private EditText edt_find_friend;
    private Button btn;
    private LinearLayout results_container;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_user);
        connectViews();
        SendBird.init(RegisterActivity.appID, this);
        sharedPreferences = getSharedPreferences("user infor", MODE_PRIVATE);
        userID = sharedPreferences.getString("id", null);
        if(userID != null){
            SendBird.connect(userID, new SendBird.ConnectHandler() {
                @Override
                public void onConnected(User user, SendBirdException e) {
                    if(e != null){
                        Toast.makeText(FindUserActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                    else{
                        ApplicationUserListQuery applicationUserListQuery = SendBird.createApplicationUserListQuery();
                        applicationUserListQuery.next(new UserListQuery.UserListQueryResultHandler() {
                            @Override
                            public void onResult(List<User> list, SendBirdException e) {
                                Toast.makeText(FindUserActivity.this, String.valueOf(list.size()), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }

        btn.setOnClickListener(this);
    }

    private void connectViews() {
        friend_container = findViewById(R.id.friends_container);
        edt_find_friend = findViewById(R.id.edt_find_user);
        btn = findViewById(R.id.btn_sthing);
        results_container = findViewById(R.id.search_results_container);

        friend_container.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.edt_find_user:

            case R.id.btn_sthing:

                String username =  edt_find_friend.getText().toString().trim();
                break;
        }
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout linearLayout;

        ViewHolder(LinearLayout linearLayout) {
            super(linearLayout);

            this.linearLayout = linearLayout;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}
