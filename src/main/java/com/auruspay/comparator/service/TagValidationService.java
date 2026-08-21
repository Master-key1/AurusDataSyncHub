package com.auruspay.comparator.service;

import com.auruspay.comparator.model.ValidationResults;
import com.auruspay.service.ServiceProvider;

/**
 * Field-level validation contract. Each method is called ONLY when both the
 * Approved and Declined values are present (non-blank) for the field -
 * missing-value handling is done upstream in {@link FieldValidators}.
 *
 * Each method:
 *   1. Checks that approvedValue individually satisfies the field's format/set rule.
 *   2. Checks that declinedValue individually satisfies the same rule.
 *   3. Compares the two values against each other.
 */
public interface TagValidationService {

    ValidationResults validateDID(String field, String approvedValue, String declinedValue);
    ValidationResults validateApp(String field, String approvedValue, String declinedValue);
    ValidationResults validateAuth(String field, String approvedValue, String declinedValue, ServiceProvider serviceProvider);
    ValidationResults validateClientRef(String field, String approvedValue, String declinedValue);
    ValidationResults validatePymtType(String field, String approvedValue, String declinedValue);
    ValidationResults validateTxnType(String field, String approvedValue, String declinedValue);
    ValidationResults validateDateTime(String field, String approvedValue, String declinedValue);
    ValidationResults validateStan(String field, String approvedValue, String declinedValue);
    ValidationResults validatePOSEntryMode(String field, String approvedValue, String declinedValue);
    ValidationResults validateTxnAmt(String field, String approvedValue, String declinedValue);
    ValidationResults validateMerchEcho(String field, String approvedValue, String declinedValue);
    ValidationResults validateOrderNum(String field, String approvedValue, String declinedValue);
    ValidationResults validateRefNum(String field, String approvedValue, String declinedValue);
    ValidationResults validateTermID(String field, String approvedValue, String declinedValue);
    ValidationResults validateMerchID(String field, String approvedValue, String declinedValue);
    ValidationResults validateMerchCatCode(String field, String approvedValue, String declinedValue);
    ValidationResults validatePOSCondCode(String field, String approvedValue, String declinedValue);
    ValidationResults validateTermCatCode(String field, String approvedValue, String declinedValue);
    ValidationResults validateTermEntryCap(String field, String approvedValue, String declinedValue);
    ValidationResults validateTxnCrncy(String field, String approvedValue, String declinedValue);
    ValidationResults validateTermLocInd(String field, String approvedValue, String declinedValue);
    ValidationResults validateCardCaptCap(String field, String approvedValue, String declinedValue);
    ValidationResults validateProgramID(String field, String approvedValue, String declinedValue);
    ValidationResults validateGroupID(String field, String approvedValue, String declinedValue);
    ValidationResults validatePOSID(String field, String approvedValue, String declinedValue);
    ValidationResults validateSettleInd(String field, String approvedValue, String declinedValue);
    ValidationResults validateTranInit(String field, String approvedValue, String declinedValue);
    ValidationResults validateAcctNum(String field, String approvedValue, String declinedValue);
    ValidationResults validateCardExpiryDate(String field, String approvedValue, String declinedValue);
    ValidationResults validateTrack2(String field, String approvedValue, String declinedValue);
    ValidationResults validateCardType(String field, String approvedValue, String declinedValue);
    ValidationResults validateAVSResultCode(String field, String approvedValue, String declinedValue);
    ValidationResults validateCCVInd(String field, String approvedValue, String declinedValue);
    ValidationResults validateCCVData(String field, String approvedValue, String declinedValue);
    ValidationResults validateCCVResultCode(String field, String approvedValue, String declinedValue);
    ValidationResults validatePINData(String field, String approvedValue, String declinedValue);
    ValidationResults validateKeySerialNumData(String field, String approvedValue, String declinedValue);
    ValidationResults validateAddAmtType(String field, String approvedValue, String declinedValue);
    ValidationResults validateAddAmtAcctType(String field, String approvedValue, String declinedValue);
    ValidationResults validatePartAuthCap(String field, String approvedValue, String declinedValue);
    ValidationResults validateEMVData(String field, String approvedValue, String declinedValue);
    ValidationResults validateCardSeqNum(String field, String approvedValue, String declinedValue);
    ValidationResults validatePC3Add(String field, String approvedValue, String declinedValue);
    ValidationResults validateACI(String field, String approvedValue, String declinedValue);
    ValidationResults validateAVSBillingPostalCode(String field, String approvedValue, String declinedValue);

    ValidationResults validateAVSBillingAddr(String field, String approvedValue, String declinedValue);
    ValidationResults validateDevTypeInd(String field, String approvedValue, String declinedValue);
    ValidationResults validateEcommTxnInd(String field, String approvedValue, String declinedValue);
    ValidationResults validateInfoReqInd(String field, String approvedValue, String declinedValue);
    ValidationResults validateServiceID(String field, String approvedValue, String declinedValue);
    ValidationResults validateTPPID(String field, String approvedValue, String declinedValue);
}