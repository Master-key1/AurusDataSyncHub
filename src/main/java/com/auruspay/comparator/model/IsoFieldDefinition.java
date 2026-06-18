package com.auruspay.comparator.model;

public class IsoFieldDefinition {

    private String id;
    private String name;
    private int minLength;
    private int maxLength;
    private String classType;
    private String failedMsg;

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
}