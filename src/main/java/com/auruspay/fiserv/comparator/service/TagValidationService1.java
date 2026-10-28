package com.auruspay.comparator.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.auruspay.comparator.model.ValidationResults;
import com.auruspay.dto.TransactionContext;
import com.auruspay.service.ServiceProvider;

@Component
public class TagValidationService1 {
	
	private static final Set<String> VALID_PYMT_TYPES = Set.of("Credit", "Debit", "PLDebit", "EBT", "Check", "Prepaid",
			"PvtLabl", "Fleet", "AltCNP");
	private static final Set<String> VALID_TXN_TYPES = Set.of("Activation", "Authorization", "BalanceInquiry",
			"BalanceLock", "BatchSettleDetail", "BatchSettleL3", "CanadaKeyRequest", "CancelDeferredAuth", "Cashout",
			"CashoutActiveStatus", "Change", "CloseBatch", "Completion", "DisableInternetUse", "EncryptionKeyRequest",
			"EchoTest", "FileDownload", "FraudScore", "GenerateKey", "HostTotals", "InternetActivation", "Load",
			"OpenBatch", "PCL3AddDetail", "ProductEligInquiry", "Redemption", "RedemptionUnlock", "Refund", "Reload",
			"Sale", "TACertAuthority", "TAKeyRequest", "TATokenRequest", "Verification", "VoucherClear");
	private static final Pattern NUMERIC_GENERIC = Pattern.compile("\\d+");

	private static final DateTimeFormatter GMF_DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	private static final Pattern STAN_REGEX = Pattern.compile("^\\d{6}$");

	private static final Set<Integer> VALID_ENTRY_MODES = Set.of(0, 1, 3, 4, 5, 7, 8, 9, 10, 79, 80, 82, 86, 90, 91,95);
	
	private static final Pattern TXN_AMT_REGEX = Pattern.compile("^\\d{12}$");

	private static final Pattern UUID_REGEX = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
	

    public ValidationResults validateDID(String tag, String valueA, String valueD) {

        boolean valueAValid = valueA != null && valueA.matches("\\d{20}");
        boolean valueDValid = valueD != null && valueD.matches("\\d{20}");

        if (valueAValid && valueDValid) {
            return new ValidationResults(
                    tag,
                    valueA,
                    valueD,
                    "VALID",
                    "DID",
                    "Both values are valid DID"
            );
        }

        String reason;

        if (!valueAValid && !valueDValid) {
            reason = "Both valueA and valueD must be 20 digits";
        } else if (!valueAValid) {
            reason = "valueA must be 20 digits";
        } else {
            reason = "valueD must be 20 digits";
        }

        return new ValidationResults(
                tag,
                valueA,
                valueD,
                "INVALID",
                "DID",
                reason
        );
    }


