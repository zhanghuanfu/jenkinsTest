package com.cicc.quotation.gfi;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cicc.esper.EsperListener;
import com.cicc.esper.module.Context;
import com.cicc.quotation.bean.QuotationProtos;
import com.cicc.quotation.bean.QuotationProtos.ProtoQuotation;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.TextFormat;
import com.stock.businesslogic.Stock;
import com.stock.framework.database.TransactionManager;
import com.stock.framework.global.StepConsts;
import com.stock.securityinfo.BasePropertiesUtil;

public class GFIQuotationManager {
	
	private static GFIQuotationManager instance;
	private static Logger loggerGFIQuo = LoggerFactory.getLogger("GFIQuotationManager");
	//��һ��key exchId + stkId  �ڶ���keyΪtimeMark
	private ConcurrentHashMap<String, Map<Integer, GFIQuotation>> gfiQuotationMap = new ConcurrentHashMap<String, Map<Integer, GFIQuotation>>();
	//��һ��keyΪexchId���ڶ���keyΪstkId
	private HashMap<String, Map<String, Stock>> stkInfos = new HashMap<String, Map<String, Stock>>();
	Context context = null;
	long baseVolume;
//	int lastITime;
	int timeMark = 0;
	
	private GFIQuotationManager() {
		TransactionManager transMgr = null;
		try {
			transMgr = new TransactionManager();
			String[] exchIds = {"0", "1", "H", "K"};
			for(int i = 0 ; i < exchIds.length; i ++) {
				stkInfos.put(exchIds[i], BasePropertiesUtil.getDBStkInfos(exchIds[i], transMgr));
			}
		} catch (Throwable e) {
			loggerGFIQuo.error("Error when processing GFIQuotationManager.getInstance e=" + e, e);
			e.printStackTrace();
			if (transMgr != null)
				transMgr.cancelTransaction();
		}
		
		context = new Context();
		context.registerEvent(GFIQuotation.class);
		context.init();
		context.attach(EsperListener.class);
	}
	
	/**
	 * ��ȡ�����ʵ��
	 */
	public synchronized static GFIQuotationManager getInstance() {
		if (instance == null) {
			instance = new GFIQuotationManager();
		}
		return instance;
	}
	
//	public ConcurrentHashMap<String, GFIQuotation> getGFIQuotationMap() {
//		return gfiQuotationMap;
//	}
	
	public void processor(byte[] data) throws Exception {
		try {
			QuotationProtos.ProtoQuotationArray quotArray = QuotationProtos.ProtoQuotationArray.parseFrom(data);
			
			String category = quotArray.getStrCategory();
			if(StepConsts.QUOTATIONTYPE_SNAPSHOT_STOCK.equals(category)) {
				//�ֻ����鴦��
				loggerGFIQuo.info(TextFormat.shortDebugString(quotArray));
				if(quotArray.getQuotationListCount()>0){
					for(ProtoQuotation quota:quotArray.getQuotationListList())
						processQuotation(quota);
				}else
					processQuotation(quotArray.getQuotation());
			}
		} catch (InvalidProtocolBufferException  e) {
			loggerGFIQuo.error("protobuf��������"+e+" ,dataLength:"+data.length+",da("+new String(data)+")");
		} catch(Exception e) {
			loggerGFIQuo.error("�������",e);
		}
	}

	private void processQuotation(ProtoQuotation quotation) {
		if (quotation == null) {
			return;
		}
		
		if (!quotation.hasStrCode() || !quotation.hasStrExchId()) {
			return;
		}
		
		if (!quotation.hasITradingDay() || !quotation.hasLVolume() || !quotation.hasITime()) {
			return;
		}
		
		Stock stockInfo = stkInfos.get(quotation.getStrExchId()).get(quotation.getStrCode());
		if(stockInfo.getBspermit() == 0) {
			dealWithDataByTime(quotation);
		}
	}
	
