package pivx.org.pivxwallet.ui.words_restore_activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;
import pivx.org.pivxwallet.ui.pincode_activity.PincodeActivity;

/**
 * Created by Neoperol on 7/19/17.
 */

public class RestoreWordsActivity extends BaseActivity {
    private View root;
    private ViewPager viewPager;
    private RestoreWordsActivity.ViewPagerAdapter viewPagerAdapter;
    private int[] layouts;
    private Button btnBack, btnNext;
    private EditText txtWord1, txtWord2, txtWord3, txtWord4, txtWord5, txtWord6, txtWord7, txtWord8 , txtWord9, txtWord10, txtWord11, txtWord12 ;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        root = getLayoutInflater().inflate(R.layout.security_words_restore, container);

        setTitle("Security words");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        btnBack = (Button) findViewById(R.id.btn_back);
        btnBack.setVisibility(View.GONE);
        btnNext = (Button) findViewById(R.id.btn_next);

        layouts = new int[]{
                R.layout.words_slide1,
                R.layout.words_slide2};

        viewPagerAdapter = new RestoreWordsActivity.ViewPagerAdapter();
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        //Words
        txtWord1 = (EditText) findViewById(R.id.text_word1);
        txtWord2 = (EditText) findViewById(R.id.text_word2);
        txtWord3 = (EditText) findViewById(R.id.text_word3);
        txtWord4 = (EditText) findViewById(R.id.text_word4);
        txtWord5 = (EditText) findViewById(R.id.text_word5);
        txtWord6 = (EditText) findViewById(R.id.text_word6);
        txtWord7 = (EditText) findViewById(R.id.text_word7);
        txtWord8 = (EditText) findViewById(R.id.text_word8);
        txtWord9 = (EditText) findViewById(R.id.text_word9);
        txtWord10 = (EditText) findViewById(R.id.text_word10);
        txtWord11 = (EditText) findViewById(R.id.text_word11);
        txtWord12 = (EditText) findViewById(R.id.text_word12);


    }

    public  void btnBackClick(View v) {
        int current = getItem(-1);
        if (current < layouts.length) {
            // move to previus screen
            viewPager.setCurrentItem(current);
        }
    }

    public  void btnNextClick(View v)
    {
        // checking for last page
        int current = getItem(1);
        if (current < layouts.length) {
            // move to next screen
            viewPager.setCurrentItem(current);
        } else {
            //Restore Wallet
            launchHomeScreen();
        }
    }


    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {

            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == 0) {
                // last page. make button text to GOT IT
                btnNext.setText(getString(R.string.next_words));
                btnNext.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_next, 0);
                btnBack.setVisibility(View.GONE);
            } else {
                // still pages are left
                btnNext.setText(getString(R.string.restore));
                btnNext.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                btnBack.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };




    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    private void launchHomeScreen() {
        startActivity(new Intent(this, PincodeActivity.class));
        finish();
    }


    public class ViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;


        public ViewPagerAdapter() {

        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);

            return view;

        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}
