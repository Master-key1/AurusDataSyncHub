package com.auruspay.vantiv.dto;

import java.util.List;

public class ComparisonReport {

    private ExecutiveSummary executiveSummary;

    private List<FieldComparisonResult> comparison;

    public ExecutiveSummary getExecutiveSummary() {
        return executiveSummary;
    }

    public void setExecutiveSummary(ExecutiveSummary executiveSummary) {
        this.executiveSummary = executiveSummary;
    }

    public List<FieldComparisonResult> getComparison() {
        return comparison;
    }

    public void setComparison(List<FieldComparisonResult> comparison) {
        this.comparison = comparison;
    }
}