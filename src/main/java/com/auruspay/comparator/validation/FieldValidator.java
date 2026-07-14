package com.auruspay.comparator.validation;

import com.auruspay.comparator.XmlComparator;
import com.auruspay.comparator.model.ValidationIssue;
import com.auruspay.comparator.model.ValidationResult;
import com.auruspay.comparator.model.ValidationResults;
import com.auruspay.comparator.service.TagValidationService;
import com.auruspay.dto.TransactionContext;
import com.auruspay.service.ServiceProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validates individual GMF field values. Performance: Regex patterns and static
 * sets are pre-compiled/built at class-load time.
 */

@Component
public class FieldValidator {

    private final TagValidationService tagValidationService;

    private final ValidationIssue validationIssue;
	
	
	@Autowired
	 private ServiceProvider serviceProvider ;
	
	
	
	
	

	


	// ---- Pre-compiled patterns ----
	// Add this pattern to your static patterns
	private static final Pattern ORDER_NUM_REGEX = Pattern.compile("^[A-Za-z0-9]{1,15}$");
	private static final Pattern STAN_REGEX = Pattern.compile("^\\d{6}$");
	private static final Pattern NUMERIC_GENERIC = Pattern.compile("^\\d+$");
	private static final Pattern REF_NUM_REGEX = Pattern.compile("^\\d{1,12}$");
	private static final Pattern TPPID_REGEX = Pattern.compile("^[A-Za-z0-9]{1,10}$");
	private static final Pattern TERM_ID_REGEX = Pattern.compile("^\\d{8}$");
	private static final Pattern MERCH_ID_REGEX = Pattern.compile("^[A-Za-z0-9]{1,15}$");
	private static final Pattern MCC_REGEX = Pattern.compile("^\\d{4}$");
	private static final Pattern POS_COND_REGEX = Pattern.compile("^\\d{2}$");
	private static final Pattern TERM_CAT_REGEX = Pattern.compile("^\\d{2}$");
	private static final Pattern TERM_ENTRY_CAP_REGEX = Pattern.compile("^\\d{2}$");
	private static final Pattern TXN_AMT_REGEX = Pattern.compile("^\\d{12}$");
	private static final Pattern CRNCY_REGEX = Pattern.compile("^\\d{3}$");
	private static final Pattern SINGLE_DIGIT_REGEX = Pattern.compile("^\\d{1}$");
	private static final Pattern GROUP_ID_REGEX = Pattern.compile("^[A-Za-z0-9]{5,13}$");
	private static final Pattern POS_ID_REGEX = Pattern.compile("^\\d{1,6}$");
	private static final Pattern UUID_REGEX = Pattern
			.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
	private static final Pattern ACI_REGEX = Pattern.compile("^[A-Za-z]$");
	private static final Pattern POSTAL_CODE_REGEX = Pattern.compile("^[A-Za-z0-9]{3,10}$");
	private static final Pattern TRACK2_REGEX = Pattern.compile("^\\d{13,19}=\\d{4}\\d{3}[0-9A-Za-z]*$");
	// Matches YYYYMM (6) or YYYYMMDD (8)
	private static final Pattern CARD_EXPIRY_DATE_REGEX = Pattern.compile("^\\d{6}(\\d{2})?$");
	private static final DateTimeFormatter GMF_DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	private static final Pattern SETTLE_IND_REGEX = Pattern.compile("^[123]$");
	// ---- Validation Sets ----
	private static final Set<String> VALID_POS_ENTRY_MODES = Set.of("00", "01", "03", "04", "05", "07", "08", "09",
			"10", "79", "80", "82", "86", "90", "91", "95");
	private static final Set<String> VALID_PIN_CAPABILITY = Set.of("0", "1", "2", "3", "4", "5", "6");
	private static final Set<String> VALID_PYMT_TYPES = Set.of("Credit", "Debit", "PLDebit", "EBT", "Check", "Prepaid",
			"PvtLabl", "Fleet", "AltCNP");
	private static final Set<String> VALID_TXN_TYPES = Set.of("Activation", "Authorization", "BalanceInquiry",
			"BalanceLock", "BatchSettleDetail", "BatchSettleL3", "CanadaKeyRequest", "CancelDeferredAuth", "Cashout",
			"CashoutActiveStatus", "Change", "CloseBatch", "Completion", "DisableInternetUse", "EncryptionKeyRequest",
			"EchoTest", "FileDownload", "FraudScore", "GenerateKey", "HostTotals", "InternetActivation", "Load",
			"OpenBatch", "PCL3AddDetail", "ProductEligInquiry", "Redemption", "RedemptionUnlock", "Refund", "Reload",
			"Sale", "TACertAuthority", "TAKeyRequest", "TATokenRequest", "Verification", "VoucherClear");
	private static final Set<String> VALID_CARD_TYPES = Set.of("Amex", "Diners", "Discover", "JCB", "MaestroInt",
			"MasterCard", "Visa", "UnionPay", "PPayCL", "GiftCard", "Exxon", "SpeedPass", "CarCareOne", "Shell",
			"ValeroUCC", "GenProp", "Gulf", "Sinclair", "Sunoco", "CostPlus", "Dicks", "Mexican", "BPBusiness",
			"EssoFleet", "ExxonFleet", "FleetCor", "FleetOne", "MCFleet", "NGFC", "ValeroFlt", "VisaFleet", "Voyager",
			"Wex", "WexOTR", "Comdata", "Paypal");
	private static final Pattern POSID_REGEX = Pattern.compile("^\\d{1,4}$");

