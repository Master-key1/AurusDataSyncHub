
package com.auruspay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestDto {

    @JsonProperty("category")
    private String category;

    @JsonProperty("txnId")
    private String txnId;

    @JsonProperty("uuid")
    private String uuid;

    public RequestDto() {
    }

    public RequestDto(String category, String txnId, String uuid) {
        this.category = category;
        this.txnId = txnId;
        this.uuid = uuid;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "RequestDto{" +
                "category='" + category + '\'' +
                ", txnId='" + txnId + '\'' +
                ", uuid='" + uuid + '\'' +
                '}';
    }
}
