package com.worldpay.iso8583.parser;

import com.worldpay.iso8583.model.DE60;

public class DE60Parser {

    public DE60 parse(String value) {

        DE60 de60 = new DE60();

        if (value == null) {
            return de60;
        }

        int pos = 0;

        de60.setTerminalType(read(value, pos, 1));
        pos += 1;

        de60.setPhysicalTerminalLocation(read(value, pos, 1));
        pos += 1;

        de60.setTerminalEntryCapability(read(value, pos, 1));
        pos += 1;

        de60.setMerchantTypeIndicator(read(value, pos, 1));
        pos += 1;

        de60.setPosCardRetentionIndicator(read(value, pos, 1));
        pos += 1;

        de60.setPosTransactionStatusIndicator(read(value, pos, 1));
        pos += 1;

        de60.setPosTransactionRoutingIndicator(read(value, pos, 1));
        pos += 1;

        de60.setChainCode(read(value, pos, 6));
        pos += 6;

        de60.setDivisionNumber(read(value, pos, 3));
        pos += 3;
        
        de60.setStoreNumber(read(value, pos, 8));
        pos += 8;
        
        de60.setLaneNumber(read(value, pos, 3));
        pos += 3;

        if (pos < value.length()) {
            de60.setEmployeeNumber(value.substring(pos));
        }

        return de60;
    }

    private String read(String value, int start, int length) {

        if (start >= value.length()) {
            return "";
        }

        int end = Math.min(start + length, value.length());

        return value.substring(start, end);
    }
}