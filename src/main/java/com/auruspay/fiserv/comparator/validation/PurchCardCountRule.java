package com.auruspay.comparator.validation;

import com.auruspay.comparator.model.ValidationResult;
import com.auruspay.dto.TransactionContext;
import org.springframework.stereotype.Component;


@Component
public class PurchCardCountRule implements BusinessRule {


    @Override
    public ValidationResult validate(TransactionContext context) {


        if(context == null) {

            return new ValidationResult(
                    "INVALID",
                    "MISMATCH",
                    "TransactionContext is null"
            );
        }


        if(context.getPurchCardlvl2Grp() == null) {

            return new ValidationResult(
                    "INVALID",
                    "MISMATCH",
                    "PurchCardlvl2Grp missing"
            );
        }


        String pc3Add =
                context.getPurchCardlvl2Grp()
                       .getPC3Add();


        if(pc3Add == null || pc3Add.isBlank()) {

            return new ValidationResult(
                    "INVALID",
                    "MISMATCH",
                    "PC3Add missing"
            );
        }


        int expected =
                Integer.parseInt(pc3Add);


        int actual =
                context.getPurchCardlvl3Grp() == null
                ? 0
                : context.getPurchCardlvl3Grp().size();



        if(expected != actual) {

            return new ValidationResult(
                    "INVALID",
                    "MISMATCH",
                    "PC3Add expected "
                    + expected
                    + " PurchCardlvl3Grp but found "
                    + actual
            );
        }


        return new ValidationResult(
                "VALID",
                "MATCHED",
                "PC3Add count validation passed. Count="
                + actual
        );
    }
}