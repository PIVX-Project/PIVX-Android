package pivx.org.pivxwallet.ui.export_account;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.WriterException;

import org.pivxj.crypto.DeterministicKey;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.module.PivxContext;
import pivx.org.pivxwallet.ui.base.BaseActivity;
import pivx.org.pivxwallet.utils.AndroidUtils;
import pivx.org.pivxwallet.utils.CrashReporter;

import static android.graphics.Color.WHITE;
import static pivx.org.pivxwallet.utils.QrUtils.encodeAsBitmap;

/**
 * Created by furszy on 8/29/17.
 */

public class ExportKeyActivity extends BaseActivity implements View.OnClickListener {

    private View root;
    private TextView txt_title,txt_key,txt_warn,txt_title_derivation_path,txt_derivation_path;
    private ImageView img_qr;
    private String xpubKey;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        try {
            setTitle(R.string.export_wallet);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            root = getLayoutInflater().inflate(R.layout.export_key_main, container);
            txt_title = (TextView) root.findViewById(R.id.txt_title);
            txt_key = (TextView) root.findViewById(R.id.txt_key);
            txt_warn = (TextView) root.findViewById(R.id.txt_warn);
            txt_title_derivation_path = (TextView) root.findViewById(R.id.txt_title_derivation_path);
            txt_derivation_path = (TextView) root.findViewById(R.id.txt_derivation_path);
            img_qr = (ImageView) root.findViewById(R.id.img_qr);

            initValues();
        }catch (Exception e){
            e.printStackTrace();
            CrashReporter.saveBackgroundTrace(e,pivxApplication.getPackageInfo());
            Toast.makeText(this,R.string.unknown_error_message,Toast.LENGTH_LONG).show();
            onBackPressed();
        }
    }

    private void initValues() throws WriterException {
        DeterministicKey deterministicKey = pivxModule.getWatchingKey();
        xpubKey = deterministicKey.serializePubB58(PivxContext.NETWORK_PARAMETERS);
        txt_title.setText(R.string.public_key);
        txt_key.setText(xpubKey);
        txt_key.setOnClickListener(this);
        txt_warn.setText(R.string.warn_sharing_pub_key);
        txt_title_derivation_path.setText(R.string.derivation_path);
        txt_derivation_path.setText(deterministicKey.getPathAsString());
        loadQr(xpubKey);
        img_qr.setOnClickListener(this);
    }

    private void loadQr(String text) throws WriterException {
        Bitmap qrBitmap = null;
        if (qrBitmap == null) {
            Resources r = getResources();
            int px = convertDpToPx(r,225);
            qrBitmap = encodeAsBitmap(text, px, px, Color.parseColor("#1A1A1A"), WHITE );
        }
        img_qr.setImageBitmap(qrBitmap);
    }

    public static int convertDpToPx(Resources resources, int dp){
        return Math.round(dp*(resources.getDisplayMetrics().xdpi/ DisplayMetrics.DENSITY_DEFAULT));
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.txt_key || id == R.id.img_qr){
            AndroidUtils.copyToClipboard(this,xpubKey);
            Toast.makeText(v.getContext(), R.string.copy_key_message, Toast.LENGTH_LONG).show();
        }
    }
}
