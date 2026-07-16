package com.auruspay.service;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Service
public class TransactionJsonService {

	private static final Logger log = LoggerFactory.getLogger(TransactionJsonService.class);

	private final ObjectMapper mapper;

	@Value("${app.data.file-path:data/data.json}")
	private String filePath;

	public TransactionJsonService() {

		mapper = new ObjectMapper();

		mapper.enable(SerializationFeature.INDENT_OUTPUT);
	}

	/**
	 * CREATE
	 */
	public String create(String key, Map<String, Object> transactionData) throws IOException {

		File file = new File(filePath);

		log.info("Data file location : {}", file.getAbsolutePath());

		Map<String, Object> json = loadExisting(file);

		if (json.containsKey(key)) {

			log.warn("Duplicate transaction : {}", key);

			return "Already exists : " + key;
		}

		json.put(key, transactionData);

		save(file, json);

		log.info("Transaction created : {}", key);

		return "Created : " + key;
	}

	/**
	 * READ BY KEY
	 */
	public Object getByKey(String key) throws IOException {

		File file = new File(filePath);

		Map<String, Object> json = loadExisting(file);

		Object data = json.get(key);

		if (data == null) {

			log.warn("Transaction not found : {}", key);
		}

		return data;
	}

	/**
	 * READ ALL
	 */
	public Map<String, Object> getAll() throws IOException {

		File file = new File(filePath);

		return loadExisting(file);
	}

	/**
	 * UPDATE COMPLETE TRANSACTION
	 */
	public String update(String key, Map<String, Object> updatedData) throws IOException {

		File file = new File(filePath);

		Map<String, Object> json = loadExisting(file);

		if (!json.containsKey(key)) {

			return "Transaction not found : " + key;
		}

		json.put(key, updatedData);

		save(file, json);

		log.info("Transaction updated : {}", key);

		return "Updated : " + key;
	}

	/**
	 * UPDATE SINGLE FIELD
	 *
	 * Example: processor_response update
	 */
	public String updateField(String key, String fieldName, Object fieldValue) throws IOException {

		File file = new File(filePath);

		Map<String, Object> json = loadExisting(file);

		Map<String, Object> transaction = (Map<String, Object>) json.get(key);

		if (transaction == null) {

			return "Transaction not found : " + key;
		}

		transaction.put(fieldName, fieldValue);

		json.put(key, transaction);

		save(file, json);

		log.info("Field updated. Key={}, Field={}", key, fieldName);

		return "Field updated";
	}

	/**
	 * UPDATE NESTED FIELD
	 *
	 * Example:
	 *
	 * cct_response -> 72.6 = APPROVAL
	 *
	 */
	public String updateNestedField(String key, String parentField, String childKey, Object value) throws IOException {

		File file = new File(filePath);

		Map<String, Object> json = loadExisting(file);

		Map<String, Object> transaction = (Map<String, Object>) json.get(key);

		if (transaction == null) {

			return "Transaction not found : " + key;
		}

		Map<String, Object> nested = (Map<String, Object>) transaction.get(parentField);

		if (nested == null) {

			nested = new LinkedHashMap<>();

			transaction.put(parentField, nested);
		}

		nested.put(childKey, value);

		json.put(key, transaction);

		save(file, json);

		log.info("Nested field updated. Key={}, Parent={}, Child={}", key, parentField, childKey);

		return "Nested field updated";
	}

	/**
	 * DELETE
	 */
	public String delete(String key) throws IOException {

		File file = new File(filePath);

		Map<String, Object> json = loadExisting(file);

		if (json.remove(key) == null) {

			return "Transaction not found : " + key;
		}

		save(file, json);

		log.info("Transaction deleted : {}", key);

		return "Deleted : " + key;
	}

	/**
	 * LOAD JSON FILE
	 */
	private Map<String, Object> loadExisting(File file) throws IOException {

		if (!file.exists()) {

			log.warn("JSON file does not exist. Creating new file");

			return new LinkedHashMap<>();
		}

		return mapper.readValue(file, new TypeReference<LinkedHashMap<String, Object>>() {
		});
	}

	/**
	 * SAVE JSON FILE
	 */
	private void save(File file, Map<String, Object> data) throws IOException {

		if (file.getParentFile() != null) {

			file.getParentFile().mkdirs();
		}

		mapper.writeValue(file, data);
	}

}