	private static final List<String> VALID_POS_COND_CODES = List.of("00", "01", "02", "03", "04", "05", "06", "08",
			"59", "71");
	private static final Set<Integer> VALID_ENTRY_MODES = Set.of(0, 1, 3, 4, 5, 7, 8, 9, 10, 79, 80, 82, 86, 90, 91,
			95);

	// 1. Add this set of valid TermEntryCapablt codes
	private static final Set<String> VALID_TERM_ENTRY_CAPS = Set.of("00", "01", "02", "03", "04", "05", "06", "07",
			"08", "09", "10", "11", "12", "13");

	// ---- Specific Field Validators ----
	private static final Pattern PROGRAM_ID_REGEX = Pattern.compile("^[A-Za-z0-9]{1,20}$");
	private static final Set<String> VALID_TRAN_INIT_VALUES = Set.of("Merchant", "Terminal", "Customer");
	private static final Pattern ACCT_NUM_REGEX = Pattern.compile("^\\d{1,23}$");
	private static final Pattern AVS_REGEX = Pattern.compile("^[A-Z0-9]$");
	private static final Set<String> VALID_CCV_INDICATORS = Set.of("Ntprvd", "Prvded", "Illegible", "NtOnCrd");
	private static final Pattern CCV_DATA_REGEX = Pattern.compile("^\\d{3,4}$");
	private static final Set<String> VALID_CCV_RESULT_CODES = Set.of("Match", "NoMtch", "NotPrc", "NotPrv", "NotPrt",
			"Unknwn");

	private static final Pattern PIN_DATA_REGEX = Pattern.compile("^[0-9A-Fa-f]{16}$");
	private static final Pattern KSN_DATA_REGEX = Pattern.compile("^[A-Fa-f0-9]{20}$");
	private static final Set<String> VALID_ADD_AMT_TYPES = Set.of(
		    "Cashback", "Surchrg", "Hltcare", "Transit", "RX", "Vision", "Clinical", 
		    "Copay", "Dental", "FirstAuthAmt", "PreAuthAmt", "TotalAuthAmt", "Tax", "Fee", 
		    "Fuel", "Service", "eWICDiscount", "IncElig", "TranFee", "Tip", "AnticipAmt", 
		    "FutureAmt", "BegBal", "EndingBal", "AvailBal", "LedgerBal", "HoldBal", 
		    "OrigReqAmt", "OpenToBuy", "IncEarned", "IncEarnMonth"
		);
	private static final Set<String> VALID_ACCOUNT_TYPES = Set.of(
		    "Unknown", "Checking", "Credit", "CashBenefit", "SNAP", 
		    "Prepaid", "Savings", "SpendingPower", "Universal"
		);

