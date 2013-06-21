package com.ponyets.receipt;

import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends BaseActivity implements ActionBar.TabListener {

    private ViewPager viewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(new MainPaperAdapter(getSupportFragmentManager()));
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                getActionBar().setSelectedNavigationItem(position);
            }
        });

        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.addTab(actionBar.newTab().setText(R.string.receipt).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.credit).setTabListener(this));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_receipt) {
            DialogFragment fragment = new AddReceiptDialogFragment();
            fragment.show(getSupportFragmentManager(), "add receipt");
        } else if (item.getItemId() == R.id.repay) {
            DialogFragment fragment = new RepayDialogFragment();
            fragment.show(getSupportFragmentManager(), "repay");
        }
        return true;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    private static class MainPaperAdapter extends FragmentPagerAdapter {
        private MainPaperAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new ReceiptListFragment();
                case 1:
                default:
                    return new CreditListFragment();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
