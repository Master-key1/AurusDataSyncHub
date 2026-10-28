package com.auruspay.dto;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;

@Component
public class RequestDto {

    @JsonProperty("team")
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
        return "WrapperRequestDto{" +
                "category='" + category + '\'' +
                ", txnId='" + txnId + '\'' +
                ", uuid='" + uuid + '\'' +
                '}';
    }
}