	private void dealWithDataByTime(ProtoQuotation quotation) {
//		//gfi_5min���һ���㷨
//		int iTime = quotation.getITime() / 100000;
//		int tradeTime = (iTime - iTime % 5) + 5;
//		boolean isNext5Min = (tradeTime - lastITime) > 5 ? true : false;
//		
//		if(iTime < 930) {
//			if (!quotation.hasLVolume()) {
//				return;
//			}
//			baseVolume = quotation.getLVolume();
//			isNext5Min = false;
//		}
//		
//		if(iTime < 935 && iTime >= 930) {
//			isNext5Min = false;
//		}
//		
//		if(isNext5Min) {
//			if (!quotation.hasLVolume()) {
//				return;
//			}
//			baseVolume = quotation.getLVolume();
//		}
//		
//		if(iTime > lastITime) {
//			lastITime = iTime;
//		}
		Map<String, String> dateEvent = new HashMap<String, String>();
		dateEvent.put("exchId", quotation.getStrExchId());
		dateEvent.put("stkId", quotation.getStrCode());
		dateEvent.put("volume", "quotation.getLVolume()");
		dateEvent.put("iTime", "quotation.getITime()");
		dateEvent.put("tradeDate", "quotation.getITradingDay()");
		
		context.getRunTime().sendEvent(dateEvent);
		
		
//		int iTime = quotation.getITime() / 100000;
//		checkFirst(quotation, iTime);
//			
//		int timeDif = timeMark - iTime;
//		//�������ʱ���ڵ�ǰʱ���֮ǰ
//		if(timeDif > 5) {
//			return;
//		}
//		
////		//�������ʱ���ڵ�ǰʱ���
////		if(timeDif > 0 && timeDif <= 5) {
////			
////		}
//		
//		//�������ʱ�����¸�ʱ���, �������¸�ʱ���֮���ʱ���
//		if(timeDif <= 0) {
//			baseVolume = quotation.getLVolume();
//			timeMark = creatTimeMark(iTime);
//		}
//		
//		String stkId = quotation.getStrCode().trim();
//		GFIQuotation gfiQuotation = getGfiQuotationAndNew(quotation.getStrExchId(), stkId, timeMark);
//		
//		gfiQuotation.setMinQty(quotation.getLVolume() - baseVolume); // �ɽ���
//		gfiQuotation.setTradeDate(quotation.getITradingDay()); //��������
//		gfiQuotation.setTradeTime(timeMark);  //����ʱ��
		
//		//esper����һ���¼�������ʽ
//		EsperService service = new EsperService();
//		service.startAsync().awaitRunning();
//		
//		EPAdministrator admin = EsperService.getContext().getEpAdministrator();
////		EPAdministrator admin = StrategyService.getInstance().getEsperS().getContext().getEpAdministrator();
//		
//		String epl = "select tradedate,tradetime,exchid,stkid,minqty from GFIQuotation.win:time(5 sec)";
//		admin.createEPL(epl);
//		service.attach(EsperListener.class);
		
//        context.getRunTime().sendEvent(gfiQuotation);
	}

	private void checkFirst(ProtoQuotation quotation, int iTime) {
		if(baseVolume == 0) {
			baseVolume = quotation.getLVolume();
		}
		
		if(timeMark == 0) {
			timeMark = creatTimeMark(iTime);
		}
	}
	
	private int creatTimeMark(int iTime) {
		if(iTime < 935) {
			return 935;
		}
		return (iTime - iTime % 5) + 5;
	}

	private GFIQuotation getGfiQuotationAndNew(String strExchId, String stkId, int mark) {
		GFIQuotation gfiQuotation = null;
		String quotationMapKey = strExchId + stkId;
		if (gfiQuotationMap.get(quotationMapKey) != null) {
			if(gfiQuotationMap.get(quotationMapKey).get(mark) != null) {
				gfiQuotation = gfiQuotationMap.get(quotationMapKey).get(mark);
			}
		}
		
		gfiQuotation = new GFIQuotation();
		gfiQuotation.setExchId(strExchId);
		gfiQuotation.setStkId(stkId);
		Map<Integer, GFIQuotation> impMap = new HashMap<Integer, GFIQuotation>();
		impMap.put(mark, gfiQuotation);
		gfiQuotationMap.put(quotationMapKey, impMap);
		return gfiQuotation;
	}
	
	public void flushToDB() {
		TransactionManager transMgr = null;
		PreparedStatement ps = null;
		String sql = null;
		try {
			transMgr = new TransactionManager();
			transMgr.beginTransaction();
			
			sql = "insert into gfi_stk5min(tradeDate, tradeTime, exchId, stkId, minQty)" +
					" values(?, ?, ?, ?, ?)";
			ps = transMgr.getConnection().prepareSql(sql);
			
			for(Map<Integer, GFIQuotation> gfiQuotationAll : gfiQuotationMap.values()) {
				for(GFIQuotation gfiQuotation : gfiQuotationAll.values()) {
					ps.setInt(1, gfiQuotation.getTradeDate());
					ps.setInt(2, gfiQuotation.getTradeTime());
					ps.setString(3, gfiQuotation.getExchId());
					ps.setString(4, gfiQuotation.getStkId());
					ps.setLong(5, gfiQuotation.getMinQty());
					
					ps.execute();
				}
			}
			
			transMgr.commit();
			transMgr.endTransaction();
			transMgr=null;
		} catch (Exception e) {
			loggerGFIQuo.error("Error when processing GFIQuotationManager e=" + e, e);
			transMgr.cancelTransaction();
			transMgr=null;
			e.printStackTrace();
		} finally {

			if (transMgr != null)
				transMgr.cancelTransaction();
		}
	}
}
