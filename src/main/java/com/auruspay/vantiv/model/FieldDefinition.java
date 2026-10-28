package com.worldpay.iso8583.model;

/**
 * Static metadata for one ISO 8583 bit, taken from the Worldpay Reference Guide.
 *
 * @param bit          data element number (2-127)
 * @param name         human-readable field name
 * @param format       wire encoding
 * @param byteLength   for FIXED_* formats: number of bytes on the wire.
 *                     Ignored for the two *VAR formats (length is read from the message itself).
 * @param digitLength  for FIXED_NUMERIC_PACKED: number of significant digits to keep
 *                     (packed fields sometimes pad the last nibble - e.g. 3 digits in 2 bytes).
 *                     0 means "use all digits implied by byteLength".
 */
public record FieldDefinition(
        int bit,
        String name,
        FieldFormat format,
        int byteLength,
        int digitLength
) {
    public static FieldDefinition fixedNumeric(int bit, String name, int byteLength, int digitLength) {
        return new FieldDefinition(bit, name, FieldFormat.FIXED_NUMERIC_PACKED, byteLength, digitLength);
    }

    public static FieldDefinition fixedAlpha(int bit, String name, int byteLength) {
        return new FieldDefinition(bit, name, FieldFormat.FIXED_ALPHA, byteLength, 0);
    }

    public static FieldDefinition llVarNumeric(int bit, String name) {
        return new FieldDefinition(bit, name, FieldFormat.LLVAR_NUMERIC_PACKED, 0, 0);
    }

    public static FieldDefinition track2(int bit, String name) {
        return new FieldDefinition(bit, name, FieldFormat.TRACK2_DATA, 0, 0);
    }

    public static FieldDefinition llVarRaw(int bit, String name) {
        return new FieldDefinition(bit, name, FieldFormat.LLVAR_RAW, 0, 0);
    }

    public static FieldDefinition lllVarBinary(int bit, String name) {
        return new FieldDefinition(bit, name, FieldFormat.LLLVAR_BINARY_LENGTH, 0, 0);
    }

    public static FieldDefinition binaryFixed(int bit, String name, int byteLength) {
        return new FieldDefinition(bit, name, FieldFormat.BINARY_FIXED, byteLength, 0);
    }

	public static FieldDefinition decimalAscii(double d, String string,int byteLength) {
		// TODO Auto-generated method stub
		return null;
	}
	public static FieldDefinition lllVarAscii(int bit, String name) {
	    return new FieldDefinition(
	            bit,
	            name,
	            FieldFormat.LLLVAR_ASCII_BINARY_LENGTH,
	            0,
	            0);
	}
}