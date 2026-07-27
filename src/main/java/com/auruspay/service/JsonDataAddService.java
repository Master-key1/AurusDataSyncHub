package com.auruspay.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.auruspay.decryptor.AurusDecryptor;
import com.auruspay.dto.ProcessRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class JsonDataAddService {

	private static final Logger log = LoggerFactory.getLogger(JsonDataAddService.class);

	@Autowired
	private AurusDecryptor aurusDecryptor;

	private final ObjectMapper mapper = new ObjectMapper();

	@Value("${app.data.file-path:data/data.json}")
	private String filePath;

	private static final Pattern CTRL_CHARS = Pattern.compile("[\\x00-\\x1F&&[^\\n\\r\\t]]");


	// ================= SAVE DATA =================
	public synchronized String saveData(ProcessRequest request) throws Exception {

		log.info("========== Save transaction started ==========");

		log.info("Request received. cctRequest present={}, processorRequest present={}, processorResponse present={}, cctResponse present={},\nprocessorId present={}",
				request.getCctRequest() != null,
				request.getProcessorRequest() != null,
				request.getProcessorResponse() != null,
				request.getCctResponse() != null,
				request.getProcessorId() != null);


		String cctRequest = decrypt(request.getCctRequest());
		String processorRequest = decrypt(request.getProcessorRequest());
		String processorResponse = decrypt(request.getProcessorResponse());
		String cctResponse = decrypt(request.getCctResponse());
		String processorId =request.getProcessorId().replace("]","").replace("[","");
		log.info("Decryption completed. cctRequest ={},\n processorRequest ={},\n processorResponse ={},\n cctResponse ={}",
		        cctRequest,
		        processorRequest,
		        processorResponse,
		        cctResponse);


		String txnId = generateTxnId(cctRequest ,processorId);

		log.info("Generated Transaction ID: {}", txnId);


		File file = new File(filePath);

		log.info("Data file location: {}", file.getAbsolutePath());


		Map<String, Object> finalJson = loadExisting(file);


		log.debug("Existing transaction count: {}", finalJson.size());


		if (finalJson.containsKey(txnId)) {
			log.warn("Duplicate transaction found: {}", txnId);
			return "Already exists: " + txnId;
		}


		Map<String, Object> txnData = new LinkedHashMap<>();

		txnData.put("cct_request", safeParse(cctRequest));
		txnData.put("processor_request", processorRequest);
		txnData.put("processor_response", processorResponse);
		txnData.put("cct_response", safeParse(cctResponse));


		finalJson.put(txnId, txnData);


		try {
			mapper.writerWithDefaultPrettyPrinter()
					.writeValue(file, finalJson);

			log.info("Transaction written successfully. txnId={}, totalRecords={}",
					txnId,
					finalJson.size());

		} catch (Exception e) {
			log.error("Failed writing transaction data. txnId={}", txnId, e);
			throw e;
		}


		log.info("========== Save transaction completed ==========");

		return txnId;
	}



	// ================= DECRYPT =================
	private String decrypt(String value) {

		try {

			if (value == null) {
				log.warn("Decrypt called with null value");
				return null;
			}


			log.debug("Decrypt started. Input length={}", value.length());


			String result = aurusDecryptor.decryptor(value.trim());


			log.info("Decrypt successful. Output ={}",(result));


			return result;


		} catch (Exception e) {

			log.error("Decryption failed. Input length={}",
					value == null ? 0 : value.length(),
					e);

			return null;
		}
	}




	// ================= SAFE JSON PARSER =================
	private JsonNode safeParse(String value) {

		try {

			if (value == null || value.isBlank()) {
				log.warn("JSON parsing skipped. Empty payload");
				return null;
			}


			log.debug("JSON parsing started. Payload length={}",
					value.length());


			String cleaned = clean(value);


			log.debug("JSON cleaned. Original length={}, cleaned length={}",
					value.length(),
					cleaned.length());


			JsonNode node = mapper.readTree(cleaned);


			log.debug("JSON parsing successful. Node type={}",
					node == null ? "null" : node.getNodeType());


			return node;


		} catch (Exception e) {

			log.error("Invalid JSON payload. Payload preview={}",
					truncate(value),
					e);

			return null;
		}
	}





	// ================= CLEAN INPUT =================
	private String clean(String value) {

		if (value == null) {
			return null;
		}


		int originalLength = value.length();


		String cleaned = value
				.replaceAll("\\\\r\\\\n", "")
				.replace("\r", "")
				.replace("\n", "");


		cleaned = CTRL_CHARS.matcher(cleaned).replaceAll("");


		log.trace("Clean completed. Before={}, After={}",
				originalLength,
				cleaned.length());


		return cleaned.trim();
	}





	// ================= LOAD EXISTING FILE =================
	@SuppressWarnings("unchecked")
	private Map<String, Object> loadExisting(File file) {


		try {


			if (!file.exists()) {

				log.warn("Data file does not exist. Creating new file: {}",
						file.getAbsolutePath());


				if (file.getParentFile() != null) {
					file.getParentFile().mkdirs();
				}


				file.createNewFile();


				Map<String,Object> empty = new LinkedHashMap<>();

				mapper.writeValue(file, empty);


				return empty;
			}



			log.debug("Loading existing JSON file. Size={} bytes",
					file.length());



			if (file.length() > 0) {

				Map<String,Object> data =
						mapper.readValue(file, LinkedHashMap.class);


				log.debug("Loaded {} transactions",
						data.size());


				return data;
			}



		} catch (Exception e) {

			log.error("Error loading existing JSON file: {}",
					file.getAbsolutePath(),
					e);

		}


		return new LinkedHashMap<>();
	}





	// ================= TXN ID =================
	private String generateTxnId(String cctRequest , String processorId) {


		try {


			if(cctRequest == null ){
				log.error("Cannot generate txnId. cctRequest is null");
				return "FD_UNKNOWN_TXN";
			}
			if(processorId == null ){
				log.error("Cannot generate txnId. processorId is null");
				return "FD_UNKNOWN_TXN";
			}

			log.debug("CCT :  {} ",cctRequest);

			String cleaned = clean(cctRequest);


			Map<String,Object> map =
					mapper.readValue(cleaned, LinkedHashMap.class);



			String txnId = String.join("_",
					"FD_"+processorId,
					get(map,"3.1"),
					get(map,"3.5"),
					get(map,"3.21"),
					get(map,"4.1"),
					get(map,"4.3"),
					get(map,"4.20"),
					get(map,"4.21"),
					get(map,"4.30"),
					get(map,"4.40")
			);



			log.debug("TxnId generated successfully: {}", txnId);


			return txnId;



		}catch(Exception e){

			log.error("TxnId generation failed. cctRequest preview={}",
					truncate(cctRequest),
					e);

			return "FD_UNKNOWN_TXN";
		}
	}




	private String get(Map<String,Object> map,String key){

		Object val = map.get(key);

		if(val == null  || val.toString().isBlank() || val.toString().isEmpty()){
			log.warn("Missing txnId field: {}", key);
			return "NA";
		}

		return String.valueOf(val);
	}




	private int length(String value){
		return value == null ? 0 : value.length();
	}




	private String truncate(String value){

		if(value == null)
			return "null";

		return value.length() > 200
				? value.substring(0,200)+"..."
				: value;
	}
}