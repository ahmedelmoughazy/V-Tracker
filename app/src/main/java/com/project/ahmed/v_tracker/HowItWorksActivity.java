package com.project.ahmed.v_tracker;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;


/**
 * Created by Ahmed on 3/16/2018.
 */

public class HowItWorksActivity extends AppCompatActivity {

    ImageView pageOneImageView;
    ImageView pageTwoImageView;
    ViewPager mViewPager;
    HowItWorksPageAdapter mHowItWorksPageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_it_works);

        pageOneImageView = findViewById(R.id.page_one_image_view);
        pageOneImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageOneImageView.setImageResource(R.drawable.dot_selected);
                pageTwoImageView.setImageResource(R.drawable.dot);
                mViewPager.setCurrentItem(0,true);
            }
        });

        pageTwoImageView = findViewById(R.id.page_two_image_view);
        pageTwoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageOneImageView.setImageResource(R.drawable.dot);
                pageTwoImageView.setImageResource(R.drawable.dot_selected);
                mViewPager.setCurrentItem(1,true);
            }
        });

        mViewPager = findViewById(R.id.how_it_works_pager);
        mHowItWorksPageAdapter = new HowItWorksPageAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mHowItWorksPageAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        pageOneImageView.setImageResource(R.drawable.dot_selected);
                        pageTwoImageView.setImageResource(R.drawable.dot);
                        break;
                    case 1:
                        pageOneImageView.setImageResource(R.drawable.dot);
                        pageTwoImageView.setImageResource(R.drawable.dot_selected);
                        break;
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
