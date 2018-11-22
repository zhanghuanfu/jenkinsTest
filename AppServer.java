/**
 * Title:        Stock Centralized Trade System --- Apllication Server
 * Description:  证券集中交易系统应用服务器
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
 * 系统启动类
 */
public class AppServer {

	public AppServer() {
	}

	public static void main(String args[]) throws Throwable {
		AppServer app = new AppServer();

		//根据输入参数确定系统需要采取的动作
		app.distributeCommand(args);

		try {
			//初始化
			app.init();

			System.out.println("version=" + Version.ID);
			ConnectionLogMan.getInstance().println("version=" + Version.ID);

			//实时加载加密提供者
			RC4Crypt.loadSecurityProvider();

			//BasicConnectionPool.setEncrypt(false); //设置为不加密

			//检测各种调试工具的状态
			DebugWriter.checkDeubugingStatus();
			//启动SQL日志线程
			DBDebugPrinter.instance().start();
			//启动连接池连接数变化监控日志线程
			ConnectionLogMan.getInstance().start();
			ConnectionLogMan.getInstance().println(
				"\n============ Start AppServer: "
					+ TradeDate.getDateTime(TradeDate.getLocalCurrentTimeMillis())
					+ " ============");

			//初始化连接池
			BasicConnectionPool bConn = BasicConnectionPool.getInstance();
			SpecificConnectionPool.getInstance();
			QueryConnectionPool.getInstance();
			System.out.println(">> 正在初始化数据库连接池");
			System.out.println("");
			int freeConnection = bConn.getFreeConnections();

			if (freeConnection <= 0) {
				System.out.println("************************************");
				System.out.println("***** 系统无法和数据库建立连接 *****");
				System.out.println("*****       系统启动失败       *****");
				System.out.println("************************************");
				System.exit(0);
			}

			//装载券商标识
			GlobalSetting.getInstance().init();

			//启动分发器
			Controller controller = new Controller();
			controller.start();

			//装载一次性Load到内存
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

			//启动后台报表临时数据删除线程
			DeleteRptDataThread delRptData = DeleteRptDataThread.getInstance();
			delRptData.start();
			System.out.println(">> 报表数据删除线程启动......");
			//启动刷新管理器
			RefreshManager refreshMgr = new RefreshManager();
			refreshMgr.go();
			System.out.println(">> 自动刷新服务已启动......");
			System.out.println();

			//行情服务启动
			try {
				 QuotationService.getInstance();
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(">> 行情服务启动出错！！");
				System.exit(0);
			}

			//启动自动下单定时器
			IntelligentOrderTimer.getInstance().activate();

			//启动自动撤单定时器
			AutoWithdrawTimer.getInstance().activate();

			//启动FIX交易委托成交信息处理定时器
			FixOrderKnockInfoDealerTimer.getInstance().activate();

			//数据转移执行器
			FixDataMoveExecutor.getInstance();

			//启动子股东实时成交分配
			DispatchManager.getInstance();

			//启动自动套利定时器
			AutoArbitrageManager.getInstance().activate();

			//启动移动平均涨跌幅行情计算线程
			MoveAveUpDownThread.getInstance().start();

			//启动报价定单定时器
			AutoPriceOrderTimer.getInstance().activate();

			//加入FIX回报处理的Adaptor chengb 2010/9/30
			ReturnProcessManager.getInstance().addReturnListener(new FixOrderReturnAdaptor());
			
			// 启动监控信息发送模块
			try {
				AuditManager auditManager = AuditManager.getInstance();
				auditManager.initial(); //初始化
				auditManager.start();
				System.out.println(">> 监控信息发送模块启动成功...\n");
			} catch (Exception e) {
				Log.log(e);
				System.out.println(">> 无法启动启动监控信息发送模块!!\n");
			}

			// 启动动消息发送处理
			try {
				MessageDispatcher msgDisp = MessageDispatcher.getInstance();
				msgDisp.initial();
				msgDisp.start();
				System.out.println(">> 消息发送模块启动成功...\n");
			} catch (Exception e) {
				Log.log(e);
				System.out.println(">> 无法启动消息发送模块!!\n");
			}
			CreditAuditManager.getInstance();

            //启动意向行情服务
            //IntentQuotationManager.getInstance();

			//启动通信层
			 CommunicationService.getInstance();
			System.out.println(">> 正在准备启动通信层.......\n");
			System.out.println(">> 应用服务器启动成功.......\n");

			//启动数据库连接池监控程序
			DBConnectionMonitor dbMonitor = new DBConnectionMonitor();
			dbMonitor.start();
			System.out.println(">> 数据库连接池监控程序已启动！！");

			//记录系统启动状态
			logSecuritiesName();
			ConnectionLogMan.getInstance().println(
				"============ Start Over: "
					+ TradeDate.getDateTime(TradeDate.getLocalCurrentTimeMillis())
					+ " ============");

			//根据配置文件启动各项服务
			ServiceLoader serviceMgr = new ServiceLoader("startup");
			serviceMgr.loadService();

		} catch (Exception e) {
			DebugWriter.println("出现异常，系统无法正常启动！");
			Log.log(e);
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void init() {
	}

	/**
	 * 根据命令行的输入参数确定系统需要采取的动作
	 * @author eyzang@croot.com
	 * @param args
	 */
	private void distributeCommand(String[] args) {
		//AppServerManager appMgr = null;
		if (args.length == 0) {
			//没有参数, 启动appserver
			return;
		} else if (args[0].equalsIgnoreCase("-help") || args[0].equalsIgnoreCase("-h")) {
			usage();
		} else if (args[0].equalsIgnoreCase("shutdown")) {
			String result = "";
			SystemCommandSender sender = new SystemCommandSender();

			if (args.length > 1 && args[1].equalsIgnoreCase("force")) {
				//强行退出
				result = sender.sendShutdownCommand(true);
			} else {
				//等待所有客户退出后,再关闭appserver
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
	 * appserver命令行用法
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
	 * 记录系统启动状态
	 */
	public static void logSecuritiesName() {
		String securitiesName = "根网";
		try {
			securitiesName = GlobalParaManager.getInstance().getPara("securitiesName");
		} catch (Exception e) {
		}
		String s = "\n" + securitiesName + "集中交易系统启动成功！\n";
		System.out.println(s);
		ConnectionLogMan.getInstance().println(s);
	}
}