package com.auruspay.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.auruspay.comparator.service.EMVComparators;
import com.auruspay.comparator.service.EmvParsers;
import com.auruspay.comparator.service.EmvRootCauseReport;
import com.auruspay.comparator.service.TagValidationService;
import com.auruspay.dto.TransactionContext;

@Component
public class ServiceProvider {

    @Autowired
    private TransactionContext transactionContext;

    @Autowired
    private EmvParsers emvParsers;

    @Autowired
    private TagValidationService tagValidationService;

    @Autowired
    private EMVComparators advanceEmvcomparator;

    @Autowired
    private EmvRootCauseReport emvRootCauseReport;

    public ServiceProvider(TransactionContext transactionContext, EmvParsers emvParser,
            TagValidationService tagValidationService) {
        super();
        this.transactionContext = transactionContext;
        this.emvParsers = emvParser;
        this.tagValidationService = tagValidationService;
    }

    public EMVComparators getAdvanceEmvcomparator() {
        return advanceEmvcomparator;
    }

    public void setAdvanceEmvcomparator(EMVComparators advanceEmvcomparator) {
        this.advanceEmvcomparator = advanceEmvcomparator;
    }

    public EmvRootCauseReport getEmvRootCauseReport() {
        return emvRootCauseReport;
    }

    public void setEmvRootCauseReport(EmvRootCauseReport emvRootCauseReport) {
        this.emvRootCauseReport = emvRootCauseReport;
    }

    public TransactionContext getTransactionContext() {
        return transactionContext;
    }

    public void setTransactionContext(TransactionContext transactionContext) {
        this.transactionContext = transactionContext;
    }

    public EmvParsers getEmvParser() {
        return emvParsers;
    }

    public void setEmvParser(EmvParsers emvParser) {
        this.emvParsers = emvParser;
    }

    public TagValidationService getTagValidationService() {
        return tagValidationService;
    }

    public void setTagValidationService(TagValidationService tagValidationService) {
        this.tagValidationService = tagValidationService;
    }
}