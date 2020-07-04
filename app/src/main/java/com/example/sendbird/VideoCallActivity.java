package com.example.sendbird;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class VideoCallActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener, SubscriberKit.SubscriberListener {
    public static final String URL_GET_AUTHEN = "https://pacpac-chat.000webhostapp.com/src/MediaChat.php";
    private static final String API_KEY = "46466472";
    private static  String SESSION_ID = "";
    private static  String TOKEN = "";
    private static final String LOG_TAG = VideoCallActivity.class.getSimpleName();


    private String senderID="";
    private String receiveID="";
    private String userName;
    private String avatar;

    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;

    private FrameLayout mPublisherViewContainer;
    private FrameLayout mSubscriberViewContainer;
    public boolean mConnected;
    public String channelId;

    private CircleImageView btn_hangup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        getIntentExtra();
        connectViews();

        setAuthen();
    }

    private void connectViews() {
        mPublisherViewContainer = (FrameLayout)findViewById(R.id.publisher_container);
        mSubscriberViewContainer = (FrameLayout)findViewById(R.id.subscriber_container);
        btn_hangup = (CircleImageView)findViewById(R.id.civ_end_call);
        btn_hangup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
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
        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(this);

        // set publisher video style to fill view
        mPublisher.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                BaseVideoRenderer.STYLE_VIDEO_FILL);
        mPublisherViewContainer.addView(mPublisher.getView());
        if (mPublisher.getView() instanceof GLSurfaceView) {
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }

        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        mConnected = false;
        Log.i(LOG_TAG, "Session Disconnected");
        mSession = null;
        showPopUp();
    }

    private void showPopUp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Cuộc gọi đã kết thúc");
        builder.setIcon(R.drawable.arsenal);
        builder.setCancelable(false);
        builder.setPositiveButton("Đóng", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.create();
        builder.show();
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.d(LOG_TAG, "onStreamReceived: New Stream Received "+stream.getStreamId() + " in session: "+session.getSessionId());

        if (mSubscriber == null) {
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSubscriber.getRenderer().setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
            mSubscriber.setSubscriberListener(this);
            mSession.subscribe(mSubscriber);
            mSubscriberViewContainer.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Dropped");
        if (mSubscriber != null) {
            mSubscriber = null;
            mSubscriberViewContainer.removeAllViews();
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
            mPublisherViewContainer.removeAllViews();
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
    }

    @Override
    public void onDisconnected(SubscriberKit subscriberKit) {
        //finish();
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
            //finish();
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
                    mSession = new Session.Builder(VideoCallActivity.this, API_KEY, SESSION_ID).build();
                    mSession.setSessionListener(VideoCallActivity.this);
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
