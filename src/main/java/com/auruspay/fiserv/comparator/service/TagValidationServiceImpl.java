package com.auruspay.comparator.service;

import com.auruspay.comparator.model.ValidationResults;
import com.auruspay.comparator.service.FieldLabels.FieldMeta;
import com.auruspay.dto.TransactionContext;
import com.auruspay.service.ServiceProvider;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Validates individual GMF field values coming from the Approved and
 * Declined transaction records.
 *
 * Callers of these methods have already guaranteed both values are present
 * (non-blank); see {@link FieldValidators} for the upstream "missing value"
 * handling.
 *
 * Performance: Regex patterns and static sets are pre-compiled/built at
 * class-load time.
 */
@Service
public class TagValidationServiceImpl implements TagValidationService {

    // ---- Pre-compiled patterns ----
    private static final Pattern DID_REGEX = Pattern.compile("^\\d{20}$");
    private static final Pattern ORDER_NUM_REGEX = Pattern.compile("^[A-Za-z0-9]{1,15}$");
    private static final Pattern STAN_REGEX = Pattern.compile("^\\d{6}$");
    private static final Pattern NUMERIC_GENERIC = Pattern.compile("^\\d+$");
    private static final Pattern REF_NUM_REGEX = Pattern.compile("^\\d{1,12}$");
    private static final Pattern TERM_ID_REGEX = Pattern.compile("^\\d{8}$");
    private static final Pattern MERCH_ID_REGEX = Pattern.compile("^[A-Za-z0-9]{1,15}$");
    private static final Pattern MCC_REGEX = Pattern.compile("^\\d{4}$");
    private static final Pattern TXN_AMT_REGEX = Pattern.compile("^\\d{0,12}$");
    private static final Pattern POSID_REGEX = Pattern.compile("^\\d{1,4}$");
    private static final Pattern UUID_REGEX = Pattern
            .compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    private static final Set<String> VALID_ACI = Set.of("Y", "N", "Z", "E", "R", "S");
    private static final Pattern POSTAL_CODE_REGEX = Pattern.compile("^[A-Za-z0-9]{3,10}$");
    private static final Pattern TRACK2_REGEX = Pattern.compile("^\\d{13,19}=\\d{4}\\d{3}[0-9A-Za-z]*$");
    // Matches YYYYMM (6) or YYYYMMDD (8)
    private static final Pattern CARD_EXPIRY_DATE_REGEX = Pattern.compile("^\\d{6}(\\d{2})?$");
    private static final DateTimeFormatter GMF_DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Pattern SETTLE_IND_REGEX = Pattern.compile("^[123]$");
    private static final Pattern CLIENT_REF_REGEX = Pattern.compile("^[A-Z0-9]{1,16}$");
    // Free-text billing address: letters, digits, space, and common punctuation, 1-40 chars
    private static final Pattern AVS_BILLING_ADDR_REGEX = Pattern.compile("^[A-Za-z0-9 ,.#/-]{1,40}$");
    private static final Pattern DEV_TYPE_IND_REGEX = Pattern.compile("^\\d{2}$");
    private static final Pattern ECOMM_TXN_IND_REGEX = Pattern.compile("^\\d{2}$");
    private static final Set<String> VALID_INFO_REQ_IND = Set.of("Y", "N");
    private static final Pattern SERVICE_ID_REGEX = Pattern.compile("^\\d{1,6}$");
    private static final Pattern TPPID_REGEX = Pattern.compile("^[A-Z]{3}\\d{3}$");

    // ---- Validation Sets ----
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
    private static final List<String> VALID_POS_COND_CODES = List.of("00", "01", "02", "03", "04", "05", "06", "08",
            "59", "71");
    private static final Set<Integer> VALID_ENTRY_MODES = Set.of(0, 1, 3, 4, 5, 7, 8, 9, 10, 79, 80, 82, 86, 90, 91,
            95);
    private static final Set<String> VALID_TERM_ENTRY_CAPS = Set.of("00", "01", "02", "03", "04", "05", "06", "07",
            "08", "09", "10", "11", "12", "13");
    private static final Set<String> VALID_TERM_CAT_CODES = Set.of("00", "01", "05", "06", "07", "08", "09", "12", "13",
            "17", "18");

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
    private static final Pattern EMV_DATA_REGEX = Pattern.compile("^[0-9A-Fa-f]{2,1998}$");
    private static final Pattern CARD_SEQ_REGEX = Pattern.compile("^\\d{3}$");
    private static final Pattern PC3_ADD_REGEX = Pattern.compile("^\\d{3}$");
    private static final Set<String> GROUP_IDS = Set.of("10001", "20001", "30001", "40001");