	private static final Pattern EMV_DATA_REGEX = Pattern.compile("^[0-9A-Fa-f]{2,1998}$"); // ..999 bytes = 1998 hex chars
	private static final Pattern CARD_SEQ_REGEX = Pattern.compile("^\\d{3}$");
	private static final Pattern PC3_ADD_REGEX = Pattern.compile("^\\d{3}$");

    FieldValidator(ValidationIssue validationIssue, TagValidationService tagValidationService) {
        this.validationIssue = validationIssue;
        this.tagValidationService = tagValidationService;
    }
	
	// validation method
	public ValidationResult validate(String id, String value) {
		if (value == null)
			return new ValidationResult("INVALID", "NOT NULL", "Value is null");
		value = value.trim();
		if ("TAG MISSING".equals(value))
			return new ValidationResult("MISSING", "Required", "Field is missing");
		if ("EMPTY VALUE".equals(value) || value.isEmpty())
			return new ValidationResult("EMPTY", "Non Empty", "Field value cannot be empty");

		return switch (id) {
		
		case "DID" -> value.matches("\\d{20}") 
        ? new ValidationResult("VALID", "DID", "Valid DID") 
        : new ValidationResult("INVALID", "DID", "Must be 20 digits");
        
    case "App" -> value.equals("RAPIDCONNECTSRS") 
        ? new ValidationResult("VALID", "App", "Valid App ID") 
        : new ValidationResult("INVALID", "App", "Unrecognized App ID");
        
    case "Auth" -> {
        yield validateAuth(value);
       }
    case "ClientRef" -> value.length() <= 16 && value.matches("[A-Z0-9]+") 
        ? new ValidationResult("VALID", "ClientRef", "Valid Reference") 
        : new ValidationResult("INVALID", "ClientRef", "Reference must be alphanumeric, max 16 chars");
		
		case "PymtType" -> validatePymtType(value);
		case "TxnType" -> validateTxnType(value);
		case "LocalDateTime", "TrnmsnDateTime" -> validateDateTime(value);
		case "STAN" -> validateStan(value);

		case "POSEntryMode" -> validatePOSEntryMode(value);
		case "TxnAmt" -> validateTxnAmt(value);
		case "MerchEcho" -> validateMerchEcho(value);

		case "OrderNum" -> validateOrderNum(value);

		case "RefNum" -> matchOrInvalid(value, REF_NUM_REGEX, "1-12 numeric digits", "Reference Number");
	//	case "TPPID" -> matchOrInvalid(value, TPPID_REGEX, "1-10 alphanumeric", "TPP ID");
		case "TermID" -> matchOrInvalid(value, TERM_ID_REGEX, "8 numeric digits", "Terminal ID");
		case "MerchID" -> matchOrInvalid(value, MERCH_ID_REGEX, "1-15 alphanumeric", "Merchant ID");
		case "MerchCatCode" -> matchOrInvalid(value, MCC_REGEX, "4 numeric digits", "Merchant Category Code");
		case "POSCondCode" -> validatePOSCondCode(value);
		case "TermCatCode" -> validateTermCatCode(value);
		case "TermEntryCapablt" -> validateTermEntryCap(value);
		case "TxnCrncy" -> validateTxnCrncy(value);
		case "TermLocInd" -> validateTermLocInd(value);
		case "CardCaptCap" -> validateCardCaptCap(value);
		case "ProgramID" -> validateProgramID(value);
		//case "PartAuthrztnApprvlCapablt" ->matchOrInvalid(value, SINGLE_DIGIT_REGEX, "1 numeric digit", "Partial Authorization Approval Capability");
		// case "GroupID" -> matchOrInvalid(value, GROUP_ID_REGEX, "1-6 numeric digits",
		// "Group ID");
		case "GroupID" -> validateGroupID(value);

		case "POSID" -> validatePOSID(value);
		case "SettleInd" -> validateSettleInd(value);
		case "TranInit" -> validateTranInit(value);
		// CardGroup
		case "AcctNum" -> validateAcctNum(value);
		case "CardExpiryDate" -> validateCardExpiryDate(value);
		case "Track2Data" -> validateTrack2(value);
		case "CardType" -> validateCardType(value);
		case "AVSResultCode" -> validateAVSResultCode(value);
		case "CCVInd" -> validateCCVInd(value);
		case "CCVData" -> validateCCVData(value);
		case "CCVResultCode" -> validateCCVResultCode(value);
		// Pin group
		case "PINData" -> validatePINData(value);
		case "KeySerialNumData" -> validateKeySerialNumData(value);

		//ADDITIONAL AMOUNTS Group AddtlAmtGrp        
		case "AddAmtType" -> validateAddAmtType(value);
		case "AddAmtAcctType" -> validateAddAmtAcctType(value);
		case "PartAuthrztnApprvlCapablt" -> validatePartAuthCap(value);
		// EMV Grp 
		case "EMVData" -> validateEMVData(value);
		case "CardSeqNum" -> validateCardSeqNum(value);
		case "PC3Add" -> validatePC3Add(value);
		case "ACI" -> matchOrInvalid(value, ACI_REGEX, "single alphabetic", "Authorization Characteristics Indicator");
		case "AVSBillingPostalCode" ->
			matchOrInvalid(value, POSTAL_CODE_REGEX, "3-10 alphanumeric", "AVS Billing Postal Code");

		default -> new ValidationResult("VALID", "N/A", "No validation rule configured");
		};
	}
	
