package com.langamy.activities;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bignerdranch.android.main.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.langamy.api.LangamyAPI;
import com.langamy.base.classes.BaseVariables;
import com.langamy.base.classes.NetworkMonitor;
import com.langamy.base.classes.StudySet;
import com.langamy.database.StudySetCursorWrapper;
import com.langamy.database.StudySetsBaseHelper;
import com.langamy.database.StudySetsScheme;
import com.langamy.fragments.CreateStudySetsFragment;
import com.langamy.fragments.ProfileFragment;
import com.langamy.fragments.StudySetsFragment;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.langamy.database.StudySetsScheme.*;

public class MainActivity extends AppCompatActivity {

    //    Fragments
    ProfileFragment mProfileFragment;
    StudySetsFragment mStudySetsFragment;
    CreateStudySetsFragment createStudySetsFragment;

    private ArrayList<Fragment> fragments;
    private SQLiteDatabase mDatabase;

    private BroadcastReceiver broadcastReceiver;
    public Retrofit retrofit = BaseVariables.retrofit;
    public LangamyAPI mLangamyAPI = retrofit.create(LangamyAPI.class);
    private GoogleSignInAccount acct;
    private ViewPager2 mViewPager;
    private TabLayout tabLayout;

    private int[] tabIcons = {
            R.drawable.ic_my_studysets,
            R.drawable.ic_create_studyset,
            R.drawable.ic_account
    };

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sf = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sf.edit();
        try {
            sf.getBoolean(BaseVariables.HELP_CREATE_STUDYSETS_FRAGMENT, true);
        } catch (Exception e) {
            ed.putBoolean(BaseVariables.HELP_CREATE_STUDYSETS_FRAGMENT, true);
            ed.commit();
        }

        acct = GoogleSignIn.getLastSignedInAccount(MainActivity.this);

        if (acct == null) {
            Intent intent = new Intent(this, GoogleSignInActivity.class);
            startActivity(intent);
            return;
        }

        setContentView(R.layout.start_activity);

        mViewPager = findViewById(R.id.pager);
        tabLayout = findViewById(R.id.tab_layout);

        //Fragment inizialization
        mProfileFragment = new ProfileFragment();
        mStudySetsFragment = new StudySetsFragment();
        createStudySetsFragment = new CreateStudySetsFragment();

        fragments = new ArrayList<>();

        fragments.add(mStudySetsFragment);
        fragments.add(createStudySetsFragment);
        fragments.add(mProfileFragment);

        mViewPager.setAdapter(createCardAdapter());
        mViewPager.setOffscreenPageLimit(2);

        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                invalidateFragmentMenus(position);
            }
        });

        mDatabase = new StudySetsBaseHelper(this)
                .getWritableDatabase();

        new TabLayoutMediator(tabLayout, mViewPager,
                (tab, position) -> {
                    tab.setIcon(tabIcons[position]);
                    if (position == 0) {
                        tab.getIcon().setColorFilter(getResources().getColor(R.color.lightDark), PorterDuff.Mode.SRC_IN);
                    }
                }).attach();


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(getResources().getColor(R.color.lightDark), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(getResources().getColor(android.R.color.white), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        KeyboardVisibilityEvent.setEventListener(
                this,
                isOpen -> {
                    if (isOpen) {
                        tabLayout.setVisibility(View.GONE);
                    } else {
                        tabLayout.setVisibility(View.VISIBLE);
                    }
                });

        broadcastReceiver = new NetworkMonitor();
    }

    public TabLayout getNavigationView() {
        return tabLayout;
    }

    public ViewPager2 getViewPager() {
        return mViewPager;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageData) {
        super.onActivityResult(requestCode, resultCode, imageData);

        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, imageData);
        }

    }

    private ViewPagerAdapter createCardAdapter() {
        return new ViewPagerAdapter(this);
    }

    public class ViewPagerAdapter extends FragmentStateAdapter {

        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragments.get(position);
        }

        @Override
        public int getItemCount() {
            return fragments.size();
        }


    }

    private void invalidateFragmentMenus(int position) {

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this);
        for (int i = 0; i < fragments.size(); i++) {
            viewPagerAdapter.createFragment(i).setHasOptionsMenu(i == position);
        }
        invalidateOptionsMenu();

    }

}
