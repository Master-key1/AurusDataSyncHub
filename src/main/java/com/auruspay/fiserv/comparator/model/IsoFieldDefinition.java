package com.auruspay.comparator.model;

public class IsoFieldDefinition {

    private String id;
    private String name;
    private int minLength;
    private int maxLength;
    private String classType;
    private String failedMsg;

    // The ISO field XML's value="..." attribute — a comma-separated list of
    // allowed tokens for ENUM classType fields, e.g. value="0,1,2". Null/blank
    // for every non-ENUM field, and for ENUM fields that don't restrict values.
    private String value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
    }

    public String getFailedMsg() {
        return failedMsg;
    }

    public void setFailedMsg(String failedMsg) {
        this.failedMsg = failedMsg;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}