	private ValidationResult validatePC3Add(String value) {

	    if (value == null || !PC3_ADD_REGEX.matcher(value).matches()) {

	        return new ValidationResult(
	                "INVALID",
	                "3 digit numeric",
	                "PC3Add must be a 3 digit numeric value"
	        );
	    }


	    int count = Integer.parseInt(value);


	    if (count < 0 || count > 999) {

	        return new ValidationResult(
	                "INVALID",
	                "000-999",
	                "PC3Add value out of range"
	        );
	    }


	    return new ValidationResult(
	            "VALID",
	            "000-999",
	            "Valid PC3Add format"
	    );
	}
	
	private ValidationResult validateAuth1(String value) {

	    TransactionContext context = serviceProvider.getTransactionContext();

	    if (context == null) {
	        return new ValidationResult("INVALID", "Auth", "TransactionContext is null.");
	    }

	    String termId = context.getTermID();
	    String merchId = context.getMerchID();
	    String groupId = context.getGroupID();
	    String auth = context.getAuth();

	    // Null / blank check
	    if (isBlank(auth) || isBlank(termId) || isBlank(merchId) || isBlank(groupId)) {
	        return new ValidationResult(
	                "INVALID",
	                "Auth",
	                "Auth, TermID, MerchID, and GroupID are required.");
	    }

	    // Build expected auth
	    String expectedAuth = groupId + merchId + "|" + termId;

	    // Compare
	    if (auth.equals(expectedAuth)) {
	        return new ValidationResult(
	                "VALID",
	                "Auth",
	                "Valid Auth format.");
	    }

	    return new ValidationResult(
	            "INVALID",
	            "Auth",
	            "Expected Auth: " + expectedAuth + ", but found: " + auth);
	}
	