    // =====================================================================
    // Generic comparison engine
    // =====================================================================

    /**
     * Compares approvedValue and declinedValue for a field whose validity is
     * fully described by a single predicate.
     *
     * PATTERN = MATCHED only if both sides pass {@code formatCheck}.
     * VALUE   = MATCH only if the two values are equal AND both sides are valid.
     * Reason  = grammatically correct, field-specific explanation.
     */
    private ValidationResults compare(String field, String approved, String declined, Predicate<String> formatCheck) {
        FieldMeta meta = FieldLabels.of(field);
        String label = meta.label();
        String descriptor = meta.descriptor();

        boolean aValid = formatCheck.test(approved);
        boolean dValid = formatCheck.test(declined);

        if (aValid && dValid) {
            if (approved.equals(declined)) {
                return new ValidationResults(field, approved, declined, "MATCH",
                        "Approved and Declined values match. Valid " + label + ".", "MATCHED");
            }
            return new ValidationResults(field, approved, declined, "MISMATCH",
                    "Both Approved and Declined values are valid " + label + ", but they do not match.",
                    "MATCHED");
        }

        if (!aValid && !dValid) {
            return new ValidationResults(field, approved, declined, "MISMATCH",
                    invalidPhrase(label, descriptor) + " in both Approved and Declined field values.",
                    "MISMATCH");
        }

        String badSide = aValid ? "Declined" : "Approved";
        return new ValidationResults(field, approved, declined, "MISMATCH",
                invalidPhrase(label, descriptor) + " in the " + badSide + " field value.",
                "MISMATCH");
    }

    private String invalidPhrase(String label, String descriptor) {
        return "Unrecognized".equals(descriptor)
                ? "Unrecognized " + label
                : "Invalid " + label + " format";
    }

    // =====================================================================
    // Field validators
    // =====================================================================

