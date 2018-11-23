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
 * ͬ�����Ϲ��ܵķַ���
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

			//���ϵͳ״̬�Ƿ�����
			//SystemStatusChecker.checkSystemStatus();
			if (functionId.equals(FunctionIdConf.Credit_QueryNeedSynchronized)) {
				//03200005 ��ѯ��Ҫͬ������Ϣ
				return new QueryNeedSynchronized(datapack).execute();
			} else if (functionId.equals(FunctionIdConf.Credit_QueryRequeryInfo)) {
				//03200010 ��ȡͬ��������Ϣ
				return new QueryRequeryInfo(datapack).execute();

			} else if (functionId.equals(FunctionIdConf.Credit_QueryAnswerInfo)) {
				//03200015 ��ȡӦ����Ϣ
				return new QueryAnswerInfo(datapack).execute();

			} else if (functionId.equals(FunctionIdConf.Credit_QueryAcctAttribute)) {
				//03200020��ȡ�ʻ�������Ϣ����
				return new SynchronizeService(datapack).queryAcctAttribute();

			} else if (functionId.equals(FunctionIdConf.Credit_QueryAnswerCustInfo)) {
				//03200025 ��ȡ��̨Ӧ��Ŀͻ���Ϣ
				return new QueryAnswerCustInfo(datapack).execute();

			} else if (functionId.equals(FunctionIdConf.Credit_QueryAnswerRegInfo)) {
				//03200030 ��ȡ��̨Ӧ��Ĺɶ���Ϣ
				return new QueryAnswerRegInfo(datapack).execute();

			} else if (functionId.equals(FunctionIdConf.Credit_QueryMarkToMarketAuditing)) {
				//03200031���ն��м�ز�ѯ 
				return new QueryMarkToMarketAuditing(datapack).execute();

			} else if (functionId.equals(FunctionIdConf.Credit_QueryCreditAmtAuditing)) {
				//03200032�ʻ����ö��ʹ�ü�ز�ѯ
				return new QueryCreditAmtAuditing(datapack).execute();

			} else if (
				functionId.equals(FunctionIdConf.Credit_QueryCreditTotalAndSpecAcctFundAuditing)) {
				//03200033�����ܶ��ʹ�ú�����ר���ʽ�ʹ�������ز�ѯ
				return new QueryCreditTotalAndSpecAcctFundAuditing(datapack).execute();

			} else if (functionId.equals(FunctionIdConf.Credit_QueryCreditForceCloseAuditing)) {
				//03200034ǿ��ƽ��ִ�м�ز�ѯ
				return new QueryCreditForceCloseAuditing(datapack).execute();

			} else if (functionId.equals(FunctionIdConf.Credit_QueryCreditCloseAuditing)) {
				//03200035���ú�Լ���ڼ�ز�ѯ
				return new QueryCreditCloseAuditing(datapack).execute();

			} else if (functionId.equals(FunctionIdConf.Credit_QueryCreditTradingAuditing)) {
				//03200036���ÿͻ�����������
				return new QueryCreditTradingAuditing(datapack).execute();

			} else if (functionId.equals(FunctionIdConf.Credit_SynchronizedAccountInfo)) {
				//03200040 ͬ���ʽ���Ϣ
				return new SynchronizeService(datapack).synchronizedAccountInfo();

			} else if (functionId.equals(FunctionIdConf.Credit_SynchronizedCustInfo)) {
				//03200041 ͬ���ͻ�����
				return new SynchronizeService(datapack).synchronizedCustInfo();

			} else if (functionId.equals(FunctionIdConf.Credit_SynchronizedRegInfo)) {
				//03200042 ͬ���ɶ���Ϣ
				return new SynchronizeService(datapack).synchronizedRegInfo();

			} else if (functionId.equals(FunctionIdConf.Credit_SynchronizedStklistInfo)) {
				//03200045 ͬ���ɷ���Ϣ
				return new SynchronizeService(datapack).synchronizedStklistInfo();

			} else if (functionId.equals(FunctionIdConf.Credit_FinishedSynchroniz)) {
				//03200050 �ʻ����ͬ��
				return new SynchronizeService(datapack).finishedSynchroniz();

			} else if (functionId.equals(FunctionIdConf.Credit_CustInfoRequeryAnswer)) {
				//03200055 �ͻ���Ϣ��ѯӦ��
				return new SynchronizeService(datapack).custInfoRequeryAnswer();

			} else if (functionId.equals(FunctionIdConf.Credit_RegInfoRequeryAnswer)) {
				//03200060 �ɶ���Ϣ��ѯӦ��
				return new SynchronizeService(datapack).regInfoRequeryAnswer();

			} else if (functionId.equals(FunctionIdConf.Credit_CustSpecialInfoModifyAnswer)) {
				//03200065  �ͻ��ض������޸�
				return new SynchronizeService(datapack).custSpecialInfoModifyAnswer();

			} else if (functionId.equals(FunctionIdConf.Credit_RequestCounterModifyPrivilege)) {
				//03200070  ���̨�����޸Ĺɶ�����Ȩ��
				return new SynchronizeService(datapack).requestCounterModifyPrivilege();

			} else if (functionId.equals(FunctionIdConf.Credit_RequestCounterFreeze)) {
				//03200075  ���̨ϵͳ���ɷݺ��ʽ𶳽�������
				return new SynchronizeService(datapack).requestCounterFreeze();

			} else if (functionId.equals(FunctionIdConf.Credit_RequestCounterCustInfo)) {
				//03200080  ���̨ϵͳ�����ȡ�ͻ���ɶ���Ϣ
				return new SynchronizeService(datapack).requestCounterCustInfo();

			} else if (functionId.equals(FunctionIdConf.Credit_RequestCounterModifySpecInfo)) {
				//03200085  ���̨ϵͳ���ͻ��ض������޸�������
				return new SynchronizeService(datapack).requestCounterModifySpecInfo();

			} else if (functionId.equals(FunctionIdConf.Credit_RequestCounterAcctAndStk)) {
				//03200090  �����̨ϵͳͬ���ͻ����ʽ�͹ɷ���Ϣ����
				return new SynchronizeService(datapack).requestCounterAcctAndStk();

			} else if (functionId.equals(FunctionIdConf.Credit_QueryCreditMonitorPara)) {
				//03200100  ��ȡ������ȯ��ز���
				return new CreditMonitorParaManager(datapack).queryMonitorPara();

			} else if (functionId.equals(FunctionIdConf.Credit_ModiffyCreditMonitorPara)) {
				//03200105  �޸�������ȯ��ز���
				return new CreditMonitorParaManager(datapack).modifyMonitorPara();

			} else if (functionId.equals(FunctionIdConf.Credit_GetCreditReferenceAmt)) {
				//03200110  �����ʻ��ο����ʶ�ȹ���
				return new SynchronizeService(datapack).getCreditReferenceAmt();

			} else if (functionId.equals(FunctionIdConf.Credit_AwokeCreditOvertop)) {
				//03200115  ������ó�������
				return new SynchronizeService(datapack).awokeCreditOvertop();

			} else {
				//�޴˹��ܺ�
				throw new BLException("06010001");
			}
		} catch (Throwable e) {
			ExceptionTreatment.transactErr(datapack, e);
		}
		return datapack;
	}

	/**
	 * ����ԱȨ��
	 * @return
	 */
	private void checkOperator() throws BLException {
		//ȡGUI�˷���������Ϣ
		String optId = datapack.getStringValue((short) 1, "optId");
		String optMode = datapack.getStringValue((short) 1, "optMode");
		String optPwd = datapack.getStringValue((short) 1, "optPwd");
		//String MAC = datapack.getComputerMAC();

		//��֤����
		if (optId == null || optPwd == null || optMode == null) {
			throw new BLException("06010000");
		}

		//��֤��Ա���
		OperatorManager optManager = OperatorManager.getInstance();
		//Operator opt = 
				optManager.getOperator(optId, true);
	}

}
