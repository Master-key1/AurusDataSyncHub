package com.auruspay.comparator.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.auruspay.comparator.model.ValidationResults;
import com.auruspay.comparator.service.FieldLabels.FieldMeta;
import com.auruspay.service.ServiceProvider;

@Component
public class FieldValidators {

    @Autowired
    private ServiceProvider serviceProvider;

    public ValidationResults validate(String field1, String approvedValue, String field2, String declinedValue, Map<String, String> approvedRequest, Map<String, String> declinedRequest) {

        boolean approvedMissing = isMissing(approvedValue);
        boolean declinedMissing = isMissing(declinedValue);

        if (approvedMissing || declinedMissing) {
            String label = FieldLabels.of(field1).label();
            String reason;
            if (approvedMissing && declinedMissing) {
                reason = label + " is missing in both Approved and Declined field values.";
            } else if (approvedMissing) {
                reason = label + " is missing in the Approved field value.";
            } else {
                reason = label + " is missing in the Declined field value.";
            }
            return new ValidationResults(field1, approvedValue, declinedValue, "MISMATCH", reason, "MISMATCH");
        }
        
        

        String approved = approvedValue.trim();
        String declined = declinedValue.trim();

        return switch (field1) {
        
            case "DID" -> serviceProvider.getTagValidationService().validateDID(field1, approved, declined);
            case "App" -> serviceProvider.getTagValidationService().validateApp(field1, approved, declined);
            case "Auth" -> serviceProvider.getTagValidationService().validateAuth(field1, approved, declined, serviceProvider);
            case "ClientRef" -> serviceProvider.getTagValidationService().validateClientRef(field1, approved, declined);
            case "PymtType" -> serviceProvider.getTagValidationService().validatePymtType(field1, approved, declined);
            case "TxnType" -> serviceProvider.getTagValidationService().validateTxnType(field1, approved, declined);
            case "LocalDateTime", "TrnmsnDateTime" -> serviceProvider.getTagValidationService().validateDateTime(field1, approved, declined);
            case "STAN" -> serviceProvider.getTagValidationService().validateStan(field1, approved, declined);
            case "POSEntryMode" -> serviceProvider.getTagValidationService().validatePOSEntryMode(field1, approved, declined);
            case "TxnAmt" -> serviceProvider.getTagValidationService().validateTxnAmt(field1, approved, declined);
            case "MerchEcho" -> serviceProvider.getTagValidationService().validateMerchEcho(field1, approved, declined);

            case "OrderNum" -> serviceProvider.getTagValidationService().validateOrderNum(field1, approved, declined);
            case "RefNum" -> serviceProvider.getTagValidationService().validateRefNum(field1, approved, declined);
            case "TermID" -> serviceProvider.getTagValidationService().validateTermID(field1, approved, declined);
            case "MerchID" -> serviceProvider.getTagValidationService().validateMerchID(field1, approved, declined);
            case "MerchCatCode" -> serviceProvider.getTagValidationService().validateMerchCatCode(field1, approved, declined);
            case "POSCondCode" -> serviceProvider.getTagValidationService().validatePOSCondCode(field1, approved, declined);
            case "TermCatCode" -> serviceProvider.getTagValidationService().validateTermCatCode(field1, approved, declined);
            case "TermEntryCapablt" -> serviceProvider.getTagValidationService().validateTermEntryCap(field1, approved, declined);
            case "TxnCrncy" -> serviceProvider.getTagValidationService().validateTxnCrncy(field1, approved, declined);
            case "TermLocInd" -> serviceProvider.getTagValidationService().validateTermLocInd(field1, approved, declined);
            case "CardCaptCap" -> serviceProvider.getTagValidationService().validateCardCaptCap(field1, approved, declined);
            case "ProgramID" -> serviceProvider.getTagValidationService().validateProgramID(field1, approved, declined);
            case "GroupID" -> serviceProvider.getTagValidationService().validateGroupID(field1, approved, declined);

            case "POSID" -> serviceProvider.getTagValidationService().validatePOSID(field1, approved, declined);
            case "SettleInd" -> serviceProvider.getTagValidationService().validateSettleInd(field1, approved, declined);
            case "TranInit" -> serviceProvider.getTagValidationService().validateTranInit(field1, approved, declined);

            // CardGroup
            case "AcctNum" -> serviceProvider.getTagValidationService().validateAcctNum(field1, approved, declined);
            case "CardExpiryDate" -> serviceProvider.getTagValidationService().validateCardExpiryDate(field1, approved, declined);
            case "Track2Data" -> serviceProvider.getTagValidationService().validateTrack2(field1, approved, declined);
            case "CardType" -> serviceProvider.getTagValidationService().validateCardType(field1, approved, declined);
            case "AVSResultCode" -> serviceProvider.getTagValidationService().validateAVSResultCode(field1, approved, declined);
            case "CCVInd" -> serviceProvider.getTagValidationService().validateCCVInd(field1, approved, declined);
            case "CCVData" -> serviceProvider.getTagValidationService().validateCCVData(field1, approved, declined);
            case "CCVResultCode" -> serviceProvider.getTagValidationService().validateCCVResultCode(field1, approved, declined);

            // Pin group
            case "PINData" -> serviceProvider.getTagValidationService().validatePINData(field1, approved, declined);
            case "KeySerialNumData" -> serviceProvider.getTagValidationService().validateKeySerialNumData(field1, approved, declined);

            // Additional amounts group (AddtlAmtGrp)
            case "AddAmtType" -> serviceProvider.getTagValidationService().validateAddAmtType(field1, approved, declined);
            case "AddAmtAcctType" -> serviceProvider.getTagValidationService().validateAddAmtAcctType(field1, approved, declined);
            case "PartAuthrztnApprvlCapablt" -> serviceProvider.getTagValidationService().validatePartAuthCap(field1, approved, declined);

            // EMV group
            case "EMVData" -> serviceProvider.getTagValidationService().validateEMVData(field1, approved, declined);
            case "CardSeqNum" -> serviceProvider.getTagValidationService().validateCardSeqNum(field1, approved, declined);
            case "PC3Add" -> serviceProvider.getTagValidationService().validatePC3Add(field1, approved, declined);

            case "ACI" -> serviceProvider.getTagValidationService().validateACI(field1, approved, declined);
            case "AVSBillingPostalCode" -> serviceProvider.getTagValidationService().validateAVSBillingPostalCode(field1, approved, declined);

            case "AVSBillingAddr" -> serviceProvider.getTagValidationService().validateAVSBillingAddr(field1, approved, declined);
            case "DevTypeInd" -> serviceProvider.getTagValidationService().validateDevTypeInd(field1, approved, declined);
            case "EcommTxnInd" -> serviceProvider.getTagValidationService().validateEcommTxnInd(field1, approved, declined);
            case "InfoReqInd" -> serviceProvider.getTagValidationService().validateInfoReqInd(field1, approved, declined);
            case "ServiceID" -> serviceProvider.getTagValidationService().validateServiceID(field1, approved, declined);
            case "TPPID" -> serviceProvider.getTagValidationService().validateTPPID(field1, approved, declined);

            default -> {
                boolean equal = approved.equals(declined);
                yield new ValidationResults(
                        field1,
                        approved,
                        declined,
                        equal ? "MATCH" : "MISMATCH",
                        "No validation rule configured for " + field1 + ".",
                        "MATCHED"
                );
            }
        };
    }

    private boolean isMissing(String value) {
        return value == null
                || value.trim().isEmpty()
                || "TAG MISSING".equalsIgnoreCase(value.trim())
                || "EMPTY VALUE".equalsIgnoreCase(value.trim());
    }
}