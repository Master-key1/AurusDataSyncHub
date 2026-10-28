package com.auruspay.comparator;

public class IsoFieldDefinition {

    private String fieldName;
    private String classType;
    private int minLength;
    private int maxLength;

    public IsoFieldDefinition(
            String fieldName,
            String classType,
            int minLength,
            int maxLength) {

        this.fieldName = fieldName;
        this.classType = classType;
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getClassType() {
        return classType;
    }

    public void setClassType(String classType) {
        this.classType = classType;
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
}