    public ValidationResults validateApp(String tag, String valueA, String valueD) {

        boolean valueAValid = "RAPIDCONNECTSRS".equals(valueA);
        boolean valueDValid = "RAPIDCONNECTSRS".equals(valueD);

        if (valueAValid && valueDValid) {
            return new ValidationResults(
                    tag,
                    valueA,
                    valueD,
                    "VALID",
                    "App",
                    "Both values are valid App ID"
            );
        }

        return new ValidationResults(
                tag,
                valueA,
                valueD,
                "INVALID",
                "App",
                !valueAValid ? "valueA has invalid App ID" : "valueD has invalid App ID"
        );
    }
    private boolean isBlank(String value) {
	    return value == null || value.trim().isEmpty();
	}

    
    public ValidationResults validateAuth(String tag, String valueA, String valueD ,ServiceProvider serviceProvider) {

        TransactionContext context = serviceProvider.getTransactionContext();

        String termId = context.getTermID();
        String merchId = context.getMerchID();
        String groupId = context.getGroupID();

        List<String> missingFields = new ArrayList<>();

        if (isBlank(valueA)) missingFields.add("valueA Auth");
        if (isBlank(valueD)) missingFields.add("valueD Auth");
        if (isBlank(termId)) missingFields.add("TermID");
        if (isBlank(merchId)) missingFields.add("MerchID");
        if (isBlank(groupId)) missingFields.add("GroupID");

        if (!missingFields.isEmpty()) {
            return new ValidationResults(
                    tag,
                    valueA,
                    valueD,
                    "INVALID",
                    "Auth",
                    String.join(", ", missingFields) + " are blank."
            );
        }

        String expectedAuth = groupId + merchId + "|" + termId;

        boolean valueAValid = expectedAuth.equals(valueA);
        boolean valueDValid = expectedAuth.equals(valueD);

        if (valueAValid && valueDValid) {
            return new ValidationResults(
                    tag,
                    valueA,
                    valueD,
                    "VALID",
                    "Auth",
                    "Both Auth values are valid."
            );
        }

        String reason;

        if (!valueAValid && !valueDValid) {
            reason = "Both valueA and valueD are invalid. Expected Auth: " + expectedAuth;
        } else if (!valueAValid) {
            reason = "valueA Auth is invalid. Expected: " + expectedAuth;
        } else {
            reason = "valueD Auth is invalid. Expected: " + expectedAuth;
        }

        return new ValidationResults(
                tag,
                valueA,
                valueD,
                "INVALID",
                "Auth",
                reason
        );
    }
    public ValidationResults validateClientRef(String tag, String valueA, String valueD) {

        boolean valueAValid = valueA != null 
                && valueA.length() <= 16 
                && valueA.matches("[A-Z0-9]+");

        boolean valueDValid = valueD != null 
                && valueD.length() <= 16 
                && valueD.matches("[A-Z0-9]+");

        if (valueAValid && valueDValid) {
            return new ValidationResults(
                    tag,
                    valueA,
                    valueD,
                    "VALID",
                    "ClientRef",
                    "Both values are valid Reference"
            );
        }

        String reason;

        if (!valueAValid && !valueDValid) {
            reason = "Both valueA and valueD must be alphanumeric, max 16 chars";
        } else if (!valueAValid) {
            reason = "valueA ClientRef must be alphanumeric, max 16 chars";
        } else {
            reason = "valueD ClientRef must be alphanumeric, max 16 chars";
        }

        return new ValidationResults(
                tag,
                valueA,
                valueD,
                "INVALID",
                "ClientRef",
                reason
        );
    }
    
    

    public ValidationResults validatePymtType(String tag, String valueA, String valueD) {

        boolean valueAValid = VALID_PYMT_TYPES.contains(valueA);
        boolean valueDValid = VALID_PYMT_TYPES.contains(valueD);

        if (valueAValid && valueDValid) {
            return new ValidationResults(
                    tag,
                    valueA,
                    valueD,
                    "VALID",
                    "Set",
                    "Both values are valid Payment Type"
            );
        }

        String reason;

        if (!valueAValid && !valueDValid) {
            reason = "Both valueA and valueD are invalid Payment Type";
        } else if (!valueAValid) {
            reason = "valueA is invalid Payment Type";
        } else {
            reason = "valueD is invalid Payment Type";
        }

        return new ValidationResults(
                tag,
                valueA,
                valueD,
                "INVALID",
                VALID_PYMT_TYPES.toString(),
                reason
        );
    }
    
    public ValidationResults validateTxnType(String tag, String valueA, String valueD) {

        boolean valueAValid = VALID_TXN_TYPES.contains(valueA);
        boolean valueDValid = VALID_TXN_TYPES.contains(valueD);

        if (valueAValid && valueDValid) {
            return new ValidationResults(
                    tag,
                    valueA,
                    valueD,
                    "VALID",
                    "Set",
                    "Both values are valid Transaction Type"
            );
        }

        String reason;

        if (!valueAValid && !valueDValid) {
            reason = "Both valueA and valueD are invalid Transaction Type";
        } else if (!valueAValid) {
            reason = "valueA is invalid Transaction Type";
        } else {
            reason = "valueD is invalid Transaction Type";
        }

        return new ValidationResults(
                tag,
                valueA,
                valueD,
                "INVALID",
                VALID_TXN_TYPES.toString(),
                reason
        );
    }
    
    public ValidationResults validateDateTime(String tag, String valueA, String valueD) {

        boolean valueAValid = isValidDateTime(valueA);
        boolean valueDValid = isValidDateTime(valueD);

        if (valueAValid && valueDValid) {
            return new ValidationResults(
                    tag,
                    valueA,
                    valueD,
                    "VALID",
                    "yyyyMMddHHmmss",
                    "Both values are valid date/time"
            );
        }

        String reason;

        if (!valueAValid && !valueDValid) {
            reason = "Both valueA and valueD have invalid date/time format";
        } else if (!valueAValid) {
            reason = "valueA has invalid date/time format";
        } else {
            reason = "valueD has invalid date/time format";
        }

        return new ValidationResults(
                tag,
                valueA,
                valueD,
                "INVALID",
                "yyyyMMddHHmmss",
                reason
        );
    }


