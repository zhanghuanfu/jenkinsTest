package com.stock.customization.credit.controller;

import com.stock.businesslogic.global.OperatorManager;
import com.stock.businesslogic.util.ExceptionTreatment;
import com.stock.customization.credit.dealsynchronize.SynchronizeService;
import com.stock.customization.credit.paraadmin.CreditMonitorParaManager;
import com.stock.customization.credit.query.QueryAnswerCustInfo;
import com.stock.customization.credit.query.QueryAnswerInfo;
import com.stock.customization.credit.query.QueryAnswerRegInfo;
import com.stock.customization.credit.query.QueryCreditAmtAuditing;
import com.stock.customization.credit.query.QueryCreditCloseAuditing;
import com.stock.customization.credit.query.QueryCreditForceCloseAuditing;
import com.stock.customization.credit.query.QueryCreditTotalAndSpecAcctFundAuditing;
import com.stock.customization.credit.query.QueryCreditTradingAuditing;
import com.stock.customization.credit.query.QueryMarkToMarketAuditing;
import com.stock.customization.credit.query.QueryNeedSynchronized;
import com.stock.customization.credit.query.QueryRequeryInfo;
import com.stock.framework.communication.DataPack;
import com.stock.framework.global.BLException;
import com.stock.framework.global.FunctionIdConf;
import com.stock.framework.util.DebugWriter;

/**
 * 同步资料功能的分发器
 * @author lilijuan@croot.com
 */
public class SynchronizeController {
	private DataPack datapack = null;

	public SynchronizeController(DataPack datapack) {
		this.datapack = datapack;
	}

