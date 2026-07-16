package com.auruspay.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.auruspay.comparator.service.TagValidationService;
import com.auruspay.comparator.util.EMVComparator;
import com.auruspay.comparator.util.EMVParser;
import com.auruspay.dto.TransactionContext;

@Component
public class ServiceProvider {

	@Autowired
	private TransactionContext transactionContext;
	@Autowired
	private EMVParser emvParser;
	@Autowired
	private TagValidationService tagValidationService;
	@Autowired
	private EMVComparator emvComparator;

	public ServiceProvider(TransactionContext transactionContext, EMVParser emvParser,
			TagValidationService tagValidationService) {
		super();
		this.transactionContext = transactionContext;
		this.emvParser = emvParser;
		this.tagValidationService = tagValidationService;
	}
	
	

	public EMVComparator getEmvComparator() {
		return emvComparator;
	}



	public void setEmvComparator(EMVComparator emvComparator) {
		this.emvComparator = emvComparator;
	}



	public TransactionContext getTransactionContext() {
		return transactionContext;
	}

	public void setTransactionContext(TransactionContext transactionContext) {
		this.transactionContext = transactionContext;
	}

	public EMVParser getEmvParser() {
		return emvParser;
	}

	public void setEmvParser(EMVParser emvParser) {
		this.emvParser = emvParser;
	}

	public TagValidationService getTagValidationService() {
		return tagValidationService;
	}

	public void setTagValidationService(TagValidationService tagValidationService) {
		this.tagValidationService = tagValidationService;
	}

}