    private boolean isValidDateTime(String value) {

        if (value == null || value.length() != 14 
                || !NUMERIC_GENERIC.matcher(value).matches()) {
            return false;
        }

        try {
            LocalDateTime.parse(value, GMF_DATETIME_FORMAT);
            return true;

        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
    public ValidationResults validateStan(String tag, String valueA, String valueD) {

        boolean valueAValid = isValidStan(valueA);
        boolean valueDValid = isValidStan(valueD);

        if (valueAValid && valueDValid) {
            return new ValidationResults(
                    tag,
                    valueA,
                    valueD,
                    "VALID",
                    "000001-999999",
                    "Both values are valid STAN"
            );
        }

        String reason;

        if (!valueAValid && !valueDValid) {
            reason = "Both valueA and valueD have invalid STAN";
        } else if (!valueAValid) {
            reason = "valueA STAN must be 6 digits and between 000001-999999";
        } else {
            reason = "valueD STAN must be 6 digits and between 000001-999999";
        }

        return new ValidationResults(
                tag,
                valueA,
                valueD,
                "INVALID",
                "000001-999999",
                reason
        );
    }


    private boolean isValidStan(String value) {

        if (value == null || !STAN_REGEX.matcher(value).matches()) {
            return false;
        }

        int stan = Integer.parseInt(value);

        return stan >= 1 && stan <= 999999;
    }
    
    public ValidationResults validatePOSEntryMode(String tag, String valueA, String valueD) {

        boolean valueAValid = isValidPOSEntryMode(valueA);
        boolean valueDValid = isValidPOSEntryMode(valueD);

        if (valueAValid && valueDValid) {
            return new ValidationResults(
                    tag,
                    valueA,
                    valueD,
                    "VALID",
                    "N3",
                    "Both values are valid POS Entry Mode"
            );
        }

        String reason;

        if (!valueAValid && !valueDValid) {
            reason = "Both valueA and valueD have invalid POS Entry Mode";
        } else if (!valueAValid) {
            reason = "valueA has invalid POS Entry Mode";
        } else {
            reason = "valueD has invalid POS Entry Mode";
        }

        return new ValidationResults(
                tag,
                valueA,
                valueD,
                "INVALID",
                "N3",
                reason
        );
    }


    private boolean isValidPOSEntryMode(String value) {

        if (value == null || value.length() != 3 || !value.matches("\\d{3}")) {
            return false;
        }

        int entryPart = Integer.parseInt(value.substring(0, 2));
        int authPart = value.charAt(2) - '0';

        return VALID_ENTRY_MODES.contains(entryPart)
                && authPart >= 0
                && authPart <= 6;
    }
    public ValidationResults validateTxnAmt(String tag, String valueA, String valueD) {

        boolean valueAValid = valueA != null && TXN_AMT_REGEX.matcher(valueA).matches();
        boolean valueDValid = valueD != null && TXN_AMT_REGEX.matcher(valueD).matches();

        if (valueAValid && valueDValid) {
            return new ValidationResults(
                    tag,
                    valueA,
                    valueD,
                    "VALID",
                    "^\\d{12}$",
                    "Both values are valid Transaction Amount"
            );
        }

        String reason;

        if (!valueAValid && !valueDValid) {
            reason = "Both valueA and valueD must be 12 digits";
        } else if (!valueAValid) {
            reason = "valueA Transaction Amount must be 12 digits";
        } else {
            reason = "valueD Transaction Amount must be 12 digits";
        }

        return new ValidationResults(
                tag,
                valueA,
                valueD,
                "INVALID",
                "^\\d{12}$",
                reason
        );
    }
    
    public ValidationResults validateMerchEcho(String tag, String valueA, String valueD) {

        boolean valueAValid = valueA != null && UUID_REGEX.matcher(valueA).matches();
        boolean valueDValid = valueD != null && UUID_REGEX.matcher(valueD).matches();

        if (valueAValid && valueDValid) {
            return new ValidationResults(
                    tag,
                    valueA,
                    valueD,
                    "VALID",
                    "UUID",
                    "Both values are valid Merchant Echo"
            );
        }

        String reason;

        if (!valueAValid && !valueDValid) {
            reason = "Both valueA and valueD must be UUID format";
        } else if (!valueAValid) {
            reason = "valueA MerchEcho must be UUID format";
        } else {
            reason = "valueD MerchEcho must be UUID format";
        }

        return new ValidationResults(
                tag,
                valueA,
                valueD,
                "INVALID",
                "UUID",
                reason
        );
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}