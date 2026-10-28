package com.auruspay.comparator.model;

import java.util.*;

public class XmlGroupData {

    private Map<String, String> normalFields = new LinkedHashMap<>();

    private Map<String, String> purchCardlvl2Grp = new LinkedHashMap<>();

    private List<Map<String, String>> purchCardlvl3Grp = new ArrayList<>();


    public Map<String, String> getNormalFields() {
        return normalFields;
    }

    public void setNormalFields(Map<String, String> normalFields) {
        this.normalFields = normalFields;
    }


    public Map<String, String> getPurchCardlvl2Grp() {
        return purchCardlvl2Grp;
    }

    public void setPurchCardlvl2Grp(Map<String, String> purchCardlvl2Grp) {
        this.purchCardlvl2Grp = purchCardlvl2Grp;
    }


    public List<Map<String, String>> getPurchCardlvl3Grp() {
        return purchCardlvl3Grp;
    }

    public void setPurchCardlvl3Grp(List<Map<String, String>> purchCardlvl3Grp) {
        this.purchCardlvl3Grp = purchCardlvl3Grp;
    }
}