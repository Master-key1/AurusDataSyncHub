package com.auruspay.comparator;

import com.auruspay.comparator.model.ValidationResult;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class FieldValidator {

private static final String STAN_PATTERN = "^\\d{6}$";

private static final Set<String> VALID_PYMT_TYPES = Set.of(
        "Credit",
        "Debit",
        "PLDebit",
        "EBT",
        "Check",
        "Prepaid",
        "PvtLabl",
        "Fleet",
        "AltCNP"
);

private static final Set<String> VALID_TXN_TYPES = Set.of(
        "Activation",
        "Authorization",
        "BalanceInquiry",
        "BalanceLock",
        "BatchSettleDetail",
        "BatchSettleL3",
        "CanadaKeyRequest",
        "CancelDeferredAuth",
        "Cashout",
        "CashoutActiveStatus",
        "Change",
        "CloseBatch",
        "Completion",
        "DisableInternetUse",
        "EncryptionKeyRequest",
        "EchoTest",
        "FileDownload",
        "FraudScore",
        "GenerateKey",
        "HostTotals",
        "InternetActivation",
        "Load",
        "OpenBatch",
        "PCL3AddDetail",
        "ProductEligInquiry",
        "Redemption",
        "RedemptionUnlock",
        "Refund",
        "Reload",
        "Sale",
        "TACertAuthority",
        "TAKeyRequest",
        "TATokenRequest",
        "Verification",
        "VoucherClear"
);

public ValidationResult validate(String id, String value) {

    if (value == null) {
        return new ValidationResult(
                "INVALID",
                "NOT NULL",
                "Value is null"
        );
    }

    value = value.trim();

    if ("TAG MISSING".equals(value)) {
        return new ValidationResult(
                "MISSING",
                "Required",
                "Field is missing in request"
        );
    }

    if ("EMPTY VALUE".equals(value) || value.isEmpty()) {
        return new ValidationResult(
                "EMPTY",
                "Non Empty",
                "Field value cannot be empty"
        );
    }

    return switch (id) {

        case "STAN" ->
                validateStan(value);

        case "PymtType" ->
                validatePymtType(value);

        case "TxnType" ->
                validateTxnType(value);

        default ->
                new ValidationResult(
                        "VALID",
                        "N/A",
                        "No validation rule configured"
                );
    };
}

private ValidationResult validateStan(String value) {

    if (!value.matches(STAN_PATTERN)) {
        return new ValidationResult(
                "INVALID",
                STAN_PATTERN,
                "STAN must contain exactly 6 numeric digits"
        );
    }

    int stan = Integer.parseInt(value);

    if (stan < 1 || stan > 999999) {
        return new ValidationResult(
                "INVALID",
                "000001-999999",
                "STAN must be between 000001 and 999999"
        );
    }

    return new ValidationResult(
            "VALID",
            STAN_PATTERN,
            "Valid STAN"
    );
}

private ValidationResult validatePymtType(String value) {

    if (value.length() > 7) {
        return new ValidationResult(
                "INVALID",
                "Length 1-7",
                "Payment Type length must be between 1 and 7"
        );
    }

    if (!VALID_PYMT_TYPES.contains(value)) {
        return new ValidationResult(
                "INVALID",
                VALID_PYMT_TYPES.toString(),
                "Unsupported Payment Type"
        );
    }

    return new ValidationResult(
            "VALID",
            VALID_PYMT_TYPES.toString(),
            "Valid Payment Type"
    );
}

private ValidationResult validateTxnType(String value) {

    if (value.length() > 20) {
        return new ValidationResult(
                "INVALID",
                "Length 1-20",
                "Transaction Type length must be between 1 and 20"
        );
    }

    if (!VALID_TXN_TYPES.contains(value)) {
        return new ValidationResult(
                "INVALID",
                VALID_TXN_TYPES.toString(),
                "Unsupported Transaction Type"
        );
    }

    return new ValidationResult(
            "VALID",
            VALID_TXN_TYPES.toString(),
            "Valid Transaction Type"
    );
}

}