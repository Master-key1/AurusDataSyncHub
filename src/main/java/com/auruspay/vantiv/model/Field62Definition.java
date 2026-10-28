package com.worldpay.iso8583.model;

public record Field62Definition(
        int number,
        String name,
        FieldFormat format,
        int length
) {
}
