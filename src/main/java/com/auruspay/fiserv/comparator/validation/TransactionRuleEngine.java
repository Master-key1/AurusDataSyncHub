package com.auruspay.comparator.validation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.auruspay.comparator.model.ValidationResult;
import com.auruspay.comparator.service.EmvParsers;
import com.auruspay.comparator.util.EMVParser;
import com.auruspay.dto.TransactionContext;
import com.auruspay.service.ServiceProvider;

@Component
public class TransactionRuleEngine {
	@Autowired
	private ServiceProvider serviceProvider;
	
	private static final List<String> RESTRICTED_COUNTRIES = List.of("Mexican", "Jamaica", "Bahamas", "Barbados",
			"StVincent", "Anguilla", "StKittsNevis", "StLucia");
	// Define this at the class level (as a constant)
	private static final Set<Integer> VALID_ENTRY_MODES = Set.of(0, 1, 3, 4, 5, 7, 8, 9, 10, 79, 80, 82, 86, 90, 91,
			95);
	private static final Map<String, Set<String>> PROGRAM_TXN_MAP = Map.of("InComm",
			Set.of("Activation", "Authorization", "Reload", "BalanceInquiry", "Redemption", "Refund"), "Solspark",
			Set.of("Activation"), "Blackhawk", Set.of("Activation", "Reload"), "ConnectPay",
			Set.of("Authorization", "Completion", "Refund", "Sale"), "NetSpend", Set.of("Activation", "Reload"));
	Map<String, String> emvTags;

	public Map<String, String> getEmvTags() {
		return emvTags;
	}

	public void setEmvTags(Map<String, String> emvTags) {
		this.emvTags = emvTags;
	}

	public static List<String> getRestrictedCountries() {
		return RESTRICTED_COUNTRIES;
	}

	public static Set<Integer> getValidEntryModes() {
		return VALID_ENTRY_MODES;
	}

	public static Map<String, Set<String>> getProgramTxnMap() {
		return PROGRAM_TXN_MAP;
	}

	public String validatePLPOSDebitRules(TransactionContext ctx) {

		if (ctx == null) {
			return "TransactionContext cannot be null.";
		}

		String nonUSMerch = ctx.getNonUSMerch();
		String paymentType = ctx.getPymtType();
		String settleTransType = ctx.getSettlementTxnType();
		String plPosDebitFlg = ctx.getPlposDebitFlg();
		String track2 = ctx.getTrack2Data();
		String encryptionBlock = ctx.getEncrptBlock();
		String txnType = ctx.getTxnType();

		// Rule 1: Canadian Merchant Check
		if ("Canadian".equalsIgnoreCase(nonUSMerch)) {
			return "PLPOSDebitFlg not allowed for Canadian merchants.";
		}

		// Rule 2: Payment/Transaction Type Check
		if (!"Credit".equalsIgnoreCase(paymentType) || !List.of("Authorization", "Sale", "Refund").contains(txnType)) {
			return "PLPOSDebitFlg only allowed for Credit Authorization, Sale, or Refund.";
		}

		// Rule 3: Settlement Transaction Type Check
		if ("5".equals(settleTransType)) {
			return "PLPOSDebitFlg cannot be sent when Settlement Transaction Type is '5' (Refund Credit).";
		}

		// Rule 4: Mandatory Data Check
		if ("1".equals(plPosDebitFlg)) {
			if (isBlank(track2) && isBlank(encryptionBlock)) {
				return "PLPOSDebitFlg '1' requires either Track 2 Data or an Encryption Block.";
			}
		}

		// All validations passed
		return null;
	}

	/**
	 * Returns true if the string is null, empty, or contains only whitespace.
	 */
	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	public String validateRefNumRules(TransactionContext ctx) {
		String refNum = ctx.getRefNum();
		String settleInd = ctx.getSettleInd();

		// Rule: Length based on Settlement Indicator
		if ("3".equals(settleInd)) {
			if (refNum.length() > 22)
				return "RefNum length must be <= 22 when Settlement Indicator is 3.";
		} else if (ctx.isBatchSettleDetail()) {
			if (refNum.length() > 10)
				return "RefNum length must be <= 10 for BatchSettleDetail.";
		} else {
			if (!refNum.matches("\\d+") || refNum.length() > 12)
				return "RefNum must be numeric and <= 12 digits.";
		}

		// Rule: Canadian Debit Group formatting
		if (ctx.isCanadianDebitGroup()) {
			if (!refNum.startsWith("0000"))
				return "RefNum for Canadian Debit must start with '0000'.";
			if (refNum.length() != 12)
				return "RefNum for Canadian Debit must be 12 bytes long.";
		}

		// Rule: Paypal restriction
		if ("Paypal".equals(ctx.getPymtType()) && refNum.length() > 12) {
			return "Paypal RefNum must be 12 bytes maximum.";
		}

		return null;
	}

	public String validatePaymentTypeRules(TransactionContext ctx) {

		// Rule: BatchSettleDetail supports specific types
		if ("BatchSettleDetail".equals(ctx.getTxnType())) {
			List<String> allowed = List.of("Credit", "Debit", "PLDebit", "EBT", "PvtLabl");
			if (!allowed.contains(ctx.getPymtType())) {
				return "Payment Type " + ctx.getPymtType() + " not supported for BatchSettleDetail.";
			}
		}

		// Rule: PLDebit only for Ecommerce Bill Payment and FraudScore
		if ("PLDebit".equals(ctx.getPymtType()) && !"Ecommerce Bill Payment".equals(ctx.getTxnType())
				&& !"FraudScore".equals(ctx.getTxnType())) {
			return "PLDebit is only applicable to Ecommerce Bill Payment and FraudScore transactions.";
		}

		// Rule: Caribbean/Latin America country restrictions
		if ("Mexican".equals(ctx.getNonUSMerch())) {
			List<String> allowed = List.of("Credit", "PvtLabl", "Prepaid");
			if (!allowed.contains(ctx.getPymtType())) {
				return "For Mexican merchants, only Credit, PvtLabl, and Prepaid are supported.";
			}
		}

		return null;
	}