	private ValidationResult validateAuth(String value) {

	    TransactionContext context = serviceProvider.getTransactionContext();

	    String auth = context.getAuth();
	    String termId = context.getTermID();
	    String merchId = context.getMerchID();
	    String groupId = context.getGroupID();

	    List<String> missingFields = new ArrayList();

	    if (isBlank(auth)) missingFields.add("Auth");
	    if (isBlank(termId)) missingFields.add("TermID");
	    if (isBlank(merchId)) missingFields.add("MerchID");
	    if (isBlank(groupId)) missingFields.add("GroupID");

	    if (!missingFields.isEmpty()) {
	        return new ValidationResult(
	                "INVALID",
	                "Auth",
	                String.join(", ", missingFields) + " are blank.");
	    }

	    String expectedAuth = groupId + merchId + "|" + termId;

	    if (auth.equals(expectedAuth)) {
	        return new ValidationResult(
	                "VALID",
	                "Auth",
	                "Valid Auth format.");
	    }

	    return new ValidationResult(
	            "INVALID",
	            "Auth",
	            "Expected Auth: " + expectedAuth + ", but found: " + auth);
	}
	private boolean isBlank(String value) {
	    return value == null || value.trim().isEmpty();
	}

	
	private ValidationResult validateCardSeqNum(String value) {
	    // Valid values: 000-099. Right-justified, zero-filled.
	    if (value == null || !CARD_SEQ_REGEX.matcher(value).matches()) {
	        return new ValidationResult("INVALID", "3 digits (000-099)", "CardSeqNum must be a 3-digit numeric string");
	    }
	    
	    int val = Integer.parseInt(value);
	    if (val > 99) {
	        return new ValidationResult("INVALID", "000-099", "CardSeqNum must be between 000 and 099");
	    }
	    
	    return new ValidationResult("VALID", "CardSeqNum", "Valid sequence number");
	}

	private ValidationResult validateEMVData(String value) {
	    if (value == null || !EMV_DATA_REGEX.matcher(value).matches()) {
	        return new ValidationResult("INVALID", "Hex string", "EMVData must be a valid hex string up to 999 bytes");
	    }
	    return new ValidationResult("VALID", "EMVData", "Valid EMV hex block");
	}
	
	private ValidationResult validatePartAuthCap(String value) {
	    if (value == null || !("0".equals(value) || "1".equals(value))) {
	        return new ValidationResult("INVALID", "0 or 1", "PartAuthrztnApprvlCapablt must be 0 or 1");
	    }
	    return new ValidationResult("VALID", "PartAuthrztnApprvlCapablt", "Valid");
	}

		private ValidationResult validateAddAmtAcctType(String value) {
		    if (value == null || !VALID_ACCOUNT_TYPES.contains(value)) {
		        return new ValidationResult("INVALID", "Permitted Set", "Invalid Additional Amount Account Type");
		    }
		    return new ValidationResult("VALID", "AddAmtAcctType", "Valid");
		}
	

		private ValidationResult validateAddAmtType(String value) {
		    if (value == null || !VALID_ADD_AMT_TYPES.contains(value)) {
		        return new ValidationResult("INVALID", "Permitted Set", "Invalid Additional Amount Type");
		    }
		    return new ValidationResult("VALID", "AddAmtType", "Valid");
		}
	

	private ValidationResult validateKeySerialNumData(String value) {
		if (value == null || !KSN_DATA_REGEX.matcher(value).matches()) {
			return new ValidationResult("INVALID", "20 chars", "KeySerialNumData must be 20 alphanumeric characters");
		}
		return new ValidationResult("VALID", "KeySerialNumData", "Valid KSN format");
	}

	private ValidationResult validatePINData(String value) {
		if (value == null || !PIN_DATA_REGEX.matcher(value).matches()) {
			return new ValidationResult("INVALID", "16 hex digits", "PINData must be 16 hexadecimal characters");
		}
		return new ValidationResult("VALID", "PINData", "Valid PIN Data format");
	}

	private ValidationResult validateCCVResultCode(String value) {
		if (value == null || !VALID_CCV_RESULT_CODES.contains(value)) {
			return new ValidationResult("INVALID", "Permitted Set",
					"CCVResultCode must be one of: Match, NoMtch, NotPrc, NotPrv, NotPrt, Unknwn");
		}
		return new ValidationResult("VALID", "CCVResultCode", "Valid CCV Result Code");
	}

	private ValidationResult validateCCVData(String value) {
		if (value == null || !CCV_DATA_REGEX.matcher(value).matches()) {
			return new ValidationResult("INVALID", "3 or 4 digits", "CCVData must be 3 or 4 numeric digits");
		}
		return new ValidationResult("VALID", "CCVData", "Valid CCV Data format");
	}