	public DataPack makeService() throws Exception {
		try {
			checkOperator();

			String functionId = datapack.getFunctionCode().trim();
			DebugWriter.println("functionId=" + functionId);

			//检查系统状态是否正常
			//SystemStatusChecker.checkSystemStatus();
			if (functionId.equals(FunctionIdConf.Credit_QueryNeedSynchronized)) {
				//03200005 查询需要同步的信息
				return new QueryNeedSynchronized(datapack).execute();
			} else if (functionId.equals(FunctionIdConf.Credit_QueryRequeryInfo)) {
				//03200010 获取同步请求信息
				return new QueryRequeryInfo(datapack).execute();

			} else if (functionId.equals(FunctionIdConf.Credit_QueryAnswerInfo)) {
				//03200015 获取应答信息
				return new QueryAnswerInfo(datapack).execute();

			} else if (functionId.equals(FunctionIdConf.Credit_QueryAcctAttribute)) {
				//03200020获取帐户属性信息功能
				return new SynchronizeService(datapack).queryAcctAttribute();

			} else if (functionId.equals(FunctionIdConf.Credit_QueryAnswerCustInfo)) {
				//03200025 获取柜台应答的客户信息
				return new QueryAnswerCustInfo(datapack).execute();

			} else if (functionId.equals(FunctionIdConf.Credit_QueryAnswerRegInfo)) {
				//03200030 获取柜台应答的股东信息
				return new QueryAnswerRegInfo(datapack).execute();

			} else if (functionId.equals(FunctionIdConf.Credit_QueryMarkToMarketAuditing)) {
				//03200031逐日盯市监控查询 
				return new QueryMarkToMarketAuditing(datapack).execute();

			} else if (functionId.equals(FunctionIdConf.Credit_QueryCreditAmtAuditing)) {
				//03200032帐户信用额度使用监控查询
				return new QueryCreditAmtAuditing(datapack).execute();

			} else if (
				functionId.equals(FunctionIdConf.Credit_QueryCreditTotalAndSpecAcctFundAuditing)) {
				//03200033信用总额度使用和融资专户资金使用情况监控查询
				return new QueryCreditTotalAndSpecAcctFundAuditing(datapack).execute();

			} else if (functionId.equals(FunctionIdConf.Credit_QueryCreditForceCloseAuditing)) {
				//03200034强制平仓执行监控查询
				return new QueryCreditForceCloseAuditing(datapack).execute();

			} else if (functionId.equals(FunctionIdConf.Credit_QueryCreditCloseAuditing)) {
				//03200035信用合约到期监控查询
				return new QueryCreditCloseAuditing(datapack).execute();

			} else if (functionId.equals(FunctionIdConf.Credit_QueryCreditTradingAuditing)) {
				//03200036信用客户交易情况监控
				return new QueryCreditTradingAuditing(datapack).execute();

			} else if (functionId.equals(FunctionIdConf.Credit_SynchronizedAccountInfo)) {
				//03200040 同步资金信息
				return new SynchronizeService(datapack).synchronizedAccountInfo();

			} else if (functionId.equals(FunctionIdConf.Credit_SynchronizedCustInfo)) {
				//03200041 同步客户资料
				return new SynchronizeService(datapack).synchronizedCustInfo();

			} else if (functionId.equals(FunctionIdConf.Credit_SynchronizedRegInfo)) {
				//03200042 同步股东信息
				return new SynchronizeService(datapack).synchronizedRegInfo();

			} else if (functionId.equals(FunctionIdConf.Credit_SynchronizedStklistInfo)) {
				//03200045 同步股份信息
				return new SynchronizeService(datapack).synchronizedStklistInfo();

			} else if (functionId.equals(FunctionIdConf.Credit_FinishedSynchroniz)) {
				//03200050 帐户完成同步
				return new SynchronizeService(datapack).finishedSynchroniz();

			} else if (functionId.equals(FunctionIdConf.Credit_CustInfoRequeryAnswer)) {
				//03200055 客户信息查询应答
				return new SynchronizeService(datapack).custInfoRequeryAnswer();

			} else if (functionId.equals(FunctionIdConf.Credit_RegInfoRequeryAnswer)) {
				//03200060 股东信息查询应答
				return new SynchronizeService(datapack).regInfoRequeryAnswer();

			} else if (functionId.equals(FunctionIdConf.Credit_CustSpecialInfoModifyAnswer)) {
				//03200065  客户特定资料修改
				return new SynchronizeService(datapack).custSpecialInfoModifyAnswer();

			} else if (functionId.equals(FunctionIdConf.Credit_RequestCounterModifyPrivilege)) {
				//03200070  向柜台请求修改股东操作权限
				return new SynchronizeService(datapack).requestCounterModifyPrivilege();

			} else if (functionId.equals(FunctionIdConf.Credit_RequestCounterFreeze)) {
				//03200075  向柜台系统发股份和资金冻结请求功能
				return new SynchronizeService(datapack).requestCounterFreeze();

			} else if (functionId.equals(FunctionIdConf.Credit_RequestCounterCustInfo)) {
				//03200080  向柜台系统请求获取客户或股东信息
				return new SynchronizeService(datapack).requestCounterCustInfo();

			} else if (functionId.equals(FunctionIdConf.Credit_RequestCounterModifySpecInfo)) {
				//03200085  向柜台系统发客户特定资料修改请求功能
				return new SynchronizeService(datapack).requestCounterModifySpecInfo();

			} else if (functionId.equals(FunctionIdConf.Credit_RequestCounterAcctAndStk)) {
				//03200090  请求柜台系统同步客户的资金和股份信息功能
				return new SynchronizeService(datapack).requestCounterAcctAndStk();

			} else if (functionId.equals(FunctionIdConf.Credit_QueryCreditMonitorPara)) {
				//03200100  获取融资融券监控参数
				return new CreditMonitorParaManager(datapack).queryMonitorPara();

			} else if (functionId.equals(FunctionIdConf.Credit_ModiffyCreditMonitorPara)) {
				//03200105  修改融资融券监控参数
				return new CreditMonitorParaManager(datapack).modifyMonitorPara();

			} else if (functionId.equals(FunctionIdConf.Credit_GetCreditReferenceAmt)) {
				//03200110  信用帐户参考融资额度功能
				return new SynchronizeService(datapack).getCreditReferenceAmt();

			} else if (functionId.equals(FunctionIdConf.Credit_AwokeCreditOvertop)) {
				//03200115  额度设置超限提醒
				return new SynchronizeService(datapack).awokeCreditOvertop();

			} else {
				//无此功能号
				throw new BLException("06010001");
			}
		} catch (Throwable e) {
			ExceptionTreatment.transactErr(datapack, e);
		}
		return datapack;
	}

	/**
	 * 检查柜员权限
	 * @return
	 */
	private void checkOperator() throws BLException {
		//取GUI端发过来的信息
		String optId = datapack.getStringValue((short) 1, "optId");
		String optMode = datapack.getStringValue((short) 1, "optMode");
		String optPwd = datapack.getStringValue((short) 1, "optPwd");
		//String MAC = datapack.getComputerMAC();

		//验证参数
		if (optId == null || optPwd == null || optMode == null) {
			throw new BLException("06010000");
		}

		//验证柜员身份
		OperatorManager optManager = OperatorManager.getInstance();
		//Operator opt = 
				optManager.getOperator(optId, true);
	}

}