	public String validateSettleIndRules(TransactionContext ctx) {

		String txnType = ctx.getTxnType();
		String paymentType = ctx.getPymtType();
		String settleInd = ctx.getSettleInd();
		String nonUSMerchant = ctx.getNonUSMerch();
		// Rule: Must be 3 for Check Completion
		if ("Completion".equals(txnType) && "Check".equals(paymentType) && !"3".equals(settleInd)) {
			return "Check Completion transactions must have Settlement Indicator '3'.";
		}

		// Rule: If settleInd is 1, Debit Sale only supported if NonUSMerchant is
		// 'Canadian'
		if ("1".equals(settleInd) && "Debit".equals(paymentType) && !"Canadian".equals(nonUSMerchant)) {
			return "Settlement Indicator '1' for Debit Sales requires Non-US Merchant = 'Canadian'.";
		}

		// Rule: If settleInd is 2 or 3, specific countries are not allowed
		if (("2".equals(settleInd) || "3".equals(settleInd)) && RESTRICTED_COUNTRIES.contains(nonUSMerchant)) {
			return "Settlement Indicator 2 or 3 is not allowed for the specified Non-US Merchant country: "
					+ nonUSMerchant;
		}

		// Return null if all rules pass
		return null;
	}

	private String validateTxnTypeRules(TransactionContext ctx) {
		if ("BalanceInquiry".equals(ctx.getTxnType())
				&& !List.of("Credit", "Debit", "EBT", "Prepaid", "Generic Prepaid").contains(ctx.getPymtType())) {
			return "BalanceInquiry not supported for Payment Type: " + ctx.getPymtType();
		}
		if ("ProductEligInquiry".equals(ctx.getTxnType()) && !"Visa".equals(ctx.getCardType())) {
			return "ProductEligInquiry is only applicable when Card Type is 'Visa'.";
		}
		if ("CanadaKeyRequest".equals(ctx.getTxnType()) && !"Canadian".equals(ctx.getNonUSMerch())) {
			return "CanadaKeyRequest is only applicable to Canadian merchants.";
		}
		return null;
	}

	private boolean isEmpty(String val) {
		return val == null || val.trim().isEmpty();
	}

	public String validateOrderNumRules(TransactionContext ctx) {
		String orderNum = ctx.getOrderNum();
		String settleInd = ctx.getSettleInd();

		// Rule: Settlement Indicator 3 requirements
		if ("3".equals(settleInd)) {
			if (!orderNum.matches("\\d+") || orderNum.length() > 8) {
				return "Order Number must be numeric and <= 8 digits when Settlement Indicator is 3.";
			}
		}
		/*
		 * // Rule: MOTO and Ecommerce mandatory check if
		 * (("MOTO".equals(ctx.getIndustryType()) ||
		 * "Ecomm".equals(ctx.getIndustryType())) && (orderNum == null ||
		 * orderNum.isEmpty())) { return
		 * "Order Number is mandatory for all MOTO and Ecommerce financial transactions."
		 * ; }
		 * 
		 * // Rule: Check transaction restriction if ("Check".equals(ctx.getPymtType())
		 * && ctx.getMicr() != null && orderNum != null) { return
		 * "Order Number must not be sent on Check transactions when MICR field is present."
		 * ; }
		 * 
		 * // Rule: Settlement Indicator 1 requirement if ("1".equals(settleInd) &&
		 * "Retail".equals(ctx.getIndustryType()) && (orderNum == null)) { return
		 * "Order Number is mandatory for Retail transactions with Settlement Indicator 1."
		 * ; }
		 */
		return null; // Valid
	}

	public String validateMerchCatCodeRules(TransactionContext ctx) {
		String mcc = ctx.getMerchCatCode();

		// Rule: CancelDeferredAuth restriction
		if ("CancelDeferredAuth".equals(ctx.getTxnType()) && !"3".equals(ctx.getSettleInd())) {
			return "MerchCatCode can only be sent on 'CancelDeferredAuth' when Settlement Indicator is '3'.";
		}
		/*
		 * // Rule: Multi-MID Payment Facilitator restriction if
		 * (ctx.isMultiMidPaymentFacilitator() && !ctx.hasExistingDebtIndicator()) {
		 * return
		 * "MerchCatCode must not be sent for Multi-MID Payment Facilitators unless debt indicators are present."
		 * ; }
		 * 
		 * // Rule: Visa MCC 5411 restriction if ("Visa".equals(ctx.getCardType()) &&
		 * "5411".equals(mcc)) { if ("Card Present".equals(ctx.getEnvironment())) { //
		 * Assuming context has this return
		 * "MCC 5411 is only permitted for Card Not Present transactions when Card Type is Visa."
		 * ; } }
		 */

		return null; // Valid
	}

