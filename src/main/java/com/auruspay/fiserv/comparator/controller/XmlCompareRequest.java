package com.auruspay.comparator.controller;

/**
 * Request payload for the XML comparison endpoint.
 */
public class XmlCompareRequest {

    private String approvedXml;
    private String declinedXml;

    public XmlCompareRequest() {
    }

    public XmlCompareRequest(String approvedXml, String declinedXml) {
        this.approvedXml = approvedXml;
        this.declinedXml = declinedXml;
    }

    public String getApprovedXml() {
        return approvedXml;
    }

    public void setApprovedXml(String approvedXml) {
        this.approvedXml = approvedXml;
    }

    public String getDeclinedXml() {
        return declinedXml;
    }

    public void setDeclinedXml(String declinedXml) {
        this.declinedXml = declinedXml;
    }
}