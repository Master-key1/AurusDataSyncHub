package com.auruspay.comparator.validation;

import com.auruspay.comparator.model.ValidationResult;
import com.auruspay.dto.TransactionContext;

public interface BusinessRule {

    ValidationResult validate(TransactionContext context);

}