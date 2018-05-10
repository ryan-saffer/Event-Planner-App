package com.example.ryansaffer.eventplanner.Activities;

import android.support.v4.app.FragmentManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.ryansaffer.eventplanner.Fragments.FriendsFragment;
import com.example.ryansaffer.eventplanner.Fragments.HomeFragment;
import com.example.ryansaffer.eventplanner.R;

public class MainActivity extends AppCompatActivity
        implements  NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "HomeFragment";

    private DrawerLayout mDrawerLayout;
    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the Toolbar (since we are using a DrawerLayout)
        Toolbar toolbar = findViewById(R.id.toolbar_fragment_home);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        // Set up the DrawerLayout
        mDrawerLayout = findViewById(R.id.drawer_layout);

        // Set up the Navigation View
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Create the Events Fragment and place it into the container
        Fragment fragment = new HomeFragment();
        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment;
        switch (item.getItemId()) {
            case R.id.drawer_home:
                fragment = new HomeFragment();
                mFragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(null).commit();
                break;
            case R.id.drawer_friends:
                fragment = new FriendsFragment();
                mFragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(null).commit();
                break;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
