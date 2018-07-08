package com.project.ahmed.v_tracker;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Ahmed on 11/16/2017.
 */
public class HomeActivity extends AppCompatActivity {

    private final int ENABLE_BT_REQUEST_CODE = 1;
    public static BluetoothAdapter mBluetoothAdapter;
    ViewPager viewPager;
    PageAdapter pageAdapter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.popup_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.how_it_works:
                Intent intent = new Intent(HomeActivity.this,HowItWorksActivity.class);
                startActivity(intent);
                return true;
            case R.id.contact:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);

        toolbar.setTitle(R.string.app_name);
        toolbar.setLogoDescription("Track Your Valuables");

        setSupportActionBar(toolbar);



        //replacing icons on tab layout
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.devices));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.map));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        //getting bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //checking bluetooth
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Device Doesn't Support Bluetooth", Toast.LENGTH_SHORT).show();
            finish();
            //if bluetooth is not enabled ask for permission to enable it
        } else if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetoothIntent = new Intent(mBluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetoothIntent, ENABLE_BT_REQUEST_CODE);
        }

        // adding page adapter on pager
        viewPager = findViewById(R.id.pager);
        pageAdapter = new PageAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pageAdapter);

        //changing the tabs according to swiping
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));


        if(getIntent().getExtras() != null) {
            String orderNotification = getIntent().getStringExtra("trackNotification");
            if (orderNotification.equals("trackNotification"))
            {
                viewPager.setCurrentItem(1,true);
            }
        }


        //changing the pages according to tabs clicking
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }


}
