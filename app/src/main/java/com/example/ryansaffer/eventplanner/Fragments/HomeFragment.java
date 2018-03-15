package com.example.ryansaffer.eventplanner.Fragments;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.ryansaffer.eventplanner.CreateEventActivity;
import com.example.ryansaffer.eventplanner.Fragments.EventsFragments.MyEventsFragment;
import com.example.ryansaffer.eventplanner.Fragments.EventsFragments.AllEventsFragment;
import com.example.ryansaffer.eventplanner.R;
import com.example.ryansaffer.eventplanner.SignInActivity;
import com.google.firebase.auth.FirebaseAuth;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private FragmentStatePagerAdapter mPagerAdapter;
    private ViewPager mViewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Create the adapter that will return a fragment for each section
        mPagerAdapter = new FragmentStatePagerAdapter(getFragmentManager()) {
            private final Fragment[] mFragments = new Fragment[] {
                    new AllEventsFragment(),
                    new MyEventsFragment(),
            };
            private final String[] mFragmentNames = new String[] {
                    "All Events",
                    "My Events"
            };

            @Override
            public Fragment getItem(int position) {
                return mFragments[position];
            }

            @Override
            public int getCount() {
                return mFragments.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mFragmentNames[position];
            }
        };

        setHasOptionsMenu(true);

        // Set up the ViewPager with the sections adapter.
        mViewPager = rootView.findViewById(R.id.container_main);
        mViewPager.setAdapter(mPagerAdapter);
        TabLayout tabsLayout = rootView.findViewById(R.id.tabs_main);
        tabsLayout.setupWithViewPager(mViewPager);

        rootView.findViewById(R.id.fab_new_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), CreateEventActivity.class));
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main_activity, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getActivity(), SignInActivity.class));
                return true;
            default:
//                return super.onOptionsItemSelected(item);
                return false;
        }
    }
}