	public String validatePOSEntryModeRules(TransactionContext ctx) {
		String entryMode = ctx.getPosEntryMode();
		String entryPart = entryMode.substring(0, 2);
		String authPart = entryMode.substring(2, 3);
		/*
		 * // Rule: Debit PIN capability requirement if
		 * ("Debit".equals(ctx.getPymtType()) &&
		 * !"05,09,11,14".contains(ctx.getVoidReason())) { // Simplified check if
		 * (!"14".contains(authPart)) { return
		 * "Debit transactions require PIN entry capability (1 or 4)."; } }
		 * 
		 * // Rule: AMEX Digital Wallet (08) if ("08".equals(entryPart) &&
		 * !"Amex".equals(ctx.getCardType())) { return
		 * "POS Entry Mode 08 is only applicable for AMEX transactions."; }
		 * 
		 * // Rule: MasterCard Remote (09) if ("09".equals(entryPart)) { if
		 * (!"MasterCard".equals(ctx.getCardType())) return
		 * "Mode 09 is for MasterCard only."; if (ctx.getEmvData() == null) return
		 * "EMVData is mandatory when POS Entry Mode is 09."; }
		 */

		// Rule: Stored Credential/Stored Credentials (10)
		if ("10".equals(entryPart) && !"Subsequent".equals(ctx.getStoredCredInd())) {
			return "Mode 10 requires Stored Credential Indicator = 'Subsequent'.";
		}

		return null;
	}

	public String validatePOSCondCodeRules(TransactionContext ctx) {
		String posCondCode = ctx.getPosCondCode();
		/*
		 * // Rule: Check Service Provider enforcement if
		 * ("TeleCheck".equals(ctx.getCheckServiceProvider()) &&
		 * !"06".equals(posCondCode)) { return
		 * "POS Condition Code must be '06' for TeleCheck provider."; }
		 */

		// Rule: PINLess Debit enforcement
		if ("PINLessDebit".equals(ctx.getPymtType()) && !"04,59".contains(posCondCode)) {
			return "PINLess Debit POS Condition Code must be '04' or '59'.";
		}

		// Rule: Visa Code 71 restrictions
		if ("71".equals(posCondCode)) {
			if (!"Visa".equals(ctx.getCardType()))
				return "Code '71' is only valid for Visa.";
			if (ctx.getNonUSMerch() != null)
				return "Code '71' is not allowed for Non-US merchants.";
		}
		/*
		 * // Rule: Canadian Debit In-App requirement if (ctx.isCanadianDebitInApp() &&
		 * !"59".equals(posCondCode)) { return
		 * "Canadian Debit In-App transactions must use POS Condition Code '59'."; }
		 */
		// Rule: Canadian Debit In-App requirement
		if (ctx.isCanadianDebitGroup() && !"59".equals(posCondCode)) {
			return "Canadian Debit In-App transactions must use POS Condition Code '59'.";
		}

		return null; // Valid
	}

	public String validateCrossFieldRules(TransactionContext ctx) {
		String termCat = ctx.getTermCatCode();// data.get("TermCatCode");
		String posCond = ctx.getPosCondCode();// data.get("POSCondCode");
		String txnType = ctx.getTxnType();// data.get("TxnType");

		// Example Rule from Image: The value of 05 (AFD) is not applicable when
		// Transaction Type = 'Refund'
		if ("05".equals(termCat) && "Refund".equals(txnType)) {
			return "Validation Error: TermCatCode 05 is not applicable for Refund transactions.";
		}

		// Example Rule: For MOTO (POS Condition Code = 08), this field must be 00
		if ("08".equals(posCond) && !"00".equals(termCat)) {
			return "Validation Error: MOTO transactions must have TermCatCode 00.";
		}
		return null;
	}

	public String validateMandatoryRules(TransactionContext ctx) {
		String txnType = ctx.getTxnType(); // data.get("TxnType");
		String settlementInd = ctx.getSettleInd(); // .get("SettlementIndicator");
		String termEntryCap = ctx.getTermEntryCapablt();// data.get("TermEntryCapablt");

		// Rule: Mandatory for 'CancelDeferredAuth' when SettlementIndicator = '3'
		if ("CancelDeferredAuth".equals(txnType) && "3".equals(settlementInd)) {
			if (termEntryCap == null || termEntryCap.isEmpty() || "TAG MISSING".equals(termEntryCap)) {
				return "Validation Error: TermEntryCapablt is mandatory for CancelDeferredAuth when SettlementIndicator is 3.";
			}
		}
		return null;
	}

	public String validateFinancialRules(TransactionContext ctx) {
		String txnCrncy = ctx.getTxnCrncy();
		String settlementInd = ctx.getSettleInd();

		// Rule: When Settlement Indicator is 2, only valid currency code is 840
		if ("2".equals(settlementInd) && !"840".equals(txnCrncy)) {
			return "Validation Error: For SettlementIndicator 2, TxnCrncy must be 840.";
		}

		// Rule: When Settlement Indicator is 3, currencies with 3 minor units must not
		// be sent.
		// Assuming 3-minor unit currencies include codes like 972 (TND), 051 (AMD),
		// etc.
		if ("3".equals(settlementInd) && isCurrencyWithThreeMinorUnits(txnCrncy)) {
			return "Validation Error: Currencies with 3 minor units are not allowed for SettlementIndicator 3.";
		}

		return null;
	}

