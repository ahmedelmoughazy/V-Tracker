package com.project.ahmed.v_tracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class LaunchScreenActivity extends AppCompatActivity {

    ImageView mImageView;
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_screen);

        mImageView = (ImageView)findViewById(R.id.logo);
        //for scaling the logo
        final Animation scaleUp   = AnimationUtils.loadAnimation(getBaseContext(),R.anim.scale_up);
        final Animation scaleDown = AnimationUtils.loadAnimation(getBaseContext(),R.anim.scale_down);
        mImageView.startAnimation(scaleDown);

        scaleDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                mImageView.startAnimation(scaleUp);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        scaleUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                intent = new Intent(LaunchScreenActivity.this,HomeActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

}
