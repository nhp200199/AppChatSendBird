package com.example.sendbird;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CreateGroupActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText edt_group_name;
    private Button btn_create;
    private ListView lv_friends;
    private TextView tv_members;
    private ImageView img_back;

    private ContactItemAdapter adapter;
    private ArrayList<ContactItem> friendList;

    private FriendAdapter friendAdapter;
    private ArrayList<FriendItem> itemFriendsSelected;
    private RecyclerView friendsSelectedContainer;

    private List<Integer> integerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        connectViews();

        lv_friends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int i = member_exists(position, integerList);
                if(member_exists(position, integerList) > -1){
                    itemFriendsSelected.remove(member_exists(position, integerList));
                    integerList.remove(integerList.indexOf(position));
                    //Log.d("Find", String.valueOf(i));
                    //Toast.makeText(CreateGroupActivity.this, "Find: " + i, Toast.LENGTH_SHORT).show();
                }
                else {
                    itemFriendsSelected.add(new FriendItem(friendList.get(position).getUid(), friendList.get(position).getName(), friendList.get(position).getAvatar()));

                    friendsSelectedContainer.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            friendsSelectedContainer.smoothScrollToPosition(friendsSelectedContainer.getAdapter().getItemCount() - 1);
                        }
                    }, 100);

                    integerList.add(position);
                }
                friendAdapter.notifyDataSetChanged();
            }
        });

        btn_create.setOnClickListener(this);
        edt_group_name.addTextChangedListener(textWatcher);
        img_back.setOnClickListener(this);
        
    }
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(s.toString().trim().isEmpty())
                btn_create.setEnabled(false);
            else btn_create.setEnabled(true);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void connectViews() {
        edt_group_name = findViewById(R.id.edt_group_name);
        btn_create = findViewById(R.id.btn_create_group);
        lv_friends = findViewById(R.id.friends_container);
        tv_members = findViewById(R.id.tv_members);
        friendsSelectedContainer = findViewById(R.id.friends_selected_container);
        img_back = findViewById(R.id.img_back);

        friendList = new ArrayList<ContactItem>();
        friendList.add(new ContactItem("1", "Phuc", ""));
        friendList.add(new ContactItem("2", "Cuongguknkj", ""));
        friendList.add(new ContactItem("2", "Cuongguknkj", ""));

        adapter = new ContactItemAdapter(this, 1, friendList);
        lv_friends.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        itemFriendsSelected = new ArrayList<FriendItem>();

        friendAdapter = new FriendAdapter(this, itemFriendsSelected);
        friendAdapter.setListener(new ConversationAdapter.Listener() {
            @Override
            public void onClick(int position) {
                itemFriendsSelected.remove(position);
                friendAdapter.notifyDataSetChanged();
                integerList.remove(position);
            }
        });

        friendsSelectedContainer.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        friendsSelectedContainer.setAdapter(friendAdapter);

        integerList = new ArrayList<Integer>();
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_create_group:
                if(itemFriendsSelected.size() == 0)
                    Toast.makeText(CreateGroupActivity.this, "Chưa chọn thành viên", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(CreateGroupActivity.this, "successful", Toast.LENGTH_SHORT).show();
                break;

            case R.id.img_back:
                onBackPressed();
        }

    }
    private int member_exists(int postitionSelected, List<Integer> positionArray) {
        if(positionArray == null)
            return -1;
        for (int i = 0; i < positionArray.size(); i++) {
            if(postitionSelected == positionArray.get(i))
            {
                return i;
            }
        }
        return -1;
    }
}