	/**
	 * Helper to identify currencies with 3 minor units. You can expand this set
	 * based on ISO 4217 standards.
	 */
	private boolean isCurrencyWithThreeMinorUnits(String currencyCode) {
		// Sample list of ISO 4217 codes with 3 decimal places
		Set<String> threeMinorUnitCurrencies = Set.of("051", // AMD
				"060", // BMD
				"084", // BZD
				"104", // MMK
				"262", // DJF
				"972" // TND
		);
		return threeMinorUnitCurrencies.contains(currencyCode);
	}

	private String validateTerminalEnvironmentRules(TransactionContext ctx) {
		String termCat = ctx.getTermCatCode();
		String posCond = ctx.getPosCondCode();
		String pymtType = ctx.getPymtType();
		String txnType = ctx.getTxnType();

		// Rule: MOTO (POS Cond Code 08) must be 00
		if ("08".equals(posCond) && !"00".equals(termCat)) {
			return "Validation Error: MOTO transactions must have TermCatCode 00.";
		}

		// Rule: Ecommerce (POS Cond Code 59) must be 00 except specific cases
		if ("59".equals(posCond) && !"00".equals(termCat)) {
			// Exceptions for 59: PLDebit (00 or 13), TeleCheckPPD (00 or 13),
			// Credit/PvtLabl (00 or 05)
			boolean isPLDebitException = "PLDebit".equals(pymtType) && Set.of("00", "13").contains(termCat);
			boolean isTeleCheckException = "TeleCheckPPD".equals(ctx.getCheckServiceProvider())
					&& Set.of("00", "13").contains(termCat);
			boolean isCreditException = Set.of("Credit", "PvtLabl").contains(pymtType)
					&& Set.of("00", "05").contains(termCat);

			if (!isPLDebitException && !isTeleCheckException && !isCreditException) {
				return "Validation Error: TermCatCode mismatch for Ecommerce transaction.";
			}
		}

		// Rule: AFD (05) is not applicable for Refund
		if ("05".equals(termCat) && "Refund".equals(txnType)) {
			return "Validation Error: TermCatCode 05 (AFD) is not applicable for Refund.";
		}

		// Rule: IVR (13) validity
		if ("13".equals(termCat) && !("3".equals(ctx.getSettleInd())
				&& ("PLDebit".equals(pymtType) || "TeleCheckPPD".equals(ctx.getCheckServiceProvider())))) {
			return "Validation Error: IVR (13) is only valid for PLDebit or TeleCheckPPD with SettlementIndicator 3.";
		}

		return null;
	}

	// Logic for rules involving Card Capture and Track Data
	private String validateTerminalSecurityRules(TransactionContext ctx) {
		// Rule: If CardCaptCap is 0, Track2Data should not be present
		if ("0".equals(ctx.getCardCaptCap()) && isPresent(ctx.getTrack2Data())) {
			return "Validation Error: Track2Data must not be present when CardCaptCap is 0.";
		}

		// Rule: POSID is mandatory for mobile transactions at an AFD (TermCatCode 05)
		if ("05".equals(ctx.getTermCatCode()) && !isPresent(ctx.getPosID())) {
			return "Validation Error: POSID is mandatory for AFD mobile transactions.";
		}

		return null;
	}

	// Helper to handle "TAG MISSING" vs null vs empty strings consistently
	private boolean isPresent(String value) {
		return value != null && !value.isEmpty() && !"TAG MISSING".equals(value);
	}

	private String validateProgramIDRules(TransactionContext ctx) {
		// String progID = ctx.getProgramID();
		String progID = null;
		String txnType = ctx.getTxnType();

		// 1. Transaction Type Check
		if (PROGRAM_TXN_MAP.containsKey(progID)) {
			if (!PROGRAM_TXN_MAP.get(progID).contains(txnType)) {
				return "Validation Error: ProgramID '" + progID + "' does not support TxnType: " + txnType;
			}
		}

		// 2. Debit / PIN Data Requirement Check
		if ("ConnectPay".equals(progID) && "Debit".equalsIgnoreCase(ctx.getPymtType())) {
			boolean pinMissing = !isPresent(ctx.getPinData());

			// Rule: If Debit Authorization and PIN missing, ConnectPay is mandatory
			if ("Authorization".equals(txnType) && pinMissing) {
				return "Validation Error: ConnectPay required for Debit Authorization when PIN is missing.";
			}
			/*
			 * // Rule: If Debit Sale/Refund and PIN missing (unless Canadian In-App),
			 * ConnectPay mandatory boolean isCanadianInApp =
			 * "Canadian".equals(ctx.getMerchType()) &&
			 * "InApp".equals(ctx.getEntryMethod()); if (Set.of("Sale",
			 * "Refund").contains(txnType) && pinMissing && !isCanadianInApp) { return
			 * "Validation Error: ConnectPay required for Debit Sale/Refund when PIN is missing."
			 * ; }
			 */
		}

		return null;
	}

