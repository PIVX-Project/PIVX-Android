package pivx.org.pivxwallet.ui.base;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.tools.adapter.BaseRecyclerAdapter;
import pivx.org.pivxwallet.ui.base.tools.adapter.BaseRecyclerViewHolder;
import pivx.org.pivxwallet.wallofcoins.buyingwizard.BuyDashBaseActivity;

/**
 * Created by furszy on 6/20/17.
 */

public abstract class BaseRecyclerFragment<T> extends BaseFragment {

    private static final Logger log = LoggerFactory.getLogger(BaseRecyclerFragment.class);

    private View root;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView recycler;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View container_empty_screen;
    private TextView txt_empty;
    private ImageView imgEmptyView;

    private BaseRecyclerAdapter adapter;
    protected List<T> list;
    protected ExecutorService executor;

    private String emptyText;

    private boolean refreshSwipeEnabled = true;

    public BaseRecyclerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        root = inflater.inflate(R.layout.base_recycler_fragment, container, false);
        recycler = (RecyclerView) root.findViewById(R.id.recycler_contacts);
        swipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipeRefresh);
        container_empty_screen = root.findViewById(R.id.container_empty_screen);
        txt_empty = (TextView) root.findViewById(R.id.txt_empty);
        imgEmptyView = (ImageView) root.findViewById(R.id.img_empty_view);
        recycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recycler.setLayoutManager(layoutManager);
        adapter = initAdapter();
        if (adapter==null) throw new IllegalStateException("Base adapter cannot be null");
        recycler.setAdapter(adapter);
        swipeRefreshLayout.setEnabled(refreshSwipeEnabled);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        load();
                    }
                }
        );

        imgEmptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), BuyDashBaseActivity.class);
                startActivity(intent);
            }
        });
        txt_empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), BuyDashBaseActivity.class);
                startActivity(intent);
            }
        });
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (executor==null){
            executor = Executors.newSingleThreadExecutor();
        }
        load();
    }

    /**
     * Method to override
     */
    private void load() {
        swipeRefreshLayout.setRefreshing(true);
        if (executor!=null)
            executor.execute(loadRunnable);
    }

    public void refresh(){
        load();
    }


    @Override
    public void onStop() {
        super.onStop();
        if (executor!=null){
            executor.shutdownNow();
            executor = null;
        }
    }

    /**
     *
     * @return list of items
     */
    protected abstract List<T> onLoading();

    /**
     *
     * @return the main adapter
     */
    protected abstract BaseRecyclerAdapter<T,? extends BaseRecyclerViewHolder> initAdapter();

    protected <V>  V findViewById(int id,Class<V> clazz){
        return (V) root.findViewById(id);
    }

    protected Runnable loadRunnable = new Runnable() {
        @Override
        public void run() {
            if (getActivity()!=null) {
                boolean res = false;
                try {
                    list = onLoading();
                    res = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    res = false;
                    log.info("cantLoadListException: " + e.getMessage());
                }
                final boolean finalRes = res;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        if (finalRes) {
                            adapter.changeDataSet(list);
                            if (list != null && !list.isEmpty()) {
                                hideEmptyScreen();
                            } else {
                                showEmptyScreen();
                                txt_empty.setText(emptyText);
                                txt_empty.setTextColor(Color.BLACK);
                            }
                        }
                    }
                });
            }
        }
    };

    protected void setSwipeRefresh(boolean enable){
        this.refreshSwipeEnabled = enable;
    }

    protected void setEmptyText(String text){
        this.emptyText = text;
        if (txt_empty!=null){
            txt_empty.setText(emptyText);
        }
    }

    protected void setEmptyTextColor(int color){
        if (txt_empty!=null){
            txt_empty.setTextColor(color);
        }
    }

    protected void setEmptyView(int imgRes){
        if (imgEmptyView!=null){
            imgEmptyView.setImageResource(imgRes);
        }
    }

    private void showEmptyScreen(){
//        if (container_empty_screen!=null)
//            AnimationUtils.fadeInView(container_empty_screen,300);
        container_empty_screen.setVisibility(View.VISIBLE);

    }

    private void hideEmptyScreen(){
        container_empty_screen.setVisibility(View.GONE);
//        if (container_empty_screen!=null)
//            AnimationUtils.fadeOutView(container_empty_screen,300);
    }


    public RecyclerView getRecycler() {
        return recycler;
    }

    public BaseRecyclerAdapter getAdapter() {
        return adapter;
    }
}