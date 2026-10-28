package com.worldpay.iso8583.model;

public class DE60 {

    private String terminalType;
    private String physicalTerminalLocation;
    private String terminalEntryCapability;
    private String merchantTypeIndicator;
    private String posCardRetentionIndicator;
    private String posTransactionStatusIndicator;
    private String posTransactionRoutingIndicator;
    private String chainCode;
    private String divisionNumber;
    private String storeNumber;
    private String laneNumber;
    private String employeeNumber;

    public String getTerminalType() {
        return terminalType;
    }

    public void setTerminalType(String terminalType) {
        this.terminalType = terminalType;
    }

    public String getPhysicalTerminalLocation() {
        return physicalTerminalLocation;
    }

    public void setPhysicalTerminalLocation(String physicalTerminalLocation) {
        this.physicalTerminalLocation = physicalTerminalLocation;
    }

    public String getTerminalEntryCapability() {
        return terminalEntryCapability;
    }

    public void setTerminalEntryCapability(String terminalEntryCapability) {
        this.terminalEntryCapability = terminalEntryCapability;
    }

    public String getMerchantTypeIndicator() {
        return merchantTypeIndicator;
    }

    public void setMerchantTypeIndicator(String merchantTypeIndicator) {
        this.merchantTypeIndicator = merchantTypeIndicator;
    }

    public String getPosCardRetentionIndicator() {
        return posCardRetentionIndicator;
    }

    public void setPosCardRetentionIndicator(String posCardRetentionIndicator) {
        this.posCardRetentionIndicator = posCardRetentionIndicator;
    }

    public String getPosTransactionStatusIndicator() {
        return posTransactionStatusIndicator;
    }

    public void setPosTransactionStatusIndicator(String posTransactionStatusIndicator) {
        this.posTransactionStatusIndicator = posTransactionStatusIndicator;
    }

    public String getPosTransactionRoutingIndicator() {
        return posTransactionRoutingIndicator;
    }

    public void setPosTransactionRoutingIndicator(String posTransactionRoutingIndicator) {
        this.posTransactionRoutingIndicator = posTransactionRoutingIndicator;
    }

    public String getChainCode() {
        return chainCode;
    }

    public void setChainCode(String chainCode) {
        this.chainCode = chainCode;
    }

    public String getDivisionNumber() {
        return divisionNumber;
    }

    public void setDivisionNumber(String divisionNumber) {
        this.divisionNumber = divisionNumber;
    }

    public String getStoreNumber() {
        return storeNumber;
    }

    public void setStoreNumber(String storeNumber) {
        this.storeNumber = storeNumber;
    }
    
    public String getLaneNumber() {
        return laneNumber;
    }

    public void setLaneNumber(String laneNumber) {
        this.laneNumber = laneNumber;
    }

    public String getEmployeeNumber() {
        return storeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }
}