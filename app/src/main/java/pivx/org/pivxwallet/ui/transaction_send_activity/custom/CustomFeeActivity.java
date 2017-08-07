package pivx.org.pivxwallet.ui.transaction_send_activity.custom;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;

/**
 * Created by furszy on 8/3/17.
 */

public class CustomFeeActivity extends BaseActivity implements View.OnClickListener {

    private View root;
    private TextView txt_fee_recommended, txt_default;
    private TextView txt_fee_custom;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        root = getLayoutInflater().inflate(R.layout.custom_fee_main, container);
        setTitle("Custom fee");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        viewPager = (ViewPager) root.findViewById(R.id.view_pager);
        txt_fee_recommended = (TextView) root.findViewById(R.id.txt_fee_recommended);
        txt_fee_custom = (TextView) root.findViewById(R.id.txt_fee_custom);
        txt_default = (TextView) root.findViewById(R.id.txt_default);

        txt_fee_custom.setOnClickListener(this);
        txt_fee_recommended.setOnClickListener(this);
        txt_default.setOnClickListener(this);

        setupViewPager(viewPager);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.txt_fee_recommended){
            viewPager.setCurrentItem(0);
            txt_fee_recommended.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_border_curce));
            txt_fee_custom.setBackgroundColor(Color.parseColor("#00000000"));
        }else if (id == R.id.txt_fee_custom){
            viewPager.setCurrentItem(1);
            txt_fee_custom.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_border_curce));
            txt_fee_recommended.setBackgroundColor(Color.parseColor("#00000000"));
        }
        else if (id == R.id.txt_default){
            viewPager.setCurrentItem(0);
            txt_fee_recommended.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_button_border_curce));
            txt_fee_custom.setBackgroundColor(Color.parseColor("#00000000"));
        }

    }

    private void setupViewPager(ViewPager viewPager) {
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new RecommendedFeeFragment());
        viewPagerAdapter.addFragment(new CustomFeeFragment());
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position==0){
                    txt_fee_recommended.setTypeface(null, Typeface.BOLD);
                    txt_fee_custom.setTypeface(null, Typeface.NORMAL);
                }else {
                    txt_fee_custom.setTypeface(null, Typeface.BOLD);
                    txt_fee_recommended.setTypeface(null, Typeface.NORMAL);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment) {
            mFragmentList.add(fragment);
        }

    }
}