	private String validateSettlementRules(TransactionContext ctx) {
		String settleInd = ctx.getSettleInd();
		String txnType = ctx.getTxnType();
		String nonUSMerch = ctx.getNonUSMerch();

		// 1. Payment Type Indicator Constraint
		// "When the Payment Type Indicator field is present, this field must not
		// contain 1 or 2."
		/*
		 * if (isPresent(ctx.getPymtTypeIndicator()) && Set.of("1",
		 * "2").contains(settleInd)) { return
		 * "Validation Error: SettleInd 1 or 2 not allowed when Payment Type Indicator is present."
		 * ; }
		 */

		// 2. Debit Sale/Canadian Merchant Rule
		// "When this field contains value 1, Debit Sale transactions only supported if
		// NonUSMerch is Canadian."
		if ("1".equals(settleInd) && "Debit".equals(ctx.getPymtType()) && "Sale".equals(txnType)) {
			if (!"Canadian".equals(nonUSMerch)) {
				return "Validation Error: SettleInd 1 requires NonUSMerch to be Canadian for Debit Sales.";
			}
		}

		// 3. Geographic Restrictions (Values 2 or 3)
		// Rule: Non US Merchant value cannot be Mexico, Jamaica, etc.
		Set<String> restrictedGeos = Set.of("Mexican", "Jamaica", "Bahamas", "Barbados", "StVincent", "Anguilla",
				"StKittsNevis", "StLucia");
		if (Set.of("2", "3").contains(settleInd) && restrictedGeos.contains(nonUSMerch)) {
			return "Validation Error: SettleInd 2/3 not allowed for merchant region: " + nonUSMerch;
		}

		return null;
	}

	private String validateTranInitRules(TransactionContext ctx) {
		String tranInit = ctx.getTranInit();
		boolean isMissing = !isPresent(tranInit);

		// Rule: Mandatory if ACI, MCACI, DiscACI, or AmexACI is 'I'
		if (isMissing && Set.of("I", "I", "I", "I").contains(ctx.getAci())) { // Simplified check
			return "Validation Error: TranInit is mandatory when Auth Indicator (ACI) is 'I'.";
		}

		// Rule: Mandatory for Private Label/Fleet when EMV Group is present
		if (isMissing && "PrivateLabel".equals(ctx.getPymtType()) && isPresent(ctx.getEmvData())) {
			return "Validation Error: TranInit is mandatory for Private Label/Fleet with EMV Group.";
		}

		// Rule: Mandatory if VisaAuthInd or StoredCredInd is 'CrdOnFile'
		if (isMissing && ("CrdOnFile".equals(ctx.getVisaAuthInd()) || "CrdOnFile".equals(ctx.getStoredCredInd()))) {
			return "Validation Error: TranInit is mandatory for CrdOnFile transactions.";
		}
		/*
		 * // Rule: MasterCard Merchant-Initiated Industry Practice (e.g., partial
		 * shipment) if (isMissing && "MasterCard".equals(ctx.getCardType()) &&
		 * isMerchantInitiatedPractice(ctx)) { return
		 * "Validation Error: TranInit is mandatory for MasterCard Merchant-Initiated transactions."
		 * ; }
		 */

		return null;
	}

	private String validateAccountRules(TransactionContext ctx) {
		String acctNum = ctx.getAcctNum();
		boolean isPresent = isPresent(acctNum);

		/*
		 * // Rule: Must not be present if TransArmor token/encryption is used if
		 * (isPresent && (isPresent(ctx.getTransArmorToken()) ||
		 * isPresent(ctx.getEncrptBlock()))) { return
		 * "Validation Error: AcctNum must not be present when token or encryption block is used."
		 * ; }
		 */
		// Rule: Mandatory for Manual Entry (POS Entry Mode 01 or 79)
		if (Set.of("01", "79").contains(ctx.getPosEntryMode()) && !"SpeedPass".equals(ctx.getCardType())
				&& !isPresent) {
			return "Validation Error: AcctNum mandatory for manual entry (unless SpeedPass).";
		}

		// Rule: Payal Transactions require 16-digit Certification Number
		if ("Paypal".equals(ctx.getCardType()) && isPresent && acctNum.length() != 16) {
			return "Validation Error: Paypal transactions require a 16-digit AcctNum/Certification Number.";
		}

		// Rule: Sunoco/Valero exception (limit 19 digits)
		if (isPresent && !Set.of("Sunoco", "Valero").contains(ctx.getCardType()) && acctNum.length() > 19) {
			return "Validation Error: AcctNum limited to 19 digits for non-Sunoco/Valero cards.";
		}

		return null;
	}

	private String validateCardExpiryRules(TransactionContext ctx) {
		String expiry = ctx.getCardExpiryDate();
		boolean isPresent = isPresent(expiry);

		// Rule: Exclude if included in Encryption Block (RSA/AESDUKPT)
		if (isPresent && (ctx.getEncrptBlock().isEmpty())) {
			return "Validation Error: CardExpiryDate should be omitted when included in encryption block.";
		}

		/*
		 * // Rule: Mandatory for Credit Refund with Payment Token if (!isPresent &&
		 * "Credit".equals(ctx.getPymtType()) && "Refund".equals(ctx.getTxnType()) &&
		 * isPresent(ctx.getToken())) { return
		 * "Validation Error: CardExpiryDate is mandatory for tokenized Credit Refund.";
		 * }
		 */

		// Rule: Mandatory when Encryption Target is 'PAN' and Type is 'RSA' or
		// 'AESDUKPT'
		if (!isPresent && "PAN".equals(ctx.getEncrptTrgt())
				&& Set.of("RSA", "AESDUKPT").contains(ctx.getEncrptType())) {
			return "Validation Error: CardExpiryDate must be sent for RSA/AESDUKPT PAN encryption.";
		}

		// Rule: Must not be sent for Payal
		if (isPresent && "Paypal".equals(ctx.getCardType())) {
			return "Validation Error: CardExpiryDate must not be sent for Paypal transactions.";
		}

		return null;
	}

