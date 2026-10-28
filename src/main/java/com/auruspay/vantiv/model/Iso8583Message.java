package com.worldpay.iso8583.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Result of parsing one raw ISO 8583 hex dump: the MTI, which bits were set,
 * the decoded value per bit, and any structural problems found along the way
 * (bad length, truncated field, non-digit in a packed-numeric field, etc.).
 */
public class Iso8583Message {

    private String mti;
    private final List<Integer> presentBits = new ArrayList<>();
    private final Map<Integer, String> fieldValues = new LinkedHashMap<>();
    private final List<String> parseIssues = new ArrayList<>();
    private final Map<Integer, Map<String, String>> subFields = new LinkedHashMap<>();
   
    
    public Map<String, String> getSubFields(int bit) {
        return subFields.get(bit);
    }
    
    public Map<Integer, Map<String, String>> getSubFields() {
        return subFields;
    }

    public void putSubFields(int bit, Map<String, String> value) {
        subFields.put(bit, value);
    }

    public String getMti() {
        return mti;
    }

    public void setMti(String mti) {
        this.mti = mti;
    }

    public List<Integer> getPresentBits() {
        return presentBits;
    }

    public Map<Integer, String> getFieldValues() {
        return fieldValues;
    }

    public List<String> getParseIssues() {
        return parseIssues;
    }

    public void putField(int bit, String value) {
        presentBits.add(bit);
        fieldValues.put(bit, value);
    }

    public void addIssue(String issue) {
        parseIssues.add(issue);
    }

    public boolean hasField(int bit) {
        return fieldValues.containsKey(bit);
    }
}