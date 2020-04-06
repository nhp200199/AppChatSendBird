package com.example.sendbird;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {
    public static final String EXTRA_ID = "FriendId";

    boolean flag = false;

    private String receiverID, senderID, currentState;

    private ImageView img_cover;
    private ImageView img_back;
    private ImageView img_gender;
    private CircleImageView civ_avatar;
    private TextView tv_userName;
    private TextView tv_birthday;
    private TextView tv_gender;
    private TextView tv_email;
    private Button btn_add_friend, btn_cancel_request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);
        connectViews();

        if(getIntent().hasExtra(EXTRA_ID)){
            retrieveUserInfo();
        }

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        btn_add_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_add_friend.setEnabled(false);

                if(currentState.equals("new"))
                {
                    sendRequest();
                }
                else if(currentState.equals("request sent")){
                    cancelRequest();

                }
                else if(currentState.equals("request received")){
                    acceptRequest();
                }

                else if(currentState.equals("friends")){
                    displayRemoveFriendConfirmDialog();
                    btn_add_friend.setEnabled(true);
                }
            }
        });

    }


    private void connectViews() {

        img_cover = findViewById(R.id.img_cover);
        img_back = findViewById(R.id.back);
        civ_avatar = findViewById(R.id.civ_avatar);
        tv_birthday= findViewById(R.id.tv_birthday);
        tv_gender = findViewById(R.id.tv_gender);
        tv_email = findViewById(R.id.tv_email);
        img_gender = findViewById(R.id.img_gender);
        tv_userName = findViewById(R.id.tv_username);
        btn_add_friend = findViewById(R.id.btn_request_add_friend);
        btn_cancel_request = findViewById(R.id.btn_decline_request);

        currentState = "new";
    }

    private void retrieveUserInfo() {
        Toast.makeText(PersonProfileActivity.this, getIntent().getStringExtra(EXTRA_ID), Toast.LENGTH_SHORT).show();
    }

    private void sendRequest() {

    }

    private void cancelRequest() {

    }

    private void acceptRequest() {

    }

    private void displayRemoveFriendConfirmDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(PersonProfileActivity.this)
                .setMessage("Bạn có muốn hủy kết bạn với " + tv_userName.getText().toString() + "?")
                .setTitle("Thông báo")
                .setPositiveButton("Không", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();

                    }
                })
                .setNegativeButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        removeContact();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void removeContact(){

    }


}
