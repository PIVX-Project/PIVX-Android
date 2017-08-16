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
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;
import pivx.org.pivxwallet.ui.pincode_activity.PincodeActivity;

import static pivx.org.pivxwallet.module.PivxContext.PIVX_WALLET_APP_RELEASED_ON_PLAY_STORE_TIME;

/**
 * Created by Neoperol on 7/19/17.
 * // todo: this activity is awfull, should do this right..
 */

public class RestoreWordsActivity extends BaseActivity {
    private View root;
    private ViewPager viewPager;
    private RestoreWordsActivity.ViewPagerAdapter viewPagerAdapter;
    private int[] layouts;
    private Button btnBack, btnNext;
    private EditText txtWord1, txtWord2, txtWord3, txtWord4, txtWord5, txtWord6, txtWord7, txtWord8 , txtWord9, txtWord10, txtWord11, txtWord12 ;
    private EditText txtWord13, txtWord14, txtWord15, txtWord16, txtWord17, txtWord18 , txtWord19, txtWord20, txtWord21, txtWord22, txtWord23, txtWord24;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        root = getLayoutInflater().inflate(R.layout.security_words_restore, container);

        setTitle(R.string.restore_mnemonic_screen_title);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        btnBack = (Button) findViewById(R.id.btn_back);
        btnBack.setVisibility(View.GONE);
        btnNext = (Button) findViewById(R.id.btn_next);

        layouts = new int[]{
                R.layout.words_slide1,
                R.layout.words_slide2,
                R.layout.words_slide3};

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
        txtWord13 = (EditText) findViewById(R.id.text_word13);
        txtWord14 = (EditText) findViewById(R.id.text_word14);
        txtWord15 = (EditText) findViewById(R.id.text_word15);
        txtWord16 = (EditText) findViewById(R.id.text_word16);
        txtWord17 = (EditText) findViewById(R.id.text_word17);
        txtWord18 = (EditText) findViewById(R.id.text_word18);
        txtWord19 = (EditText) findViewById(R.id.text_word19);
        txtWord20 = (EditText) findViewById(R.id.text_word20);
        txtWord21 = (EditText) findViewById(R.id.text_word21);
        txtWord22 = (EditText) findViewById(R.id.text_word22);
        txtWord23 = (EditText) findViewById(R.id.text_word23);
        txtWord24 = (EditText) findViewById(R.id.text_word24);
    }

    public void btnBackClick(View v) {
        int current = getItem(-1);
        if (current < layouts.length) {
            // move to previus screen
            viewPager.setCurrentItem(current);
        }
    }

    public void btnNextClick(View v) {
        // checking for last page
        int current = getItem(1);
        if (current < layouts.length) {
            // move to next screen
            viewPager.setCurrentItem(current);
        } else {
            try {
                //Restore Wallet
                String word1 = txtWord1.getText().toString();
                String word2 = txtWord2.getText().toString();
                String word3 = txtWord3.getText().toString();
                String word4 = txtWord4.getText().toString();
                String word5 = txtWord5.getText().toString();
                String word6 = txtWord6.getText().toString();
                String word7 = txtWord7.getText().toString();
                String word8 = txtWord8.getText().toString();
                String word9 = txtWord9.getText().toString();
                String word10 = txtWord10.getText().toString();
                String word11 = txtWord11.getText().toString();
                String word12 = txtWord12.getText().toString();
                String word13 = txtWord13.getText().toString();
                String word14 = txtWord14.getText().toString();
                String word15 = txtWord15.getText().toString();
                String word16 = txtWord16.getText().toString();
                String word17 = txtWord17.getText().toString();
                String word18 = txtWord18.getText().toString();
                String word19 = txtWord19.getText().toString();
                String word20 = txtWord20.getText().toString();
                String word21 = txtWord21.getText().toString();
                String word22 = txtWord22.getText().toString();
                String word23 = txtWord23.getText().toString();
                String word24 = txtWord24.getText().toString();

                List<String> mnemonic = new ArrayList<>();
                mnemonic.add(word1);
                mnemonic.add(word2);
                mnemonic.add(word3);
                mnemonic.add(word4);
                mnemonic.add(word5);
                mnemonic.add(word6);
                mnemonic.add(word7);
                mnemonic.add(word8);
                mnemonic.add(word9);
                mnemonic.add(word10);
                mnemonic.add(word11);
                mnemonic.add(word12);
                mnemonic.add(word13);
                mnemonic.add(word14);
                mnemonic.add(word15);
                mnemonic.add(word16);
                mnemonic.add(word17);
                mnemonic.add(word18);
                mnemonic.add(word19);
                mnemonic.add(word20);
                mnemonic.add(word21);
                mnemonic.add(word22);
                mnemonic.add(word23);
                mnemonic.add(word24);

                pivxModule.restoreWallet(mnemonic, PIVX_WALLET_APP_RELEASED_ON_PLAY_STORE_TIME);

                launchHomeScreen();
            } catch (IOException e) {
                e.printStackTrace();
                // todo: show an error message here..
                Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
    }


    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {

            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == 0  ) {
                // last page. make button text to GOT IT
                btnNext.setText(getString(R.string.next_words));
                btnNext.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_next, 0);
                btnBack.setVisibility(View.GONE);


            }
            else if ( position == 1) {
                btnNext.setText(getString(R.string.next_words));
                btnNext.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_next, 0);
                btnBack.setVisibility(View.VISIBLE);
            }

            else {
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
