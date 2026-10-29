package com.auruspay.vantiv.parser;

import java.util.HashMap;
import java.util.Map;

import com.auruspay.vantiv.model.Field62Definition;
import com.auruspay.vantiv.model.FieldFormat;

public class Field62Dictionary {

    private static final Map<Integer, Field62Definition> MAP =
            new HashMap<>();

    static {

        add(2,"Terminal Sequence Number",
        		FieldFormat.FIXED_HEX,6);

        add(38,"Sales Tax Amount",
        		FieldFormat.FIXED_ASCII,20);

        add(44,"DCC 2 Pass Flag",
        		FieldFormat.FIXED_ASCII,2);

        add(46,"Draft Locator ID",
        		FieldFormat.FIXED_ASCII,22);

        add(50,"Sales Tax Indicator",
        		FieldFormat.FIXED_ASCII,2);

        add(63,"Tip Amount",
        		FieldFormat.FIXED_HEX,12);

        add(68,"POS Environment Indicator",
        		FieldFormat.FIXED_ASCII,2);

        add(69,"Customer Bill-To Address",
        		FieldFormat.FIXED_ASCII,244);

        add(70,"Customer Shipping Data",
        		FieldFormat.FIXED_ASCII,228);

        add(72,"Customer Order ID",
        		FieldFormat.FIXED_ASCII,20);

        add(73,"Customer Email",
        		FieldFormat.FIXED_ASCII,36);

        add(74,"Customer Phone Number",
        		FieldFormat.FIXED_ASCII,20);

        add(75,"Customer IP Address",
        		FieldFormat.FIXED_ASCII,30);

        add(76,"Customer Web Session ID",
        		FieldFormat.FIXED_ASCII,30);

        add(88,"User Account Data",
        		FieldFormat.FIXED_ASCII,300);

        add(89,"Purchase Information",
        		FieldFormat.FIXED_ASCII,400);

        add(90,"Line Item",
        		FieldFormat.LLVAR_ASCII,0);

        add(91,"Payments General Data",
        		FieldFormat.FIXED_ASCII,40);

        add(92,"Membership Data",
        		FieldFormat.REMAINING,0);
    }

    private static void add(int number,
                            String name,
                            FieldFormat format,
                            int length){

        MAP.put(number,
                new Field62Definition(number,
                        name,
                        format,
                        length));
    }

    public static Field62Definition get(int number){

        return MAP.get(number);
    }

}