	private ValidationResult validateCCVInd(String value) {
		if (value == null || !VALID_CCV_INDICATORS.contains(value)) {
			return new ValidationResult("INVALID", "Permitted Set",
					"CCVInd must be Ntprvd, Prvded, Illegible, or NtOnCrd");
		}
		return new ValidationResult("VALID", "CCVInd", "Valid CCV Indicator");
	}

	public ValidationResult validateAVSResultCode(String value) {

		return AVS_REGEX.matcher(value).matches() ? new ValidationResult("VALID", "AVSResultCode", "Success")
				: new ValidationResult("INVALID", "AVSResultCode",
						"AVS Result Code must be a single alphanumeric character");
	}

	private ValidationResult validateAcctNum(String value) {
		if (!ACCT_NUM_REGEX.matcher(value).matches()) {
			return new ValidationResult("INVALID", "1-23 numeric", "AcctNum must be numeric (1-23 digits)");
		}
		return new ValidationResult("VALID", "AcctNum", "Valid Account Number");
	}

	private ValidationResult validateTranInit(String value) {
		if (value == null || value.length() > 8) {
			return new ValidationResult("INVALID", "Max 8 chars", "TranInit length exceeded");
		}
		if (!VALID_TRAN_INIT_VALUES.contains(value)) {
			return new ValidationResult("INVALID", VALID_TRAN_INIT_VALUES.toString(),
					"Invalid Transaction Initiation value");
		}
		return new ValidationResult("VALID", "TranInit", "Valid");
	}

	// 3. Validation method
	private ValidationResult validatePOSID(String value) {
		if (!POSID_REGEX.matcher(value).matches()) {
			return new ValidationResult("INVALID", "1-4 digits", "POSID must be 1-4 numeric digits");
		}
		return new ValidationResult("VALID", "POSID", "Valid POS ID");
	}

	// 3. Add the validation method
	private ValidationResult validateGroupID(String value) {
		if (!GROUP_ID_REGEX.matcher(value).matches()) {
			return new ValidationResult("INVALID", "5-13 Alphanumeric", "GroupID must be 5-13 characters long");
		}
		return new ValidationResult("VALID", "GroupID", "Valid Group ID");
	}

	private ValidationResult validateProgramID(String value) {
		if (!PROGRAM_ID_REGEX.matcher(value).matches()) {
			return new ValidationResult("INVALID", "1-20 Alphanumeric", "ProgramID format mismatch");
		}
		return new ValidationResult("VALID", "ProgramID", "Valid");
	}

	private ValidationResult validateCardCaptCap(String value) {
		if (!value.matches("^[01]$")) {
			return new ValidationResult("INVALID", "0 or 1", "CardCaptCap must be 0 or 1");
		}
		return new ValidationResult("VALID", "CardCaptCap", "Valid Card Capture Capability");
	}

	// 2. Transaction Currency Validation
	private ValidationResult validateTxnCrncy(String value) {
		// Length is 3 digits as per spec
		if (!value.matches("^\\d{3}$")) {
			return new ValidationResult("INVALID", "3 digits", "TxnCrncy must be 3 digits");
		}
		// Note: ISO 4217 numeric codes are used; you can maintain an allowed list here
		return new ValidationResult("VALID", "TxnCrncy", "Valid Transaction Currency");
	}

	// 3. Terminal Location Indicator Validation
	private ValidationResult validateTermLocInd(String value) {
		if (!value.matches("^[01]$")) {
			return new ValidationResult("INVALID", "0 or 1", "TermLocInd must be 0 or 1");
		}
		return new ValidationResult("VALID", "TermLocInd", "Valid Terminal Location Indicator");
	}

	// 3. Add the validation method
	private ValidationResult validateTermEntryCap(String value) {
		// Length is 2 digits as per specification
		if (!value.matches("^\\d{2}$")) {
			return new ValidationResult("INVALID", "2 digits", "TermEntryCapablt must be 2 digits");
		}
		// Check if the value exists in the permitted value set
		if (!VALID_TERM_ENTRY_CAPS.contains(value)) {
			return new ValidationResult("INVALID", VALID_TERM_ENTRY_CAPS.toString(),
					"Invalid Terminal Entry Capability code");
		}
		return new ValidationResult("VALID", "TermEntryCapablt", "Valid Terminal Entry Capability");
	}

