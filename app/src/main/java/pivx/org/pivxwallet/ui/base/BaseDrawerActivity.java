package pivx.org.pivxwallet.ui.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import chain.BlockchainState;
import pivx.org.pivxwallet.BuildConfig;
import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.contacts_activity.ContactsActivity;
import pivx.org.pivxwallet.ui.donate.DonateActivity;
import pivx.org.pivxwallet.ui.settings_activity.SettingsActivity;
import pivx.org.pivxwallet.ui.wallet_activity.WalletActivity;
import pivx.org.pivxwallet.wallofcoins.buyingwizard.BuyDashBaseActivity;
import pivx.org.pivxwallet.wallofcoins.selling_wizard.SellingBaseActivity;

import static pivx.org.pivxwallet.module.PivxContext.OUT_OF_SYNC_TIME;
import static pivx.org.pivxwallet.service.IntentsConstants.ACTION_NOTIFICATION;
import static pivx.org.pivxwallet.service.IntentsConstants.INTENT_BROADCAST_DATA_BLOCKCHAIN_STATE;
import static pivx.org.pivxwallet.service.IntentsConstants.INTENT_BROADCAST_DATA_PEER_CONNECTED;
import static pivx.org.pivxwallet.service.IntentsConstants.INTENT_BROADCAST_DATA_TYPE;
import static pivx.org.pivxwallet.service.IntentsConstants.INTENT_EXTRA_BLOCKCHAIN_STATE;

public class BaseDrawerActivity extends PivxActivity implements NavigationView.OnNavigationItemSelectedListener {

    private NavigationView navigationView;
    private FrameLayout frameLayout;
    private Toolbar toolbar;
    private DrawerLayout drawer;
    protected FrameLayout header_container;
    private TextView txt_app_version;
    private TextView txt_sync_status;
    private ImageView img_sync;

    private BlockchainState blockchainState = BlockchainState.SYNCING;

    private BroadcastReceiver walletServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(INTENT_BROADCAST_DATA_TYPE)) {
                if (intent.getStringExtra(INTENT_BROADCAST_DATA_TYPE).equals(INTENT_BROADCAST_DATA_BLOCKCHAIN_STATE)) {
                    BlockchainState blockchainStateNew = (BlockchainState) intent.getSerializableExtra(INTENT_EXTRA_BLOCKCHAIN_STATE);
                    onBlockchainStateChange();
                    if (blockchainStateNew == null) {
                        Log.e("APP", "blockchain state null..");
                        return;
                    }
                    blockchainState = blockchainStateNew;
                    updateBlockchainState();
                } else if (intent.getStringExtra(INTENT_BROADCAST_DATA_TYPE).equals(INTENT_BROADCAST_DATA_PEER_CONNECTED)) {
                    checkState();
                    updateBlockchainState();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        beforeCreate();
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        frameLayout = (FrameLayout) findViewById(R.id.content_frame);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        header_container = (FrameLayout) findViewById(R.id.header_container);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerLayout = navigationView.getHeaderView(0);
        txt_sync_status = (TextView) headerLayout.findViewById(R.id.txt_sync_status);
        img_sync = (ImageView) headerLayout.findViewById(R.id.img_sync);
        txt_app_version = (TextView) navigationView.findViewById(R.id.txt_app_version);
        txt_app_version.setText(BuildConfig.VERSION_NAME);

        onCreateView(savedInstanceState, frameLayout);

        localBroadcastManager.registerReceiver(walletServiceReceiver, new IntentFilter(ACTION_NOTIFICATION));
    }

    private void checkState() {
        long now = System.currentTimeMillis();
        long lastBlockTime = pivxApplication.getAppConf().getLastBestChainBlockTime();
        if (lastBlockTime + OUT_OF_SYNC_TIME > now) {
            // check if i'm syncing or i'm synched
            long peerHeight = pivxModule.getConnectedPeerHeight();
            if (peerHeight != -1) {
                if (pivxModule.getChainHeight() + 10 > peerHeight) {
                    blockchainState = BlockchainState.SYNC;
                } else {
                    blockchainState = BlockchainState.SYNCING;
                }
            } else {
                blockchainState = BlockchainState.NOT_CONNECTION;
            }
        } else {
            if (pivxModule.isAnyPeerConnected()) {
                long peerHeight = pivxModule.getConnectedPeerHeight();
                if (peerHeight != -1) {
                    if (pivxModule.getChainHeight() + 10 > peerHeight) {
                        blockchainState = BlockchainState.SYNC;
                    } else {
                        blockchainState = BlockchainState.SYNCING;
                    }
                } else {
                    blockchainState = BlockchainState.NOT_CONNECTION;
                }
            } else {
                blockchainState = BlockchainState.NOT_CONNECTION;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkState();

        updateBlockchainState();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(walletServiceReceiver);
    }

    /**
     * Empty method to check some status before set the main layout of the activity
     */
    protected void beforeCreate() {

    }

    /**
     * Empty method to override.
     *
     * @param savedInstanceState
     */
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {

    }

    @Override
    public void onBackPressed() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_qr) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        //to prevent current item select over and over
        if (item.isChecked()) {
            drawer.closeDrawer(GravityCompat.START);
            return false;
        }

        if (id == R.id.nav_wallet) {
            Intent intent = new Intent(this, WalletActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_address) {
            startActivity(new Intent(this, ContactsActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_donations) {
            startActivity(new Intent(this, DonateActivity.class));
        } else if (id == R.id.nav_buywithcash) {
            startActivity(new Intent(this, BuyDashBaseActivity.class));
        } /*else if (id == R.id.nav_sell_piv) {
            startActivity(new Intent(this, SellingBaseActivity.class));
        }*/


        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void setNavigationMenuItemChecked(int pos) {
        navigationView.getMenu().getItem(pos).setChecked(true);
    }

    private void updateBlockchainState() {
        // Check if the activity is on foreground
        if (!isOnForeground) return;

        if (txt_sync_status != null) {
            String text = null;
            int color = 0;
            int imgSrc = 0;
            double progress = calculateBlockchainSyncProgress();
            switch (blockchainState) {
                case SYNC:
                    text = getString(R.string.sync);
                    color = Color.parseColor("#ffffffff");
                    imgSrc = 0;
                    break;
                case SYNCING:
                    text = getString(R.string.syncing) + " " + progress + "%";
                    color = Color.parseColor("#f6a623");
                    imgSrc = R.drawable.ic_header_unsynced;
                    break;
                case NOT_CONNECTION:
                    text = getString(R.string.not_connection);
                    color = Color.parseColor("#f6a623");
                    imgSrc = R.drawable.ic_header_unsynced;
                    break;
            }
            txt_sync_status.setText(text);
            txt_sync_status.setTextColor(color);
            if (imgSrc != 0) {
                img_sync.setImageResource(imgSrc);
                img_sync.setVisibility(View.VISIBLE);
            } else
                img_sync.setVisibility(View.INVISIBLE);
        }
    }

    private double calculateBlockchainSyncProgress() {
        long nodeHeight = pivxModule.getConnectedPeerHeight();
        if (nodeHeight > 0) {
            // calculate the progress
            // nodeHeight -> 100 %
            // current height -> x %
            return (pivxModule.getChainHeight() * 100) / nodeHeight;
        }
        return -1;
    }

    /**
     * Method to override
     */
    protected void onBlockchainStateChange() {

    }


}
