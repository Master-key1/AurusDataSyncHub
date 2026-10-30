package com.auruspay.chase;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.auruspay.chase.service.ChaseValidationService;
import com.auruspay.chase.validation.Iso8583FieldValidationResult;

@RestController
public class Iso8583Controller {

	private final Iso8583MessageParser parser;

	private final ChaseValidationService chaseValidationService;

	public Iso8583Controller(Iso8583MessageParser parser, ChaseValidationService chaseValidationService) {

		this.parser = parser;
		this.chaseValidationService = chaseValidationService;
	}

	@PostMapping("/chase/parse")
	public ResponseEntity<?> parse(@RequestBody ChaseParseRequest request) {

		try {

			// Parse approved encrypted request
			Iso8583ParseResult approvedRequestParse = parser.parseMessage(request.getApprovedRequest());

			// Parse declined encrypted request
			Iso8583ParseResult declinedRequestParse = parser.parseMessage(request.getDeclinedRequest());

			// Validate formats and compare both requests
			List<Iso8583FieldValidationResult> result = chaseValidationService.validation(approvedRequestParse,
					declinedRequestParse);

			return ResponseEntity.ok(result);

		} catch (Iso8583ParseException e) {

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}
}