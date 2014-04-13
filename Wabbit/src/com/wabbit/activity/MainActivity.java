package com.wabbit.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.parse.ParseUser;
import com.wabbit.abstracts.MessagesMgr;
import com.wabbit.application.BaseApp;
import com.wabbit.fragments.ChatFragment;
import com.wabbit.fragments.PeopleNearbyFragment;
import com.wabbit.fragments.ProfileFragment;
import com.wabbit.fragments.RecentConversationFragment;
import com.wabbit.fragments.SettingsFragment;
import com.wabbit.interfaces.IHelper;
import com.wabbit.libraries.PagerSlidingTabStrip;
import com.wabbit.messenger.R;

public class MainActivity extends BaseActivity implements IHelper {
    private static MainActivity _instance;
    public static MainActivity gi(){
        return _instance;
    }

    //Sliding
    private int mTitleRes;
    protected PeopleNearbyFragment mPeopleNearbyFrag = new PeopleNearbyFragment();
    protected RecentConversationFragment mRecentConvFrag = new RecentConversationFragment();
    protected ChatFragment mChatFrag = new ChatFragment();
    protected SettingsFragment mSettingsFrag = new SettingsFragment();

    private PagerSlidingTabStrip tabs;
    private ViewPager pager;
    private MyPagerAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_main);

        setUpTabsAndPager();
        setUpFirebase();

        //Notification clicked
        onNewIntent(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent){
        Bundle extras = intent.getExtras();
        if(extras != null){
            if(extras.containsKey("com.parse.Data"))
            {   //Go to recent
                pager.setCurrentItem(1, true);
            }
        }


    }

    @Override
    public void onStart(){
        super.onStart();
    }
    @Override
    public void onStop(){
        super.onStop();
    }
    @Override
    public void onResume(){
        super.onResume();
        _instance = this;
    }
    @Override
    public void onPause(){
        _instance = null;
        super.onPause();
    }

    @Override
    public void openChatWith(String userid) {
        mChatFrag.setChatPartner(userid);

        //Open chat only if it is not opened
        Fragment prev = getSupportFragmentManager().findFragmentByTag("profile");
        if(prev == null){
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            ft.replace(R.id.chat_frag, mChatFrag, "chat_frag");
            ft.addToBackStack("chat");
            ft.commit();
        }
    }
    @Override
    public void onChatClosed(){
        mRecentConvFrag.updateList();
    }
    @Override
    public void onProfileClosed(){
    }

    @Override
    protected void onDestroy() {
        BaseApp.getApp().mixtrack("Exit app");
        BaseApp.getApp().getMixpanel().flush();
        super.onDestroy();
    }


    public void  showProfile(String userid) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("profile");
        if (prev != null) {
            ft.remove(prev);
        }

        BaseApp.getApp().mixtrack("Show profile");
        ProfileFragment newFragment = ProfileFragment.newInstance(userid);
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        ft.replace(R.id.profile_frag, newFragment, "profile");
        ft.addToBackStack("profile");
        ft.commit();
    }

    public void setUpTabsAndPager(){
        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        pager = (ViewPager) findViewById(R.id.pager);
        adapter = new MyPagerAdapter(getSupportFragmentManager());

        pager.setAdapter(adapter);
        pager.setPageMargin(0);
        pager.setOffscreenPageLimit(2);

        tabs.setIndicatorHeight((int) (getResources().getDimension(R.dimen.bar_height) / getResources().getDisplayMetrics().density));
        tabs.setIndicatorColor(0xFF00ADEF);
//        tabs.setDividerColor(0xFF00ADEF);
        tabs.setShouldExpand(true);
        tabs.setViewPager(pager);

        tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
//                if(position == 1){
//                    mRecentConvFrag.updateList();
//                }
                switch (position) {
                    case 0:
                        //TODO: reload list UI?
                        BaseApp.getApp().mixtrack("Switched to Around me Tab");
                        break;
                    case 1:
                        BaseApp.getApp().mixtrack("Switched to Recent Tab");
                        break;
                    case 2:
                        BaseApp.getApp().mixtrack("Switched to Me Tab");
//                        mSettingsFrag.startAnim();
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (getSupportFragmentManager().getBackStackEntryCount() == 0)
                    mRecentConvFrag.updateList();
            }
        });
    }

    public void setUpFirebase(){
        Firebase listRef = BaseApp.getApp().getFirebaseRef().child(ParseUser.getCurrentUser().getObjectId());
        listRef.addValueEventListener(new ValueEventListener() {
            boolean firstTime = true;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!firstTime)//Skip first time. BUG i guess
                    onNewMessage();
                firstTime = false;
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    public void onNewMessage(){
        MessagesMgr.gi().loadUnreadMessages(MessagesMgr.FROM_ONLINE, onNewMessage);
    }
    public void onLocationUpdate(){
        mPeopleNearbyFrag.onLocationChanged();
    }

    private Runnable onNewMessage = new Runnable() {
        @Override
        public void run() {
            MessagesMgr.gi().addUnreadToRecent();
            if(mChatFrag.isRunning())
                mChatFrag.onNewMessage();
            else {//If chat is closed
                mRecentConvFrag.updateList();
                pager.setCurrentItem(1, true);
            }
        }
    };

    public class MyPagerAdapter extends FragmentPagerAdapter {
        private final String[] TITLES = { "Around me", "Recent", "Me"};

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0){
                return mPeopleNearbyFrag;
            }
            if(position == 1){
                return mRecentConvFrag;
            }
            if(position == 2){
                return mSettingsFrag;
            }
            return null;
        }
    }
}