/**
 * Title:        Stock Centralized Trade System --- Apllication Server
 * Description:  ֤ȯ���н���ϵͳӦ�÷�����
 * Copyright:    Copy Right (c) 2001
 * Company:      ROOTNET
 * @author       ROOTNET/CICC DEVELOPMENT GROUP
 * @version      2.0
 */
import com.stock.framework.audit.AuditManager;
import com.stock.framework.communication.CommunicationService;
import com.stock.framework.communication.message.MessageDispatcher;
import com.stock.framework.crypt.RC4Crypt;
import com.stock.framework.database.BasicConnectionPool;
import com.stock.framework.database.DBDebugPrinter;
import com.stock.framework.database.Log;
import com.stock.framework.database.QueryConnectionPool;
import com.stock.framework.database.SpecificConnectionPool;
import com.stock.framework.dispatcher.Controller;
import com.stock.framework.global.SystemStatus;
import com.stock.framework.global.Version;
import com.stock.framework.monitor.DBConnectionMonitor;
import com.stock.framework.system.ServiceLoader;
import com.stock.framework.system.SystemCommandSender;
import com.stock.framework.util.ConnectionLogMan;
import com.stock.framework.util.DebugWriter;
import com.stock.framework.util.TradeDate;
import com.stock.businesslogic.cache.RestrictedFunctionCache;
import com.stock.businesslogic.global.BankParameterManager;
import com.stock.businesslogic.global.BondParaManager;
import com.stock.businesslogic.global.CurrencyManager;
import com.stock.businesslogic.global.CustomerTypeManager;
import com.stock.businesslogic.global.DeskInfoManager;
import com.stock.businesslogic.global.DeskUseManager;
import com.stock.businesslogic.global.ExchErrorListManager;
import com.stock.businesslogic.global.ExchangeManager;
import com.stock.businesslogic.global.FeeManager;
import com.stock.businesslogic.global.FuncCtrlManager;
import com.stock.businesslogic.global.GlobalConstManager;
import com.stock.businesslogic.global.GlobalParaManager;
import com.stock.businesslogic.global.GlobalSetting;
import com.stock.businesslogic.global.HolidayProxy;
import com.stock.businesslogic.global.OfferDeskManager;
import com.stock.businesslogic.global.OperatorManager;
import com.stock.businesslogic.global.PositionManager;
import com.stock.businesslogic.global.QueryCallFuncIdManager;
import com.stock.businesslogic.global.QueryConditionManager;
import com.stock.businesslogic.global.QueryResultManager;
import com.stock.businesslogic.global.QuerySumResultManager;
import com.stock.businesslogic.global.RefreshManager;
import com.stock.businesslogic.global.RepurchaseParameterManager;
import com.stock.businesslogic.global.SettleDayManager;
import com.stock.businesslogic.global.StockManager;
import com.stock.businesslogic.marketmaker.order.AutoPriceOrderTimer;
import com.stock.businesslogic.qfii.fixorder.FixDataMoveExecutor;
import com.stock.businesslogic.qfii.fixorder.FixOrderKnockInfoDealerTimer;
import com.stock.businesslogic.qfii.fixorder.FixOrderReturnAdaptor;
import com.stock.businesslogic.qfii.order.AutoArbitrageManager;
import com.stock.businesslogic.qfii.order.AutoWithdrawTimer;
import com.stock.businesslogic.qfii.order.IntelligentOrderTimer;
import com.stock.businesslogic.qfii.subregistration.DispatchManager;
import com.stock.businesslogic.quotation.QuotationService;
import com.stock.businesslogic.quotation.realtimeupdown.MoveAveUpDownThread;
import com.stock.businesslogic.report.DeleteRptDataThread;
import com.stock.businesslogic.returnprocessing.event.ReturnProcessManager;
import com.stock.businesslogic.util.BLUtils;
import com.stock.customization.credit.audit.CreditAuditManager;

/**
 * ϵͳ������
 */
public class AppServer {

	public AppServer() {
	}

