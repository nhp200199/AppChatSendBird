package com.example.sendbird;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.VoiceInteractor;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener, PopupDialog.DialogListener{
    public static final String USER_PROFILE_URL = "https://pacpac-chat.000webhostapp.com/UserProfile.php";
    public static final String EDIT_USER_PROFILE_URL = "https://pacpac-chat.000webhostapp.com/EditProfile.php";
    private String userID = "";
    private SharedPreferences sharedPreferences;

    private ImageView img_cover;
    private ImageView img_back;
    private ImageView img_gender;
    private CircleImageView civ_avatar;
    private ListView option_container;
    private TextView tv_userName;
    private TextView tv_birthday;
    private TextView tv_gender;
    private TextView tv_email;

    private List<OptionItem> optionItems;
    private OptionAdapter adapter;
    public String mAvatar;

    public static int convertDpToPixel(float dp, Context context){
        return  Math.round(dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT)) ;
    }



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        connectView();
        sharedPreferences = getSharedPreferences("user infor", MODE_PRIVATE);
        userID = sharedPreferences.getString("id", "");
        SendBird.init(RegisterActivity.appID, this);
        SendBird.connect(userID, new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                if(e != null)
                    Toast.makeText(UserProfileActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        optionItems = new ArrayList<OptionItem>();
        optionItems.add(new OptionItem(R.drawable.ic_user, "Đổi tên tài khoản"));
        optionItems.add(new OptionItem(R.drawable.ic_lock, "Đổi mật khẩu"));
        optionItems.add(new OptionItem(R.drawable.ic_logout, "Đăng xuất"));

        adapter = new OptionAdapter(this, 1, optionItems);
        option_container.setAdapter(adapter);

        img_cover.setOnClickListener(this);
        img_back.setOnClickListener(this);
        civ_avatar.setOnClickListener(this);
        option_container.setOnItemClickListener(listener);

        Log.d("TAG", "Main onCreated");

        loadUserInfo();

    }

    private void loadUserInfo() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest request =new StringRequest(Request.Method.POST,
                USER_PROFILE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject object = new JSONObject(response);
                            Log.d("TAG", object.toString());
                            if(object.getString("gender").equals("Nam")){
                                img_gender.setImageResource(R.drawable.ic_male);
                            }
                            else img_gender.setImageResource(R.drawable.ic_female);

                            tv_email.setText(object.getString("email"));
                            tv_birthday.setText(object.getString("DoB"));
                            tv_userName.setText(object.getString("nickname"));
                            tv_gender.setText(object.getString("gender"));

                            mAvatar = object.getString("avatar");

                            Glide.with(UserProfileActivity.this)
                                    .load(object.getString("avatar"))
                                    .placeholder(R.drawable.couple)
                                    .thumbnail(0.5f)
                                    .apply(RequestOptions.skipMemoryCacheOf(true))
                                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                                    .into(civ_avatar);
                            Glide.with(UserProfileActivity.this)
                                    .load(object.getString("cover"))
                                    .placeholder(R.drawable.arsenal)
                                    .thumbnail(0.5f)
                                    .apply(RequestOptions.skipMemoryCacheOf(true))
                                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                                    .into(img_cover);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(UserProfileActivity.this, response, Toast.LENGTH_LONG).show();
                        }
                    }
                }
                ,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(UserProfileActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params =new HashMap<>();
                params.put("id", userID);
                return params;
            }
        };
        int socketTimeout = 20000;//20s timeout
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        requestQueue.add(request);

    }

    private AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if (position == 0) {
                showDialogChangeName();

            } else if (position == 1) {
                showDialogChangePassword();
            }else if(position ==2){
                LogOut();
            }
        }
    };

    private void showDialogChangePassword() {
        Bundle bundle = new Bundle();
        bundle.putString("id", userID);

        PopupDialog dialog = new PopupDialog("Thay đổi mật khẩu", R.layout.change_password_popup);
        dialog.setCancelable(true);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "popup");
    }

    private void LogOut() {
        SendBird.disconnect(new SendBird.DisconnectHandler() {
            @Override
            public void onDisconnected() {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("id");
                editor.commit();
                Intent intent = new Intent(UserProfileActivity.this, LoginScreenActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }


    private void connectView() {

        img_cover = findViewById(R.id.img_cover);
        img_back = findViewById(R.id.back);
        civ_avatar = findViewById(R.id.civ_avatar);
        option_container = findViewById(R.id.option_container);
        tv_birthday= findViewById(R.id.tv_birthday);
        tv_gender = findViewById(R.id.tv_gender);
        tv_email = findViewById(R.id.tv_email);
        img_gender = findViewById(R.id.img_gender);
        tv_userName = findViewById(R.id.tv_username);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.back:
                onBackPressed();
                break;
            case R.id.img_cover:
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int height = displayMetrics.heightPixels;
                int width = displayMetrics.widthPixels;
                int width_px = convertDpToPixel(width, UserProfileActivity.this);
                int height_px = convertDpToPixel(250, UserProfileActivity.this);


                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setActivityTitle("My Crop")
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .setCropMenuCropButtonTitle("Done")
                        .setMinCropWindowSize(width, 250)
                        .setAspectRatio(5, 3)
                        //.setFixAspectRatio(true)
                        .start(UserProfileActivity.this);
                break;

            case R.id.civ_avatar:

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Chọn Ảnh"), 443);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of CropImageActivity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                updateCoverImage(result.getUri());
            }
        }
        else if(requestCode == 443 && resultCode == RESULT_OK && data.getData() != null){
            Uri uri = data.getData();
            updateAvatarImage(uri);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
        startActivity(intent);
        finish();

    }
    private void showDialogChangeName() {
        Bundle bundle = new Bundle();
        bundle.putString("id", userID);
        bundle.putString("username", tv_userName.getText().toString());
        bundle.putString("avatar", mAvatar);

        PopupDialog dialog = new PopupDialog("Thay đổi tên tài khoản", R.layout.chang_username_popup);
        dialog.setCancelable(true);
        dialog.setArguments(bundle);
        dialog.show(getSupportFragmentManager(), "popup");
    }
    @Override
    protected  void onResume() {
        SendBird.addChannelHandler(PersonProfileActivity.CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                String senderId = baseMessage.getSender().getUserId();
                String type = baseMessage.getCustomType();
                String message = baseMessage.getMessage();
                if (type.equals("notify")) {
                    if (message.equals("add")) {
                        List<String> friend = new ArrayList<>();
                        friend.add(baseMessage.getSender().getUserId());

                        SendBird.addFriends(friend, new SendBird.AddFriendsHandler() {
                            @Override
                            public void onResult(List<User> list, SendBirdException e) {
                                if (e != null)
                                    Toast.makeText(UserProfileActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    if (message.equals("delete")) {
                        SendBird.deleteFriend(senderId, new SendBird.DeleteFriendHandler() {
                            @Override
                            public void onResult(SendBirdException e) {
                                if (e != null)
                                    Toast.makeText(UserProfileActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

            }
        });
        super.onResume();

    }

    @Override
    protected void onPause() {
        SendBird.removeChannelHandler(PersonProfileActivity.CHANNEL_HANDLER_ID);
        super.onPause();
    }
    /*
    private class CustomDialog extends AppCompatDialogFragment{
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);


        }
    }*/

    private void updateCoverImage(Uri uri){
        Bitmap bitmap;
        final String coverImageMap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            coverImageMap = imagetoString(bitmap);

            String progressMessage = "Updating Cover Image ...";
            ProgressDialog.startProgressDialog(UserProfileActivity.this, progressMessage);

            RequestQueue requestQueue = Volley.newRequestQueue(this);
            StringRequest request =new StringRequest(Request.Method.POST,
                    EDIT_USER_PROFILE_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(final String response) {
                            if(!response.equals("There is something happens, please try again!")){
                                Glide.with(UserProfileActivity.this)
                                        .load(response)
                                        .placeholder(R.drawable.arsenal)
                                        .apply(RequestOptions.skipMemoryCacheOf(true))
                                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                                        .into(img_cover);

                                ProgressDialog.dismissProgressDialog();
                            }
                            else{
                                Toast.makeText(UserProfileActivity.this, response, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    ,
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            ProgressDialog.dismissProgressDialog();
                            Toast.makeText(UserProfileActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params =new HashMap<>();
                    params.put("image",coverImageMap);
                    params.put("id", userID);
                    params.put("type", "cover");
                    return params;
                }
            };
            int socketTimeout = 20000;//20s timeout
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            requestQueue.add(request);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void updateAvatarImage(Uri uri){
        Bitmap bitmap;
        final String avatarImageMap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            avatarImageMap = imagetoString(bitmap);

            String progressMessage = "Updating Cover Image ...";
            ProgressDialog.startProgressDialog(UserProfileActivity.this, progressMessage);

            RequestQueue requestQueue = Volley.newRequestQueue(this);
            StringRequest request =new StringRequest(Request.Method.POST,
                    EDIT_USER_PROFILE_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(final String response) {
                            if(!response.equals("There is something happens, please try again!")){
                                Log.d("Tag", response);
                                SendBird.updateCurrentUserInfo(tv_userName.getText().toString(), response, new SendBird.UserInfoUpdateHandler() {
                                    @Override
                                    public void onUpdated(SendBirdException e) {
                                        if(e !=null)
                                        {
                                            ProgressDialog.dismissProgressDialog();
                                            Toast.makeText(UserProfileActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                            ProgressDialog.dismissProgressDialog();
                                            mAvatar = response;
                                            Glide.with(UserProfileActivity.this)
                                                    .load(SendBird.getCurrentUser().getProfileUrl())
                                                    .placeholder(R.drawable.couple)
                                                    .thumbnail(0.5f)
                                                    .apply(RequestOptions.skipMemoryCacheOf(true))
                                                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                                                    .into(civ_avatar);
                                            Toast.makeText(UserProfileActivity.this,"Successfully Updated", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                            else{
                                Toast.makeText(UserProfileActivity.this, response, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    ,
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            ProgressDialog.dismissProgressDialog();
                            Toast.makeText(UserProfileActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params =new HashMap<>();
                    params.put("image",avatarImageMap);
                    params.put("id", userID);
                    params.put("type", "avatar");
                    return params;
                }
            };
            int socketTimeout = 20000;//20s timeout
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            requestQueue.add(request);

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    private String imagetoString(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte[] imgbyte= byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgbyte,Base64.DEFAULT);
    }

    @Override
    public void onNameChanged(String newName) {
        tv_userName.setText(newName);
    }
}