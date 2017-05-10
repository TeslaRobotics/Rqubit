package com.example.santiago.btqubit;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import static com.example.santiago.btqubit.GameFragment.DATA_MODE;
import static com.example.santiago.btqubit.GameFragment.GO_SET;
import static com.example.santiago.btqubit.GameFragment.HANDS_OFF;
import static com.example.santiago.btqubit.GameFragment.HANDS_ON;
import static com.example.santiago.btqubit.GameFragment.PRED_MODE;
import static com.example.santiago.btqubit.GameFragment.datamode;
import static com.example.santiago.btqubit.GameFragment.gameMode;
import static com.example.santiago.btqubit.GameFragment.handsFreeFallPos;
import static com.example.santiago.btqubit.GameFragment.inFallsetting;
import static com.example.santiago.btqubit.GameFragment.penetrationLevel;
import static com.example.santiago.btqubit.GameFragment.prdcmode;
import static com.example.santiago.btqubit.GeneralStatsFragment.cutTiming;
import static com.example.santiago.btqubit.SettingsFragment.handsfree;

public class SecondActivity extends AppCompatActivity {


    AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    ViewPager mViewPager;
    TabLayout myTabLayout;
    private Menu menu;
    boolean conected = false;
    static String tableName;
    BluetoothChatService BTserv = null;

    GameFragment launchFrag = null;
    SettingsFragment setFrag = null;

    private static String address = "20:17:03:07:47:78";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        Intent newint = getIntent();
        tableName = newint.getStringExtra(MainActivity.EXTRA_TABLENAME);

        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager(),SecondActivity.this);

        final Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitle(tableName);

        myTabLayout = (TabLayout) findViewById(R.id.tabLayout);
        setSupportActionBar(myToolbar);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);

        myTabLayout.setupWithViewPager(mViewPager);


        BTserv = new BluetoothChatService(this);

        BTserv.checkBTState();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        this.menu = menu;
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_bt:

                if (conected){
                    BTserv.stop();
                    menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_bluetooth_black_24dp));

                }
                else {
                    BTserv.connect(address);
                    menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_bluetooth_connected_black_24dp));

                }
                conected = !conected;

                break;

            case R.id.action_refresh:

                BTserv.write(GO_SET+"\n");

                if(gameMode == datamode){
                    BTserv.write(DATA_MODE+"\n");
                }
                else if(gameMode == prdcmode){
                    BTserv.write(PRED_MODE + "," + Integer.toString(cutTiming)+"\n");
                }

                if(handsfree){
                    BTserv.write("neutral,neutral,"+HANDS_ON+"\n");
                }
                else {
                    BTserv.write("neutral,neutral,"+HANDS_OFF+"\n");
                }

                penetrationLevel = 0;
                handsFreeFallPos = 0;
                inFallsetting = false;
                Toast.makeText(this, "Devices synced", Toast.LENGTH_SHORT)
                        .show();
                break;

            case R.id.action_settings:
                Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT)
                        .show();
                break;
            default:
                break;
        }

        return true;
    }

    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        private Context context;
        final int PAGE_COUNT = 4;

        private int[] imageResId = {
                R.drawable.ic_games_black_24dp,
                R.drawable.ic_equalizer_black_24dp,
                R.drawable.ic_storage_black_24dp,
                R.drawable.ic_settings_black_24dp
        };

        public AppSectionsPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:

                    return new GameFragment();

                case 1:

                    return new GeneralStatsFragment();

                case 2:

                    return new IndividualDataFragment();

                default:
                    // The other sections of the app are dummy placeholders.
                    return new SettingsFragment();
            }
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            Drawable image = ContextCompat.getDrawable(context, imageResId[position]);
            image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
            SpannableString sb = new SpannableString(" ");
            ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
            sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return sb;
        }
    }


}


