package com.example.sendbird;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AudioCallActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener, SubscriberKit.SubscriberListener {
    public static final String URL_GET_AUTHEN = "https://pacpac-chat.000webhostapp.com/src/MediaChat.php";
    private static final String API_KEY = "46466472";
    private static  String SESSION_ID = "";
    private static  String TOKEN = "";
    private static final String LOG_TAG = AudioCallActivity.class.getSimpleName();

    private CircleImageView btn_hangup;
    private Button btn_mute;
    private ImageView img_background;

    private String senderID="";
    private String receiveID="";
    private String userName;
    private String avatar;

    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;

    private TextView tv_userName;
    private CircleImageView circleImageView_ava;

    public boolean mConnected;
    public String channelId;
    private boolean isMuted;
    private int seconds;
    private boolean running;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_call);

        getIntentExtra();
        connectViews();
        tv_userName.setText(userName);
        Glide.with(this)
                .load(avatar)
                .placeholder(R.drawable.couple)
                .into(circleImageView_ava);

        img_background.setImageAlpha(150);
        Glide.with(this)
                .load(avatar)
                .placeholder(R.drawable.couple)
                .into(img_background);

        setAuthen();
    }

    private void connectViews() {
        tv_userName = (TextView) findViewById(R.id.userName);
        circleImageView_ava = (CircleImageView) findViewById(R.id.avatar);
        btn_hangup = (CircleImageView)findViewById(R.id.civ_end_call);
        img_background = (ImageView)findViewById(R.id.img_background);
        btn_hangup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btn_mute = (Button)findViewById(R.id.btn_mute_voice );
        btn_mute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPublisher != null)
                    if(!isMuted){
                        isMuted = true;
                        mPublisher.setPublishAudio(false);
                        Toast.makeText(AudioCallActivity.this, "Loa thoại đã được tắt", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        isMuted = false;
                        mPublisher.setPublishAudio(true);
                        Toast.makeText(AudioCallActivity.this, "Loa thoại đã được bật", Toast.LENGTH_SHORT).show();
                    }
            }
        });
    }

    private void getIntentExtra(){
        if(getIntent().hasExtra("senderID"))
        {
            senderID = getIntent().getStringExtra("senderID");
        }
        if(getIntent().hasExtra("receiverID"))
        {
            receiveID = getIntent().getStringExtra("receiverID");
        }
        if(getIntent().hasExtra("receiverName"))
        {
            userName = getIntent().getStringExtra("receiverName");
        }
        if(getIntent().hasExtra("receiveImg"))
        {
            avatar = getIntent().getStringExtra("receiveImg");
        }
        if(getIntent().hasExtra("channelId"))
        {
            channelId = getIntent().getStringExtra("channelId");
        }
        Log.d("test", senderID + " " + receiveID);

    }

    @Override
    public void onConnected(Session session) {
        mConnected = true;
        Log.d(LOG_TAG, "onConnected: Connected to session: "+session.getSessionId());

        // initialize Publisher and set this object to listen to Publisher events
        mPublisher = new Publisher.Builder(this).videoTrack(false).build();
        mPublisher.setPublisherListener(this);

        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        mConnected = false;
        Log.i(LOG_TAG, "Session Disconnected");
        mSession = null;
        running = false;
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.d(LOG_TAG, "onStreamReceived: New Stream Received "+stream.getStreamId() + " in session: "+session.getSessionId());

        if (mSubscriber == null) {
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSubscriber.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
            mSubscriber.setSubscriberListener(this);
            mSession.subscribe(mSubscriber);
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Dropped");
        if (mSubscriber != null) {
            mSubscriber = null;
            mSession.disconnect();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.e(LOG_TAG, "Session error: " + opentokError.getMessage());
    }
    /*end sessions listener*/


    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.i(LOG_TAG, "Stream Created");
        Log.i(LOG_TAG, "Stream ID: " + stream.getStreamId());
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.i(LOG_TAG, "Stream Destroyed");
        if (mPublisher != null) {
            mPublisher = null;
        }
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Log.e(LOG_TAG, "onError: "+opentokError.getErrorDomain() + " : " +
                opentokError.getErrorCode() +  " - "+opentokError.getMessage());

        showOpenTokError(opentokError);
    }
    /*end publisher listener*/

    private void showOpenTokError(OpentokError opentokError) {

        Toast.makeText(this, opentokError.getErrorDomain().name() +": " +opentokError.getMessage() + " Please, see the logcat.", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onConnected(SubscriberKit subscriberKit) {
        Toast.makeText(this, userName + " đã tiếp nhận cuộc gọi", Toast.LENGTH_SHORT).show();
        running = true;
        startCoutTimer();
    }
    private void startCoutTimer() {
        final TextView timeView = (TextView) findViewById(R.id.tv_conversation_time);
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;
                String time = String.format(Locale.getDefault(),
                        "%d:%02d:%02d", hours, minutes, secs);
                timeView.setText(time);
                if (running) {
                    seconds++;
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    @Override
    public void onDisconnected(SubscriberKit subscriberKit) {
        finish();
    }

    @Override
    public void onError(SubscriberKit subscriberKit, OpentokError opentokError) {
        Toast.makeText(this, userName + " đã xảy ra lỗi ở phía người nhận", Toast.LENGTH_SHORT).show();
        Log.e(LOG_TAG, opentokError.getMessage());
    }
    /*end subcriber listener*/

    @Override
    public void onBackPressed() {
        if(mConnected){
            mSession.disconnect();
        }
        else{
            finish();
        }
    }

    private void setAuthen(){

        final RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest jsonArrayRequest = new StringRequest(Request.Method.POST, URL_GET_AUTHEN
                , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject= new JSONObject(response);
                    SESSION_ID = jsonObject.getString("mySession");
                    TOKEN = jsonObject.getString("myToken");

                    // initialize and connect to the session
                    mSession = new Session.Builder(AudioCallActivity.this, API_KEY, SESSION_ID).build();
                    mSession.setSessionListener(AudioCallActivity.this);
                    mSession.connect(TOKEN);
                    Log.d("authen", SESSION_ID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("fail", error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params =new HashMap<>();
                params.put("channelId",channelId);
                return params;
            }
        };
        requestQueue.add(jsonArrayRequest);
    }
}
