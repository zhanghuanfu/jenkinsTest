package com.cicc.esper;

import com.cicc.esper.annotation.When;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.stock.framework.database.TransactionManager;
import com.stock.framework.database.exception.AppException;
import com.stock.framework.global.BLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Date;

public class EsperListener implements UpdateListener{

    protected transient static Logger log = LoggerFactory.getLogger("info");
//    original stream,used to test annotation subscriber
    public void origin(Object object){
        log.info("--subscriber triggered:"+object);
    }

//    movingAverage stream
    @When("select talib('movingAverage',price,20,'Ema',10) as value from TestTick")
    public void doTAlib(Object object){
        log.info("talib:"+object);
    }

//    OHLC bar stream
    @When("select * from TestTick#ohlcbarminute(timestamp,price)")
    public void testOHLC(Object object){
        log.info("ohlc:"+object);
    }

//    subscriber to test time control
    @When("select current_timestamp() as ct from pattern[every timer:interval(1 minute)] ")
    public void testSchedule(Object object){
        log.info("timer!"+new Date((Long)object));
    }

    //    original stream,used to test annotation listeners
    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents){
        log.info("--listener triggered:"+ newEvents[0]);
        
        EventBean event = newEvents[0];
        
        System.out.print("##########################################################################"
        		+ "###############################################################################");
        System.out.println(event.get("minqty"));
        
        TransactionManager transMgr = null;
        long tradeDate = (long) event.get("tradeDate");
        String exchId = (String) event.get("exchId");
        String stkId = (String) event.get("stkId");
        int days = 30;
        int period = 30;
        
        try {
			VwapCalculator.calculate(transMgr, tradeDate, exchId, stkId, days, period);
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		} catch (BLException e) {
			e.printStackTrace();
			return;
		} catch (AppException e) {
			e.printStackTrace();
			return;
		}
    }
}
