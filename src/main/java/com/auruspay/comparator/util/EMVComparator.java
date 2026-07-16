package com.auruspay.comparator.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.stereotype.Component;

import com.auruspay.comparator.model.EMVComparisonResult;

@Component
public class EMVComparator {

    public Map<String, EMVComparisonResult> compare(
            Map<String, String> approved,
            Map<String, String> declined) {

        Map<String, EMVComparisonResult> result = new LinkedHashMap<>();

        Set<String> allTags = new TreeSet<>();
        allTags.addAll(approved.keySet());
        allTags.addAll(declined.keySet());

        for (String tag : allTags) {

            String approvedValue = approved.get(tag);
            String declinedValue = declined.get(tag);

            String status;

            if (approvedValue == null) {
                status = "MISSING_IN_APPROVED";
            } else if (declinedValue == null) {
                status = "MISSING_IN_DECLINED";
            } else if (approvedValue.equals(declinedValue)) {
                status = "MATCH";
            } else {
                status = "MISMATCH";
            }

            result.put(tag,
                    new EMVComparisonResult(
                            tag,
                            approvedValue,
                            declinedValue,
                            status));
        }

        return result;
    }
}