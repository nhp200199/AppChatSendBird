package com.example.sendbird;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class FullViewActivity extends AppCompatActivity {
    public static final String EXTRA_IMAGE_URL= "image_url";

    private ImageView img_full;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_view);

        connectViews();

        if(getIntent().hasExtra(EXTRA_IMAGE_URL)){
            Glide.with(this)
                    .load(getIntent().getStringExtra(EXTRA_IMAGE_URL))
                    .into(img_full);
        }
    }

    private void connectViews() {
        img_full= findViewById(R.id.img_full);
    }
}
