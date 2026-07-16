package com.auruspay.dto;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class TransactionContext {

	private String did;
    private String app;
    private String auth;
    private String clientRef;
	private String pymtType;
	private String reversalInd;
	private String txnType;
	private String localDateTime;
	private String trnmsnDateTime;
	private String stan;
	private String refNum;
	private String orderNum;
	private String tppid;
	private String termID;
	private String merchID;
	private String merchCatCode;

	private String posEntryMode;
	private String posCondCode;
	private String termCatCode;
	private String termEntryCapablt;
	private String txnAmt;
	private String txnCrncy;
	private String termLocInd;
	private String cardCaptCap;
	private String groupID;
	private String posID;
	private String settleInd;
	private String clerkID;
	private String seNum;
	private String plposDebitFlg;

	private String netAccInd;
	private String merchEcho;
	private String wltID;
	private String nonUSMerch;
	private String devBatchID;
	private String digWltInd;
	private String digWltProgType;
	private String tranInit;
	private String pymntSvc;
	private String merchCustom1;
	private String refundType;
	private String rtFallbackInd;
	private String dfrdAuthInd;
	private String busPymtType;

	private String termClassCode;
	private String merchCtryOfOrigin;
	private String planRegSysId;
	private String agreementID;
	private String delChargeInd;
	private String noShowInd;
	private String rateTableID;
	private String xchgRate;
	private String prstFXEligInd;
	private String cnvDate;
	private String merchPymtGtwyID;
	private String digComProgType;
	private String digComChkInEntTypeNm;

	private String digComChkInMthd;
	private String altNetTranID;
	private String enablerVerVal;
	private String acctTypeID;
	private String digComDomainID;
	private String mcrInd;
	private String txnLinkID;
	private String extdAuthInd;

	// BillPayGrp
	private String billPymtTxnInd;
	private String merchAdviceCode;
	private String installInvNum;
	private String installPymntDesc;
	private String installmentType;

	// MIT Group
	private String mitTotPymtCt;
	private String mitAmtType;
	private String mitAmt;
	private String mitUniqueID;
	private String mitFreq;
	private String mitValFlag;
	private String mitValRef;
	private String mitSeqInd;
	private String mitPymtCrncy;
	private String mitTotPymtAmt;
	private String mitPymtType;
	private String mitRegRefNum;

	// AltMerchNameAndAddrGrp
	private String merchName;
	private String merchAddr;
	private String merchCity;
	private String merchState;
	private String merchCnty;
	private String merchPostalCode;
	private String merchCtry;
	private String merchEmail;
	private String merchSvcLocCity;
	private String merchSvcLocState;
	private String merchSvcLocPostalCode;
	private String merchSvcLocCtry;

	// CardGrp
	private String acctNum;
	private String cardExpiryDate;
	private String track1Data;
	private String track2Data;
	private String cardType;
	private String avsResultCode;
	private String ccvInd;
	private String ccvData;
	private String ccvResultCode;
	private String mvvmAID;
	private String infoReqInd;
	private String fndAcctNum;
	private String pymtAcctRefReqInd;
	private String pymtAcctRef;
	private String almSvcCode;
	private String almProdCode;
	private String almProdClass;
	private String almRateType;

	// RealTimeAcctUpdGrp
	private String acctUpdReqInd;
	private String acctUpdCardStat;
	private String acctUpdCardNum;
	private String acctUpdExpDate;
	private String acctUpdResultCode;

	// AuthOptGrp
	private String dfrdAuthTranID;
	private String overrideInd;
	private String authOptReasonCode;

	// InstallPaySvcGrp
	private String installType;
	private String installPymtOptions;
	private String noOfInstall;
	private String installIntRate;
	private String installFee;
	private String installAPR;
	private String firstInstallAmt;
	private String subInstallAmt;
	private String totInstallAmtDue;

	// PINGrp
	private String pinData;
	private String keySerialNumData;
	private String keyOffset;
	private String msKeyID;

	// AddtlAmtGrp
	private String addAmt;
	private String addAmtCrncy;
	private String addAmtType;
	private String addAmtAcctType;

	private String partAuthrztnApprvlCapablt;
	private String balRetCapablt;

	// EcommGrp
	private String ecommTxnInd;
	private String custSvcPhoneNumber;
	private String ecommURL;
	private String mcsn;
	private String mcsc;
	private String motoIndicator;

	// SecrTxnGrp
	private String visaXID;
	private String visaSecrTxnAD;
	private String cavvResultCode;
	private String amexXID;
	private String amexSecrAD;
	private String safekey;
	private String ucafCollectInd;
	private String mcSecrAD;
	private String discAuthType;
	private String discSecData;
	private String secDataDowngrade;
	private String tknAVD;
	private String tavvResultCode;
	private String programProtocol;
	private String dirServerTransID;

	private String lowValExInd;
	private String tranRiskAnaExInd;
	private String trustMerchExInd;
	private String secrCorpExInd;
	private String delegAuthInd;
	private String merchantAuthentID;
	private String recPayExInd;
	private String exReasonCode;
	private String rmtCommAcptrId;
	private String authOutExInd;
	private String dafInd;
	private String secrXID;
	private String secrTxnAD;
	private String authenDataQltInd;

	// VisaGrp
	private String aci;
	private String mrktSpecificDataInd;
	private String existingDebtInd;
	private String cardLevelResult;
	private String sourceReasonCode;
	private String transID;
	private String visaBID;
	private String visaAUAR;
	private String taxAmtCapablt;

	private String spendQInd;
	private String checkoutInd;
	private String qci;
	private String visaAuthInd;
	private String storedCredInd;
	private String cofSchedInd;
	private String cryptoCrncyPurchInd;
	private String progDgReasonCode;
	private String acctFundingSrc;
	private String appProdPlatCode;
	private String appCHIDMethod;

	// MCGrp
	private String banknetData;
	private String mcmsdi;
	private String ccvErrorCode;
	private String posEntryModeChg;
	private String tranEditErrCode;
	private String mcposData;
	private String devTypeInd;
	private String mcaci;
	private String mcAddData;
	private String finAuthInd;

	private String tranIntgClass;
	private String mcAuthInd;
	private String storedCredenInd;
	private String crypCrncyPurchInd;
	private String highRiskSecrPurchInd;
	private String cofSchdInd;
	private String citmitFrameInd;

	// DSGrp
	private String discProcCode;
	private String discPOSEntry;
	private String discRespCode;
	private String discPOSData;
	// DSGrp (continued)
	private String discTransQualifier;
	private String discNRID;
	private String motoInd;
	private String regUserInd;
	private String regUserDate;
	private String discAuthInd;
	private String partShipInd;
	private String discACI;
	private String storedCrdInd;
	private String discSTAN;
	private String cofSchInd;
	private String nridReqInd;
	private String discDebtInd;
	private String discCryptoCrncyInd;

	// AmexGrp
	private String amExPOSData;
	private String amExTranID;
	private String gdSoldCd;
	private String reAuthInd;
	private String amexAuthInd;
	private String storedCrdIndAmex;
	private String amexACI;

	// PurchCardLvl2Grp
	private String taxAmt;
	private String taxInd;
	private String vatTaxAmt;
	// PurchCardLvl2Grp (continued)
	private String vatTaxRt;
	private String purchIdfr;
	private String pcOrderNum;
	private String discntAmt;
	private String frghtAmt;
	private String dutyAmt;
	private String destPostalCode;
	private String shipFromPostalCode;
	private String destCtryCode;
	private String merchTaxID;
	private String prodDesc;
	private String pc3Add;

	// PurchCardLvl3Grp
	private String l3ItemSeqNum;
	private String l3ItemCode;
	private String l3ItemDesc;
	private String l3Qty;
	private String l3UnitOfMsure;
	private String l3UnitCost;
	private String l3ItemTot;
	private String l3DiscntAmt;
	private String l3TaxAmt;
	private String l3TaxRt;

	// CustInfoGrp
	private String avsBillingAddr;
	private String avsBillingPostalCode;
	private String chFirstNm;
	// CustInfoGrp (continued)
	private String chLastNm;
	private String chFullNmRes;
	private String custEmailAddr;
	private String chMidNm;
	private String fullNmAcctMtchDec;
	private String lastNmAcctMtchDec;
	private String midNmAcctMtchDec;
	private String firstNmAcctMtchDec;
	private String chPhNumRes;
	private String chEmailAddrRes;
	private String customerName;
	private String custInfoEnhdRes;

	// DebtRepayGrp
	private String rcptLastNm;
	private String rcptPostalCode;
	private String rcptDateOfBirth;
	private String rcptAcctNum;

	// OrderGrp
	private String orderDate;

	// RespGrp
	private String respCode;
	private String authID;
	private String responseDate;
	private String addtlRespData;
	private String sttlmDate;
	private String athNtwkID;
	private String athNtwkNm;
	private String rtInd;
	private String sigInd;
	private String errorData;
	private String debitTraceNum;
	private String settlementTxnType;
	private String assocRespCode;

	private String categoryCode;
	private String apprProbabInd;
	private String reasonCode;
	private String maxProcDate;

	// CardInfoRespGrp
	private String issBank;
	private String issCtryCode;
	private String cardBrnd;
	private String cardInd;
	private String detProdID;

	// OrigAuthGrp
	private String origAuthID;
	private String origLocalDateTime;
	private String origTranDateTime;
	private String origSTAN;
	private String origRespCode;
	private String origAthNtwkID;

	// ProdCodeGrp
	private String servLvl;
	private String numOfProds;

	// ProdCodeDetGrp
	private String nacsProdCode;
	private String unitOfMsure;
	private String qnty;
	private String unitPrice;
	private String prodAmt;

	// FileDLGrp
	private String fileType;

	// TransitGrp
	private String traTranTypeInd;
	private String tptModeInd;
	private String atcUpdInd;
	private String traAcsTermInd;
	private String traAcsTermFunCode;
	private String exPayTransIPAN;
	// LodgingGrp
	private String folioNum;
	private String roomNum;
	private String lodRefNum;
	private String roomRt;
	private String programInd;
	private String duration;
	private String extraChrgs;

	// AutoRentalGrp
	private String rentalCity;
	private String rentalState;
	private String rentalCtry;
	private String rentalDate;
	private String rentalTime;

	private String returnCity;
	private String returnState;
	private String returnCtry;
	private String returnDate;
	private String returnTime;
	private String amtExtraChrgs;
	private String renterName;
	private String autoAgreeNum;
	private String rentalDuration;
	private String rentalExtraChrgs;
	private String autoNoShow;
	private String delChrgInd;

	// TAGrp
	
	private String keyID;
	private String encrptBlock;
	private String tknType;
	private String tkn;
	private String sctyKeyUpdInd;
	private String taSctyKey;
	private String taExpDate;
	private String caKeyID;
	
	// PINGrp
	private String numPINDigits;

	// EMVGrp
	private String emvData;
	private String cardSeqNum;
	private String xCodeResp;
	private String servCode;
	private String appExpDate;
	private String carc;
	private String procInd;
	private String procInfo;
	private String finAmtInd;

	// TAGrp
	private String sctyLvl;
	private String encrptType;
	private String encrptTrgt;
	  private PurchCardlvl2Grp purchCardlvl2Grp;


	    private List<PurchCardlvl3Grp> purchCardlvl3Grp;



	    public PurchCardlvl2Grp getPurchCardlvl2Grp() {
	        return purchCardlvl2Grp;
	    }


	    public List<PurchCardlvl3Grp> getPurchCardlvl3Grp() {
	        return purchCardlvl3Grp;
	    }
	
	
	

	public String getDid() {
		return did;
	}

	public void setDid(String did) {
		this.did = did;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public String getAuth() {
		return auth;
	}

	public void setAuth(String auth) {
		this.auth = auth;
	}

	public String getClientRef() {
		return clientRef;
	}

	public void setClientRef(String clientRef) {
		this.clientRef = clientRef;
	}

	public String getNumPINDigits() {
		return numPINDigits;
	}

	public void setNumPINDigits(String numPINDigits) {
		this.numPINDigits = numPINDigits;
	}

	public String getEmvData() {
		return emvData;
	}

	public void setEmvData(String emvData) {
		this.emvData = emvData;
	}

	public String getCardSeqNum() {
		return cardSeqNum;
	}

	public void setCardSeqNum(String cardSeqNum) {
		this.cardSeqNum = cardSeqNum;
	}

	public String getxCodeResp() {
		return xCodeResp;
	}

	public void setxCodeResp(String xCodeResp) {
		this.xCodeResp = xCodeResp;
	}

	public String getServCode() {
		return servCode;
	}

	public void setServCode(String servCode) {
		this.servCode = servCode;
	}

	public String getAppExpDate() {
		return appExpDate;
	}

	public void setAppExpDate(String appExpDate) {
		this.appExpDate = appExpDate;
	}

	public String getCarc() {
		return carc;
	}

	public void setCarc(String carc) {
		this.carc = carc;
	}

	public String getProcInd() {
		return procInd;
	}

	public void setProcInd(String procInd) {
		this.procInd = procInd;
	}

	public String getProcInfo() {
		return procInfo;
	}

	public void setProcInfo(String procInfo) {
		this.procInfo = procInfo;
	}

	public String getFinAmtInd() {
		return finAmtInd;
	}

	public void setFinAmtInd(String finAmtInd) {
		this.finAmtInd = finAmtInd;
	}

	public String getSctyLvl() {
		return sctyLvl;
	}

	public void setSctyLvl(String sctyLvl) {
		this.sctyLvl = sctyLvl;
	}

	public String getEncrptType() {
		return encrptType;
	}

	public void setEncrptType(String encrptType) {
		this.encrptType = encrptType;
	}

	public String getEncrptTrgt() {
		return encrptTrgt;
	}

	public void setEncrptTrgt(String encrptTrgt) {
		this.encrptTrgt = encrptTrgt;
	}

	public String getKeyID() {
		return keyID;
	}

	public void setKeyID(String keyID) {
		this.keyID = keyID;
	}

	public String getEncrptBlock() {
		return encrptBlock;
	}

	public void setEncrptBlock(String encrptBlock) {
		this.encrptBlock = encrptBlock;
	}

	public String getTknType() {
		return tknType;
	}

	public void setTknType(String tknType) {
		this.tknType = tknType;
	}

	public String getTkn() {
		return tkn;
	}

	public void setTkn(String tkn) {
		this.tkn = tkn;
	}

	public String getSctyKeyUpdInd() {
		return sctyKeyUpdInd;
	}

	public void setSctyKeyUpdInd(String sctyKeyUpdInd) {
		this.sctyKeyUpdInd = sctyKeyUpdInd;
	}

	public String getTaSctyKey() {
		return taSctyKey;
	}

	public void setTaSctyKey(String taSctyKey) {
		this.taSctyKey = taSctyKey;
	}

	public String getTaExpDate() {
		return taExpDate;
	}

	public void setTaExpDate(String taExpDate) {
		this.taExpDate = taExpDate;
	}

	public String getCaKeyID() {
		return caKeyID;
	}

	public void setCaKeyID(String caKeyID) {
		this.caKeyID = caKeyID;
	}

	public String getPymtType() {
		return pymtType;
	}

	public void setPymtType(String pymtType) {
		this.pymtType = pymtType;
	}

	public String getReversalInd() {
		return reversalInd;
	}

	public void setReversalInd(String reversalInd) {
		this.reversalInd = reversalInd;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public String getLocalDateTime() {
		return localDateTime;
	}

	public void setLocalDateTime(String localDateTime) {
		this.localDateTime = localDateTime;
	}

	public String getTrnmsnDateTime() {
		return trnmsnDateTime;
	}

	public void setTrnmsnDateTime(String trnmsnDateTime) {
		this.trnmsnDateTime = trnmsnDateTime;
	}

	public String getStan() {
		return stan;
	}

	public void setStan(String stan) {
		this.stan = stan;
	}

	public String getRefNum() {
		return refNum;
	}

	public void setRefNum(String refNum) {
		this.refNum = refNum;
	}

	public String getOrderNum() {
		return orderNum;
	}

	public void setOrderNum(String orderNum) {
		this.orderNum = orderNum;
	}

	public String getTppid() {
		return tppid;
	}

	public void setTppid(String tppid) {
		this.tppid = tppid;
	}

	public String getTermID() {
		return termID;
	}

	public void setTermID(String termID) {
		this.termID = termID;
	}

	public String getMerchID() {
		return merchID;
	}

	public void setMerchID(String merchID) {
		this.merchID = merchID;
	}

	public String getMerchCatCode() {
		return merchCatCode;
	}

	public void setMerchCatCode(String merchCatCode) {
		this.merchCatCode = merchCatCode;
	}

	public String getPosEntryMode() {
		return posEntryMode;
	}

	public void setPosEntryMode(String posEntryMode) {
		this.posEntryMode = posEntryMode;
	}

	public String getPosCondCode() {
		return posCondCode;
	}

	public void setPosCondCode(String posCondCode) {
		this.posCondCode = posCondCode;
	}

	public String getTermCatCode() {
		return termCatCode;
	}

	public void setTermCatCode(String termCatCode) {
		this.termCatCode = termCatCode;
	}

	public String getTermEntryCapablt() {
		return termEntryCapablt;
	}

	public void setTermEntryCapablt(String termEntryCapablt) {
		this.termEntryCapablt = termEntryCapablt;
	}

	public String getTxnAmt() {
		return txnAmt;
	}

	public void setTxnAmt(String txnAmt) {
		this.txnAmt = txnAmt;
	}

	public String getTxnCrncy() {
		return txnCrncy;
	}

	public void setTxnCrncy(String txnCrncy) {
		this.txnCrncy = txnCrncy;
	}

	public String getTermLocInd() {
		return termLocInd;
	}

	public void setTermLocInd(String termLocInd) {
		this.termLocInd = termLocInd;
	}

	public String getCardCaptCap() {
		return cardCaptCap;
	}

	public void setCardCaptCap(String cardCaptCap) {
		this.cardCaptCap = cardCaptCap;
	}

	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	public String getPosID() {
		return posID;
	}

	public void setPosID(String posID) {
		this.posID = posID;
	}

	public String getSettleInd() {
		return settleInd;
	}

	public void setSettleInd(String settleInd) {
		this.settleInd = settleInd;
	}

	public String getClerkID() {
		return clerkID;
	}

	public void setClerkID(String clerkID) {
		this.clerkID = clerkID;
	}

	public String getSeNum() {
		return seNum;
	}

	public void setSeNum(String seNum) {
		this.seNum = seNum;
	}

	public String getPlposDebitFlg() {
		return plposDebitFlg;
	}

	public void setPlposDebitFlg(String plposDebitFlg) {
		this.plposDebitFlg = plposDebitFlg;
	}

	public String getNetAccInd() {
		return netAccInd;
	}

	public void setNetAccInd(String netAccInd) {
		this.netAccInd = netAccInd;
	}

	public String getMerchEcho() {
		return merchEcho;
	}

	public void setMerchEcho(String merchEcho) {
		this.merchEcho = merchEcho;
	}

	public String getWltID() {
		return wltID;
	}

	public void setWltID(String wltID) {
		this.wltID = wltID;
	}

	public String getNonUSMerch() {
		return nonUSMerch;
	}

	public void setNonUSMerch(String nonUSMerch) {
		this.nonUSMerch = nonUSMerch;
	}

	public String getDevBatchID() {
		return devBatchID;
	}

	public void setDevBatchID(String devBatchID) {
		this.devBatchID = devBatchID;
	}

	public String getDigWltInd() {
		return digWltInd;
	}

	public void setDigWltInd(String digWltInd) {
		this.digWltInd = digWltInd;
	}

	public String getDigWltProgType() {
		return digWltProgType;
	}

	public void setDigWltProgType(String digWltProgType) {
		this.digWltProgType = digWltProgType;
	}

	public String getTranInit() {
		return tranInit;
	}

	public void setTranInit(String tranInit) {
		this.tranInit = tranInit;
	}

	public String getPymntSvc() {
		return pymntSvc;
	}

	public void setPymntSvc(String pymntSvc) {
		this.pymntSvc = pymntSvc;
	}

	public String getMerchCustom1() {
		return merchCustom1;
	}

	public void setMerchCustom1(String merchCustom1) {
		this.merchCustom1 = merchCustom1;
	}

	public String getRefundType() {
		return refundType;
	}

	public void setRefundType(String refundType) {
		this.refundType = refundType;
	}

	public String getRtFallbackInd() {
		return rtFallbackInd;
	}

	public void setRtFallbackInd(String rtFallbackInd) {
		this.rtFallbackInd = rtFallbackInd;
	}

	public String getDfrdAuthInd() {
		return dfrdAuthInd;
	}

	public void setDfrdAuthInd(String dfrdAuthInd) {
		this.dfrdAuthInd = dfrdAuthInd;
	}

	public String getBusPymtType() {
		return busPymtType;
	}

	public void setBusPymtType(String busPymtType) {
		this.busPymtType = busPymtType;
	}

	public String getTermClassCode() {
		return termClassCode;
	}

	public void setTermClassCode(String termClassCode) {
		this.termClassCode = termClassCode;
	}

	public String getMerchCtryOfOrigin() {
		return merchCtryOfOrigin;
	}

	public void setMerchCtryOfOrigin(String merchCtryOfOrigin) {
		this.merchCtryOfOrigin = merchCtryOfOrigin;
	}

	public String getPlanRegSysId() {
		return planRegSysId;
	}

	public void setPlanRegSysId(String planRegSysId) {
		this.planRegSysId = planRegSysId;
	}

	public String getAgreementID() {
		return agreementID;
	}

	public void setAgreementID(String agreementID) {
		this.agreementID = agreementID;
	}

	public String getDelChargeInd() {
		return delChargeInd;
	}

	public void setDelChargeInd(String delChargeInd) {
		this.delChargeInd = delChargeInd;
	}

	public String getNoShowInd() {
		return noShowInd;
	}

	public void setNoShowInd(String noShowInd) {
		this.noShowInd = noShowInd;
	}

	public String getRateTableID() {
		return rateTableID;
	}

	public void setRateTableID(String rateTableID) {
		this.rateTableID = rateTableID;
	}

	public String getXchgRate() {
		return xchgRate;
	}

	public void setXchgRate(String xchgRate) {
		this.xchgRate = xchgRate;
	}

	public String getPrstFXEligInd() {
		return prstFXEligInd;
	}

	public void setPrstFXEligInd(String prstFXEligInd) {
		this.prstFXEligInd = prstFXEligInd;
	}

	public String getCnvDate() {
		return cnvDate;
	}

	public void setCnvDate(String cnvDate) {
		this.cnvDate = cnvDate;
	}

	public String getMerchPymtGtwyID() {
		return merchPymtGtwyID;
	}

	public void setMerchPymtGtwyID(String merchPymtGtwyID) {
		this.merchPymtGtwyID = merchPymtGtwyID;
	}

	public String getDigComProgType() {
		return digComProgType;
	}

	public void setDigComProgType(String digComProgType) {
		this.digComProgType = digComProgType;
	}

	public String getDigComChkInEntTypeNm() {
		return digComChkInEntTypeNm;
	}

	public void setDigComChkInEntTypeNm(String digComChkInEntTypeNm) {
		this.digComChkInEntTypeNm = digComChkInEntTypeNm;
	}

	public String getDigComChkInMthd() {
		return digComChkInMthd;
	}

	public void setDigComChkInMthd(String digComChkInMthd) {
		this.digComChkInMthd = digComChkInMthd;
	}

	public String getAltNetTranID() {
		return altNetTranID;
	}

	public void setAltNetTranID(String altNetTranID) {
		this.altNetTranID = altNetTranID;
	}

	public String getEnablerVerVal() {
		return enablerVerVal;
	}

	public void setEnablerVerVal(String enablerVerVal) {
		this.enablerVerVal = enablerVerVal;
	}

	public String getAcctTypeID() {
		return acctTypeID;
	}

	public void setAcctTypeID(String acctTypeID) {
		this.acctTypeID = acctTypeID;
	}

	public String getDigComDomainID() {
		return digComDomainID;
	}

	public void setDigComDomainID(String digComDomainID) {
		this.digComDomainID = digComDomainID;
	}

	public String getMcrInd() {
		return mcrInd;
	}

	public void setMcrInd(String mcrInd) {
		this.mcrInd = mcrInd;
	}

	public String getTxnLinkID() {
		return txnLinkID;
	}

	public void setTxnLinkID(String txnLinkID) {
		this.txnLinkID = txnLinkID;
	}

	public String getExtdAuthInd() {
		return extdAuthInd;
	}

	public void setExtdAuthInd(String extdAuthInd) {
		this.extdAuthInd = extdAuthInd;
	}

	public String getBillPymtTxnInd() {
		return billPymtTxnInd;
	}

	public void setBillPymtTxnInd(String billPymtTxnInd) {
		this.billPymtTxnInd = billPymtTxnInd;
	}

	public String getMerchAdviceCode() {
		return merchAdviceCode;
	}

	public void setMerchAdviceCode(String merchAdviceCode) {
		this.merchAdviceCode = merchAdviceCode;
	}

	public String getInstallInvNum() {
		return installInvNum;
	}

	public void setInstallInvNum(String installInvNum) {
		this.installInvNum = installInvNum;
	}

	public String getInstallPymntDesc() {
		return installPymntDesc;
	}

	public void setInstallPymntDesc(String installPymntDesc) {
		this.installPymntDesc = installPymntDesc;
	}

	public String getInstallmentType() {
		return installmentType;
	}

	public void setInstallmentType(String installmentType) {
		this.installmentType = installmentType;
	}

	public String getMitTotPymtCt() {
		return mitTotPymtCt;
	}

	public void setMitTotPymtCt(String mitTotPymtCt) {
		this.mitTotPymtCt = mitTotPymtCt;
	}

	public String getMitAmtType() {
		return mitAmtType;
	}

	public void setMitAmtType(String mitAmtType) {
		this.mitAmtType = mitAmtType;
	}

	public String getMitAmt() {
		return mitAmt;
	}

	public void setMitAmt(String mitAmt) {
		this.mitAmt = mitAmt;
	}

	public String getMitUniqueID() {
		return mitUniqueID;
	}

	public void setMitUniqueID(String mitUniqueID) {
		this.mitUniqueID = mitUniqueID;
	}

	public String getMitFreq() {
		return mitFreq;
	}

	public void setMitFreq(String mitFreq) {
		this.mitFreq = mitFreq;
	}

	public String getMitValFlag() {
		return mitValFlag;
	}

	public void setMitValFlag(String mitValFlag) {
		this.mitValFlag = mitValFlag;
	}

	public String getMitValRef() {
		return mitValRef;
	}

	public void setMitValRef(String mitValRef) {
		this.mitValRef = mitValRef;
	}

	public String getMitSeqInd() {
		return mitSeqInd;
	}

	public void setMitSeqInd(String mitSeqInd) {
		this.mitSeqInd = mitSeqInd;
	}

	public String getMitPymtCrncy() {
		return mitPymtCrncy;
	}

	public void setMitPymtCrncy(String mitPymtCrncy) {
		this.mitPymtCrncy = mitPymtCrncy;
	}

	public String getMitTotPymtAmt() {
		return mitTotPymtAmt;
	}

	public void setMitTotPymtAmt(String mitTotPymtAmt) {
		this.mitTotPymtAmt = mitTotPymtAmt;
	}

	public String getMitPymtType() {
		return mitPymtType;
	}

	public void setMitPymtType(String mitPymtType) {
		this.mitPymtType = mitPymtType;
	}

	public String getMitRegRefNum() {
		return mitRegRefNum;
	}

	public void setMitRegRefNum(String mitRegRefNum) {
		this.mitRegRefNum = mitRegRefNum;
	}

	public String getMerchName() {
		return merchName;
	}

	public void setMerchName(String merchName) {
		this.merchName = merchName;
	}

	public String getMerchAddr() {
		return merchAddr;
	}

	public void setMerchAddr(String merchAddr) {
		this.merchAddr = merchAddr;
	}

	public String getMerchCity() {
		return merchCity;
	}

	public void setMerchCity(String merchCity) {
		this.merchCity = merchCity;
	}

	public String getMerchState() {
		return merchState;
	}

	public void setMerchState(String merchState) {
		this.merchState = merchState;
	}

	public String getMerchCnty() {
		return merchCnty;
	}

	public void setMerchCnty(String merchCnty) {
		this.merchCnty = merchCnty;
	}

	public String getMerchPostalCode() {
		return merchPostalCode;
	}

	public void setMerchPostalCode(String merchPostalCode) {
		this.merchPostalCode = merchPostalCode;
	}

	public String getMerchCtry() {
		return merchCtry;
	}

	public void setMerchCtry(String merchCtry) {
		this.merchCtry = merchCtry;
	}

	public String getMerchEmail() {
		return merchEmail;
	}

	public void setMerchEmail(String merchEmail) {
		this.merchEmail = merchEmail;
	}

	public String getMerchSvcLocCity() {
		return merchSvcLocCity;
	}

	public void setMerchSvcLocCity(String merchSvcLocCity) {
		this.merchSvcLocCity = merchSvcLocCity;
	}

	public String getMerchSvcLocState() {
		return merchSvcLocState;
	}

	public void setMerchSvcLocState(String merchSvcLocState) {
		this.merchSvcLocState = merchSvcLocState;
	}

	public String getMerchSvcLocPostalCode() {
		return merchSvcLocPostalCode;
	}

	public void setMerchSvcLocPostalCode(String merchSvcLocPostalCode) {
		this.merchSvcLocPostalCode = merchSvcLocPostalCode;
	}

	public String getMerchSvcLocCtry() {
		return merchSvcLocCtry;
	}

	public void setMerchSvcLocCtry(String merchSvcLocCtry) {
		this.merchSvcLocCtry = merchSvcLocCtry;
	}

	public String getAcctNum() {
		return acctNum;
	}

	public void setAcctNum(String acctNum) {
		this.acctNum = acctNum;
	}

	public String getCardExpiryDate() {
		return cardExpiryDate;
	}

	public void setCardExpiryDate(String cardExpiryDate) {
		this.cardExpiryDate = cardExpiryDate;
	}

	public String getTrack1Data() {
		return track1Data;
	}

	public void setTrack1Data(String track1Data) {
		this.track1Data = track1Data;
	}

	public String getTrack2Data() {
		return track2Data;
	}

	public void setTrack2Data(String track2Data) {
		this.track2Data = track2Data;
	}

	public String getCardType() {
		return cardType;
	}

	public void setCardType(String cardType) {
		this.cardType = cardType;
	}

	public String getAvsResultCode() {
		return avsResultCode;
	}

	public void setAvsResultCode(String avsResultCode) {
		this.avsResultCode = avsResultCode;
	}

	public String getCcvInd() {
		return ccvInd;
	}

	public void setCcvInd(String ccvInd) {
		this.ccvInd = ccvInd;
	}

	public String getCcvData() {
		return ccvData;
	}

	public void setCcvData(String ccvData) {
		this.ccvData = ccvData;
	}

	public String getCcvResultCode() {
		return ccvResultCode;
	}

	public void setCcvResultCode(String ccvResultCode) {
		this.ccvResultCode = ccvResultCode;
	}

	public String getMvvmAID() {
		return mvvmAID;
	}

	public void setMvvmAID(String mvvmAID) {
		this.mvvmAID = mvvmAID;
	}

	public String getInfoReqInd() {
		return infoReqInd;
	}

	public void setInfoReqInd(String infoReqInd) {
		this.infoReqInd = infoReqInd;
	}

	public String getFndAcctNum() {
		return fndAcctNum;
	}

	public void setFndAcctNum(String fndAcctNum) {
		this.fndAcctNum = fndAcctNum;
	}

	public String getPymtAcctRefReqInd() {
		return pymtAcctRefReqInd;
	}

	public void setPymtAcctRefReqInd(String pymtAcctRefReqInd) {
		this.pymtAcctRefReqInd = pymtAcctRefReqInd;
	}

	public String getPymtAcctRef() {
		return pymtAcctRef;
	}

	public void setPymtAcctRef(String pymtAcctRef) {
		this.pymtAcctRef = pymtAcctRef;
	}

	public String getAlmSvcCode() {
		return almSvcCode;
	}

	public void setAlmSvcCode(String almSvcCode) {
		this.almSvcCode = almSvcCode;
	}

	public String getAlmProdCode() {
		return almProdCode;
	}

	public void setAlmProdCode(String almProdCode) {
		this.almProdCode = almProdCode;
	}

	public String getAlmProdClass() {
		return almProdClass;
	}

	public void setAlmProdClass(String almProdClass) {
		this.almProdClass = almProdClass;
	}

	public String getAlmRateType() {
		return almRateType;
	}

	public void setAlmRateType(String almRateType) {
		this.almRateType = almRateType;
	}

	public String getAcctUpdReqInd() {
		return acctUpdReqInd;
	}

	public void setAcctUpdReqInd(String acctUpdReqInd) {
		this.acctUpdReqInd = acctUpdReqInd;
	}

	public String getAcctUpdCardStat() {
		return acctUpdCardStat;
	}

	public void setAcctUpdCardStat(String acctUpdCardStat) {
		this.acctUpdCardStat = acctUpdCardStat;
	}

	public String getAcctUpdCardNum() {
		return acctUpdCardNum;
	}

	public void setAcctUpdCardNum(String acctUpdCardNum) {
		this.acctUpdCardNum = acctUpdCardNum;
	}

	public String getAcctUpdExpDate() {
		return acctUpdExpDate;
	}

	public void setAcctUpdExpDate(String acctUpdExpDate) {
		this.acctUpdExpDate = acctUpdExpDate;
	}

	public String getAcctUpdResultCode() {
		return acctUpdResultCode;
	}

	public void setAcctUpdResultCode(String acctUpdResultCode) {
		this.acctUpdResultCode = acctUpdResultCode;
	}

	public String getDfrdAuthTranID() {
		return dfrdAuthTranID;
	}

	public void setDfrdAuthTranID(String dfrdAuthTranID) {
		this.dfrdAuthTranID = dfrdAuthTranID;
	}

	public String getOverrideInd() {
		return overrideInd;
	}

	public void setOverrideInd(String overrideInd) {
		this.overrideInd = overrideInd;
	}

	public String getAuthOptReasonCode() {
		return authOptReasonCode;
	}

	public void setAuthOptReasonCode(String authOptReasonCode) {
		this.authOptReasonCode = authOptReasonCode;
	}

	public String getInstallType() {
		return installType;
	}

	public void setInstallType(String installType) {
		this.installType = installType;
	}

	public String getInstallPymtOptions() {
		return installPymtOptions;
	}

	public void setInstallPymtOptions(String installPymtOptions) {
		this.installPymtOptions = installPymtOptions;
	}

	public String getNoOfInstall() {
		return noOfInstall;
	}

	public void setNoOfInstall(String noOfInstall) {
		this.noOfInstall = noOfInstall;
	}

	public String getInstallIntRate() {
		return installIntRate;
	}

	public void setInstallIntRate(String installIntRate) {
		this.installIntRate = installIntRate;
	}

	public String getInstallFee() {
		return installFee;
	}

	public void setInstallFee(String installFee) {
		this.installFee = installFee;
	}

	public String getInstallAPR() {
		return installAPR;
	}

	public void setInstallAPR(String installAPR) {
		this.installAPR = installAPR;
	}

	public String getFirstInstallAmt() {
		return firstInstallAmt;
	}

	public void setFirstInstallAmt(String firstInstallAmt) {
		this.firstInstallAmt = firstInstallAmt;
	}

	public String getSubInstallAmt() {
		return subInstallAmt;
	}

	public void setSubInstallAmt(String subInstallAmt) {
		this.subInstallAmt = subInstallAmt;
	}

	public String getTotInstallAmtDue() {
		return totInstallAmtDue;
	}

	public void setTotInstallAmtDue(String totInstallAmtDue) {
		this.totInstallAmtDue = totInstallAmtDue;
	}

	public String getPinData() {
		return pinData;
	}

	public void setPinData(String pinData) {
		this.pinData = pinData;
	}

	public String getKeySerialNumData() {
		return keySerialNumData;
	}

	public void setKeySerialNumData(String keySerialNumData) {
		this.keySerialNumData = keySerialNumData;
	}

	public String getKeyOffset() {
		return keyOffset;
	}

	public void setKeyOffset(String keyOffset) {
		this.keyOffset = keyOffset;
	}

	public String getMsKeyID() {
		return msKeyID;
	}

	public void setMsKeyID(String msKeyID) {
		this.msKeyID = msKeyID;
	}

	public String getAddAmt() {
		return addAmt;
	}

	public void setAddAmt(String addAmt) {
		this.addAmt = addAmt;
	}

	public String getAddAmtCrncy() {
		return addAmtCrncy;
	}

	public void setAddAmtCrncy(String addAmtCrncy) {
		this.addAmtCrncy = addAmtCrncy;
	}

	public String getAddAmtType() {
		return addAmtType;
	}

	public void setAddAmtType(String addAmtType) {
		this.addAmtType = addAmtType;
	}

	public String getAddAmtAcctType() {
		return addAmtAcctType;
	}

	public void setAddAmtAcctType(String addAmtAcctType) {
		this.addAmtAcctType = addAmtAcctType;
	}

	public String getPartAuthrztnApprvlCapablt() {
		return partAuthrztnApprvlCapablt;
	}

	public void setPartAuthrztnApprvlCapablt(String partAuthrztnApprvlCapablt) {
		this.partAuthrztnApprvlCapablt = partAuthrztnApprvlCapablt;
	}

	public String getBalRetCapablt() {
		return balRetCapablt;
	}

	public void setBalRetCapablt(String balRetCapablt) {
		this.balRetCapablt = balRetCapablt;
	}

	public String getEcommTxnInd() {
		return ecommTxnInd;
	}

	public void setEcommTxnInd(String ecommTxnInd) {
		this.ecommTxnInd = ecommTxnInd;
	}

	public String getCustSvcPhoneNumber() {
		return custSvcPhoneNumber;
	}

	public void setCustSvcPhoneNumber(String custSvcPhoneNumber) {
		this.custSvcPhoneNumber = custSvcPhoneNumber;
	}

	public String getEcommURL() {
		return ecommURL;
	}

	public void setEcommURL(String ecommURL) {
		this.ecommURL = ecommURL;
	}

	public String getMcsn() {
		return mcsn;
	}

	public void setMcsn(String mcsn) {
		this.mcsn = mcsn;
	}

	public String getMcsc() {
		return mcsc;
	}

	public void setMcsc(String mcsc) {
		this.mcsc = mcsc;
	}

	public String getMotoIndicator() {
		return motoIndicator;
	}

	public void setMotoIndicator(String motoIndicator) {
		this.motoIndicator = motoIndicator;
	}

	public String getVisaXID() {
		return visaXID;
	}

	public void setVisaXID(String visaXID) {
		this.visaXID = visaXID;
	}

	public String getVisaSecrTxnAD() {
		return visaSecrTxnAD;
	}

	public void setVisaSecrTxnAD(String visaSecrTxnAD) {
		this.visaSecrTxnAD = visaSecrTxnAD;
	}

	public String getCavvResultCode() {
		return cavvResultCode;
	}

	public void setCavvResultCode(String cavvResultCode) {
		this.cavvResultCode = cavvResultCode;
	}

	public String getAmexXID() {
		return amexXID;
	}

	public void setAmexXID(String amexXID) {
		this.amexXID = amexXID;
	}

	public String getAmexSecrAD() {
		return amexSecrAD;
	}

	public void setAmexSecrAD(String amexSecrAD) {
		this.amexSecrAD = amexSecrAD;
	}

	public String getSafekey() {
		return safekey;
	}

	public void setSafekey(String safekey) {
		this.safekey = safekey;
	}

	public String getUcafCollectInd() {
		return ucafCollectInd;
	}

	public void setUcafCollectInd(String ucafCollectInd) {
		this.ucafCollectInd = ucafCollectInd;
	}

	public String getMcSecrAD() {
		return mcSecrAD;
	}

	public void setMcSecrAD(String mcSecrAD) {
		this.mcSecrAD = mcSecrAD;
	}

	public String getDiscAuthType() {
		return discAuthType;
	}

	public void setDiscAuthType(String discAuthType) {
		this.discAuthType = discAuthType;
	}

	public String getDiscSecData() {
		return discSecData;
	}

	public void setDiscSecData(String discSecData) {
		this.discSecData = discSecData;
	}

	public String getSecDataDowngrade() {
		return secDataDowngrade;
	}

	public void setSecDataDowngrade(String secDataDowngrade) {
		this.secDataDowngrade = secDataDowngrade;
	}

	public String getTknAVD() {
		return tknAVD;
	}

	public void setTknAVD(String tknAVD) {
		this.tknAVD = tknAVD;
	}

	public String getTavvResultCode() {
		return tavvResultCode;
	}

	public void setTavvResultCode(String tavvResultCode) {
		this.tavvResultCode = tavvResultCode;
	}

	public String getProgramProtocol() {
		return programProtocol;
	}

	public void setProgramProtocol(String programProtocol) {
		this.programProtocol = programProtocol;
	}

	public String getDirServerTransID() {
		return dirServerTransID;
	}

	public void setDirServerTransID(String dirServerTransID) {
		this.dirServerTransID = dirServerTransID;
	}

	public String getLowValExInd() {
		return lowValExInd;
	}

	public void setLowValExInd(String lowValExInd) {
		this.lowValExInd = lowValExInd;
	}

	public String getTranRiskAnaExInd() {
		return tranRiskAnaExInd;
	}

	public void setTranRiskAnaExInd(String tranRiskAnaExInd) {
		this.tranRiskAnaExInd = tranRiskAnaExInd;
	}

	public String getTrustMerchExInd() {
		return trustMerchExInd;
	}

	public void setTrustMerchExInd(String trustMerchExInd) {
		this.trustMerchExInd = trustMerchExInd;
	}

	public String getSecrCorpExInd() {
		return secrCorpExInd;
	}

	public void setSecrCorpExInd(String secrCorpExInd) {
		this.secrCorpExInd = secrCorpExInd;
	}

	public String getDelegAuthInd() {
		return delegAuthInd;
	}

	public void setDelegAuthInd(String delegAuthInd) {
		this.delegAuthInd = delegAuthInd;
	}

	public String getMerchantAuthentID() {
		return merchantAuthentID;
	}

	public void setMerchantAuthentID(String merchantAuthentID) {
		this.merchantAuthentID = merchantAuthentID;
	}

	public String getRecPayExInd() {
		return recPayExInd;
	}

	public void setRecPayExInd(String recPayExInd) {
		this.recPayExInd = recPayExInd;
	}

	public String getExReasonCode() {
		return exReasonCode;
	}

	public void setExReasonCode(String exReasonCode) {
		this.exReasonCode = exReasonCode;
	}

	public String getRmtCommAcptrId() {
		return rmtCommAcptrId;
	}

	public void setRmtCommAcptrId(String rmtCommAcptrId) {
		this.rmtCommAcptrId = rmtCommAcptrId;
	}

	public String getAuthOutExInd() {
		return authOutExInd;
	}

	public void setAuthOutExInd(String authOutExInd) {
		this.authOutExInd = authOutExInd;
	}

	public String getDafInd() {
		return dafInd;
	}

	public void setDafInd(String dafInd) {
		this.dafInd = dafInd;
	}

	public String getSecrXID() {
		return secrXID;
	}

	public void setSecrXID(String secrXID) {
		this.secrXID = secrXID;
	}

	public String getSecrTxnAD() {
		return secrTxnAD;
	}

	public void setSecrTxnAD(String secrTxnAD) {
		this.secrTxnAD = secrTxnAD;
	}

	public String getAuthenDataQltInd() {
		return authenDataQltInd;
	}

	public void setAuthenDataQltInd(String authenDataQltInd) {
		this.authenDataQltInd = authenDataQltInd;
	}

	public String getAci() {
		return aci;
	}

	public void setAci(String aci) {
		this.aci = aci;
	}

	public String getMrktSpecificDataInd() {
		return mrktSpecificDataInd;
	}

	public void setMrktSpecificDataInd(String mrktSpecificDataInd) {
		this.mrktSpecificDataInd = mrktSpecificDataInd;
	}

	public String getExistingDebtInd() {
		return existingDebtInd;
	}

	public void setExistingDebtInd(String existingDebtInd) {
		this.existingDebtInd = existingDebtInd;
	}

	public String getCardLevelResult() {
		return cardLevelResult;
	}

	public void setCardLevelResult(String cardLevelResult) {
		this.cardLevelResult = cardLevelResult;
	}

	public String getSourceReasonCode() {
		return sourceReasonCode;
	}

	public void setSourceReasonCode(String sourceReasonCode) {
		this.sourceReasonCode = sourceReasonCode;
	}

	public String getTransID() {
		return transID;
	}

	public void setTransID(String transID) {
		this.transID = transID;
	}

	public String getVisaBID() {
		return visaBID;
	}

	public void setVisaBID(String visaBID) {
		this.visaBID = visaBID;
	}

	public String getVisaAUAR() {
		return visaAUAR;
	}

	public void setVisaAUAR(String visaAUAR) {
		this.visaAUAR = visaAUAR;
	}

	public String getTaxAmtCapablt() {
		return taxAmtCapablt;
	}

	public void setTaxAmtCapablt(String taxAmtCapablt) {
		this.taxAmtCapablt = taxAmtCapablt;
	}

	public String getSpendQInd() {
		return spendQInd;
	}

	public void setSpendQInd(String spendQInd) {
		this.spendQInd = spendQInd;
	}

	public String getCheckoutInd() {
		return checkoutInd;
	}

	public void setCheckoutInd(String checkoutInd) {
		this.checkoutInd = checkoutInd;
	}

	public String getQci() {
		return qci;
	}

	public void setQci(String qci) {
		this.qci = qci;
	}

	public String getVisaAuthInd() {
		return visaAuthInd;
	}

	public void setVisaAuthInd(String visaAuthInd) {
		this.visaAuthInd = visaAuthInd;
	}

	public String getStoredCredInd() {
		return storedCredInd;
	}

	public void setStoredCredInd(String storedCredInd) {
		this.storedCredInd = storedCredInd;
	}

	public String getCofSchedInd() {
		return cofSchedInd;
	}

	public void setCofSchedInd(String cofSchedInd) {
		this.cofSchedInd = cofSchedInd;
	}

	public String getCryptoCrncyPurchInd() {
		return cryptoCrncyPurchInd;
	}

	public void setCryptoCrncyPurchInd(String cryptoCrncyPurchInd) {
		this.cryptoCrncyPurchInd = cryptoCrncyPurchInd;
	}

	public String getProgDgReasonCode() {
		return progDgReasonCode;
	}

	public void setProgDgReasonCode(String progDgReasonCode) {
		this.progDgReasonCode = progDgReasonCode;
	}

	public String getAcctFundingSrc() {
		return acctFundingSrc;
	}

	public void setAcctFundingSrc(String acctFundingSrc) {
		this.acctFundingSrc = acctFundingSrc;
	}

	public String getAppProdPlatCode() {
		return appProdPlatCode;
	}

	public void setAppProdPlatCode(String appProdPlatCode) {
		this.appProdPlatCode = appProdPlatCode;
	}

	public String getAppCHIDMethod() {
		return appCHIDMethod;
	}

	public void setAppCHIDMethod(String appCHIDMethod) {
		this.appCHIDMethod = appCHIDMethod;
	}

	public String getBanknetData() {
		return banknetData;
	}

	public void setBanknetData(String banknetData) {
		this.banknetData = banknetData;
	}

	public String getMcmsdi() {
		return mcmsdi;
	}

	public void setMcmsdi(String mcmsdi) {
		this.mcmsdi = mcmsdi;
	}

	public String getCcvErrorCode() {
		return ccvErrorCode;
	}

	public void setCcvErrorCode(String ccvErrorCode) {
		this.ccvErrorCode = ccvErrorCode;
	}

	public String getPosEntryModeChg() {
		return posEntryModeChg;
	}

	public void setPosEntryModeChg(String posEntryModeChg) {
		this.posEntryModeChg = posEntryModeChg;
	}

	public String getTranEditErrCode() {
		return tranEditErrCode;
	}

	public void setTranEditErrCode(String tranEditErrCode) {
		this.tranEditErrCode = tranEditErrCode;
	}

	public String getMcposData() {
		return mcposData;
	}

	public void setMcposData(String mcposData) {
		this.mcposData = mcposData;
	}

	public String getDevTypeInd() {
		return devTypeInd;
	}

	public void setDevTypeInd(String devTypeInd) {
		this.devTypeInd = devTypeInd;
	}

	public String getMcaci() {
		return mcaci;
	}

	public void setMcaci(String mcaci) {
		this.mcaci = mcaci;
	}

	public String getMcAddData() {
		return mcAddData;
	}

	public void setMcAddData(String mcAddData) {
		this.mcAddData = mcAddData;
	}

	public String getFinAuthInd() {
		return finAuthInd;
	}

	public void setFinAuthInd(String finAuthInd) {
		this.finAuthInd = finAuthInd;
	}

	public String getTranIntgClass() {
		return tranIntgClass;
	}

	public void setTranIntgClass(String tranIntgClass) {
		this.tranIntgClass = tranIntgClass;
	}

	public String getMcAuthInd() {
		return mcAuthInd;
	}

	public void setMcAuthInd(String mcAuthInd) {
		this.mcAuthInd = mcAuthInd;
	}

	public String getStoredCredenInd() {
		return storedCredenInd;
	}

	public void setStoredCredenInd(String storedCredenInd) {
		this.storedCredenInd = storedCredenInd;
	}

	public String getCrypCrncyPurchInd() {
		return crypCrncyPurchInd;
	}

	public void setCrypCrncyPurchInd(String crypCrncyPurchInd) {
		this.crypCrncyPurchInd = crypCrncyPurchInd;
	}

	public String getHighRiskSecrPurchInd() {
		return highRiskSecrPurchInd;
	}

	public void setHighRiskSecrPurchInd(String highRiskSecrPurchInd) {
		this.highRiskSecrPurchInd = highRiskSecrPurchInd;
	}

	public String getCofSchdInd() {
		return cofSchdInd;
	}

	public void setCofSchdInd(String cofSchdInd) {
		this.cofSchdInd = cofSchdInd;
	}

	public String getCitmitFrameInd() {
		return citmitFrameInd;
	}

	public void setCitmitFrameInd(String citmitFrameInd) {
		this.citmitFrameInd = citmitFrameInd;
	}

	public String getDiscProcCode() {
		return discProcCode;
	}

	public void setDiscProcCode(String discProcCode) {
		this.discProcCode = discProcCode;
	}

	public String getDiscPOSEntry() {
		return discPOSEntry;
	}

	public void setDiscPOSEntry(String discPOSEntry) {
		this.discPOSEntry = discPOSEntry;
	}

	public String getDiscRespCode() {
		return discRespCode;
	}

	public void setDiscRespCode(String discRespCode) {
		this.discRespCode = discRespCode;
	}

	public String getDiscPOSData() {
		return discPOSData;
	}

	public void setDiscPOSData(String discPOSData) {
		this.discPOSData = discPOSData;
	}

	public String getDiscTransQualifier() {
		return discTransQualifier;
	}

	public void setDiscTransQualifier(String discTransQualifier) {
		this.discTransQualifier = discTransQualifier;
	}

	public String getDiscNRID() {
		return discNRID;
	}

	public void setDiscNRID(String discNRID) {
		this.discNRID = discNRID;
	}

	public String getMotoInd() {
		return motoInd;
	}

	public void setMotoInd(String motoInd) {
		this.motoInd = motoInd;
	}

	public String getRegUserInd() {
		return regUserInd;
	}

	public void setRegUserInd(String regUserInd) {
		this.regUserInd = regUserInd;
	}

	public String getRegUserDate() {
		return regUserDate;
	}

	public void setRegUserDate(String regUserDate) {
		this.regUserDate = regUserDate;
	}

	public String getDiscAuthInd() {
		return discAuthInd;
	}

	public void setDiscAuthInd(String discAuthInd) {
		this.discAuthInd = discAuthInd;
	}

	public String getPartShipInd() {
		return partShipInd;
	}

	public void setPartShipInd(String partShipInd) {
		this.partShipInd = partShipInd;
	}

	public String getDiscACI() {
		return discACI;
	}

	public void setDiscACI(String discACI) {
		this.discACI = discACI;
	}

	public String getStoredCrdInd() {
		return storedCrdInd;
	}

	public void setStoredCrdInd(String storedCrdInd) {
		this.storedCrdInd = storedCrdInd;
	}

	public String getDiscSTAN() {
		return discSTAN;
	}

	public void setDiscSTAN(String discSTAN) {
		this.discSTAN = discSTAN;
	}

	public String getCofSchInd() {
		return cofSchInd;
	}

	public void setCofSchInd(String cofSchInd) {
		this.cofSchInd = cofSchInd;
	}

	public String getNridReqInd() {
		return nridReqInd;
	}

	public void setNridReqInd(String nridReqInd) {
		this.nridReqInd = nridReqInd;
	}

	public String getDiscDebtInd() {
		return discDebtInd;
	}

	public void setDiscDebtInd(String discDebtInd) {
		this.discDebtInd = discDebtInd;
	}

	public String getDiscCryptoCrncyInd() {
		return discCryptoCrncyInd;
	}

	public void setDiscCryptoCrncyInd(String discCryptoCrncyInd) {
		this.discCryptoCrncyInd = discCryptoCrncyInd;
	}

	public String getAmExPOSData() {
		return amExPOSData;
	}

	public void setAmExPOSData(String amExPOSData) {
		this.amExPOSData = amExPOSData;
	}

	public String getAmExTranID() {
		return amExTranID;
	}

	public void setAmExTranID(String amExTranID) {
		this.amExTranID = amExTranID;
	}

	public String getGdSoldCd() {
		return gdSoldCd;
	}

	public void setGdSoldCd(String gdSoldCd) {
		this.gdSoldCd = gdSoldCd;
	}

	public String getReAuthInd() {
		return reAuthInd;
	}

	public void setReAuthInd(String reAuthInd) {
		this.reAuthInd = reAuthInd;
	}

	public String getAmexAuthInd() {
		return amexAuthInd;
	}

	public void setAmexAuthInd(String amexAuthInd) {
		this.amexAuthInd = amexAuthInd;
	}

	public String getStoredCrdIndAmex() {
		return storedCrdIndAmex;
	}

	public void setStoredCrdIndAmex(String storedCrdIndAmex) {
		this.storedCrdIndAmex = storedCrdIndAmex;
	}

	public String getAmexACI() {
		return amexACI;
	}

	public void setAmexACI(String amexACI) {
		this.amexACI = amexACI;
	}

	public String getTaxAmt() {
		return taxAmt;
	}

	public void setTaxAmt(String taxAmt) {
		this.taxAmt = taxAmt;
	}

	public String getTaxInd() {
		return taxInd;
	}

	public void setTaxInd(String taxInd) {
		this.taxInd = taxInd;
	}

	public String getVatTaxAmt() {
		return vatTaxAmt;
	}

	public void setVatTaxAmt(String vatTaxAmt) {
		this.vatTaxAmt = vatTaxAmt;
	}

	public String getVatTaxRt() {
		return vatTaxRt;
	}

	public void setVatTaxRt(String vatTaxRt) {
		this.vatTaxRt = vatTaxRt;
	}

	public String getPurchIdfr() {
		return purchIdfr;
	}

	public void setPurchIdfr(String purchIdfr) {
		this.purchIdfr = purchIdfr;
	}

	public String getPcOrderNum() {
		return pcOrderNum;
	}

	public void setPcOrderNum(String pcOrderNum) {
		this.pcOrderNum = pcOrderNum;
	}

	public String getDiscntAmt() {
		return discntAmt;
	}

	public void setDiscntAmt(String discntAmt) {
		this.discntAmt = discntAmt;
	}

	public String getFrghtAmt() {
		return frghtAmt;
	}

	public void setFrghtAmt(String frghtAmt) {
		this.frghtAmt = frghtAmt;
	}

	public String getDutyAmt() {
		return dutyAmt;
	}

	public void setDutyAmt(String dutyAmt) {
		this.dutyAmt = dutyAmt;
	}

	public String getDestPostalCode() {
		return destPostalCode;
	}

	public void setDestPostalCode(String destPostalCode) {
		this.destPostalCode = destPostalCode;
	}

	public String getShipFromPostalCode() {
		return shipFromPostalCode;
	}

	public void setShipFromPostalCode(String shipFromPostalCode) {
		this.shipFromPostalCode = shipFromPostalCode;
	}

	public String getDestCtryCode() {
		return destCtryCode;
	}

	public void setDestCtryCode(String destCtryCode) {
		this.destCtryCode = destCtryCode;
	}

	public String getMerchTaxID() {
		return merchTaxID;
	}

	public void setMerchTaxID(String merchTaxID) {
		this.merchTaxID = merchTaxID;
	}

	public String getProdDesc() {
		return prodDesc;
	}

	public void setProdDesc(String prodDesc) {
		this.prodDesc = prodDesc;
	}

	public String getPc3Add() {
		return pc3Add;
	}

	public void setPc3Add(String pc3Add) {
		this.pc3Add = pc3Add;
	}

	public String getL3ItemSeqNum() {
		return l3ItemSeqNum;
	}

	public void setL3ItemSeqNum(String l3ItemSeqNum) {
		this.l3ItemSeqNum = l3ItemSeqNum;
	}

	public String getL3ItemCode() {
		return l3ItemCode;
	}

	public void setL3ItemCode(String l3ItemCode) {
		this.l3ItemCode = l3ItemCode;
	}

	public String getL3ItemDesc() {
		return l3ItemDesc;
	}

	public void setL3ItemDesc(String l3ItemDesc) {
		this.l3ItemDesc = l3ItemDesc;
	}

	public String getL3Qty() {
		return l3Qty;
	}

	public void setL3Qty(String l3Qty) {
		this.l3Qty = l3Qty;
	}

	public String getL3UnitOfMsure() {
		return l3UnitOfMsure;
	}

	public void setL3UnitOfMsure(String l3UnitOfMsure) {
		this.l3UnitOfMsure = l3UnitOfMsure;
	}

	public String getL3UnitCost() {
		return l3UnitCost;
	}

	public void setL3UnitCost(String l3UnitCost) {
		this.l3UnitCost = l3UnitCost;
	}

	public String getL3ItemTot() {
		return l3ItemTot;
	}

	public void setL3ItemTot(String l3ItemTot) {
		this.l3ItemTot = l3ItemTot;
	}

	public String getL3DiscntAmt() {
		return l3DiscntAmt;
	}

	public void setL3DiscntAmt(String l3DiscntAmt) {
		this.l3DiscntAmt = l3DiscntAmt;
	}

	public String getL3TaxAmt() {
		return l3TaxAmt;
	}

	public void setL3TaxAmt(String l3TaxAmt) {
		this.l3TaxAmt = l3TaxAmt;
	}

	public String getL3TaxRt() {
		return l3TaxRt;
	}

	public void setL3TaxRt(String l3TaxRt) {
		this.l3TaxRt = l3TaxRt;
	}

	public String getAvsBillingAddr() {
		return avsBillingAddr;
	}

	public void setAvsBillingAddr(String avsBillingAddr) {
		this.avsBillingAddr = avsBillingAddr;
	}

	public String getAvsBillingPostalCode() {
		return avsBillingPostalCode;
	}

	public void setAvsBillingPostalCode(String avsBillingPostalCode) {
		this.avsBillingPostalCode = avsBillingPostalCode;
	}

	public String getChFirstNm() {
		return chFirstNm;
	}

	public void setChFirstNm(String chFirstNm) {
		this.chFirstNm = chFirstNm;
	}

	public String getChLastNm() {
		return chLastNm;
	}

	public void setChLastNm(String chLastNm) {
		this.chLastNm = chLastNm;
	}

	public String getChFullNmRes() {
		return chFullNmRes;
	}

	public void setChFullNmRes(String chFullNmRes) {
		this.chFullNmRes = chFullNmRes;
	}

	public String getCustEmailAddr() {
		return custEmailAddr;
	}

	public void setCustEmailAddr(String custEmailAddr) {
		this.custEmailAddr = custEmailAddr;
	}

	public String getChMidNm() {
		return chMidNm;
	}

	public void setChMidNm(String chMidNm) {
		this.chMidNm = chMidNm;
	}

	public String getFullNmAcctMtchDec() {
		return fullNmAcctMtchDec;
	}

	public void setFullNmAcctMtchDec(String fullNmAcctMtchDec) {
		this.fullNmAcctMtchDec = fullNmAcctMtchDec;
	}

	public String getLastNmAcctMtchDec() {
		return lastNmAcctMtchDec;
	}

	public void setLastNmAcctMtchDec(String lastNmAcctMtchDec) {
		this.lastNmAcctMtchDec = lastNmAcctMtchDec;
	}

	public String getMidNmAcctMtchDec() {
		return midNmAcctMtchDec;
	}

	public void setMidNmAcctMtchDec(String midNmAcctMtchDec) {
		this.midNmAcctMtchDec = midNmAcctMtchDec;
	}

	public String getFirstNmAcctMtchDec() {
		return firstNmAcctMtchDec;
	}

	public void setFirstNmAcctMtchDec(String firstNmAcctMtchDec) {
		this.firstNmAcctMtchDec = firstNmAcctMtchDec;
	}

	public String getChPhNumRes() {
		return chPhNumRes;
	}

	public void setChPhNumRes(String chPhNumRes) {
		this.chPhNumRes = chPhNumRes;
	}

	public String getChEmailAddrRes() {
		return chEmailAddrRes;
	}

	public void setChEmailAddrRes(String chEmailAddrRes) {
		this.chEmailAddrRes = chEmailAddrRes;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getCustInfoEnhdRes() {
		return custInfoEnhdRes;
	}

	public void setCustInfoEnhdRes(String custInfoEnhdRes) {
		this.custInfoEnhdRes = custInfoEnhdRes;
	}

	public String getRcptLastNm() {
		return rcptLastNm;
	}

	public void setRcptLastNm(String rcptLastNm) {
		this.rcptLastNm = rcptLastNm;
	}

	public String getRcptPostalCode() {
		return rcptPostalCode;
	}

	public void setRcptPostalCode(String rcptPostalCode) {
		this.rcptPostalCode = rcptPostalCode;
	}

	public String getRcptDateOfBirth() {
		return rcptDateOfBirth;
	}

	public void setRcptDateOfBirth(String rcptDateOfBirth) {
		this.rcptDateOfBirth = rcptDateOfBirth;
	}

	public String getRcptAcctNum() {
		return rcptAcctNum;
	}

	public void setRcptAcctNum(String rcptAcctNum) {
		this.rcptAcctNum = rcptAcctNum;
	}

	public String getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(String orderDate) {
		this.orderDate = orderDate;
	}

	public String getRespCode() {
		return respCode;
	}

	public void setRespCode(String respCode) {
		this.respCode = respCode;
	}

	public String getAuthID() {
		return authID;
	}

	public void setAuthID(String authID) {
		this.authID = authID;
	}

	public String getResponseDate() {
		return responseDate;
	}

	public void setResponseDate(String responseDate) {
		this.responseDate = responseDate;
	}

	public String getAddtlRespData() {
		return addtlRespData;
	}

	public void setAddtlRespData(String addtlRespData) {
		this.addtlRespData = addtlRespData;
	}

	public String getSttlmDate() {
		return sttlmDate;
	}

	public void setSttlmDate(String sttlmDate) {
		this.sttlmDate = sttlmDate;
	}

	public String getAthNtwkID() {
		return athNtwkID;
	}

	public void setAthNtwkID(String athNtwkID) {
		this.athNtwkID = athNtwkID;
	}

	public String getAthNtwkNm() {
		return athNtwkNm;
	}

	public void setAthNtwkNm(String athNtwkNm) {
		this.athNtwkNm = athNtwkNm;
	}

	public String getRtInd() {
		return rtInd;
	}

	public void setRtInd(String rtInd) {
		this.rtInd = rtInd;
	}

	public String getSigInd() {
		return sigInd;
	}

	public void setSigInd(String sigInd) {
		this.sigInd = sigInd;
	}

	public String getErrorData() {
		return errorData;
	}

	public void setErrorData(String errorData) {
		this.errorData = errorData;
	}

	public String getDebitTraceNum() {
		return debitTraceNum;
	}

	public void setDebitTraceNum(String debitTraceNum) {
		this.debitTraceNum = debitTraceNum;
	}

	public String getSettlementTxnType() {
		return settlementTxnType;
	}

	public void setSettlementTxnType(String settlementTxnType) {
		this.settlementTxnType = settlementTxnType;
	}

	public String getAssocRespCode() {
		return assocRespCode;
	}

	public void setAssocRespCode(String assocRespCode) {
		this.assocRespCode = assocRespCode;
	}

	public String getCategoryCode() {
		return categoryCode;
	}

	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}

	public String getApprProbabInd() {
		return apprProbabInd;
	}

	public void setApprProbabInd(String apprProbabInd) {
		this.apprProbabInd = apprProbabInd;
	}

	public String getReasonCode() {
		return reasonCode;
	}

	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
	}

	public String getMaxProcDate() {
		return maxProcDate;
	}

	public void setMaxProcDate(String maxProcDate) {
		this.maxProcDate = maxProcDate;
	}

	public String getIssBank() {
		return issBank;
	}

	public void setIssBank(String issBank) {
		this.issBank = issBank;
	}

	public String getIssCtryCode() {
		return issCtryCode;
	}

	public void setIssCtryCode(String issCtryCode) {
		this.issCtryCode = issCtryCode;
	}

	public String getCardBrnd() {
		return cardBrnd;
	}

	public void setCardBrnd(String cardBrnd) {
		this.cardBrnd = cardBrnd;
	}

	public String getCardInd() {
		return cardInd;
	}

	public void setCardInd(String cardInd) {
		this.cardInd = cardInd;
	}

	public String getDetProdID() {
		return detProdID;
	}

	public void setDetProdID(String detProdID) {
		this.detProdID = detProdID;
	}

	public String getOrigAuthID() {
		return origAuthID;
	}

	public void setOrigAuthID(String origAuthID) {
		this.origAuthID = origAuthID;
	}

	public String getOrigLocalDateTime() {
		return origLocalDateTime;
	}

	public void setOrigLocalDateTime(String origLocalDateTime) {
		this.origLocalDateTime = origLocalDateTime;
	}

	public String getOrigTranDateTime() {
		return origTranDateTime;
	}

	public void setOrigTranDateTime(String origTranDateTime) {
		this.origTranDateTime = origTranDateTime;
	}

	public String getOrigSTAN() {
		return origSTAN;
	}

	public void setOrigSTAN(String origSTAN) {
		this.origSTAN = origSTAN;
	}

	public String getOrigRespCode() {
		return origRespCode;
	}

	public void setOrigRespCode(String origRespCode) {
		this.origRespCode = origRespCode;
	}

	public String getOrigAthNtwkID() {
		return origAthNtwkID;
	}

	public void setOrigAthNtwkID(String origAthNtwkID) {
		this.origAthNtwkID = origAthNtwkID;
	}

	public String getServLvl() {
		return servLvl;
	}

	public void setServLvl(String servLvl) {
		this.servLvl = servLvl;
	}

	public String getNumOfProds() {
		return numOfProds;
	}

	public void setNumOfProds(String numOfProds) {
		this.numOfProds = numOfProds;
	}

	public String getNacsProdCode() {
		return nacsProdCode;
	}

	public void setNacsProdCode(String nacsProdCode) {
		this.nacsProdCode = nacsProdCode;
	}

	public String getUnitOfMsure() {
		return unitOfMsure;
	}

	public void setUnitOfMsure(String unitOfMsure) {
		this.unitOfMsure = unitOfMsure;
	}

	public String getQnty() {
		return qnty;
	}

	public void setQnty(String qnty) {
		this.qnty = qnty;
	}

	public String getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(String unitPrice) {
		this.unitPrice = unitPrice;
	}

	public String getProdAmt() {
		return prodAmt;
	}

	public void setProdAmt(String prodAmt) {
		this.prodAmt = prodAmt;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getTraTranTypeInd() {
		return traTranTypeInd;
	}

	public void setTraTranTypeInd(String traTranTypeInd) {
		this.traTranTypeInd = traTranTypeInd;
	}

	public String getTptModeInd() {
		return tptModeInd;
	}

	public void setTptModeInd(String tptModeInd) {
		this.tptModeInd = tptModeInd;
	}

	public String getAtcUpdInd() {
		return atcUpdInd;
	}

	public void setAtcUpdInd(String atcUpdInd) {
		this.atcUpdInd = atcUpdInd;
	}

	public String getTraAcsTermInd() {
		return traAcsTermInd;
	}

	public void setTraAcsTermInd(String traAcsTermInd) {
		this.traAcsTermInd = traAcsTermInd;
	}

	public String getTraAcsTermFunCode() {
		return traAcsTermFunCode;
	}

	public void setTraAcsTermFunCode(String traAcsTermFunCode) {
		this.traAcsTermFunCode = traAcsTermFunCode;
	}

	public String getExPayTransIPAN() {
		return exPayTransIPAN;
	}

	public void setExPayTransIPAN(String exPayTransIPAN) {
		this.exPayTransIPAN = exPayTransIPAN;
	}

	public String getFolioNum() {
		return folioNum;
	}

	public void setFolioNum(String folioNum) {
		this.folioNum = folioNum;
	}

	public String getRoomNum() {
		return roomNum;
	}

	public void setRoomNum(String roomNum) {
		this.roomNum = roomNum;
	}

	public String getLodRefNum() {
		return lodRefNum;
	}

	public void setLodRefNum(String lodRefNum) {
		this.lodRefNum = lodRefNum;
	}

	public String getRoomRt() {
		return roomRt;
	}

	public void setRoomRt(String roomRt) {
		this.roomRt = roomRt;
	}

	public String getProgramInd() {
		return programInd;
	}

	public void setProgramInd(String programInd) {
		this.programInd = programInd;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getExtraChrgs() {
		return extraChrgs;
	}

	public void setExtraChrgs(String extraChrgs) {
		this.extraChrgs = extraChrgs;
	}

	public String getRentalCity() {
		return rentalCity;
	}

	public void setRentalCity(String rentalCity) {
		this.rentalCity = rentalCity;
	}

	public String getRentalState() {
		return rentalState;
	}

	public void setRentalState(String rentalState) {
		this.rentalState = rentalState;
	}

	public String getRentalCtry() {
		return rentalCtry;
	}

	public void setRentalCtry(String rentalCtry) {
		this.rentalCtry = rentalCtry;
	}

	public String getRentalDate() {
		return rentalDate;
	}

	public void setRentalDate(String rentalDate) {
		this.rentalDate = rentalDate;
	}

	public String getRentalTime() {
		return rentalTime;
	}

	public void setRentalTime(String rentalTime) {
		this.rentalTime = rentalTime;
	}

	public String getReturnCity() {
		return returnCity;
	}

	public void setReturnCity(String returnCity) {
		this.returnCity = returnCity;
	}

	public String getReturnState() {
		return returnState;
	}

	public void setReturnState(String returnState) {
		this.returnState = returnState;
	}

	public String getReturnCtry() {
		return returnCtry;
	}

	public void setReturnCtry(String returnCtry) {
		this.returnCtry = returnCtry;
	}

	public String getReturnDate() {
		return returnDate;
	}

	public void setReturnDate(String returnDate) {
		this.returnDate = returnDate;
	}

	public String getReturnTime() {
		return returnTime;
	}

	public void setReturnTime(String returnTime) {
		this.returnTime = returnTime;
	}

	public String getAmtExtraChrgs() {
		return amtExtraChrgs;
	}

	public void setAmtExtraChrgs(String amtExtraChrgs) {
		this.amtExtraChrgs = amtExtraChrgs;
	}

	public String getRenterName() {
		return renterName;
	}

	public void setRenterName(String renterName) {
		this.renterName = renterName;
	}

	public String getAutoAgreeNum() {
		return autoAgreeNum;
	}

	public void setAutoAgreeNum(String autoAgreeNum) {
		this.autoAgreeNum = autoAgreeNum;
	}

	public String getRentalDuration() {
		return rentalDuration;
	}

	public void setRentalDuration(String rentalDuration) {
		this.rentalDuration = rentalDuration;
	}

	public String getRentalExtraChrgs() {
		return rentalExtraChrgs;
	}

	public void setRentalExtraChrgs(String rentalExtraChrgs) {
		this.rentalExtraChrgs = rentalExtraChrgs;
	}

	public String getAutoNoShow() {
		return autoNoShow;
	}

	public void setAutoNoShow(String autoNoShow) {
		this.autoNoShow = autoNoShow;
	}

	public String getDelChrgInd() {
		return delChrgInd;
	}

	public void setDelChrgInd(String delChrgInd) {
		this.delChrgInd = delChrgInd;
	}

	public boolean isCanadianDebitGroup() {
		// TODO Auto-generated method stub
		if(getTxnCrncy().equals("124")) {
			return true;
		}
		return false;
	}

	public boolean isBatchSettleDetail() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public Object getCheckServiceProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isOriginalAuthProcessedWithAVS() {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean wasOriginalAuthProcessedWithCCV() {
		// TODO Auto-generated method stub
		return true;
	}

	public boolean isHsmSupported() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public String toString() {
		return "TransactionContext [did=" + did + ", app=" + app + ", auth=" + auth + ", clientRef=" + clientRef
				+ ", pymtType=" + pymtType + ", reversalInd=" + reversalInd + ", txnType=" + txnType
				+ ", localDateTime=" + localDateTime + ", trnmsnDateTime=" + trnmsnDateTime + ", stan=" + stan
				+ ", refNum=" + refNum + ", orderNum=" + orderNum + ", tppid=" + tppid + ", termID=" + termID
				+ ", merchID=" + merchID + ", merchCatCode=" + merchCatCode + ", posEntryMode=" + posEntryMode
				+ ", posCondCode=" + posCondCode + ", termCatCode=" + termCatCode + ", termEntryCapablt="
				+ termEntryCapablt + ", txnAmt=" + txnAmt + ", txnCrncy=" + txnCrncy + ", termLocInd=" + termLocInd
				+ ", cardCaptCap=" + cardCaptCap + ", groupID=" + groupID + ", posID=" + posID + ", settleInd="
				+ settleInd + ", clerkID=" + clerkID + ", seNum=" + seNum + ", plposDebitFlg=" + plposDebitFlg
				+ ", netAccInd=" + netAccInd + ", merchEcho=" + merchEcho + ", wltID=" + wltID + ", nonUSMerch="
				+ nonUSMerch + ", devBatchID=" + devBatchID + ", digWltInd=" + digWltInd + ", digWltProgType="
				+ digWltProgType + ", tranInit=" + tranInit + ", pymntSvc=" + pymntSvc + ", merchCustom1="
				+ merchCustom1 + ", refundType=" + refundType + ", rtFallbackInd=" + rtFallbackInd + ", dfrdAuthInd="
				+ dfrdAuthInd + ", busPymtType=" + busPymtType + ", termClassCode=" + termClassCode
				+ ", merchCtryOfOrigin=" + merchCtryOfOrigin + ", planRegSysId=" + planRegSysId + ", agreementID="
				+ agreementID + ", delChargeInd=" + delChargeInd + ", noShowInd=" + noShowInd + ", rateTableID="
				+ rateTableID + ", xchgRate=" + xchgRate + ", prstFXEligInd=" + prstFXEligInd + ", cnvDate=" + cnvDate
				+ ", merchPymtGtwyID=" + merchPymtGtwyID + ", digComProgType=" + digComProgType
				+ ", digComChkInEntTypeNm=" + digComChkInEntTypeNm + ", digComChkInMthd=" + digComChkInMthd
				+ ", altNetTranID=" + altNetTranID + ", enablerVerVal=" + enablerVerVal + ", acctTypeID=" + acctTypeID
				+ ", digComDomainID=" + digComDomainID + ", mcrInd=" + mcrInd + ", txnLinkID=" + txnLinkID
				+ ", extdAuthInd=" + extdAuthInd + ", billPymtTxnInd=" + billPymtTxnInd + ", merchAdviceCode="
				+ merchAdviceCode + ", installInvNum=" + installInvNum + ", installPymntDesc=" + installPymntDesc
				+ ", installmentType=" + installmentType + ", mitTotPymtCt=" + mitTotPymtCt + ", mitAmtType="
				+ mitAmtType + ", mitAmt=" + mitAmt + ", mitUniqueID=" + mitUniqueID + ", mitFreq=" + mitFreq
				+ ", mitValFlag=" + mitValFlag + ", mitValRef=" + mitValRef + ", mitSeqInd=" + mitSeqInd
				+ ", mitPymtCrncy=" + mitPymtCrncy + ", mitTotPymtAmt=" + mitTotPymtAmt + ", mitPymtType=" + mitPymtType
				+ ", mitRegRefNum=" + mitRegRefNum + ", merchName=" + merchName + ", merchAddr=" + merchAddr
				+ ", merchCity=" + merchCity + ", merchState=" + merchState + ", merchCnty=" + merchCnty
				+ ", merchPostalCode=" + merchPostalCode + ", merchCtry=" + merchCtry + ", merchEmail=" + merchEmail
				+ ", merchSvcLocCity=" + merchSvcLocCity + ", merchSvcLocState=" + merchSvcLocState
				+ ", merchSvcLocPostalCode=" + merchSvcLocPostalCode + ", merchSvcLocCtry=" + merchSvcLocCtry
				+ ", acctNum=" + acctNum + ", cardExpiryDate=" + cardExpiryDate + ", track1Data=" + track1Data
				+ ", track2Data=" + track2Data + ", cardType=" + cardType + ", avsResultCode=" + avsResultCode
				+ ", ccvInd=" + ccvInd + ", ccvData=" + ccvData + ", ccvResultCode=" + ccvResultCode + ", mvvmAID="
				+ mvvmAID + ", infoReqInd=" + infoReqInd + ", fndAcctNum=" + fndAcctNum + ", pymtAcctRefReqInd="
				+ pymtAcctRefReqInd + ", pymtAcctRef=" + pymtAcctRef + ", almSvcCode=" + almSvcCode + ", almProdCode="
				+ almProdCode + ", almProdClass=" + almProdClass + ", almRateType=" + almRateType + ", acctUpdReqInd="
				+ acctUpdReqInd + ", acctUpdCardStat=" + acctUpdCardStat + ", acctUpdCardNum=" + acctUpdCardNum
				+ ", acctUpdExpDate=" + acctUpdExpDate + ", acctUpdResultCode=" + acctUpdResultCode
				+ ", dfrdAuthTranID=" + dfrdAuthTranID + ", overrideInd=" + overrideInd + ", authOptReasonCode="
				+ authOptReasonCode + ", installType=" + installType + ", installPymtOptions=" + installPymtOptions
				+ ", noOfInstall=" + noOfInstall + ", installIntRate=" + installIntRate + ", installFee=" + installFee
				+ ", installAPR=" + installAPR + ", firstInstallAmt=" + firstInstallAmt + ", subInstallAmt="
				+ subInstallAmt + ", totInstallAmtDue=" + totInstallAmtDue + ", pinData=" + pinData
				+ ", keySerialNumData=" + keySerialNumData + ", keyOffset=" + keyOffset + ", msKeyID=" + msKeyID
				+ ", addAmt=" + addAmt + ", addAmtCrncy=" + addAmtCrncy + ", addAmtType=" + addAmtType
				+ ", addAmtAcctType=" + addAmtAcctType + ", partAuthrztnApprvlCapablt=" + partAuthrztnApprvlCapablt
				+ ", balRetCapablt=" + balRetCapablt + ", ecommTxnInd=" + ecommTxnInd + ", custSvcPhoneNumber="
				+ custSvcPhoneNumber + ", ecommURL=" + ecommURL + ", mcsn=" + mcsn + ", mcsc=" + mcsc
				+ ", motoIndicator=" + motoIndicator + ", visaXID=" + visaXID + ", visaSecrTxnAD=" + visaSecrTxnAD
				+ ", cavvResultCode=" + cavvResultCode + ", amexXID=" + amexXID + ", amexSecrAD=" + amexSecrAD
				+ ", safekey=" + safekey + ", ucafCollectInd=" + ucafCollectInd + ", mcSecrAD=" + mcSecrAD
				+ ", discAuthType=" + discAuthType + ", discSecData=" + discSecData + ", secDataDowngrade="
				+ secDataDowngrade + ", tknAVD=" + tknAVD + ", tavvResultCode=" + tavvResultCode + ", programProtocol="
				+ programProtocol + ", dirServerTransID=" + dirServerTransID + ", lowValExInd=" + lowValExInd
				+ ", tranRiskAnaExInd=" + tranRiskAnaExInd + ", trustMerchExInd=" + trustMerchExInd + ", secrCorpExInd="
				+ secrCorpExInd + ", delegAuthInd=" + delegAuthInd + ", merchantAuthentID=" + merchantAuthentID
				+ ", recPayExInd=" + recPayExInd + ", exReasonCode=" + exReasonCode + ", rmtCommAcptrId="
				+ rmtCommAcptrId + ", authOutExInd=" + authOutExInd + ", dafInd=" + dafInd + ", secrXID=" + secrXID
				+ ", secrTxnAD=" + secrTxnAD + ", authenDataQltInd=" + authenDataQltInd + ", aci=" + aci
				+ ", mrktSpecificDataInd=" + mrktSpecificDataInd + ", existingDebtInd=" + existingDebtInd
				+ ", cardLevelResult=" + cardLevelResult + ", sourceReasonCode=" + sourceReasonCode + ", transID="
				+ transID + ", visaBID=" + visaBID + ", visaAUAR=" + visaAUAR + ", taxAmtCapablt=" + taxAmtCapablt
				+ ", spendQInd=" + spendQInd + ", checkoutInd=" + checkoutInd + ", qci=" + qci + ", visaAuthInd="
				+ visaAuthInd + ", storedCredInd=" + storedCredInd + ", cofSchedInd=" + cofSchedInd
				+ ", cryptoCrncyPurchInd=" + cryptoCrncyPurchInd + ", progDgReasonCode=" + progDgReasonCode
				+ ", acctFundingSrc=" + acctFundingSrc + ", appProdPlatCode=" + appProdPlatCode + ", appCHIDMethod="
				+ appCHIDMethod + ", banknetData=" + banknetData + ", mcmsdi=" + mcmsdi + ", ccvErrorCode="
				+ ccvErrorCode + ", posEntryModeChg=" + posEntryModeChg + ", tranEditErrCode=" + tranEditErrCode
				+ ", mcposData=" + mcposData + ", devTypeInd=" + devTypeInd + ", mcaci=" + mcaci + ", mcAddData="
				+ mcAddData + ", finAuthInd=" + finAuthInd + ", tranIntgClass=" + tranIntgClass + ", mcAuthInd="
				+ mcAuthInd + ", storedCredenInd=" + storedCredenInd + ", crypCrncyPurchInd=" + crypCrncyPurchInd
				+ ", highRiskSecrPurchInd=" + highRiskSecrPurchInd + ", cofSchdInd=" + cofSchdInd + ", citmitFrameInd="
				+ citmitFrameInd + ", discProcCode=" + discProcCode + ", discPOSEntry=" + discPOSEntry
				+ ", discRespCode=" + discRespCode + ", discPOSData=" + discPOSData + ", discTransQualifier="
				+ discTransQualifier + ", discNRID=" + discNRID + ", motoInd=" + motoInd + ", regUserInd=" + regUserInd
				+ ", regUserDate=" + regUserDate + ", discAuthInd=" + discAuthInd + ", partShipInd=" + partShipInd
				+ ", discACI=" + discACI + ", storedCrdInd=" + storedCrdInd + ", discSTAN=" + discSTAN + ", cofSchInd="
				+ cofSchInd + ", nridReqInd=" + nridReqInd + ", discDebtInd=" + discDebtInd + ", discCryptoCrncyInd="
				+ discCryptoCrncyInd + ", amExPOSData=" + amExPOSData + ", amExTranID=" + amExTranID + ", gdSoldCd="
				+ gdSoldCd + ", reAuthInd=" + reAuthInd + ", amexAuthInd=" + amexAuthInd + ", storedCrdIndAmex="
				+ storedCrdIndAmex + ", amexACI=" + amexACI + ", taxAmt=" + taxAmt + ", taxInd=" + taxInd
				+ ", vatTaxAmt=" + vatTaxAmt + ", vatTaxRt=" + vatTaxRt + ", purchIdfr=" + purchIdfr + ", pcOrderNum="
				+ pcOrderNum + ", discntAmt=" + discntAmt + ", frghtAmt=" + frghtAmt + ", dutyAmt=" + dutyAmt
				+ ", destPostalCode=" + destPostalCode + ", shipFromPostalCode=" + shipFromPostalCode
				+ ", destCtryCode=" + destCtryCode + ", merchTaxID=" + merchTaxID + ", prodDesc=" + prodDesc
				+ ", pc3Add=" + pc3Add + ", l3ItemSeqNum=" + l3ItemSeqNum + ", l3ItemCode=" + l3ItemCode
				+ ", l3ItemDesc=" + l3ItemDesc + ", l3Qty=" + l3Qty + ", l3UnitOfMsure=" + l3UnitOfMsure
				+ ", l3UnitCost=" + l3UnitCost + ", l3ItemTot=" + l3ItemTot + ", l3DiscntAmt=" + l3DiscntAmt
				+ ", l3TaxAmt=" + l3TaxAmt + ", l3TaxRt=" + l3TaxRt + ", avsBillingAddr=" + avsBillingAddr
				+ ", avsBillingPostalCode=" + avsBillingPostalCode + ", chFirstNm=" + chFirstNm + ", chLastNm="
				+ chLastNm + ", chFullNmRes=" + chFullNmRes + ", custEmailAddr=" + custEmailAddr + ", chMidNm="
				+ chMidNm + ", fullNmAcctMtchDec=" + fullNmAcctMtchDec + ", lastNmAcctMtchDec=" + lastNmAcctMtchDec
				+ ", midNmAcctMtchDec=" + midNmAcctMtchDec + ", firstNmAcctMtchDec=" + firstNmAcctMtchDec
				+ ", chPhNumRes=" + chPhNumRes + ", chEmailAddrRes=" + chEmailAddrRes + ", customerName=" + customerName
				+ ", custInfoEnhdRes=" + custInfoEnhdRes + ", rcptLastNm=" + rcptLastNm + ", rcptPostalCode="
				+ rcptPostalCode + ", rcptDateOfBirth=" + rcptDateOfBirth + ", rcptAcctNum=" + rcptAcctNum
				+ ", orderDate=" + orderDate + ", respCode=" + respCode + ", authID=" + authID + ", responseDate="
				+ responseDate + ", addtlRespData=" + addtlRespData + ", sttlmDate=" + sttlmDate + ", athNtwkID="
				+ athNtwkID + ", athNtwkNm=" + athNtwkNm + ", rtInd=" + rtInd + ", sigInd=" + sigInd + ", errorData="
				+ errorData + ", debitTraceNum=" + debitTraceNum + ", settlementTxnType=" + settlementTxnType
				+ ", assocRespCode=" + assocRespCode + ", categoryCode=" + categoryCode + ", apprProbabInd="
				+ apprProbabInd + ", reasonCode=" + reasonCode + ", maxProcDate=" + maxProcDate + ", issBank=" + issBank
				+ ", issCtryCode=" + issCtryCode + ", cardBrnd=" + cardBrnd + ", cardInd=" + cardInd + ", detProdID="
				+ detProdID + ", origAuthID=" + origAuthID + ", origLocalDateTime=" + origLocalDateTime
				+ ", origTranDateTime=" + origTranDateTime + ", origSTAN=" + origSTAN + ", origRespCode=" + origRespCode
				+ ", origAthNtwkID=" + origAthNtwkID + ", servLvl=" + servLvl + ", numOfProds=" + numOfProds
				+ ", nacsProdCode=" + nacsProdCode + ", unitOfMsure=" + unitOfMsure + ", qnty=" + qnty + ", unitPrice="
				+ unitPrice + ", prodAmt=" + prodAmt + ", fileType=" + fileType + ", traTranTypeInd=" + traTranTypeInd
				+ ", tptModeInd=" + tptModeInd + ", atcUpdInd=" + atcUpdInd + ", traAcsTermInd=" + traAcsTermInd
				+ ", traAcsTermFunCode=" + traAcsTermFunCode + ", exPayTransIPAN=" + exPayTransIPAN + ", folioNum="
				+ folioNum + ", roomNum=" + roomNum + ", lodRefNum=" + lodRefNum + ", roomRt=" + roomRt
				+ ", programInd=" + programInd + ", duration=" + duration + ", extraChrgs=" + extraChrgs
				+ ", rentalCity=" + rentalCity + ", rentalState=" + rentalState + ", rentalCtry=" + rentalCtry
				+ ", rentalDate=" + rentalDate + ", rentalTime=" + rentalTime + ", returnCity=" + returnCity
				+ ", returnState=" + returnState + ", returnCtry=" + returnCtry + ", returnDate=" + returnDate
				+ ", returnTime=" + returnTime + ", amtExtraChrgs=" + amtExtraChrgs + ", renterName=" + renterName
				+ ", autoAgreeNum=" + autoAgreeNum + ", rentalDuration=" + rentalDuration + ", rentalExtraChrgs="
				+ rentalExtraChrgs + ", autoNoShow=" + autoNoShow + ", delChrgInd=" + delChrgInd + ", keyID=" + keyID
				+ ", encrptBlock=" + encrptBlock + ", tknType=" + tknType + ", tkn=" + tkn + ", sctyKeyUpdInd="
				+ sctyKeyUpdInd + ", taSctyKey=" + taSctyKey + ", taExpDate=" + taExpDate + ", caKeyID=" + caKeyID
				+ ", numPINDigits=" + numPINDigits + ", emvData=" + emvData + ", cardSeqNum=" + cardSeqNum
				+ ", xCodeResp=" + xCodeResp + ", servCode=" + servCode + ", appExpDate=" + appExpDate + ", carc="
				+ carc + ", procInd=" + procInd + ", procInfo=" + procInfo + ", finAmtInd=" + finAmtInd + ", sctyLvl="
				+ sctyLvl + ", encrptType=" + encrptType + ", encrptTrgt=" + encrptTrgt + ", purchCardlvl2Grp="
				+ purchCardlvl2Grp + ", purchCardlvl3Grp=" + purchCardlvl3Grp + "]";
	}

	
}