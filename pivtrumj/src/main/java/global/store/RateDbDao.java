package global.store;

import global.PivxRate;

/**
 * Created by furszy on 3/3/18.
 */

public interface RateDbDao<T> extends AbstractDbDao<T>{

    PivxRate getRate(String coin);


    void insertOrUpdateIfExist(PivxRate pivxRate);

}