    @Override
    public ValidationResults validateDID(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, v -> DID_REGEX.matcher(v).matches());
    }

    @Override
    public ValidationResults validateApp(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, "RAPIDCONNECTSRS"::equals);
    }

    @Override
    public ValidationResults validateAuth(String field, String approvedValue, String declinedValue,
                                           ServiceProvider serviceProvider) {
        TransactionContext context = serviceProvider.getTransactionContext();
        if (context == null) {
            return new ValidationResults(field, approvedValue, declinedValue, "MISMATCH",
                    "Unable to validate Auth: transaction context is unavailable.", "MISMATCH");
        }

        String termId = context.getTermID();
        String merchId = context.getMerchID();
        String groupId = context.getGroupID();

        List<String> missingContext = new ArrayList<>();
        if (isBlank(termId)) missingContext.add("TermID");
        if (isBlank(merchId)) missingContext.add("MerchID");
        if (isBlank(groupId)) missingContext.add("GroupID");

        if (!missingContext.isEmpty()) {
            return new ValidationResults(field, approvedValue, declinedValue, "MISMATCH",
                    "Unable to validate Auth: " + String.join(", ", missingContext) + " missing from transaction context.",
                    "MISMATCH");
        }

        String expectedAuth = groupId + merchId + "|" + termId;
        return compare(field, approvedValue, declinedValue, expectedAuth::equals);
    }

    @Override
    public ValidationResults validateClientRef(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, v -> CLIENT_REF_REGEX.matcher(v).matches());
    }

    @Override
    public ValidationResults validatePymtType(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, VALID_PYMT_TYPES::contains);
    }

    @Override
    public ValidationResults validateTxnType(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, VALID_TXN_TYPES::contains);
    }

    @Override
    public ValidationResults validateDateTime(String field, String approvedValue, String declinedValue) {
        Predicate<String> validDate = v -> {
            if (v.length() != 14 || !NUMERIC_GENERIC.matcher(v).matches()) {
                return false;
            }
            try {
                LocalDateTime.parse(v, GMF_DATETIME_FORMAT);
                return true;
            } catch (DateTimeParseException e) {
                return false;
            }
        };
        return compare(field, approvedValue, declinedValue, validDate);
    }

    @Override
    public ValidationResults validateStan(String field, String approvedValue, String declinedValue) {
        Predicate<String> validStan = v -> {
            if (!STAN_REGEX.matcher(v).matches()) return false;
            int stan = Integer.parseInt(v);
            return stan >= 1 && stan <= 999999;
        };
        return compare(field, approvedValue, declinedValue, validStan);
    }

    @Override
    public ValidationResults validatePOSEntryMode(String field, String approvedValue, String declinedValue) {
        Predicate<String> validMode = v -> {
            if (v.length() != 3 || !v.matches("\\d{3}")) return false;
            int entryPart = Integer.parseInt(v.substring(0, 2));
            int authPart = v.charAt(2) - '0';
            return VALID_ENTRY_MODES.contains(entryPart) && authPart >= 0 && authPart <= 6;
        };
        return compare(field, approvedValue, declinedValue, validMode);
    }

    @Override
    public ValidationResults validateTxnAmt(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, v -> TXN_AMT_REGEX.matcher(v).matches());
    }

    @Override
    public ValidationResults validateMerchEcho(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, v -> UUID_REGEX.matcher(v).matches());
    }

    @Override
    public ValidationResults validateOrderNum(String field, String approvedValue, String declinedValue) {
        Predicate<String> validOrderNum = v -> ORDER_NUM_REGEX.matcher(v).matches() && !v.matches("^0+$");
        return compare(field, approvedValue, declinedValue, validOrderNum);
    }

    @Override
    public ValidationResults validateRefNum(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, v -> REF_NUM_REGEX.matcher(v).matches());
    }

    @Override
    public ValidationResults validateTermID(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, v -> TERM_ID_REGEX.matcher(v).matches());
    }

    @Override
    public ValidationResults validateMerchID(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, v -> MERCH_ID_REGEX.matcher(v).matches());
    }

    @Override
    public ValidationResults validateMerchCatCode(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, v -> MCC_REGEX.matcher(v).matches());
    }

    @Override
    public ValidationResults validatePOSCondCode(String field, String approvedValue, String declinedValue) {
        Predicate<String> valid = v -> v.matches("\\d{2}") && VALID_POS_COND_CODES.contains(v);
        return compare(field, approvedValue, declinedValue, valid);
    }

    @Override
    public ValidationResults validateTermCatCode(String field, String approvedValue, String declinedValue) {
        Predicate<String> valid = v -> v.matches("^\\d{2}$") && VALID_TERM_CAT_CODES.contains(v);
        return compare(field, approvedValue, declinedValue, valid);
    }

    @Override
    public ValidationResults validateTermEntryCap(String field, String approvedValue, String declinedValue) {
        Predicate<String> valid = v -> v.matches("^\\d{2}$") && VALID_TERM_ENTRY_CAPS.contains(v);
        return compare(field, approvedValue, declinedValue, valid);
    }

    @Override
    public ValidationResults validateTxnCrncy(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, v -> v.matches("^\\d{3}$"));
    }

    @Override
    public ValidationResults validateTermLocInd(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, v -> v.matches("^[01]$"));
    }

    @Override
    public ValidationResults validateCardCaptCap(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, v -> v.matches("^[01]$"));
    }

    @Override
    public ValidationResults validateProgramID(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, v -> PROGRAM_ID_REGEX.matcher(v).matches());
    }

    @Override
    public ValidationResults validateGroupID(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, GROUP_IDS::contains);
    }

    @Override
    public ValidationResults validatePOSID(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, v -> POSID_REGEX.matcher(v).matches());
    }

    @Override
    public ValidationResults validateSettleInd(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, v -> SETTLE_IND_REGEX.matcher(v).matches());
    }

    @Override
    public ValidationResults validateTranInit(String field, String approvedValue, String declinedValue) {
        Predicate<String> valid = v -> v.length() <= 8 && VALID_TRAN_INIT_VALUES.contains(v);
        return compare(field, approvedValue, declinedValue, valid);
    }

    @Override
    public ValidationResults validateAcctNum(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, v -> ACCT_NUM_REGEX.matcher(v).matches());
    }

    @Override
    public ValidationResults validateCardExpiryDate(String field, String approvedValue, String declinedValue) {
        Predicate<String> valid = v -> {
            if (!CARD_EXPIRY_DATE_REGEX.matcher(v).matches()) return false;
            int month = Integer.parseInt(v.substring(4, 6));
            return month >= 1 && month <= 12;
        };
        return compare(field, approvedValue, declinedValue, valid);
    }

    @Override
    public ValidationResults validateTrack2(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, v -> TRACK2_REGEX.matcher(v).matches());
    }

    @Override
    public ValidationResults validateCardType(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, VALID_CARD_TYPES::contains);
    }

    @Override
    public ValidationResults validateAVSResultCode(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, v -> AVS_REGEX.matcher(v).matches());
    }

    @Override
    public ValidationResults validateCCVInd(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, VALID_CCV_INDICATORS::contains);
    }

    @Override
    public ValidationResults validateCCVData(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, v -> CCV_DATA_REGEX.matcher(v).matches());
    }

    @Override
    public ValidationResults validateCCVResultCode(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, VALID_CCV_RESULT_CODES::contains);
    }

    @Override
    public ValidationResults validatePINData(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, v -> PIN_DATA_REGEX.matcher(v).matches());
    }

    @Override
    public ValidationResults validateKeySerialNumData(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, v -> KSN_DATA_REGEX.matcher(v).matches());
    }

    @Override
    public ValidationResults validateAddAmtType(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, VALID_ADD_AMT_TYPES::contains);
    }

    @Override
    public ValidationResults validateAddAmtAcctType(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, VALID_ACCOUNT_TYPES::contains);
    }

    @Override
    public ValidationResults validatePartAuthCap(String field, String approvedValue, String declinedValue) {
        Predicate<String> valid = v -> "0".equals(v) || "1".equals(v);
        return compare(field, approvedValue, declinedValue, valid);
    }

    @Override
    public ValidationResults validateEMVData(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, v -> EMV_DATA_REGEX.matcher(v).matches());
    }

    @Override
    public ValidationResults validateCardSeqNum(String field, String approvedValue, String declinedValue) {
        Predicate<String> valid = v -> CARD_SEQ_REGEX.matcher(v).matches() && Integer.parseInt(v) <= 99;
        return compare(field, approvedValue, declinedValue, valid);
    }

    @Override
    public ValidationResults validatePC3Add(String field, String approvedValue, String declinedValue) {
        Predicate<String> valid = v -> {
            if (!PC3_ADD_REGEX.matcher(v).matches()) return false;
            int count = Integer.parseInt(v);
            return count >= 0 && count <= 999;
        };
        return compare(field, approvedValue, declinedValue, valid);
    }

    @Override
    public ValidationResults validateACI(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, VALID_ACI::contains);
    }

    @Override
    public ValidationResults validateAVSBillingPostalCode(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, v -> POSTAL_CODE_REGEX.matcher(v).matches());
    }

    @Override
    public ValidationResults validateAVSBillingAddr(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, v -> AVS_BILLING_ADDR_REGEX.matcher(v).matches());
    }

    @Override
    public ValidationResults validateDevTypeInd(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, v -> DEV_TYPE_IND_REGEX.matcher(v).matches());
    }

    @Override
    public ValidationResults validateEcommTxnInd(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, v -> ECOMM_TXN_IND_REGEX.matcher(v).matches());
    }

    @Override
    public ValidationResults validateInfoReqInd(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, VALID_INFO_REQ_IND::contains);
    }

    @Override
    public ValidationResults validateServiceID(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, v -> SERVICE_ID_REGEX.matcher(v).matches());
    }

    @Override
    public ValidationResults validateTPPID(String field, String approvedValue, String declinedValue) {
        return compare(field, approvedValue, declinedValue, v -> TPPID_REGEX.matcher(v).matches());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}