	public static void main(String args[]) throws Throwable {
		AppServer app = new AppServer();

		//�����������ȷ��ϵͳ��Ҫ��ȡ�Ķ���
		app.distributeCommand(args);

		try {
			//��ʼ��
			app.init();

			System.out.println("version=" + Version.ID);
			ConnectionLogMan.getInstance().println("version=" + Version.ID);

			//ʵʱ���ؼ����ṩ��
			RC4Crypt.loadSecurityProvider();

			//BasicConnectionPool.setEncrypt(false); //����Ϊ������

			//�����ֵ��Թ��ߵ�״̬
			DebugWriter.checkDeubugingStatus();
			//����SQL��־�߳�
			DBDebugPrinter.instance().start();
			//�������ӳ��������仯�����־�߳�
			ConnectionLogMan.getInstance().start();
			ConnectionLogMan.getInstance().println(
				"\n============ Start AppServer: "
					+ TradeDate.getDateTime(TradeDate.getLocalCurrentTimeMillis())
					+ " ============");

			//��ʼ�����ӳ�
			BasicConnectionPool bConn = BasicConnectionPool.getInstance();
			SpecificConnectionPool.getInstance();
			QueryConnectionPool.getInstance();
			System.out.println(">> ���ڳ�ʼ�����ݿ����ӳ�");
			System.out.println("");
			int freeConnection = bConn.getFreeConnections();

			if (freeConnection <= 0) {
				System.out.println("************************************");
				System.out.println("***** ϵͳ�޷������ݿ⽨������ *****");
				System.out.println("*****       ϵͳ����ʧ��       *****");
				System.out.println("************************************");
				System.exit(0);
			}

			//װ��ȯ�̱�ʶ
			GlobalSetting.getInstance().init();

			//�����ַ���
			Controller controller = new Controller();
			controller.start();

			//װ��һ����Load���ڴ�
			BondParaManager.getInstance();
			DeskInfoManager.getInstance();
			 GlobalParaManager.getInstance();
			 GlobalConstManager.getInstance();
			PositionManager.getInstance();

			OperatorManager.getInstance();
			RepurchaseParameterManager.getInstance();
			FeeManager.getInstance();
			ExchErrorListManager.getInstance();
			ExchangeManager.getInstance();

			DeskUseManager.getInstance();
			 CustomerTypeManager.getInstance();
			CurrencyManager.getInstance();
			BankParameterManager.getInstance();
			StockManager.getInstance();

			QueryCallFuncIdManager.getInstance();
			 QueryConditionManager.getInstance();
			QueryResultManager.getInstance();
			QuerySumResultManager.getInstance();
			// add by jrliu 2004-5-9
			 FuncCtrlManager.getInstance();
			HolidayProxy.getInstance();

			SettleDayManager.getInstance();
			TradeDate.loadAdjustTime();
			RC4Crypt.getInstance();
			SystemStatus.getInstance();
			//ContractNumManager.getInstance();
			OfferDeskManager.getInstance();
			//SerialNumManager.getInstance().refresh();
			RestrictedFunctionCache.getInstance().refresh();
			BLUtils.setMultipleThreadPara();

			//������̨������ʱ����ɾ���߳�
			DeleteRptDataThread delRptData = DeleteRptDataThread.getInstance();
			delRptData.start();
			System.out.println(">> ��������ɾ���߳�����......");
			//����ˢ�¹�����
			RefreshManager refreshMgr = new RefreshManager();
			refreshMgr.go();
			System.out.println(">> �Զ�ˢ�·���������......");
			System.out.println();

			//�����������
			try {
				 QuotationService.getInstance();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(">> �����������������");
				System.exit(0);
			}

			//�����Զ��µ���ʱ��
			IntelligentOrderTimer.getInstance().activate();

			//�����Զ�������ʱ��
			AutoWithdrawTimer.getInstance().activate();

			//����FIX����ί�гɽ���Ϣ����ʱ��
			FixOrderKnockInfoDealerTimer.getInstance().activate();

			//����ת��ִ����
			FixDataMoveExecutor.getInstance();

			//�����ӹɶ�ʵʱ�ɽ�����
			DispatchManager.getInstance();

			//�����Զ�������ʱ��
			AutoArbitrageManager.getInstance().activate();

			//�����ƶ�ƽ���ǵ�����������߳�
			MoveAveUpDownThread.getInstance().start();

			//�������۶�����ʱ��
			AutoPriceOrderTimer.getInstance().activate();

			//����FIX�ر������Adaptor chengb 2010/9/30
			ReturnProcessManager.getInstance().addReturnListener(new FixOrderReturnAdaptor());
			
			// ���������Ϣ����ģ��
			try {
				AuditManager auditManager = AuditManager.getInstance();
				auditManager.initial(); //��ʼ��
				auditManager.start();
				System.out.println(">> �����Ϣ����ģ�������ɹ�...\n");
			} catch (Exception e) {
				Log.log(e);
				System.out.println(">> �޷��������������Ϣ����ģ��!!\n");
			}

			// ��������Ϣ���ʹ���
			try {
				MessageDispatcher msgDisp = MessageDispatcher.getInstance();
				msgDisp.initial();
				msgDisp.start();
				System.out.println(">> ��Ϣ����ģ�������ɹ�...\n");
			} catch (Exception e) {
				Log.log(e);
				System.out.println(">> �޷�������Ϣ����ģ��!!\n");
			}
			CreditAuditManager.getInstance();

            //���������������
            //IntentQuotationManager.getInstance();

			//����ͨ�Ų�
			 CommunicationService.getInstance();
			System.out.println(">> ����׼������ͨ�Ų�.......\n");
			System.out.println(">> Ӧ�÷����������ɹ�.......\n");

			//�������ݿ����ӳؼ�س���
			DBConnectionMonitor dbMonitor = new DBConnectionMonitor();
			dbMonitor.start();
			System.out.println(">> ���ݿ����ӳؼ�س�������������");

			//��¼ϵͳ����״̬
			logSecuritiesName();
			ConnectionLogMan.getInstance().println(
				"============ Start Over: "
					+ TradeDate.getDateTime(TradeDate.getLocalCurrentTimeMillis())
					+ " ============");

			//���������ļ������������
			ServiceLoader serviceMgr = new ServiceLoader("startup");
			serviceMgr.loadService();

		} catch (Exception e) {
			DebugWriter.println("�����쳣��ϵͳ�޷�����������");
			Log.log(e);
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void init() {
	}

	/**
	 * ���������е��������ȷ��ϵͳ��Ҫ��ȡ�Ķ���
	 * @author eyzang@croot.com
	 * @param args
	 */
	private void distributeCommand(String[] args) {
		//AppServerManager appMgr = null;
		if (args.length == 0) {
			//û�в���, ����appserver
			return;
		} else if (args[0].equalsIgnoreCase("-help") || args[0].equalsIgnoreCase("-h")) {
			usage();
		} else if (args[0].equalsIgnoreCase("shutdown")) {
			String result = "";
			SystemCommandSender sender = new SystemCommandSender();

			if (args.length > 1 && args[1].equalsIgnoreCase("force")) {
				//ǿ���˳�
				result = sender.sendShutdownCommand(true);
			} else {
				//�ȴ����пͻ��˳���,�ٹر�appserver
				result = sender.sendShutdownCommand(false);
			}
			System.out.println(">> " + result);
			System.exit(0);
		} else if (args[0].equalsIgnoreCase("version")) {
			System.out.println("\nversion=" + Version.ID);
			System.exit(0);
		} else {
			usage();
		}
	}

	/**
	 * appserver�������÷�
	 */
	private void usage() {
		System.out.println("\nUsage: java AppServer [shutdown] [force|synch] [version]");
		System.out.println("Example:");
		System.out.println("\tjava AppServer shutdown force");
		System.out.println("\tjava AppServer shutdown");
		System.out.println("\tjava AppServer version");
		System.exit(0);
	}

	/**
	 * ��¼ϵͳ����״̬
	 */
	public static void logSecuritiesName() {
		String securitiesName = "����";
		try {
			securitiesName = GlobalParaManager.getInstance().getPara("securitiesName");
		} catch (Exception e) {
		}
		String s = "\n" + securitiesName + "���н���ϵͳ�����ɹ���\n";
		System.out.println(s);
		ConnectionLogMan.getInstance().println(s);
	}
}