	private ValidationResult validateRefNum(String value) {
		// Length can range from 1 to 22 based on the rules.
		// We check max length here; rules engine checks specific length per scenario.
		if (value.length() > 22) {
			return new ValidationResult("INVALID", "Length <= 22", "Reference Number exceeds maximum length of 22");
		}
		return new ValidationResult("VALID", "Length <= 22", "Valid Reference Number format");
	}

	private ValidationResult validateMerchCatCode(String value) {
		if (!MCC_REGEX.matcher(value).matches()) {
			return new ValidationResult("INVALID", "4 numeric digits",
					"Merchant Category Code must be exactly 4 digits");
		}
		return new ValidationResult("VALID", "4 numeric digits", "Valid Merchant Category Code");
	}

	// Add this case to your switch statement in the validate method
	private ValidationResult validateCardExpiryDate(String value) {
		if (!CARD_EXPIRY_DATE_REGEX.matcher(value).matches()) {
			return new ValidationResult("INVALID", "YYYYMM or YYYYMMDD", "Card Expiry Date must be 6 or 8 digits");
		}
		// Basic logical check: Extract YYYY and MM
		int year = Integer.parseInt(value.substring(0, 4));
		int month = Integer.parseInt(value.substring(4, 6));

		if (month < 1 || month > 12) {
			return new ValidationResult("INVALID", "01-12", "Invalid month in Expiry Date");
		}
		return new ValidationResult("VALID", "YYYYMM(DD)", "Valid Card Expiry Date");
	}

	// Add this validation method
	private ValidationResult validateOrderNum(String value) {
		if (!ORDER_NUM_REGEX.matcher(value).matches()) {
			return new ValidationResult("INVALID", "1-15 Alphanumeric", "Order Number format mismatch");
		}

		// Rule: The value in this field cannot contain all zeroes.
		if (value.matches("^0+$")) {
			return new ValidationResult("INVALID", "Non-zero", "Order Number cannot contain all zeroes");
		}

		return new ValidationResult("VALID", "1-15 Alphanumeric", "Valid Order Number");
	}

	
	// 1. Add this set of valid TermCatCodes
	private static final Set<String> VALID_TERM_CAT_CODES = Set.of("00", "01", "05", "06", "07", "08", "09", "12", "13",
			"17", "18");

	// 3. Add the validation method
	private ValidationResult validateTermCatCode(String value) {
		if (!value.matches("^\\d{2}$")) {
			return new ValidationResult("INVALID", "2 digits", "TermCatCode must be 2 digits");
		}
		if (!VALID_TERM_CAT_CODES.contains(value)) {
			return new ValidationResult("INVALID", VALID_TERM_CAT_CODES.toString(), "Invalid Terminal Category Code");
		}
		return new ValidationResult("VALID", "TermCatCode", "Valid Terminal Category Code");
	}

	private ValidationResult validatePOSEntryMode(String value) {
		if (value == null || value.length() != 3 || !value.matches("\\d{3}")) {
			return new ValidationResult("INVALID", "N3", "POS Entry Mode must be exactly 3 digits.");
		}

		// Using Integer.parseInt is fine, but ensure you handle potential errors if
		// necessary
		int entryPart = Integer.parseInt(value.substring(0, 2));
		int authPart = value.charAt(2) - '0'; // Faster way to get the integer value of a single digit

		if (!VALID_ENTRY_MODES.contains(entryPart)) {
			return new ValidationResult("INVALID", "Invalid Entry Mode", "First two digits are not valid.");
		}

		if (authPart < 0 || authPart > 6) {
			return new ValidationResult("INVALID", "Invalid Auth Cap", "Third digit must be 0-6.");
		}

		return new ValidationResult("VALID", "N3", "Valid POS Entry Mode.");
	}