	private String validateCardTypeRules(TransactionContext ctx) {
		String cardType = ctx.getCardType();
		String nonUSMerch = ctx.getNonUSMerch(); // e.g., 'Europe', 'APAC', 'Canadian'
		String txnType = ctx.getTxnType();

		// Rule: Geography-based restrictions (Europe/APAC)
		if ("Europe".equals(nonUSMerch)
				&& !Set.of("Visa", "MasterCard", "MaestroInt", "JCB", "Diners", "UnionPay").contains(cardType)) {
			return "Validation Error: CardType " + cardType + " not allowed for Europe merchant.";
		}

		// Rule: UnionPay specific restrictions
		if ("UnionPay".equals(cardType) && !"Canadian".equals(nonUSMerch) && !"Europe".equals(nonUSMerch)
				&& !"APAC".equals(nonUSMerch)) {
			return "Validation Error: UnionPay only supported for Canadian, Europe, or APAC merchants.";
		}

		// Rule: PPayCL (Prepaid Closed Loop) restricted transaction types
		if ("PPayCL".equals(cardType)) {
			Set<String> allowed = Set.of("Activation", "Load", "Redemption", "RedemptionUnlock", "Reload",
					"BalanceInquiry", "BalanceLock", "Cashout", "CashoutActiveStatus", "Refund");
			if (!allowed.contains(txnType)) {
				return "Validation Error: PPayCL does not support " + txnType;
			}
		}

		return null;
	}

	public String validateAVS(TransactionContext ctx) {
		String avs = ctx.getAvsResultCode();
		String cardType = ctx.getCardType();
		String networkId = null; // ctx.getAuthorizingNetworkId();

		// Rule: AVS must not be returned for PPayCL (Prepaid Closed Loop)
		if ("PPayCL".equals(cardType) && isPresent(avs)) {
			return "Rule Violation: AVS Result Code must not be returned for PPayCL.";
		}

		// Rule: Completion and BatchSettleDetail must carry over AVS results from
		// original Auth
		if (Set.of("Completion", "BatchSettleDetail").contains(ctx.getTxnType())) {
			if (ctx.isOriginalAuthProcessedWithAVS() && !isPresent(avs)) {
				return "Rule Violation: AVS Result Code is mandatory for Completion/BatchSettle when original Auth had AVS.";
			}
		}

		// Rule: Network-specific validation strategy
		if ("06".equals(networkId)) { // PULSE Network example
			return validateSet(avs, Set.of("0", "A", "E", "N", "R", "S", "U", "W", "X", "Y", "Z"), "PULSE");
		} else if ("Visa".equals(cardType)) {
			return validateSet(avs, Set.of("A", "N", "R", "U", "Y", "Z"), "Visa");
		}

		return null; // Passed checks
	}

	private String validateSet(String code, Set<String> validCodes, String context) {
		return validCodes.contains(code) ? null : "Rule Violation: Invalid AVS code '" + code + "' for " + context;
	}

	private String validateCCVIndRules(TransactionContext ctx) {
		String ccvInd = ctx.getCcvInd();
		boolean isPresent = isPresent(ccvInd);

		// Rule: Do not send if CCV data is in the Onguard Encryption Block
		if (isPresent && isPresent(ctx.getEncrptBlock())) {
			return "Rule Violation: CCVInd must not be sent when data is in the Onguard Encryption Block.";
		}

		// Rule: Refund transaction requirement
		if ("Refund".equals(ctx.getTxnType()) && isPresent && !isPresent(ctx.getRefundType())) {
			return "Rule Violation: CCVInd for Refund requires RefundType to be present.";
		}

		// Rule: GenProp Card Type restriction
		if ("GenProp".equals(ctx.getCardType()) && isPresent && !"59".equals(ctx.getPosCondCode())) {
			return "Rule Violation: CCVInd for GenProp is only applicable when POSConditionCode is 59.";
		}

		return null;
	}

	private String validateCCVDataRules(TransactionContext ctx) {
		String ccvData = ctx.getCcvData();
		boolean isPresent = isPresent(ccvData);

		// Rule: When CCVData is present, CCVInd must be 'Prvded'
		if (isPresent && !"Prvded".equals(ctx.getCcvInd())) {
			return "Rule Violation: CCVData present, but CCVInd is not 'Prvded'.";
		}

		// Rule: Do not send if CCV data is in the Onguard Encryption Block
		if (isPresent && isPresent(ctx.getEncrptBlock())) {
			return "Rule Violation: CCVData must not be sent when data is in the Onguard Encryption Block.";
		}

		// Rule: Refund transaction requirement
		if ("Refund".equals(ctx.getTxnType()) && isPresent && !isPresent(ctx.getRefundType())) {
			return "Rule Violation: CCVData for Refund requires RefundType to be present.";
		}

		// Rule: GenProp Card Type restriction
		if ("GenProp".equals(ctx.getCardType()) && isPresent && !"59".equals(ctx.getPosCondCode())) {
			return "Rule Violation: CCVData for GenProp is only applicable when POSConditionCode is 59.";
		}

		return null;
	}

	private String validateCCVResultCodeRules(TransactionContext ctx) {
		String ccvResult = ctx.getAvsResultCode();
		boolean isPresent = isPresent(ccvResult);

		// Rule: Persistence across transaction lifecycle
		// "If present in the Authorization Response, this field must be present in the
		// Completion transaction."
		if ("Completion".equals(ctx.getTxnType()) && ctx.wasOriginalAuthProcessedWithCCV() && !isPresent) {
			return "Rule Violation: CCVResultCode is mandatory in Completion when original Authorization had CCV validation.";
		}

		// Rule: GenProp Card Type restriction
		if ("GenProp".equals(ctx.getCardType()) && isPresent && !"59".equals(ctx.getPosCondCode())) {
			return "Rule Violation: CCVResultCode for GenProp is only applicable when POSConditionCode is 59.";
		}

		return null;
	}

