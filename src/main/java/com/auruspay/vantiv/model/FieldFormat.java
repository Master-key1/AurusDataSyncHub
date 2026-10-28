package com.worldpay.iso8583.model;

/**
 * Wire encodings used across the Worldpay ISO 8583 field set.
 * See "ISO 8583 Reference Guide" Chapter 5, Attributes line per field.
 */
public enum FieldFormat {

    FIXED_NUMERIC_PACKED,
    FIXED_ALPHA,
    LLVAR_NUMERIC_PACKED,
    TRACK2_DATA,
    LLVAR_RAW,
    LLLVAR_BINARY_LENGTH,
    LLLVAR_ASCII_BINARY_LENGTH,
    BINARY_FIXED,
    DECIMAL_ASCII,
    FIXED_HEX,
    FIXED_ASCII,
    LLVAR_ASCII,
    LLVAR_HEX,
    REMAINING;
    // methods or fields go here
}