	private ValidationResult validatePOSCondCode(String value) {
		if (value == null || !value.matches("\\d{2}")) {
			return new ValidationResult("INVALID", "N2", "POS Condition Code must be 2 digits.");
		}
		if (!VALID_POS_COND_CODES.contains(value)) {
			return new ValidationResult("INVALID", "00-71", "POS Condition Code value is not supported.");
		}
		return new ValidationResult("VALID", "N2", "Valid POS Condition Code.");
	}

	private ValidationResult validateTxnAmt(String value) {
		if (!TXN_AMT_REGEX.matcher(value).matches()) {
			return new ValidationResult("INVALID", "^\\d{12}$", "Transaction Amount must be 12 digits");
		}
		return new ValidationResult("VALID", "^\\d{12}$", "Valid Transaction Amount");
	}

	private ValidationResult validateMerchEcho(String value) {
		if (!UUID_REGEX.matcher(value).matches()) {
			return new ValidationResult("INVALID", "UUID", "MerchEcho must be UUID format");
		}
		return new ValidationResult("VALID", "UUID", "Valid Merchant Echo");
	}

	private ValidationResult validateTrack2(String value) {
		if (!TRACK2_REGEX.matcher(value).matches()) {
			return new ValidationResult("INVALID", "PAN=ExpiryYYMM[data]", "Invalid Track2 format");
		}
		return new ValidationResult("VALID", "PAN=ExpiryYYMM[data]", "Valid Track2 Data");
	}

	private ValidationResult validateStan(String value) {
		if (!STAN_REGEX.matcher(value).matches())
			return new ValidationResult("INVALID", "^\\d{6}$", "STAN must be 6 digits");
		int stan = Integer.parseInt(value);
		return (stan >= 1 && stan <= 999999) ? new ValidationResult("VALID", "000001-999999", "Valid STAN")
				: new ValidationResult("INVALID", "000001-999999", "STAN out of range");
	}

	private ValidationResult validatePymtType(String value) {
		return VALID_PYMT_TYPES.contains(value) ? new ValidationResult("VALID", "Set", "Valid Payment Type")
				: new ValidationResult("INVALID", VALID_PYMT_TYPES.toString(), "Invalid Payment Type");
	}

	private ValidationResult validateTxnType(String value) {
		return VALID_TXN_TYPES.contains(value) ? new ValidationResult("VALID", "Set", "Valid Transaction Type")
				: new ValidationResult("INVALID", VALID_TXN_TYPES.toString(), "Invalid Transaction Type");
	}

	// Add this validation method
	private ValidationResult validateCardType(String value) {
		if (VALID_CARD_TYPES.contains(value)) {
			return new ValidationResult("VALID", "Set", "Valid Card Type");
		}
		return new ValidationResult("INVALID", VALID_CARD_TYPES.toString(), "Invalid Card Type provided");
	}

	private ValidationResult validateSettleInd(String value) {
		if (!SETTLE_IND_REGEX.matcher(value).matches()) {
			return new ValidationResult("INVALID", "1, 2, or 3", "Settlement Indicator must be 1, 2, or 3");
		}
		return new ValidationResult("VALID", "1, 2, or 3", "Valid Settlement Indicator");
	}

	private ValidationResult validateDateTime(String value) {
		if (value.length() != 14 || !NUMERIC_GENERIC.matcher(value).matches())
			return new ValidationResult("INVALID", "yyyyMMddHHmmss", "Invalid format");
		try {
			LocalDateTime.parse(value, GMF_DATETIME_FORMAT);
		} catch (DateTimeParseException e) {
			return new ValidationResult("INVALID", "yyyyMMddHHmmss", "Invalid date/time");
		}
		return new ValidationResult("VALID", "yyyyMMddHHmmss", "Valid date/time");
	}

	private ValidationResult matchOrInvalid(String value, Pattern pattern, String expected, String label) {
		return pattern.matcher(value).matches() ? new ValidationResult("VALID", expected, "Valid " + label)
				: new ValidationResult("INVALID", expected, label + " format mismatch");
	}

}