	private String validatePINDataRules(TransactionContext ctx) {
		String pinData = ctx.getPinData();
		boolean isPresent = isPresent(pinData);

		/*
		 * // --- Debit Transaction Rules --- if ("Debit".equals(ctx.getPymtType())) {
		 * if (!Set.of("Completion", "Void", "FullReversal").contains(ctx.getTxnType())
		 * && !"ConnectPay".equals(ctx.getProgramID()) && !isPresent) { return
		 * "Rule Violation: PINData is mandatory for PIN Debit."; // } if
		 * ("Reversal".equals(ctx.getTxnType()) && isPresent &&
		 * !"0000000000000000".equals(pinData)) { return
		 * "Rule Violation: PINData for reversals must be all zeroes."; // } }
		 * 
		 */
		// --- EBT Transaction Rules ---
		if ("EBT".equals(ctx.getPymtType())) {
			if ("VoucherClear".equals(ctx.getTxnType()) && isPresent) {
				return "Rule Violation: PINData not allowed for EBT Voucher Clear."; //
			}
			if ("Reversal".equals(ctx.getTxnType()) && isPresent && !"0000000000000000".equals(pinData)) {
				return "Rule Violation: PINData for EBT reversals must be all zeroes."; //
			}
		}

		// --- Credit Transaction Rules ---
		if ("Credit".equals(ctx.getPymtType())) {
			// Must have EMV Group, unless CardType is UnionPay
			if (!isPresent(ctx.getEmvData()) && !"UnionPay".equals(ctx.getCardType()) && isPresent) {
				return "Rule Violation: PINData for Credit requires EMV Group (except UnionPay)."; //
			}
			// If present, Key Serial Number Data is mandatory (unless Canadian or HSM
			// present)
			if (isPresent && !"Canadian".equals(ctx.getNonUSMerch()) && !ctx.isHsmSupported()
					&& !isPresent(ctx.getKeySerialNumData())) {
				return "Rule Violation: Key Serial Number Data mandatory when PINData is present in Credit."; //
			}
		}

		// --- Fleet Transaction Rules ---
		if ("Fleet".equals(ctx.getPymtType())) {
			if (isPresent && !isPresent(ctx.getEmvData())) {
				return "Rule Violation: PINData for Fleet requires EMV Group."; //
			}
			if (isPresent && !isPresent(ctx.getKeySerialNumData())) {
				return "Rule Violation: Key Serial Number Data mandatory for Fleet PINData."; //
			}
		}

		return null;
	}

	private String validateKeySerialNumDataRules(TransactionContext ctx) {
		String ksn = ctx.getKeySerialNumData();
		boolean isPresent = isPresent(ksn);

		/*
		 * // Rule: Mandatory for PIN Debit (unless ConnectPay or EBT
		 * Completion/Refunds) if ("Debit".equals(ctx.getPymtType()) &&
		 * !"ConnectPay".equals(ctx.getProgramID())) { if (!isEbtException(ctx) &&
		 * !ctx.isMerchantUsingHSM() && !isPresent) { return
		 * "Rule Violation: KSN is mandatory for PIN Debit transactions."; } }
		 * 
		 * // Rule: Must not be present when merchant supports Host Security Module
		 * (HSM) if (isPresent && ctx.isMerchantUsingHSM()) { return
		 * "Rule Violation: KSN must not be present when the merchant supports an HSM.";
		 * }
		 * 
		 * // Rule: Offline/No-PIN contactless transactions if
		 * (isOfflineOrNoPinContactless(ctx) && !"00000000000000000000".equals(ksn)) {
		 * return
		 * "Rule Violation: KSN must contain 20 zeroes for offline/no-PIN contactless transactions."
		 * ; }
		 * 
		 */
		// Rule: UnionPay exception for Credit
		if ("Credit".equals(ctx.getPymtType()) && !isPresent(ctx.getEmvData())
				&& !"UnionPay".equals(ctx.getCardType())) {
			if (isPresent) {
				return "Rule Violation: KSN not allowed for non-EMV Credit transactions (unless UnionPay).";
			}
		}

		return null;
	}

	private String validateAdditionalAmountRules(TransactionContext ctx) {
		/*
		 * List<String> types = ctx.getAdditionalAmountTypes(); // Extract all present
		 * types
		 * 
		 * // Rule: Clinical, Copay, Dental, RX, or Vision require Hltcare Set<String>
		 * subHealth = Set.of("Clinical", "Copay", "Dental", "RX", "Vision"); if
		 * (types.stream().anyMatch(subHealth::contains) && !types.contains("Hltcare"))
		 * { return
		 * "Rule Violation: Hltcare amount is mandatory when Clinical, Copay, Dental, RX, or Vision is present."
		 * ; }
		 * 
		 * // Rule: Hltcare requires Market Specific Data Indicator = 'Healthcare' if
		 * (types.contains("Hltcare") &&
		 * !"Healthcare".equals(ctx.getMrktSpecificDataInd())) { return
		 * "Rule Violation: Market Specific Data Indicator must be 'Healthcare' when Hltcare is present."
		 * ; }
		 * 
		 * // Rule: SettleInd 3 restriction (only Surchrg and FirstAuthAmt allowed) if
		 * ("3".equals(ctx.getSettleInd())) { for (String type : types) { if
		 * (!Set.of("Surchrg", "FirstAuthAmt").contains(type)) { return
		 * "Rule Violation: Only Surchrg and FirstAuthAmt allowed when SettleInd is 3.";
		 * } } }
		 * 
		 * // Rule: Cashback logic (Non-US Europe restriction) if
		 * (types.contains("Cashback") && "Europe".equals(ctx.getNonUSMerch())) { long
		 * amount = ctx.getAddAmtValue("Cashback"); // helper to get numeric value
		 * 
		 * if (amount < 1 || amount > 10000) { // 1 penny to 100 USD return
		 * "Rule Violation: Cashback must be between 0.01 and 100.00 USD equivalent."; }
		 * }
		 */
		return null;
	}

