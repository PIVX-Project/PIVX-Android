package pivx.org.pivxwallet.ui.base.tools.adapter;


public interface ListItemListeners<M> {

    /**
     * onItem click listener event
     *
     * @param data
     * @param position
     */
    void onItemClickListener(M data, int position);

    /**
     * On Long item Click Listener
     *
     * @param data
     * @param position
     */
    void onLongItemClickListener(M data, int position);

}
