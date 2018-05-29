package pivx.org.pivxwallet.ui.words_restore_activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Lists;

import org.pivxj.crypto.MnemonicException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.module.PivxContext;
import pivx.org.pivxwallet.ui.base.BaseActivity;
import pivx.org.pivxwallet.ui.base.dialogs.SimpleTwoButtonsDialog;
import pivx.org.pivxwallet.ui.transaction_send_activity.MyFilterableAdapter;
import pivx.org.pivxwallet.ui.transaction_send_activity.MyTextWatcher;
import pivx.org.pivxwallet.ui.tutorial_activity.TutorialActivity;
import pivx.org.pivxwallet.ui.wallet_activity.WalletActivity;
import pivx.org.pivxwallet.utils.AnimationUtils;
import pivx.org.pivxwallet.utils.CrashReporter;
import pivx.org.pivxwallet.utils.DialogsUtil;

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
    private AutoCompleteTextView txtWord1, txtWord2, txtWord3, txtWord4, txtWord5, txtWord6, txtWord7, txtWord8 , txtWord9, txtWord10, txtWord11, txtWord12 ;
    private AutoCompleteTextView txtWord13, txtWord14, txtWord15, txtWord16, txtWord17, txtWord18 , txtWord19, txtWord20, txtWord21, txtWord22, txtWord23, txtWord24;

    private TextView txt_bip32_message;
    private CheckBox check_bip32;

    private View container_loading;

    private ArrayAdapter<String> mnemonicAdapter;
    private List<String> mnemonicWords;

    private ExecutorService executorService;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        root = getLayoutInflater().inflate(R.layout.security_words_restore, container);

        setTitle(R.string.restore_mnemonic_screen_title);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        btnBack = (Button) findViewById(R.id.btn_back);
        btnBack.setVisibility(View.GONE);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnBackClick(v);
            }
        });
        btnNext = (Button) findViewById(R.id.btn_next);
        container_loading = findViewById(R.id.container_loading);

        layouts = new int[]{
                R.layout.words_slide1,
                R.layout.words_slide2,
                R.layout.words_slide3};

        viewPagerAdapter = new RestoreWordsActivity.ViewPagerAdapter();
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
        viewPager.setOffscreenPageLimit(3);

        //Words
        mnemonicWords = pivxModule.getAvailableMnemonicWordsList();
        mnemonicAdapter =  new ArrayAdapter<String>
                (this, android.R.layout.select_dialog_item, mnemonicWords);
    }

    public void btnBackClick(View v) {
        int current = getItem(-1);
        if (current < layouts.length) {
            // move to previus screen
            viewPager.setCurrentItem(current);
        }
    }

    private void init(AutoCompleteTextView actv){
        actv.setThreshold(1);//will start working from first character
        actv.setAdapter(mnemonicAdapter);
    }

    private void init(AutoCompleteTextView... actvs){
        for (AutoCompleteTextView actv : actvs) {
            init(actv);
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

                final List<String> mnemonic = Lists.newArrayList(
                        word1,
                        word2,
                        word3,
                        word4,
                        word5,
                        word6,
                        word7,
                        word8,
                        word9,
                        word10,
                        word11,
                        word12,
                        word13,
                        word14,
                        word15,
                        word16,
                        word17,
                        word18,
                        word19,
                        word20,
                        word21,
                        word22,
                        word23,
                        word24
                );

                for (String s : mnemonic) {
                    if (s.equals("")){
                        DialogsUtil.buildSimpleErrorTextDialog(this,getString(R.string.invalid_inputs),getString(R.string.invalid_mnemonic_code))
                        .show(getFragmentManager(),"invalid_mnemonic_code");
                        return;
                    }
                }

                SimpleTwoButtonsDialog dialog = DialogsUtil.buildSimpleTwoBtnsDialog(
                        this,
                        R.string.restore_mnemonic_title,
                        R.string.restore_mnemonic_dialog_body,
                        new SimpleTwoButtonsDialog.SimpleTwoBtnsDialogListener() {
                            @Override
                            public void onRightBtnClicked(SimpleTwoButtonsDialog dialog) {
                                AnimationUtils.fadeInView(container_loading,500);
                                dialog.dismiss();


                                if (executorService != null){
                                    executorService.shutdownNow();
                                    executorService = null;
                                }

                                executorService = Executors.newSingleThreadExecutor();
                                executorService.submit(new Runnable() {
                                    @Override
                                    public void run() {
                                        String message = null;
                                        boolean result = false;
                                        try {
                                            // loading here..
                                            pivxModule.checkMnemonic(mnemonic);

                                            boolean isBip32 = check_bip32.isChecked();

                                            pivxModule.restoreWallet(mnemonic, PIVX_WALLET_APP_RELEASED_ON_PLAY_STORE_TIME,!isBip32);

                                            message = getString(R.string.restore_mnemonic);
                                            result = true;
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            CrashReporter.saveBackgroundTrace(e, pivxApplication.getPackageInfo());
                                            // todo: show an error message here..
                                            message = e.getMessage();
                                        }catch (MnemonicException e){
                                            e.printStackTrace();
                                            message = getString(R.string.invalid_mnemonic_code);
                                        }catch (Exception e){
                                            e.printStackTrace();
                                            CrashReporter.saveBackgroundTrace(e,pivxApplication.getPackageInfo());
                                            // todo: show an error message here..
                                            message = e.getMessage();
                                        }
                                        final boolean finalResult = result;
                                        final String finalMessage = message;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (finalResult){
                                                    Toast.makeText(RestoreWordsActivity.this, finalMessage, Toast.LENGTH_LONG).show();
                                                    // Check if the pin has been set
                                                    if (pivxApplication.getAppConf().getPincode() != null){
                                                        // Check if the app is starting
                                                        if(!pivxApplication.getAppConf().isAppInit()){
                                                            pivxApplication.getAppConf().setAppInit(true);
                                                        }
                                                        startActivity(new Intent(RestoreWordsActivity.this, WalletActivity.class));
                                                    }else {
                                                        startActivity(new Intent(RestoreWordsActivity.this, TutorialActivity.class));
                                                    }
                                                    finish();
                                                }else {
                                                    AnimationUtils.fadeOutGoneView(container_loading,500);
                                                    Toast.makeText(RestoreWordsActivity.this, finalMessage, Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });


                                    }
                                });
                            }

                            @Override
                            public void onLeftBtnClicked(SimpleTwoButtonsDialog dialog) {
                                dialog.dismiss();
                            }
                        }
                );
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    dialog.setRightBtnTextColor(getColor(R.color.bgPurple));
                }else {
                    dialog.setRightBtnTextColor(ContextCompat.getColor(this, R.color.bgPurple));
                }
                dialog.show();
            } catch (Exception e) {
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



    public class ViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;


        public ViewPagerAdapter() {

        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);

            if (position==0) {
                txtWord1 = (AutoCompleteTextView) view.findViewById(R.id.text_word1);
                txtWord2 = (AutoCompleteTextView) view.findViewById(R.id.text_word2);
                txtWord3 = (AutoCompleteTextView) view.findViewById(R.id.text_word3);
                txtWord4 = (AutoCompleteTextView) view.findViewById(R.id.text_word4);
                txtWord5 = (AutoCompleteTextView) view.findViewById(R.id.text_word5);
                txtWord6 = (AutoCompleteTextView) view.findViewById(R.id.text_word6);
                txtWord7 = (AutoCompleteTextView) view.findViewById(R.id.text_word7);
                txtWord8 = (AutoCompleteTextView) view.findViewById(R.id.text_word8);
                init(txtWord1,txtWord2,txtWord3,txtWord4,txtWord5,txtWord6,txtWord7,txtWord8);
            }else if (position==1){
                txtWord9 = (AutoCompleteTextView) view.findViewById(R.id.text_word9);
                txtWord10 = (AutoCompleteTextView) view.findViewById(R.id.text_word10);
                txtWord11 = (AutoCompleteTextView) view.findViewById(R.id.text_word11);
                txtWord12 = (AutoCompleteTextView) view.findViewById(R.id.text_word12);
                txtWord13 = (AutoCompleteTextView) view.findViewById(R.id.text_word13);
                txtWord14 = (AutoCompleteTextView) view.findViewById(R.id.text_word14);
                txtWord15 = (AutoCompleteTextView) view.findViewById(R.id.text_word15);
                txtWord16 = (AutoCompleteTextView) view.findViewById(R.id.text_word16);
                init(txtWord9,txtWord10,txtWord11,txtWord12,txtWord13,txtWord14,txtWord15,txtWord16);
            }else if(position==2){
                txtWord17 = (AutoCompleteTextView) view.findViewById(R.id.text_word17);
                txtWord18 = (AutoCompleteTextView) view.findViewById(R.id.text_word18);
                txtWord19 = (AutoCompleteTextView) view.findViewById(R.id.text_word19);
                txtWord20 = (AutoCompleteTextView) view.findViewById(R.id.text_word20);
                txtWord21 = (AutoCompleteTextView) view.findViewById(R.id.text_word21);
                txtWord22 = (AutoCompleteTextView) view.findViewById(R.id.text_word22);
                txtWord23 = (AutoCompleteTextView) view.findViewById(R.id.text_word23);
                txtWord24 = (AutoCompleteTextView) view.findViewById(R.id.text_word24);
                init(txtWord17,txtWord18,txtWord19,txtWord20,txtWord21,txtWord22,txtWord23,txtWord24);
                txt_bip32_message = (TextView) root.findViewById(R.id.txt_bip32_message);
                check_bip32 = (CheckBox) root.findViewById(R.id.check_bip32);
                txt_bip32_message.setText(getString(R.string.restore_bip32_warning, PivxContext.ENABLE_BIP44_APP_VERSION));

            }

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
