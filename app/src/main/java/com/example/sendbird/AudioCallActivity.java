package com.example.sendbird;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;

import de.hdodenhof.circleimageview.CircleImageView;

public class AudioCallActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener, SubscriberKit.SubscriberListener {
    private static final String API_KEY = "46466472";
    private static final String SESSION_ID = "2_MX40NjQ2NjQ3Mn5-MTU5MDAzMjcyMjI4NX50cG56SVRqeEI0blVTVTNmQzFvT0I4ZHF-fg";
    private static final String TOKEN = "T1==cGFydG5lcl9pZD00NjQ2NjQ3MiZzaWc9ZDcwNTNiMTVmYjlhNGI1YTM5MGUyOWU5NzMxNmRiNzE3ZDBkMzNlNjpzZXNzaW9uX2lkPTJfTVg0ME5qUTJOalEzTW41LU1UVTVNREF6TWpjeU1qSTROWDUwY0c1NlNWUnFlRUkwYmxWVFZUTm1RekZ2VDBJNFpIRi1mZyZjcmVhdGVfdGltZT0xNTkwMDM1MzY1Jm5vbmNlPTAuMTA3NzQ1Nzk2Njc5Mjg1ODUmcm9sZT1wdWJsaXNoZXImZXhwaXJlX3RpbWU9MTU5MDAzODk2MyZpbml0aWFsX2xheW91dF9jbGFzc19saXN0PQ==";
    private static final String LOG_TAG = AudioCallActivity.class.getSimpleName();


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_call);

        getIntentExtra();
        connectViews();
        tv_userName.setText(userName);
        Glide.with(this)
                .load(avatar)
                .into(circleImageView_ava);

        // initialize and connect to the session
        mSession = new Session.Builder(this, API_KEY, SESSION_ID).build();
        mSession.setSessionListener(this);
        mSession.connect(TOKEN);
    }

    private void connectViews() {
        tv_userName = (TextView) findViewById(R.id.userName);
        circleImageView_ava = (CircleImageView) findViewById(R.id.avatar);
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
        finish();
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
            finish();
        }
        else{
            finish();
        }
    }
}