	private String validateAddAmtAcctTypeRules(TransactionContext ctx) {
		// Rule: "For each instance of Additional Amount in the response message,
		// this field must also be present."
		/*
		 * List<AdditionalAmount> amounts = ctx.getAdditionalAmounts();
		 * 
		 * for (AdditionalAmount amt : amounts) { if
		 * (!isPresent(amt.getAddAmtAcctType())) { return
		 * "Rule Violation: AddAmtAcctType is missing for Additional Amount Type: " +
		 * amt.getType(); } }
		 */

		return null;
	}

	private String validateEMVDataRules(TransactionContext ctx, Map<String, String> emvMap) {
		String emvData = ctx.getEmvData();
		boolean isPresent = isPresent(emvData);

		// Rule: Mandatory in all EMV transactions
		if (emvMap != null && !isPresent) {
			return "Rule Violation: EMVData is mandatory for all EMV transactions.";
		}

		// Rule: POS Entry Mode dependencies
		Set<String> entryModes = Set.of("05", "07", "80", "86", "91", "95");
		if (isPresent && entryModes.contains(ctx.getPosEntryMode())) {
			// Validation logic: Ensure tags 5A/57 are extracted to proper fields and NOT in
			// EMVData
			if (ctx.getEmvData().contains("5A") || ctx.getEmvData().contains("57")) {
				return "Rule Violation: EMV Tags 5A and 57 must not be present in the EMVData field.";
			}
		}

		// Rule: Terminal Entry Capability logic
		if (isPresent && !"01".equals(ctx.getTermEntryCapablt())) {
			if (!Set.of("04", "06", "08", "09", "12").contains(ctx.getTermEntryCapablt())) {
				return "Rule Violation: Invalid Terminal Entry Capability for EMV transaction.";
			}
		}

		return null;
	}

	private String validateCardSeqNumRules(TransactionContext ctx, Map<String, String> emvMap) {
		String cardSeq = ctx.getCardSeqNum();
		String emvTag5F34 = emvMap.get("5F34"); // Parsed from EMVData
		boolean isPresent = isPresent(cardSeq);

		// Rule: Must contain same value as EMV tag 5F34
		if (isPresent && emvTag5F34 != null) {
			if (!cardSeq.equals((emvTag5F34))) {
				return "Rule Violation: CardSeqNum must match EMV Tag 5F34.";
			}
		}

		// Rule: Mandatory omission if EMV Tag 5F34 is not available
		if (isPresent && emvTag5F34 == null) {
			return "Rule Violation: CardSeqNum must be omitted if EMV Tag 5F34 is not available.";
		}

		// Rule: POS Entry Mode 05 or 07 requirement
		if (Set.of("05", "07").contains(ctx.getPosEntryMode()) && emvTag5F34 != null && !isPresent) {
			return "Rule Violation: CardSeqNum is required for POS Entry Mode 05/07 when 5F34 is present.";
		}

		return null;
	}

	/**
	 * Orchestrator method to run all relevant rules for a transaction.
	 */
	public String validateAllRules(TransactionContext context) {
		String violation = null;
		Map<String, String> emvMap = null;
		if (context != null) {
			System.out.println("Context [validateAllRules]: " + context);

			if (context.getEmvData() != null && !context.getEmvData().isBlank()) {
				 EmvParsers emvParser = serviceProvider.getEmvParser();

				emvMap = emvParser.parseToMap(context.getEmvData());

				// Map<String, String> emvTags = EMVParser.parseToMap(emvData);

				emvMap.forEach((tag, value) -> System.out.println(tag + " = " + value));
			}
		}
		// Example usage in service
		String error1 = validateMandatoryRules(context);
		if (error1 != null)
			return error1;

		String error2 = validateFinancialRules(context);
		if (error2 != null)
			return error2;

		if ((violation = validateSettleIndRules(context)) != null)
			return violation;
		if ((violation = validateTxnTypeRules(context)) != null)
			return violation;
		if ((violation = validatePLPOSDebitRules(context)) != null)
			return violation;
		if ((violation = validateCardExpiryRules(context)) != null)
			return violation;
		if ((violation = validateOrderNumRules(context)) != null)
			return violation;
		if ((violation = validatePOSEntryModeRules(context)) != null)
			return violation;

		if ((violation = validatePOSCondCodeRules(context)) != null)
			return violation;
		if ((violation = validateCrossFieldRules(context)) != null)
			return violation;
		if ((violation = validateMandatoryRules(context)) != null)
			return violation;
		if ((violation = validateFinancialRules(context)) != null)
			return violation;
		if ((violation = validateTerminalSecurityRules(context)) != null)
			return violation;
		if ((violation = validateCardSeqNumRules(context, emvMap)) != null)
			return violation;
		if ((violation = validateEMVDataRules(context, emvMap)) != null)
			return violation;

		return violation;
	}
}
