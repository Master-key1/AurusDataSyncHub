package com.auruspay.fiserv.comparator.validation;

import com.auruspay.fiserv.comparator.model.ValidationResult;
import com.auruspay.fiserv.dto.TransactionContext;

public interface BusinessRule {

    ValidationResult validate(TransactionContext context);

}