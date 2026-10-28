package com.auruspay.comparator.model;

public record ValidationResults1(
        String tag,
        String valueA,
        String valueD,
        String status,
        String pattern,
        String reason
) {}