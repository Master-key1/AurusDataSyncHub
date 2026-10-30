package com.auruspay.controller;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.auruspay.chase.Iso8583MessageParser;
import com.auruspay.chase.Iso8583ParseResult;
import com.auruspay.chase.service.ChaseValidationService;
import com.auruspay.chase.validation.Iso8583FieldValidationResult;
import com.auruspay.chase.validation.Iso8583FieldValidator;
import com.auruspay.decryptor.AurusDecryptor;
import com.auruspay.dto.CustomResponse;
import com.auruspay.dto.ExceptionResponse;
import com.auruspay.dto.ProcessRequest;
import com.auruspay.dto.TransactionLookupResponse;
import com.auruspay.dto.UserInput;
import com.auruspay.fiserv.comparator.JsonComparator;
import com.auruspay.fiserv.comparator.JsonFieldValidator;
import com.auruspay.fiserv.comparator.XmlComparators;
import com.auruspay.fiserv.comparator.model.ComparisionXmlResult;
import com.auruspay.fiserv.comparator.model.ComparisonJsonResult;
import com.auruspay.fiserv.comparator.model.ComparisonResult;
import com.auruspay.fiserv.comparator.model.ValidateResult;
import com.auruspay.fiserv.comparator.model.ValidationIssue;
import com.auruspay.fiserv.logservice.exception.BlankRequestException;
import com.auruspay.fiserv.logservice.exception.NoDataFoundException;
import com.auruspay.service.JsonDataAddService;
import com.auruspay.service.TransactionLookupService;
import com.auruspay.util.ExtractMultipleKeywords;
import com.auruspay.util.FileReadData;
import com.auruspay.vantiv.dto.ComparisonReport;
import com.auruspay.vantiv.service.MessageComparisonService;

import io.swagger.v3.oas.annotations.tags.Tag;
/**
 * REST endpoints for saving, decrypting, and comparing Aurus transactions.
 */
@Tag(name = "Transaction APIs", description = "Process, Lookup and Compare transactions")
@RestController
public class ProcessController {

	private static final Logger log = LoggerFactory.getLogger(ProcessController.class);

	// Keys used by the flat-file extractor to tag interesting lines. Centralised
	// here instead of being repeated as inline string literals across methods.
	private static final String KEY_CCT_REQUEST = "[STPL-GRAY-STREAM]-AURUSPAY ENCRYPTED REQUEST :";
	private static final String KEY_CCT_RESPONSE = "[STPL-GRAY-STREAM]-AURUSPAY ENCRYPTED RESPONSE :";
	private static final String KEY_PROCESSOR_REQUEST_1 = "[STPL-GRAY-STREAM]- PROCESSOR REQUEST :";
	private static final String KEY_PROCESSOR_REQUEST_2 = "[STPL-GRAY-STREAM]- REQUEST :";
	private static final String KEY_PROCESSOR_RESPONSE_1 = "[STPL-GRAY-STREAM]-FINAL RESPONSE :";
	private static final String KEY_PROCESSOR_RESPONSE_2 = "PROCESSOR RESPONSE FOR FIRST DATA PERSISTENT :";
	private static final String KEY_PROCESSOR_ID = "PROCESSOR TERMINAL DETAILS";
	private final JsonDataAddService jsonDataAddService;
	private final TransactionLookupService transactionLookupService;
	private final AurusDecryptor aurusDecryptor;
	private final XmlComparators xmlComparator;
	private final JsonComparator jsonComparator;
	private final FileReadData fileReadData;
	private final ExtractMultipleKeywords extractMultipleKeywords;
	private final JsonFieldValidator fieldValidator;
	private final MessageComparisonService comparisonService;
	private final ChaseValidationService chaseValidationService;
    private  final  Iso8583MessageParser parser;
    private final Iso8583FieldValidator validator;
	private final Set<String> fiservProcessorId = Set.of("30", "33", "45", "48");
	private final Set<String> vantivProcessorId = Set.of("32", "217", "653", "753", "802", "903", "410");
	private final Set<String> chaseProcessorId = Set.of("47", "61");

	@Value("${aurus.file.import-directory:C:/data/combine}")
	private String fileImportDirectory;

	public ProcessController(JsonDataAddService jsonDataAddService, TransactionLookupService transactionLookupService,
			AurusDecryptor aurusDecryptor, XmlComparators xmlComparator, JsonComparator jsonComparator,
			FileReadData fileReadData, ExtractMultipleKeywords extractMultipleKeywords,  MessageComparisonService comparisonService,
			JsonFieldValidator fieldValidator, ChaseValidationService chaseValidationService,
			Iso8583MessageParser parser,
			Iso8583FieldValidator validator) {
		this.jsonDataAddService = jsonDataAddService;
		this.transactionLookupService = transactionLookupService;
		this.aurusDecryptor = aurusDecryptor;
		this.xmlComparator = xmlComparator;
		this.jsonComparator = jsonComparator;
		this.fileReadData = fileReadData;
		this.extractMultipleKeywords = extractMultipleKeywords;
		this.fieldValidator = fieldValidator;
		this.comparisonService = comparisonService;
		this.chaseValidationService = chaseValidationService;
		this.parser =parser;
		this.validator = validator;
	}


  @GetMapping("/testxml")
  public ComparisionXmlResult testing () {
	 String approvedXml= null;
	String declinedXml = null;
	return xmlComparator.getXmlComparator(approvedXml, declinedXml);
  }
  public static String hexToString(String hex) {
	    if (hex == null || hex.isEmpty()) {
	        return "";
	    }

	    StringBuilder result = new StringBuilder();

	    for (int i = 0; i < hex.length(); i += 2) {
	        String hexPair = hex.substring(i, i + 2);
	        result.append((char) Integer.parseInt(hexPair, 16));
	    }

	    return result.toString();
	}
	@PostMapping("/process")
	public ResponseEntity<String> saveTransaction(ProcessRequest request) {
		/*
		/*String cct_req = "aWqr8X+VRcPujhKtSCcW+21KipeBqrta9S71UKTHM0BUy7MBch5Xo2ANxYdvv3upeXAum0NC6sIXz+uKgUgmqb9+FA5czhmU304HMNyfETyrIfRQYOkr3LwrlTm0mxBpqva6xOoKp5Cgz8IshLTzaaPFdFccsJM1LQXhnNcri+zjAbSsfNadrIcn1i34Q9mqZ4mcPEusyKTOTPoI/rdmReke1NEWXY1zC+L1pWxJHuFX2X7FY7E6okjNfoxLsjIwA5+JGC6xSZURqxXgbOT2yFWHgL1iazQ9QZ3fm6RRDu52KBlgD58Huuj6AM4wDYFExRJxenPXWA2AfOmeUHhP5noinnnjVVzKJXFMDOutGMAU2izEchETom0dAuYWRLMCTk+JtHyFL9pGx2JweBtLCAp8yARgRFtxu81aLEm1gG939PcdhkWGOSrOP+YED1sNix2Sft38QV2V7h0yjVGGo3S58j3gcdvsgjUz5Df76v8mcA+gMBwQ4Mpw44LojGC+280+2UMeRjgwNag2ZZCwqr8yBNpf3Iia9F+ymIgPlBdOo+qGexYXRLVsaV9+rXdNHdJfIQClh8FRhPO/6pukjgIOkQIhPdxmZQqYDllQNyjLk8kZlvc/4W4SnFi75xn0myf1nE0NSE8Xp/pF5C17BiJY6W2c29/XQRY7PEi0/rDVp0c6Jtji87plPyvD4rdGReSwvLZkUA82zX6Nw+BmwdoKk71whLbJpNY8ImWBXb8nLLbbQvWHTRdJ7mWMxqw4TrnG+2zzAlKnkTBO3sWP8B0ecWc50V3A09NLZxhtiAyWlgKSC2z8GaataGEpCokQJEgUsRNgodjTUzN52Ik15ZfSbYU/lGBsT/ptobtbCW5jMbQxUlk9DykXq7wU53Ak+mEx8TbVbC/O29WFAwY/mk0ex0UpLaXNEYBVm+qISYwnY1FNJgmE001NQx47cdHLFEyTxz3SubdC5cFQRJ5P5LgxJ+QFaGwH9T7SSf0vtKE1wmpBQ7Zro9exex8LR0KmFneuZ/Cn+zKA9zG31MmMl4Fz9Df6WZTLq1bKpKgpn6xZ2RYvtxfAx6IkYfYcq33A52NHt3EPLHmZOKPo8N/NrhqjDQNQx+FpoCMbr0zZObWh+aTF/g4S9TiVCVbN/CcH4Dq+S2HseQjmpNQJH/ns8y7ojoO3RFhzvHQEkI6V6joDJ1/hCrMS6Sq3Ep5Jcz4Z0UWsmfT7ev2tvOSFZs1QPTIDNccepL8qF10e4pmfT0Unqfdew7hmfA8mG2+BGUjQhoiLKsHXH/ET9GdbE7ln5fwiOM0X+AkstCTntjB8dg8IC3ZqKDSiSXZ8+j+MtS9p3u0Y3WS01XVz8r4SwGLzwzkt6RwCshyyxPJQl9wFTwdAaKOUqzKobjLzqvAS5Qjwys87g+Cfl7EraIw6RU4G2s4xT0UeeIB9xMzEJcPI5JV8j1f52UffcIsCE1ZQ3zRrhy6RRxCl7m6foQchoSAqWMNBVP2K0hY7yX8YkgfEYJ8cUpjVjSlNm88UeG/mulgFVswWX3cZMS15yEm8yxQ1wHMpLqDd1QHmL7DzvWFzAXiVeY/4L48u2OtnGlO0ehh7i4YIMso4zY7lFGuUzJXkgLY1LG30LE9FgVcHaV5KkTHNAT2wWal6V6MFQ0KsPmJXxMPCmvARR7eZsPqiY5Y68C4zh9yzk5FhSNFv7pE70CIdJ2sjHuPQgyFxsFkWq7h1LredacLJ7plYgLYLTdCTGLQKyYlgT4M0omm7tuuFDTPqyxXivqH0kUjMjjwL0OAuJKE1HUFVObuSsNvfizhhQRI9rMuO+LbvWwF9dhVZZvEM/kESV6MBLyHmhOUJ4MXGhL54y3ORnUk4pDLYhO6m2A==";

		String cct_resp = "xtJ6x+ji8QSP/grVSiUWWkXDuZNfrv1h0esisSlUd9PXST1osQbf3gPtytwTGXmAYsCVFoGUGjIZByxDWKD/BrdDt0ywWvu3TuSsBuYO+MFGMvxRNrmv0CHpQprKkr0bi9zo3UI9ycKOqpTlhyEK7vSog3bjBFHglyglujW/FJjZfaylZI/PhTgC42C0NyoEgPi4LTlWpGtQwoPUpx5udiBf0Ot5ymMNqvuKYfqz5tfoDd11glpnhyMtGYa95DDuXUuR38tqH9BdjeUjnPcr1sWLmgVorOT2TwweBsvMXfVKX3sQZGw0xJQT5KUwOatsoar5URUCe/WkvdJ9kuai238R3ywwg6qRRfm05ZUuicM+ytDwcuQciissFL/QKJkCRx7oh0IGQck1IezCF2D4FGnyYCwEWVIjtNKM+uZz3+07aow8ynyXoHdjcYeB1oqBebQnI1ara192qVJRaIfsH9PyBM7ZwNPGaa603ZWSsX8XmB3sJ2EuuAfGtl7syhYK4Lifp1DgjCILMmekFP5te1HdGoUo/aDZP2xPZiHvM8aSk+qlrMcwzjBCj7PReSk8vfmXTKdev9FcluRYt5pNQ6QBf7/iQfuML9V/CnnADcjgLoBxMXCqE7Ibvosv5GUCDdrSdGmaB9ixyi+lnkGqY7z1svTLYBbuxKkcbGjjYyIdvgvTRNoFrZS/RCeKuLtstPOQ8nae7c4Un7Y7WNFgFQvIpeOnjDMrbSNx5+EoJE8oGrlj2eyPE5U6hzgwGGgOqb/wkqofN7nKR4DOki5UQdZzo+YLNcvffvNKLiRLXEsqc7nCLbDeCt0xHzhCE//E5ve5dcofvELpoftOg1wliiU6hLTZRpPzaSBhU5llI4NdPdf9Fv3Elq3U2HTzWS4lldwQm8IgbjABZdZ8TU8ivbGgiare06HRyrwx3YXFVCDYp0dAvs/x+RAFob7zDrYWy4zpKRRjYTTQQNt/08y+XwMpVOurGOvZ0pVg6VP9jtkBukB6D2FZUm7g2ZEvA+8WEwR5Jy2KPwHBTHoB1VIGClonoppY6lAyR22KMUVabrn/cXDQ40RbvGlPtyV4dyXg8/qH7AsJ524RTRPWZbcAEevBJBk80HZPQM9HDK9cFwUROGVHDx666ph3uoVos88azGoAMAhGr9NyZ8VO7Vn7NkG/sCq6uoIFHva8bLETL/75aVMmYBj6qorpqG1iUXtgdcnJlk249lgUut3KdDlG5oOoaCFRtY9WxGbEGpnXR61MQUhCTBoyPZZ+lZO80nMLsSfyg8Fndo+o3Q2WnPVs4n3vfHFPfXrkpma/JSx5eBXrMWtSTLswt2NzIe5ZZ5g9/UulO5RC+1ShPoe01YbcZiq06l5zm3QFHhL7pknghkpWIj7NKvU+1uQHe/H53gI1LWy8Nwybx8qDStzNFfGM8Ll6ZOS/VmR2FalmUrlwgbZlHJ2QNoUi6JWk8gy+ZAqUKrt7MBCK6KfEsGqLReDUzw==";

		String proReq = "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";
		String proRes = "qqUAGXXH2H5HfUw4Us5B+8fpY7KWwk/Phd1WVRl+AZS6Wbz/ETFzJgknTzNDhFZgoAQxTbAyJmmx+WSD8fnZGdm4W93Icu5+mcNlhzU+T7RQj8QgXXMAreFkvVVf7ndwlInnjlvKBrbTMNWL+OfO90fczjfV/EcJ9cKrOA+9E+oort9Vxnev1fRI9/m16Iu5KylgigCiRkay6o1ntJmjFn/VxXQKkVjaeHBVk0ZpooZCzi74AGt5H04Aw/iv8dBVNWAAoqCCp7eSdXJCkLWtvm1S1bcWa2t5034S1oXuNq9+2Sy72Rgor7xsUa3Z0GFuRdmqx5SPWf5qwy7fQg8jB6tHBTfLwjhQajKUe9jHXnCIENyuGKSda2iAEQjjtVK0ff2JvIKXHOcr/GVF4CiMdNRGRw160NgI4DYtmEvxuRafh656LQAYE/AUF78ujZRRVGAYmhnUxvjvs8mVpZ/vuN+sI18mNvstvhmPK0kDTlVEZmFnBchYSUBWaplR0hZP88WyhNbRzkHRImM3MA0xTpt5/m3ra7UUJ8FTbRBkDeO+m1kcscUECQH/vZAFD9itkw3NJdG7P4zGJs6sLAyRDabJafTcvKEvmdPslg0PEFfrjkMmzzseVQQ/tnKxDoMRPq0TxudRPi6YTqW4Hyz6X1zA9EvD7LjosINrPg4RuZLWvPjOiwjVZeVG/m9/ogwx11JVn+96tSAiKmHz92LtNvMAq21RNsRjXwXSsGo3p22SQdO0NhX9rN5OzoDirjs+ks1UWHxlGfJQbMHwqGQrGluAXzWUg/grSYFiDR4ZmMpy8o2a7ZJoRmVXEQcGkhf663Uk9Ubr81Vms/fzFyyZyzxyd5BhVbB+qvlf4Q+bDpfpXTCsXgn/K3xjwuhxvddBPv0nQ+le7pFWcF2dxF0HpiysvBgyX6dGu1Prj42itE9gdZ/yM+KyaWPAntE6HbJoxQxxts34jPPVpkVEe92Z9/jNWXWQuCDj9mKQy8WaW0pjEz0CahjRKVkgxr4jaQbNJaSmy8Mh9Q4vwr2+7nt2Gd87SHu08Babs8WNoBgk4uaYcwlbeD13zQEeofe4qCq3G5bngYLhl4nV7zWnav0N5yBS/B43jMjzz/g0BAfPiy5ZboVUAnmCHLDiNzTy3Eyl+RM7FQPNosCH1P3uCY0xc86jBIQnDe9mL2byYa0qfZaN/bDA+ZatMpm/X4GCW2NA4gC3vL28ImWfX8T38vMfHTDuYQQs7B/nkUlSWEVc68jvARzBPYfaa1GvXAWG3Fedn9EqC+tjLSX9GJzV3E1MXFVYWLSd+f13TrNLsdAxpIivJv6p0KsDAF5vZ2MUONeIlyR01ZanTI3znaYB1FPH0B3qwZ0X6/Zd9nJOkmkbTsU4GUsPeGzAVbMcvvfW/Os8qSVGn5P+MyMwECkUtHr3C/giaPb3hBbdsNtadxdEGZfBKaurR8oV8dbqnaWtXpTYD0x2xn/4u5Y561oR+M6rluVP/0e/3hQABqKdvh/OaZqMrXn1e1HMjrNPDiJMbYRMXq9nP3s8uIy4pDAHAQr2DdyAMoXTqjmjUW02NU5SLbq214gDcph3we9+Tnbui/4Dc0kL7eL8cPTNH65lE0CJCdCS4aezDTcOfIdxEMqeSHmDOr4W7g0lW7VR5wrgBOufnU8HopP4yJ//p5JxL2RB+mHog886EILxZp2IQmQRcOUlg2ybN/3wTewI1/2cCxqfsRUgW8SA8LwzX7udgnE9UonqMwQXChkh45otyFBZ5w4CnPd5Rlb88gSTxSXlDEOWbmjHNUobryjTunqUvkyT49LOnUXtNAuXf7pF4PArhv3XERnFlG5iOmSb2EI1VWMl";
		
		String cct_req = "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";
		String cct_resp = "4VEm8KHLrQDcavNnqK9y6XFqGK0xs4+xf3JSRRrmswseIaFHtJ6q41pq2JpBMxskD7Ebt+0wvy+iBXht64rCE6y6fwxVFrrIosX4f0kDOh4BQ7qoIUSBTLK2dZuyiOo7XwLTmEvMlePZckdqyHU42R6iWN9iwAmOp7VX7jVE9O3ZUUag+Q4wpyAslyIYXKQ/lC+qEc1ELUKcBAWXQY16ZThTjklcTpzzsBFh8tJEpylgNvz+QCTmhbhE/r6yPpS8yRuvgnYv6Q35v9/Lxyz2kufZAJ8x3TNgu/oU4BkAvC/y7v9EAGZxYK3tRSIVru4u3FNZxc6jPOJQbZIdrJo/HVNdjuXmr0lAc9PBRN36qdNVFCDSNyCB7Dk++wkg7iKp9F/oJuq1AUkEJxJ7qLxvIRvbCAt6U1VmIU0Yrg8p+ZDDLiUqy52kcnvLsqi5LguVsS1uM4bERDFXPno/oGtRiY4hERYaTVu7ow4bpK82jF0T8j59YxztbD35+Ggnb15QApBCPZfLxjFHNMxNp/lsldBHCxhMcEiE9wfzT1LxIJY4LD5PFelFEs6ZMEOkmbV/ys5dGfpFzd3cezWgWN36ZIOftJrKp94cdA8qvgvfzzxJW2ZIb8rdcx+nSMTDNx8TA/i+CTRosKSSC5vLJflKu/EhwEiKpwXbdywtYt/S+epDqYN1G9cXX2+B83ey8uCB/LuzmpI7VttC6mo9F/loMR0ynJQxfLLQ38P3i8wf0J/EVRc3zGT14VVS5oMRCHgbR/7WmVN0i5pm0gIaQFjTth+FISqk5kaznuc2HAup8E8GIs1eeBF7bSgfOPfxot8v0WVwN1CjVKyxi1do96Py6f5bX9funZRDCaNTSg2jeT/C2opJZRfI2JW6/U7BIQJHCi2E75bsKtuqxAMSrOVdZEu0jVn94jGIK5qnTqLAu/4gESdKTB/uinf1hK96X2UQAhRpyw5Ci3/HmMfXEgjG5HozeSXlOTDN33+Tua36X5IwaLD/RT2F3DGyVI1q2WyeSJGfcJskmKWyqShsLQaydvr2j/uvTRVCs1xz9b+I0NJOivnTwOY7RyMl2pYSywdukokUG1h0LHUF9OkADx62kmp03scasI2o/He1AarJrFCXcHQw+yqMG5q9KQ8b7yyW0v2CmQEDycrrMxSYs5maoiJrPyQmv/E9UeTfuwUBwDEZoOudek41FrPpFBZGHD0ciw+RDURfi0ty2I4YpRKf9qTgn0eMdY/5WK9/3Kj9jk3Wm9r6k1s0KqXx6F+h4gGFntY06jKlvgtcOyJQ1ARkOFh0v5gzVcX0EzzfEXgdGL5EUUsSDzFCwjyA/v+EG0WHVYxS0fhsC6xwl7zgfia/m8avw4/FryUt9Iz+N3bn58yT/0Sc5d9v8Vo+SO7Kr+RcmR3Wt3S+AMGigENVWnIEsY4EsBRLgi7bmJmuA3oyYTG9tmLYff9mW2K5SWl86FbGZqORXZLizzEHlJEScJfisyXDxDW13bUNbz3y02pwc47YebGH0jJ9IAyttRGciv/ULYFj2Ezcz3C1LAvapSF15wZgeGjOD4MmLMH3FLJhZxIyph9fJflNW1CUs0XTDO+gxf70nylIr4Ktx6oZKevh7gPAqy7N7oC7mvOmjBRKZvqSwwe4iK0NoaYAGYUgWH0NrmnkXplIatPQqW6Rn7wpc3RxstZcOPsV2r/5kKpQB7WeZtkCj+G2DwRHJRmLixxhLpTO4EHWKEMPmaDh07Fv+uUbZ8Ijh3zsfJuVtSDHohs=";
		String proReq = "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";
		String proRes = "IgxNsYoSEoGwM8sZIhnoLY2tMzNo+g8h95/wbbge+gP0Bo+efrMlHkuYUqUm2AdO/TjQzvDKFe8UwQSO11BPsCCxhwWyEU0zjNYe593vcvJDCfPK4zSPxyEQWuGO4Zsn988m7fHZi2onTt02ptk0VZnsp+65GJIvTswyvVkilRVL99J/z3vez4O6Rpd7WfiNnFUdL8nP8L1N/ABnQOQ2jbRkzBtB0C+BGK1DU/zNlHJVqi+7I3LOuqfibHmzLJT4l3SbbBKQVxjH4LIRsFE1Zqk6yTOS/ZK3XqJdx8q6q+XoRMm2GqPTZBBSA0Bt350cu6h9UFcrrVHmkQRbeknv4w==";
		*/
		
	//	ProcessRequest request = new ProcessRequest();
		
		/*
		request.setCctRequest(request.getCctRequest());
		request.setCctResponse(request.getCctResponse());
		request.setProcessorRequest(request.getProcessorRequest());
		request.setProcessorResponse(request.getProcessorResponse());
		//request.setProcessorId("30");
		request.setProcessorId(request.getProcessorId());

	//	String response = webClient.post().uri(url).bodyValue(request).retrieve().bodyToMono(String.class).block();

//log.info("Response received from target service: {}", response);
 
 */
		if (request == null) {
			log.warn("saveTransaction called with null request body");
			return ResponseEntity.badRequest().body("Request body is required");
		}

		try {
			if(chaseProcessorId.contains(request.getProcessorId())) {
				//String req = hexToString(request.getProcessorRequest());
				//String resp = hexToString(request.getProcessorResponse());
			/*
				if(request.getProcessorRequest() != null || !request.getProcessorRequest().isBlank() )
				request.setProcessorRequest(hexToString(request.getProcessorRequest()));
				
				if(request.getProcessorResponse() != null || !request.getProcessorResponse().isBlank() )
				request.setProcessorResponse(hexToString(request.getProcessorResponse()));
			*/	
			}
			String transactionId = jsonDataAddService.saveData(request);
			log.info("Transaction saved successfully. txnId={}", transactionId);
			return ResponseEntity.ok(transactionId);
		} catch (Exception e) {
			log.error("Error while saving transaction", e);
			return ResponseEntity.internalServerError().body("Failed to save transaction");
		}
	}
	

	@PostMapping("/decryptor")
	public ResponseEntity<String> decrypt(@RequestBody String encryptedData) {
		if (encryptedData == null || encryptedData.trim().isEmpty()) {
			log.warn("Empty encrypted data received");
			return ResponseEntity.badRequest().body("Input data is required");
		}

		try {
			String decryptedData = aurusDecryptor.decryptor(encryptedData);
			log.info("Decryption completed successfully");
			return ResponseEntity.ok(decryptedData);
		} catch (Exception e) {
			log.error("Decryption failed", e);
			return ResponseEntity.internalServerError().body("Decryption failed: " + e.getMessage());
		}
	}
	@GetMapping("/submit")
	public ResponseEntity<?> submitForms() {

	    log.info("Received transaction save request");

	    List<String> directoryPaths = List.of(
	            "/home/adatal@offaurusinc.com/Videos/Logs_Data/LOG_6_0_1/DOWNLOAD"
	    );

	    FileReadData extractor = new FileReadData();

	    Map<String, String> uploadedFiles = new HashMap<>();
	    Map<String, String> failedFiles = new HashMap<>();

	    for (String directoryPath : directoryPaths) {

	        log.info("Processing directory: {}", directoryPath);

	        List<String> filePaths = extractor.listFilesInDirectory(directoryPath, ".txt");

	        if (filePaths.isEmpty()) {
	            log.info("No .txt files found in {}", directoryPath);
	            continue;
	        }

	        Map<String, Map<String, List<String>>> allResults =
	                extractor.extractDataForFiles(filePaths);

	        if (allResults.isEmpty()) {
	            log.info("No matching data found in {}", directoryPath);
	            continue;
	        }

	        allResults.forEach((filePath, data) -> {

	            String fileName = new File(filePath).getName();

	            try {

	                if (data == null || data.isEmpty()) {
	                    failedFiles.put(filePath, "No matching data found in file");
	                    return;
	                }

	                ProcessRequest request = new ProcessRequest();

	                boolean cctRequestFound = false;
	                boolean cctResponseFound = false;
	                boolean processorRequestFound = false;
	                boolean processorResponseFound = false;
	                boolean processorIdFound = false;

	                for (Map.Entry<String, List<String>> entry : data.entrySet()) {

	                    String key = entry.getKey();

	                    String value = (entry.getValue() != null
	                            && !entry.getValue().isEmpty())
	                                    ? entry.getValue().get(0)
	                                    : null;

	                    if (value == null || value.isBlank()) {
	                        continue;
	                    }

	                    if (key.contains("[STPL-GRAY-STREAM]-AURUSPAY ENCRYPTED REQUEST :")) {

	                        request.setCctRequest(value);
	                        cctRequestFound = true;
	                    }

	                    if (key.contains("[STPL-GRAY-STREAM]-AURUSPAY ENCRYPTED RESPONSE :")) {

	                        request.setCctResponse(value);
	                        cctResponseFound = true;
	                    }

	                    if (key.contains("[STPL-GRAY-STREAM]- PROCESSOR REQUEST :")
	                            || key.contains("[STPL-GRAY-STREAM]- REQUEST :")
	                            || key.contains("[STPL-GRAY-STREAM]- PLCC PROCESSOR REQUEST ::")) {

	                        request.setProcessorRequest(value);
	                        processorRequestFound = true;
	                    }

	                    if (key.contains("[STPL-GRAY-STREAM]-FINAL RESPONSE :")
	                            || key.contains("PROCESSOR RESPONSE FOR FIRST DATA PERSISTENT :")) {

	                        request.setProcessorResponse(value);
	                        processorResponseFound = true;
	                    }

	                    if (key.contains("PROCESSOR TERMINAL DETAILS")) {

	                        request.setProcessorId(value);
	                        processorIdFound = true;
	                    }
	                }

	                // Validate required fields
	                List<String> missingFields = new ArrayList<>();

	                if (!cctRequestFound) {
	                    missingFields.add("CCT Request");
	                }

	                if (!cctResponseFound) {
	                    missingFields.add("CCT Response");
	                }

	                if (!processorRequestFound) {
	                    missingFields.add("Processor Request");
	                }

	                if (!processorResponseFound) {
	                    missingFields.add("Processor Response");
	                }

	                if (!processorIdFound) {
	                    missingFields.add("Processor ID");
	                }

	                if (!missingFields.isEmpty()) {

	                    String issue = "Missing fields: " + String.join(", ", missingFields);

	                    failedFiles.put(filePath, issue);

	                    log.error("File: {} | {}", fileName, issue);

	                    return;
	                }

	                // Set file name if required
	                // request.setFileName(fileName);

	                // TODO: Call your service/API to save the transaction
	                // processService.saveTransaction(request);

	                uploadedFiles.put(filePath, "Successfully processed");

	                log.info("File processed successfully: {}", fileName);

	            } catch (Exception e) {

	                failedFiles.put(filePath, e.getMessage());

	                log.error("Failed to process file: {}", fileName, e);
	            }
	        });
	    }

	    // Final processing summary
	    Map<String, Object> response = new HashMap<>();

	    response.put("totalProcessed", uploadedFiles.size() + failedFiles.size());
	    response.put("successCount", uploadedFiles.size());
	    response.put("failedCount", failedFiles.size());
	    response.put("uploadedFiles", uploadedFiles);
	    response.put("failedFiles", failedFiles);

	    log.info("Processing completed | Success: {} | Failed: {}",
	            uploadedFiles.size(), failedFiles.size());

	    return ResponseEntity.ok(response);
	}
	// ================= VALIDATION / COMPARISON =================

	
	@PostMapping(value = "/validationissue")
	 public ResponseEntity<?> validateTransaction(@RequestBody UserInput request) {
/*	public ResponseEntity<?> validateTransaction(){ 
		UserInput request = new UserInput();
		
		/*
		String cct_Req = "aWqr8X+VRcPujhKtSCcW+21KipeBqrta9S71UKTHM0BUy7MBch5Xo2ANxYdvv3upeXAum0NC6sIXz+uKgUgmqb9+FA5czhmU304HMNyfETyrIfRQYOkr3LwrlTm0mxBpqva6xOoKp5Cgz8IshLTzaaPFdFccsJM1LQXhnNcri+zjAbSsfNadrIcn1i34Q9mqZ4mcPEusyKTOTPoI/rdmReke1NEWXY1zC+L1pWxJHuFX2X7FY7E6okjNfoxLsjIwA5+JGC6xSZURqxXgbOT2yFWHgL1iazQ9QZ3fm6RRDu52KBlgD58Huuj6AM4wDYFExRJxenPXWA2AfOmeUHhP5noinnnjVVzKJXFMDOutGMAU2izEchETom0dAuYWRLMCTk+JtHyFL9pGx2JweBtLCACjTC8RJbN7EcmlMvQbAoN39PcdhkWGOSrOP+YED1sNix2Sft38QV2V7h0yjVGGo3S58j3gcdvsgjUz5Df76v8mcA+gMBwQ4Mpw44LojGC+280+2UMeRjgwNag2ZZCwqr8yBNpf3Iia9F+ymIgPlBdOo+qGexYXRLVsaV9+rXdNHdJfIQClh8FRhPO/6pukjgIOkQIhPdxmZQqYDllQNyjLk8kZlvc/4W4SnFi75xn0Ml9AulLv/Us8oJKJL6G1bCJY6W2c29/XQRY7PEi0/rDVp0c6Jtji87plPyvD4rdGReSwvLZkUA82zX6Nw+BmwdoKk71whLbJpNY8ImWBXb8nLLbbQvWHTRdJ7mWMxqw4TrnG+2zzAlKnkTBO3sWP8B0ecWc50V3A09NLZxhtiAyWlgKSC2z8GaataGEpCokQJEgUsRNgodjTUzN52Ik15ZfSbYU/lGBsT/ptobtbCW5jMbQxUlk9DykXq7wU53Ak+mEx8TbVbC/O29WFAwY/mk0ex0UpLaXNEYBVm+qISYwnY1FNJgmE001NQx47cdHLFEyTxz3SubdC5cFQRJ5P5LgxJ+QFaGwH9T7SSf0vtKE1wmpBQ7Zro9exex8LR0KmFneuZ/Cn+zKA9zG31MmMl4Fz9Df6WZTLq1bKpKgpn6xZ2RYvtxfAx6IkYfYcq33A52NHt3EPLHmZOKPo8N/NrhqjDQNQx+FpoCMbr0zZObWh+aTF/g4S9TiVCVbN/CcH4Dq+S2HseQjmpNQJH/ns8y7ojoO3RFhzvHQEkI6V6joDJ1/hCrMS6Sq3Ep5Jcz4Z0UWsmfT7ev2tvOSFZs1QPTIDNccepL8qF10e4pmfT0Unqfdew7hmfA8mG2+BGUjQhoiLKsHXH/ET9GdbE7ln5fwiOM0X+AkstCTntjB8dg8IC3ZqKDSiSXZ8+j+MtS9p3u0Y3WS01XVz8r4SwGLzwzkt6RwCshyyxPJQl9wFTwdAaKOUqzKobjLzqvAS5Qjwys87g+Cfl7EraIw6RU4G2s4xT0UeeIB9xMzEJcPI5JV8j1f52UffcIsCE1ZQ3zRrhy6RRxCl7m6foQchoSAqWMNBVP2K0hY7yX8YkgfEYJ8cUpjVjSlNm88UeG/mulgFVswWX3cZMS15yEm8yxQ1wHMpLqDd1QHmL7DzvWFzAXiVeY/4L48u2OtnGlO0ehh7i4YIMso4zY7lFGuUzJXkgLY1LG30LE9FgVcHaV5KkTHNAT2wWal6V6MFQ0KsPmJXxMPCmvARR7eZsPqiY5Y68C4zh9yzk5FhSNFv7pE70CIdJ2sjHuPQgyFxsFkWq7h1LredacLJ7plYgLYLTdCTGLQKyYlgT4M0omm7tuuFDTPqyxXivqH0kUjMjjwL0OAuJKE1HUFVObuSsNvfizhhQRI9rMuO+LbvWwF9dhVZZvEM/kESV6MBLyHmhOUJ4MXGhL54y3ORnUk4pDLYhO6m2A==";
		String cct_Resp = "xtJ6x+ji8QSP/grVSiUWWofSJ6ngWeKokY6oMZMJiAsaPMwHNOkghP613DKCt13+Qf00KdGOyPWwN1KvYTl0LS2IluR71dqFpSaNEDLXkddqmwfBLk7kwkr1ndo4LHLIrIt0O5ENteiY70Yl+szo6whIMpP91N3EGmaPGk0S0eRtdw3hru4NiOnSf2z97xT+Fm1SU57PFz7bvdZ0z42Md0dgl+1Jwb+cLRryfpZ2c4Ti16BiMmadHYv4OFkwv5umoHovsKkIGvQEqTu7plfI9hfAWBBc5RwDjVqHf/bbF+KM9IFYvzyclxRIPYY07sjeTNHN5OSWhlbsjvGKD2LmHUX1wIM24mOGAxWyjaB9aVCIZP1YI3BKInPhxHVzme6VNm2yUmuh1bBKWyTu5V01/u4timY0fqw0DDYP4aiKPs9M0pATiM9+DeqN6A1D9H2qNDy9x2gGTp9kMu060M4KxzFo3+fVu8rj2GtF2gjS1da0Ir4cPBYtdrej7Yh298e6Is8+GEnAjABwCNhLo+3LNjSftHwtDV+wSH7OjVyJtqogwOF5fyDFpn3elV+llLQSKhd8NXNKWvkOJwObP0jRjGbIe4O3oJB2rXwedAQ8jpQkV84tl1dW2wUwkByXNz2ofD5WLLbIwVdhS8thYvoRhqYzB6lqIcWh7jeoGPuV1csBa1S6yzP9s6x+IaxPtEYNzULiGEQzBWCulRErkuABwwHkyIkBnXGBMK1XS++veXQDPHq0pQNzV5dh3yYIOqZzRWzdgRpK8YCRvaXk6Uqa61yFxtGQqwCLRDzOGQy5FHq/y6nhH49c8GMovTHqxdEtOPEg5As9dojh1TiIuuyE3PNyBUKwrsOM2Z+NWe4wX6X3v4GJXr+KV5LhnWeoA5/H5vW2dR0E6rMIK20kE3+CqUXVdQxCCjopnRUtuUDpCNcl/gK4uK+JhFxGbPoegyuCs/gjU0gXVA2DJwa//qGRTyex8QCGE44+AtsLtj/ch/LEjWHtjoENagRuXIDABTS/AB0lbOH0CEvMsKCHe7b+lK7/Tm/IL4qe6Mdmdos9pGpO8HjoWh4UyZg6E9Qe6Ko0yYu61HsWHBZMYat3SUxdhTyXQ71OB3FXdAedIBujgYM0rdHA+3HdLHv5ZTDa9ay83xw+gNvuBEguxCPiROlCZLdxoMLdD8x/DEsJ+O5wvQvlQQYtp7579TPy6mSwJZDeD3DNGA+JFEMZZEgkFN8mOpWNJ2BwImwn+7VU72pZHBY= ";
		String proc_Req = "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 ";
		String proc_Resp = "qqUAGXXH2H5HfUw4Us5B+8fpY7KWwk/Phd1WVRl+AZS6Wbz/ETFzJgknTzNDhFZgoAQxTbAyJmmx+WSD8fnZGdm4W93Icu5+mcNlhzU+T7RQj8QgXXMAreFkvVVf7ndwlInnjlvKBrbTMNWL+OfO90fczjfV/EcJ9cKrOA+9E+oort9Vxnev1fRI9/m16Iu5LHvPsnosSl3MZYGh557nBX/VxXQKkVjaeHBVk0ZpooZCzi74AGt5H04Aw/iv8dBVgrVk2Wd9SWCSU9i+13ud5m1S1bcWa2t5034S1oXuNq83h7rUA64EpW5nWxyHVoTP0Kb7W3GIBVaMuldWytDnVKtHBTfLwjhQajKUe9jHXnA5M+RPQ3v+50D1GFsq5TtRff2JvIKXHOcr/GVF4CiMdNRGRw160NgI4DYtmEvxuRafh656LQAYE/AUF78ujZRRVGAYmhnUxvjvs8mVpZ/vuN+sI18mNvstvhmPK0kDTlVEZmFnBchYSUBWaplR0hZPtPZJwNVTAS8cf0qZ/3Mx0NgRLDcsvYrstVgnPT9v6ZeWqmDqaVhoy7ZiB/9TrJPpdbF6XyC6oL35Mt4s7BkMhxdsplLQxO9gnnnPebrsnA+dMvIUj0xIObB9/kYSV0bZPbwwntRgoA5cM4T5GJv1hN9XGCD6OEuqFNFADn8AKJJ8L4Wk4dWtZ+Rsd7RlSwc+Cu3dxnsX+ulMKEJctvsFT9QvFkc+WG2cKENjrrxJxJpJOvF8/8wTJiefhIL4aAs4U0IkxWgJ/9jVbiglyeHFstLOnUXtNAuXf7pF4PArhv3XERnFlG5iOmSb2EI1VWMl ";
		String proc_ID ="30";
*/
		//Declined
	/*	String cct_Req = "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";
		String cct_Resp ="2SfsMn1icslQB256B+dvXwOuOp4lD083QIlTZT0vvZBLsw9hne+BjMXWaYvKsrClv0K2iSe2NVhImDcaFsDLca4tkAZ0FSpnZusjaBbVJLlfVRP9rv+LJvQxS0uZvINIsAcplrnldHpfagpGti72fcA+SlT7mG0VRsNxx9I/RdRdF3M7CYOvNRUQcZm+EapP45JEvwy5T8yH2ooAouml8Gvs7eYx07t6B7rRylDuzfdWygx0WAj7rVlFFbPHp275gMCHied+5alDFiYHi4Op/4Gpst0YRl4aK5uMbYlJ0DXMEl+FFne7s3ckiuEPM67ULPF6UVIw9edIMJa6wITOxmYgS1PlBOjwoI826JM7J6ySXSAwDlBH5DPCXGneOomLTJSDm9IhXRAWKRlbe4qHXbOUYJUl0fiIZf2BAX7ok0E3ssRp6iMg4lrMQ4yXhEYljUGjUKDJGuZVZdMvE3ksVrArmml9zOoJOtW6INsl3ewJuD/qr/H9Bf4U0veIqkWAu8uwOwnyh8P928040jbqCx7NuQ2GCfB0fYNphRw1G3sdvZHvSS8k7Oz6gAfzmyDgjkUrmBik8yHhDrKf0Dhq73YyVX7qwAtyPDYRXsoljgda0ftkvr/l+qrlgrLt5g5120GUh1jYjnr7+UCCNy+qVJSjpI/cIyTBDSf6ITgzrCccQ/e4A6mUX8nl4BLtu3rl+nqgYhoW/QOYOojZW03OkNaUjdizNpDcTjTsKArYx4TOv9VU+UUQWVv2JaQ0fqj9cdG68Z+/VgkmL4ErzK2ISQAvbgQ8fASaTDUGTiyoMMsGgaspekanYVDwSa4UdziBYnLf56H5ffG/tKA2JXayFr60qMge82w67e53uHXcWjiZ7KfuuRiSL07MMr1ZIpUVe4YbZCbeyPay6pbnmuPTGxqxPUUDl+uuNXBSpyvSkhu0Ow+8ScGKz16qe7wivd1mHGhO03Bba8OcDfwW6ndEVP2Sp4wqUI3lBa71C5+0xrT6CS15lVcyYcPVnsODn/1bZUb5zTwNS0GmeCKqNS42ibvtPejKjj5tIMPpnmsJ6wmP4hW9poDxaTOUI2WicA9PK6uVAKDEmDEsh2K9KsszDFPCPzUtZtxf3hIj5pEbDZ55aRt/ZoTupmWmZI8ztdodhQ/gYfqvXGIj1w8guGd7gLquWXUsPRMZAAYZMu6ML0To9WdXGueqs8bHMh4Rjy2bnYHH00D9M+GPb9Z0bzUlyzKxBn5sFsrkYp/lpfd5ey8TKFYGQ21ypQwI5YJ+L0u+AtxCjimm1ewmJBN/7PB5hcYIC9Hz55cYnU4i4e3SS2BECJyZ+KtmjxZaI/ay3kXRSB5sxQzSnT6kCdgDwZIetEVfYBY91/OO9CX2wyRVfW3JSVVtRRX6xQV/EWs//tjOgeSJPuUXxQ5XVQX5+o6GWMFZDtaXyPZZOxdsK/hfEaKcpAwbPCmfg7hkESpRiNuOikFfMJ0ZaIXGmCotxxy1ebHa+1WA80jO1tBHABpo329ypYNO1WTE96a9rJeQXQyrDXbcaVDzhHXKku//wDVn1BwNR0ZYwWt7GHzlKNSTyh+8OBaWQw/taDKK/bsRsf29AJVANw+Kavvd59vjnXKH7HtxFeIz5UlKMnjzlXGESS4vcRjrRwJhLh64w524Z55pn3fBnCNXfl6gbWSiX82ZnJWNJ2BwImwn+7VU72pZHBY=";
		String proc_Req = "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";
		String proc_Resp = "IgxNsYoSEoGwM8sZIhnoLYHvZ5XRzy0yXGUs0BdQ0FHtAxtXISE+MG1IaNshUPvg/TjQzvDKFe8UwQSO11BPsPi0EUP6SU7/V9o7QbAVNNcKW2pIOXa3CQUHgb7vQZAtzammDdWEH28MFgInfi9M2a1eaNOLMK+CAEAFXzbEbm3k0BV7bVvJcYo7uWSGOCkFNa9rYGKavkflgF/wfjhBiXlvQlR+G3KjRMMZ+oYEcD0kuIVNjg8+OnXdp4aoXiQ0JrsVzqubMNt5dmQ2BXOHokgebMUM0p0+pAnYA8GSHrRFX2AWPdfzjvQl9sMkVX1tL9eaYZrWOzBbMJqY3bJxcA==";
	
		String proc_ID ="30";
		
		String cct_req = "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";
		String cct_resp = "4VEm8KHLrQDcavNnqK9y6XFqGK0xs4+xf3JSRRrmswseIaFHtJ6q41pq2JpBMxskD7Ebt+0wvy+iBXht64rCE6y6fwxVFrrIosX4f0kDOh4BQ7qoIUSBTLK2dZuyiOo7XwLTmEvMlePZckdqyHU42R6iWN9iwAmOp7VX7jVE9O3ZUUag+Q4wpyAslyIYXKQ/lC+qEc1ELUKcBAWXQY16ZThTjklcTpzzsBFh8tJEpylgNvz+QCTmhbhE/r6yPpS8yRuvgnYv6Q35v9/Lxyz2kufZAJ8x3TNgu/oU4BkAvC/y7v9EAGZxYK3tRSIVru4u3FNZxc6jPOJQbZIdrJo/HVNdjuXmr0lAc9PBRN36qdNVFCDSNyCB7Dk++wkg7iKp9F/oJuq1AUkEJxJ7qLxvIRvbCAt6U1VmIU0Yrg8p+ZDDLiUqy52kcnvLsqi5LguVsS1uM4bERDFXPno/oGtRiY4hERYaTVu7ow4bpK82jF0T8j59YxztbD35+Ggnb15QApBCPZfLxjFHNMxNp/lsldBHCxhMcEiE9wfzT1LxIJY4LD5PFelFEs6ZMEOkmbV/ys5dGfpFzd3cezWgWN36ZIOftJrKp94cdA8qvgvfzzxJW2ZIb8rdcx+nSMTDNx8TA/i+CTRosKSSC5vLJflKu/EhwEiKpwXbdywtYt/S+epDqYN1G9cXX2+B83ey8uCB/LuzmpI7VttC6mo9F/loMR0ynJQxfLLQ38P3i8wf0J/EVRc3zGT14VVS5oMRCHgbR/7WmVN0i5pm0gIaQFjTth+FISqk5kaznuc2HAup8E8GIs1eeBF7bSgfOPfxot8v0WVwN1CjVKyxi1do96Py6f5bX9funZRDCaNTSg2jeT/C2opJZRfI2JW6/U7BIQJHCi2E75bsKtuqxAMSrOVdZEu0jVn94jGIK5qnTqLAu/4gESdKTB/uinf1hK96X2UQAhRpyw5Ci3/HmMfXEgjG5HozeSXlOTDN33+Tua36X5IwaLD/RT2F3DGyVI1q2WyeSJGfcJskmKWyqShsLQaydvr2j/uvTRVCs1xz9b+I0NJOivnTwOY7RyMl2pYSywdukokUG1h0LHUF9OkADx62kmp03scasI2o/He1AarJrFCXcHQw+yqMG5q9KQ8b7yyW0v2CmQEDycrrMxSYs5maoiJrPyQmv/E9UeTfuwUBwDEZoOudek41FrPpFBZGHD0ciw+RDURfi0ty2I4YpRKf9qTgn0eMdY/5WK9/3Kj9jk3Wm9r6k1s0KqXx6F+h4gGFntY06jKlvgtcOyJQ1ARkOFh0v5gzVcX0EzzfEXgdGL5EUUsSDzFCwjyA/v+EG0WHVYxS0fhsC6xwl7zgfia/m8avw4/FryUt9Iz+N3bn58yT/0Sc5d9v8Vo+SO7Kr+RcmR3Wt3S+AMGigENVWnIEsY4EsBRLgi7bmJmuA3oyYTG9tmLYff9mW2K5SWl86FbGZqORXZLizzEHlJEScJfisyXDxDW13bUNbz3y02pwc47YebGH0jJ9IAyttRGciv/ULYFj2Ezcz3C1LAvapSF15wZgeGjOD4MmLMH3FLJhZxIyph9fJflNW1CUs0XTDO+gxf70nylIr4Ktx6oZKevh7gPAqy7N7oC7mvOmjBRKZvqSwwe4iK0NoaYAGYUgWH0NrmnkXplIatPQqW6Rn7wpc3RxstZcOPsV2r/5kKpQB7WeZtkCj+G2DwRHJRmLixxhLpTO4EHWKEMPmaDh07Fv+uUbZ8Ijh3zsfJuVtSDHohs=";
		String proReq = "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";
		String proRes = "IgxNsYoSEoGwM8sZIhnoLY2tMzNo+g8h95/wbbge+gP0Bo+efrMlHkuYUqUm2AdO/TjQzvDKFe8UwQSO11BPsCCxhwWyEU0zjNYe593vcvJDCfPK4zSPxyEQWuGO4Zsn988m7fHZi2onTt02ptk0VZnsp+65GJIvTswyvVkilRVL99J/z3vez4O6Rpd7WfiNnFUdL8nP8L1N/ABnQOQ2jbRkzBtB0C+BGK1DU/zNlHJVqi+7I3LOuqfibHmzLJT4l3SbbBKQVxjH4LIRsFE1Zqk6yTOS/ZK3XqJdx8q6q+XoRMm2GqPTZBBSA0Bt350cu6h9UFcrrVHmkQRbeknv4w==";
		*/
				
	
		
		log.info("Validation request received: {}", request);

		ProcessRequest declinedRequest;
		try {
			declinedRequest = decryptRequest(request);
			
		} catch (BlankRequestException e) {
			log.warn("cctRequest is empty");
			return ResponseEntity.ok(buildExceptionResponse("INVALID_REQUEST", "cctRequest cannot be empty"));
		} catch (Exception e) {
			log.error("Decryption failed: {}", e.getMessage(), e);
			return ResponseEntity.ok(buildExceptionResponse("DECRYPTION_FAILED", e.getMessage()));
		}

		log.info("Declined transaction | cctRequest={}, processorRequest={}", declinedRequest.getCctRequest(),
				declinedRequest.getProcessorRequest());

		// ---------- LOOKUP (direct checks, no exception thrown) ----------
		TransactionLookupResponse approvedTransaction;
		try {
			approvedTransaction = transactionLookupService.lookupTransaction(declinedRequest, request.getProcessorId());
		} catch (Exception e) {
			log.error("Unexpected error during transaction lookup: {}", e.getMessage(), e);
			return ResponseEntity.ok(new ExceptionResponse("DECLINED", "LOOKUP_FAILED", e.getMessage(), null));
		}

		if (approvedTransaction == null) {
			log.warn("Transaction not found. lookupKey=null");
			return ResponseEntity
					.ok(new ExceptionResponse("DECLINED", "NO_DATA_FOUND", "Transaction data key not found", null));
		}

		ProcessRequest approvedProcessRequest = approvedTransaction.getProcessRequest();

		if (approvedProcessRequest == null || (approvedProcessRequest.getCctRequest() == null
				&& approvedProcessRequest.getProcessorRequest() == null)) {
			log.warn("Transaction not found. lookupKey={}", approvedTransaction.getLookupKey());
			return ResponseEntity.ok(new ExceptionResponse("DECLINED", "NO_DATA_FOUND",
					"Transaction data was not found in the Golden Template for the generated key.",
					approvedTransaction.getLookupKey()));
		}

		log.info("Approved transaction | lookupKey={}, cctRequest={}, processorRequest={}",
				approvedTransaction.getLookupKey(), approvedProcessRequest.getCctRequest(),
				approvedProcessRequest.getProcessorRequest());
		// ---------- END LOOKUP ----------

		ComparisonJsonResult jsonComparisonResult = null;
		try {
			/*

			String app = """
					{"4.10":"0.00","4.11":"0.01","4.13":"188001","4.15":"3","4.163":"0.00","4.16":"","4.20":"840","4.21":"840","4.22":"0","12.87":"","4.26":"1","3.1":"20","3.2":"P630 Plus-A","3.3":"REG01","4.17":"","3.4":"00","4.18":"07072026","3.5":"26.04.142.003","4.19":"143048","7.1":"0.00","3.6":"1.8","7.2":"0.00","3.7":"VOS3_3.4.0-432","7.3":"0.00","92.6.3":"999","3.8":"1","7.4":"001","7.5":"null","4.32":"1","7.8":"1","7.9":"%1F1%1F1%7E%7E%7E001%7EGASOLINE%7EG%7E1%2E000%7E10%2E000%7E10%2E00%7E%1D","4.36":"","4.142":"%7B%22BTSignal%22%3A%22%22%2C%22Battery%22%3A%22NA%22%2C%22GPRSSignal%22%3A%22NotConnected%22%2C%22PingState%22%3A%220%22%2C%22WifiSignal%22%3A%22100%22%7D","4.38":"26.04.142.003","4.29":"","12.63":"1001","5.13":"03101001","4.46":"01","5.14":"04","4.47":"01","5.15":"01","4.48":"","4.49":"01","4.134":"0.00","4.135":"0.00","4.40":"01","4.41":"00","2.1":"552276417","2.2":"552276417","2.3":"10.192.12.250","6.1":"","6.2":"","6.3":"","6.4":"","6.5":"","6.6":"","6.7":"","6.8":"","6.9":"12345","4.57":"00","4.126":"0.00","4.127":"0.00","4.128":"0.00","4.52":"0","4.64":"0.00","4.69":"679999XXXXXXX2010","4.112":"0","4.63":"0","1.1":"101231310749","1.2":"03101","1.3":"18604837","5.1":"12345667","5.3":"1324566","5.4":"111","5.5":"","5.6":"","5.8":"6.000","5.9":"03101001","6.11":"","12.98":"","6.18":"","6.17":"","3.23":"10.192.15.65","4.70":"1249","3.21":"2.51","6.10":"","6.23":"","6.22":"","6.25":"","6.24":"","6.27":"","6.26":"","91.1.9":"6.000","6.21":"","6.20":"","4.1":"21","4.2":"000014","4.3":"6","6.19":"","4.4":"6CC8B6BD4B040AD65B1DB36C4ADBAB1237AA54736E6E42D2832DC0CA0F6CD1EDFB96CE99F10119FA%7EFFFF901347C7E0600096%7E0000000000000000","4.5":"0.01","8.1":"4F%7EA0000000043060D0561111%1E50%7E4D41455354524F%1E5F20%7E5553412044656269742F54657374204361726420303620202020%1E5F24%7E491231%1E5F25%7E250201%1E5F28%7E0840%1E5F2A%7E0840%1E5F2D%7E656e%1E5F30%7E0201%1E5F34%7E11%1E5F56%7E%1E82%7E1800%1E84%7EA0000000043060D0561111%1E8E%7E00000000000000000205410342035E031F0000000000000000000000%1E8F%7E%1E95%7E8000008000%1E9A%7E260707%1E9B%7E6800%1E9C%7E00%1E9F02%7E000000000100%1E9F03%7E000000000000%1E9F06%7EA0000000043060%1E9F07%7EFFC0%1E9F08%7E0002%1E9F09%7E0002%1E9F0D%7EB0509C8800%1E9F0E%7E0000000000%1E9F0F%7EB0709C9800%1E9F10%7E0110A50009020000000000000000000000FF%1E9F11%7E01%1E9F12%7E4D41455354524F4465626974%1E9F15%7E5999%1E9F1A%7E0840%1E9F1C%7E3138363034383337%1E9F1E%7E353532323736343137%1E9F21%7E050116%1E9F26%7E311B5006DBC083E8%1E9F27%7E80%1E9F33%7EE0F8C8%1E9F34%7E410302%1E9F35%7E22%1E9F36%7E0042%1E9F37%7E2FD9BC07%1E9F39%7E05%1E9F40%7E6000F0A001%1E9F41%7E00000102%1E9F42%7E840%1E9F4C%7E%1E9F53%7E52%1E9F6E%7E08400000303000%1E9F7C%7E%1EDF79%7E342e302e30%1E","4.6":"0.00","4.7":"0.00","4.8":"0.00","4.9":"0.00"}
					     		 		""";
			String dec = """
					{"4.11":"175.00","4.13":"198001","4.15":"8","4.16":"D25B79B57DED5D84","14.13":"1","3.43":"6","4.20":"840","4.21":"840","4.26":"1","4.113":"1","12.1":"0025015","1.1":"101231309688","1.2":"3100400","1.3":"35890831","3.1":"17","3.2":"AESDK","3.3":"Reg01","5.1":"0025015","4.17":"FFFF05700240AF60405F","3.4":"00","4.18":"07172026","3.5":"25.09.115.001","4.19":"105912","5.3":"0025015","7.1":"0.00","3.6":"1.8","7.2":"0.00","3.7":"10.0","7.3":"0.00","3.8":"1","7.5":"%1F%1F%1D","5.8":"5.0874","5.9":"01","4.32":"3","7.8":"1","4.79":"1","4.38":"25.09.115.001","3.21":"2.39","4.42":"P2PE NOT SUPPORTED","5.13":"01","5.14":"01","4.40":"00","2.1":"C0N43N4U45RLN8R1","2.2":"60:33:4b:10:42:98","2.3":"10.4.0.99","4.1":"246","4.2":"018297","4.3":"17","4.4":"5569269367557629D29012010901544100015","4.5":"175.00","8.1":"50%7E4D415354455243415244%1E57%7E5569269367557629D29012010901544100015%1E5A%7E5569269367557629%1E82%7E1980%1E84%7EA0000000041010%1E8E%7E000000000000000042031E031F03%1E95%7E0000048001%1E9A%7E260717%1E9B%7EE800%1E9C%7E00%1E5F24%7E290131%1E5F2A%7E0840%1E5F34%7E01%1E9F02%7E000000015000%1E9F03%7E000000000000%1E9F06%7EA0000000041010%1E9F07%7EFF00%1E9F09%7E0002%1E9F10%7E0110A04003220000000000000000000000FF%1E9F1A%7E0840%1E9F1E%7E33383337333333303330333833303332%1E9F26%7E37C0425368FE7A73%1E9F27%7E80%1E9F33%7E604808%1E9F34%7E420300%1E9F35%7E25%1E9F36%7E0019%1E9F37%7E85BD249A%1E9F39%7E07%1E9F41%7E00000643%1E9F53%7E52%1E9F6E%7E0840000330300001010080%1EBF0C%7E9F5D030000009F4D020B0A9F6E0B0840000330300001010080%1E","4.6":"0.00","4.7":"0.00"}

					     		 			""";
					     		 			
					     		 			*/
			List<ValidateResult> result = null;
			// 08069379999

		//	 jsonComparisonResult = compareJson(declinedRequest, approvedTransaction);

		jsonComparisonResult = fieldValidator.validation(declinedRequest.getCctRequest(),
				approvedTransaction.getProcessRequest().getCctRequest());

		//	jsonComparisonResult = fieldValidator.validation(dec, app);
			// log.info("jsonComparisonResult :",jsonComparisonResult);

		} catch (NoDataFoundException e) {
			log.warn("Transaction data not found. lookupKey={}", e.getLookupKey());
			return ResponseEntity
					.ok(new ExceptionResponse(e.getStatus(), e.getCode(), e.getMessage(), e.getLookupKey()));
		} catch (Exception e) {
			log.error("JSON comparison failed: {}", e.getMessage(), e);
			return ResponseEntity.ok(buildExceptionResponse("JSON_COMPARISON_FAILED", e.getMessage()));
		}

		ComparisionXmlResult xmlComparisonResult = null;
		try {
		
			if (vantivProcessorId.contains(request.getProcessorId())) {
				ComparisonReport vantivResponse = comparisonService.compare(
						approvedTransaction.getProcessRequest().getProcessorRequest(),
						declinedRequest.getProcessorRequest());
				return ResponseEntity.ok(vantivResponse);
			} else if (fiservProcessorId.contains(request.getProcessorId())) {
				xmlComparisonResult = compareXmlIfPresent(declinedRequest, approvedTransaction);
			} else if (chaseProcessorId.contains(request.getProcessorId())) {
				// this for the chase comaparator
				// String aaprovedRequest = "UKCNJ+XZuBBEynMbS6dqsnsXj3vUQUua/u9AV2iNl76Zl5bZaPN6DU4IYXltGY54Qrwufdyw4+Lqbim5rRrCB0QJ32d17pVBlgcTakut8bTM1oMqUyo63W3CbinpVlmG7T3aHJJxm5UEUekc9ZFkA2H9z/oPLwW8DguwavkQkHukorw1a1rj1T+v3FKd0b1ms4E09i6gEd5NjY2RJIz/qrP2ghufMrQZ1V/Jpf5Ad9XSDGC/yv12J8q6n2685AFokFFoy7ARUvj4bbidqJ5YVqI+0H/HvSJ8U56X81HjHRVbah41SqE2NcZw/7tKgV3Swy+NEHMBdTjXEhfAPgFsDKjrhx/uGsWXh5xN29MlRtA+M+YmhdhWQE6tOlT0oY2Nkcj5ohun0HIcXrkw5G+ztudvJcsF5MkfAV9n9pwGaQev4KMREpPSx1S9428ju7XXjGnqhJmj9mqFCp9TyLA060Y6Ryb+TDmURQozjMs8unqYmyBczAmf+aHs7A1w0h9QTMMV5wWh4ixpnVZfMRaEyfeqnbLw+XbQzVm2J6SST2w1qR4Dk6TDM7k1qf9IExiPTG5qDleSVqHjKZadqxSvuhmRujfMczBp93oWRKK6UogGE1nnxIHa8FFsbtwHDdS57pE/FcjYIhzNgcl3NluTb2kNcUN47JqSbBuXthfUlDjCrO6FyO5zhq7vb1OFYzlf23g3VYeA3Qb2xXPbMZTEQwKdGUjCyGCkUajmb40m6xpJrl5iCFTL7bRJKRBQty2jYf3P+g8vBbwOC7Bq+RCQex5x2P0JDUWoQNPQiLRXDBvnGy2Nc9mkSYP/cmDN3+28sXKq5dhGsbu8TNJkzDDuRQ== ";
	        	//  String declinedRequest = "UKCNJ+XZuBBEynMbS6dqsnsXj3vUQUua/u9AV2iNl76Zl5bZaPN6DU4IYXltGY541EHM0wR54QZD4iTgHr/yZdDPWjKp8CeJ4ibqFcMkCeWSPY7UXAARE1WOKms3z2r2XewfphJVE9X2n4RLQK7rWZqSunJHmVyXa13N7LRH6W0nxOiY9QkYAv+GvN+7Hgltj+3m/Ctho5Oqqy2EvnUWjhx10XfIBfzY4PaoB5XnzUp61DbyBsO43dNdHv4Da+wXjhXFQL/fFUukjMZodouaWpI9jtRcABETVY4qazfPavb5JeMXSe/WlFPtafYbDVgXipZMKWArjhYnGMm9kp1P7NGTXc+m/Vb7CopQj70W6W9vNOfe9Hio6kDTeBbAcez0JTSU3xRM6pF/T088Ow7qwbEBJHYkGJ/B5fXiAvlZ3SeXzqI7iww0Ok5Ytx9aT6OEoRgeaJKxQi++tGytgVFdGRnKtszgsdoo79yPXR29skXiHtIs3XzvAaWAzM8kaYjxHwbFr5y3B2oJozW+moN/PSZ9NH7asP0D5i94xjBG5wFBzLdTX/Hq+vYnfnuY/42JEEpQPHlqqu8MalGlgyX/roa+iZl8GdV9K0RYzXz7AWu3nIL8u4VNl56gDgVTmgNdhOEFZr6E8/j/Kb+oJCi2+LhUOtguq8JfC+xGGs8THDjcrdaeqMLHLOucJboC48jQV3tJzL+CFYBG9kSdM6Gl18BdERIpfnLpaT7kLtlQREb3tRW45cWd2svs1Q248KR4opmDL39apzPu3Yj9BGCo3zvKoqmGFYFvJ2rq0CqnYldZvoS6Oh4G5cSZMdNqpeO0O55DEkh9keGhZvcnycE0W9J7I8XoXoMLOrM3kL3PxnucUbjSFg1+ieghXzVxEEfxYf3P+g8vBbwOC7Bq+RCQe2h+JxoolIS8bTSp/817G4cIU3qEG/xo9GoP/sDIouaRnftBOQXFOf1mJo5oc8ndcpZEKRgqVYiH7TfcNDKP5A4f6joOkCS+7hEmkYje7nHNPRY7kybnAvtFdYZfw5L2aoBHcNYuB1YHCBAhS/sk7zwTbtHIfGjFi350yFCAeWGaQfr/ep/VjhqSV+TJWHjqhG7wJ58sbyVWJPnKD1VN43Tl36y/2yV8r4VyO4qhuhz54ysRF/+OS+HClWZksD6YvITTIn2K1wxVHdIYCi3QrBKrOgNTROxNL4e6cBqdaby5T2rkpleC6rnHO4m9wAZgxEE5eW4Zbsh+dvBFw1Mb7Jwp78WaOsXHASJPy4gEOXLxkLTyg9GGP8kFSupycrNpuvEt9g/kOywtDe4jRyhKdNDvnYRmVa2x/h9mYx4YI40sYf3P+g8vBbwOC7Bq+RCQe1HaX72gpAI0EsMKMCioxZ8VZ6S2rrhWc8bmxu9DPERb+lnMBmpQj1F0uor1zYGCpWH9z/oPLwW8DguwavkQkHth/c/6Dy8FvA4LsGr5EJB7noY+7XGxYzflDZ4D79veq1UPVQsYug9stcs47mF9xvdVsvfFhzAXnHG7k1wKFCUX/2zTXlbJ/U+fV3mo2MONxgcbno4rSXIpQ+dbCIOgp8tuC0epfXPO8I+FuIBu9m5YVzCVnQ+4mP1QCUdWo47EKqSRbIqws2Yjds/682njmje1jPu3Jg2I/mEoLCb9U0WUVgV50CPO21OuPRE9GcbGHNEFVKoX30NWyihffjBAdZEW8bpsHUJRKrp9HTxQ5qMBLRucTHq45n+WQWkJfNmiASI8Ool/UlF10wM2RKhQ0GTbeDdVh4DdBvbFc9sxlMRD23g3VYeA3Qb2xXPbMZTEQ4uRlvzdYsylHjzCtAzMlAR7WC04eh3BIpb0aTiQsGbTDC43OA9hGYJGuQCKhViUTCS09UpmDwNl2M7JBKdwoBOGzw4ES9U/YuG2fjpm6wAg ";
	            log.info("Chase request :{}",approvedTransaction.getProcessRequest().getProcessorRequest());
				Iso8583ParseResult approvedRequestParse = parser.parseMessage(approvedTransaction.getProcessRequest().getProcessorRequest());
	            
	            Iso8583ParseResult declinedRequestParse = parser.parseMessage(declinedRequest.getProcessorRequest());           
	           
	            
	            Map<String , Iso8583ParseResult> requestMap = new HashMap<>();
	            requestMap.put("aaprovedRequest", approvedRequestParse);
	            requestMap.put("declinedRequest", declinedRequestParse);
	            
	          List<Iso8583FieldValidationResult> result = chaseValidationService.validation(approvedRequestParse, declinedRequestParse);
	          
	            return ResponseEntity.ok(result);
			}
		} catch (Exception e) {
			log.error("XML comparison failed: {}", e.getMessage(), e);
			return ResponseEntity.ok(buildExceptionResponse("XML_COMPARISON_FAILED", e.getMessage()));
		}

		log.info("JSON comparison result: match={},\n mismatch={},\n validation={},\n skipped={}",
				jsonComparisonResult.getMissMatchIssue(), jsonComparisonResult.getValidationIssue());

		log.info("XML comparison result: \n mismatch={},\n validation={}", xmlComparisonResult.getXmlMissMatchIssue(),
				xmlComparisonResult.getXmlValidationIssue());

		ComparisonResult comparisonResult = new ComparisonResult();
		comparisonResult.setComparisonJsonResult(jsonComparisonResult);
		comparisonResult.setComparisionXmlResult(xmlComparisonResult);

		ValidationIssue validationIssue = new ValidationIssue();
		validationIssue.setRequestKey(approvedTransaction.getLookupKey());

		if (jsonComparisonResult.getValidationIssue() != null || xmlComparisonResult.getXmlValidationIssue() != null) {
			validationIssue.setAurusRequestValidationIssue(jsonComparisonResult.getValidationIssue());
			validationIssue.setProcessorRequestValidationIssue(xmlComparisonResult.getXmlValidationIssue());
		}
		if (jsonComparisonResult.getMissMatchIssue() != null || xmlComparisonResult.getXmlMissMatchIssue() != null) {
			validationIssue.setAurusRequestValidationIssue(jsonComparisonResult.getMissMatchIssue());
			validationIssue.setProcessorRequestValidationIssue(xmlComparisonResult.getXmlMissMatchIssue());
		} else {
			String msg = "The transaction request has been compared with both approved and declined transaction requests, and the request format is valid.\r\n"
					+ "\r\n"
					+ "The transaction was successfully sent to the processor and received by them. However, it was declined at the processor's end.\r\n"
					+ "\r\n"
					+ "Please raise this issue with the processor for further investigation and identify the reason for the decline.\r\n"
					+ "";
			return ResponseEntity.ok(buildCustomResponse(approvedTransaction.getLookupKey(), msg, "success"));
		}

		log.info("Validation completed successfully. lookupKey={}", approvedTransaction.getLookupKey());
		return ResponseEntity.ok(comparisonResult);

	}

	@PostMapping(value = "/xcompare")
	public ResponseEntity<?> validateTransactions(@RequestBody UserInput request) {

		log.info("Validation request received: {}", request);

		if (request == null) {
			log.error("Validation request is null");
			return ResponseEntity.ok(buildExceptionResponse("INVALID_REQUEST", "Request body is null"));
		}

		ProcessRequest declinedRequest;
		try {
			declinedRequest = decryptRequest(request);
		} catch (BlankRequestException e) {
			log.warn("cctRequest is empty");
			return ResponseEntity.ok(buildExceptionResponse("INVALID_REQUEST", "cctRequest cannot be empty"));
		} catch (Exception e) {
			log.error("Decryption failed: {}", e.getMessage(), e);
			return ResponseEntity.ok(buildExceptionResponse("DECRYPTION_FAILED", e.getMessage()));
		}

		log.info("Declined transaction | cctRequest={}, processorRequest={}", declinedRequest.getCctRequest(),
				declinedRequest.getProcessorRequest());

		// ---------- LOOKUP (direct checks, no exception thrown) ----------
		TransactionLookupResponse approvedTransaction;
		try {
			approvedTransaction = transactionLookupService.lookupTransaction(declinedRequest, request.getProcessorId());
		} catch (Exception e) {
			log.error("Unexpected error during transaction lookup: {}", e.getMessage(), e);
			return ResponseEntity.ok(new ExceptionResponse("DECLINED", "LOOKUP_FAILED", e.getMessage(), null));
		}

		if (approvedTransaction == null) {
			log.warn("Transaction not found. lookupKey=null");
			return ResponseEntity
					.ok(new ExceptionResponse("DECLINED", "NO_DATA_FOUND", "Transaction data key not found", null));
		}

		ProcessRequest approvedProcessRequest = approvedTransaction.getProcessRequest();

		if (approvedProcessRequest == null || (approvedProcessRequest.getCctRequest() == null
				&& approvedProcessRequest.getProcessorRequest() == null)) {
			log.warn("Transaction not found. lookupKey={}", approvedTransaction.getLookupKey());
			return ResponseEntity.ok(new ExceptionResponse("DECLINED", "NO_DATA_FOUND",
					"Transaction data was not found in the Golden Template for the generated key.",
					approvedTransaction.getLookupKey()));
		}

		log.info("Approved transaction | lookupKey={}, cctRequest={}, processorRequest={}",
				approvedTransaction.getLookupKey(), approvedProcessRequest.getCctRequest(),
				approvedProcessRequest.getProcessorRequest());
		// ---------- END LOOKUP ----------

		ComparisonJsonResult jsonComparisonResult;
		try {
			jsonComparisonResult = compareJson(declinedRequest, approvedTransaction);
		} catch (NoDataFoundException e) {
			log.warn("Transaction data not found. lookupKey={}", e.getLookupKey());
			return ResponseEntity
					.ok(new ExceptionResponse(e.getStatus(), e.getCode(), e.getMessage(), e.getLookupKey()));
		} catch (Exception e) {
			log.error("JSON comparison failed: {}", e.getMessage(), e);
			return ResponseEntity.ok(buildExceptionResponse("JSON_COMPARISON_FAILED", e.getMessage()));
		}

		ComparisionXmlResult xmlComparisonResult = new ComparisionXmlResult();
		try {
			
			xmlComparisonResult = compareXmlIfPresent(declinedRequest, approvedTransaction);
			
		} catch (Exception e) {
			log.error("XML comparison failed: {}", e.getMessage(), e);
			return ResponseEntity.ok(buildExceptionResponse("XML_COMPARISON_FAILED", e.getMessage()));
		}

		log.info("JSON comparison result: match={},\n mismatch={},\n validation={},\n skipped={}",
				jsonComparisonResult.getMissMatchIssue(), jsonComparisonResult.getValidationIssue());

		log.info("XML comparison result: match={},\n mismatch={},\n validation={},\n skipped={}",
				xmlComparisonResult.getXmlMissMatchIssue(), xmlComparisonResult.getXmlValidationIssue());

		ComparisonResult comparisonResult = new ComparisonResult();
		comparisonResult.setComparisonJsonResult(jsonComparisonResult);
		comparisonResult.setComparisionXmlResult(xmlComparisonResult);

		log.info("Validation completed successfully. lookupKey={}", approvedTransaction.getLookupKey());
		return ResponseEntity.ok(comparisonResult);

	}

	/**
	 * Shared decrypt -> lookup -> compareJson -> compareXmlIfPresent ->
	 * ValidationIssue flow used by both /validationissue and /xcompare.
	 *
	 * NOTE: All "failure" branches intentionally return HTTP 200 with status/code
	 * fields in the body (instead of 4xx/5xx) so that n8n's HTTP Request node does
	 * not treat them as node failures and terminate the workflow. Branch on
	 * body.status / body.code in n8n (e.g. an IF node on {{$json.code ===
	 * "NO_DATA_FOUND"}}) instead of on HTTP status.
	 */
	private ResponseEntity<?> processValidation(UserInput request) {
		log.info("Validation request received: {}", request);

		if (request == null) {
			log.error("Validation request is null");
			return ResponseEntity.ok(buildExceptionResponse("INVALID_REQUEST", "Request body is null"));
		}

		ProcessRequest declinedRequest;
		try {
			declinedRequest = decryptRequest(request);
		} catch (BlankRequestException e) {
			log.warn("cctRequest is empty");
			return ResponseEntity.ok(buildExceptionResponse("INVALID_REQUEST", "cctRequest cannot be empty"));
		} catch (Exception e) {
			log.error("Decryption failed: {}", e.getMessage(), e);
			return ResponseEntity.ok(buildExceptionResponse("DECRYPTION_FAILED", e.getMessage()));
		}

		log.info("Declined transaction | cctRequest={}, processorRequest={}", declinedRequest.getCctRequest(),
				declinedRequest.getProcessorRequest());

		// ---------- LOOKUP (direct checks, no exception thrown) ----------
		TransactionLookupResponse approvedTransaction;
		try {
			approvedTransaction = transactionLookupService.lookupTransaction(declinedRequest, request.getProcessorId());
		} catch (Exception e) {
			log.error("Unexpected error during transaction lookup: {}", e.getMessage(), e);
			return ResponseEntity.ok(new ExceptionResponse("DECLINED", "LOOKUP_FAILED", e.getMessage(), null));
		}

		if (approvedTransaction == null) {
			log.warn("Transaction not found. lookupKey=null");
			return ResponseEntity
					.ok(new ExceptionResponse("DECLINED", "NO_DATA_FOUND", "Transaction data key not found", null));
		}

		ProcessRequest approvedProcessRequest = approvedTransaction.getProcessRequest();

		if (approvedProcessRequest == null || (approvedProcessRequest.getCctRequest() == null
				&& approvedProcessRequest.getProcessorRequest() == null)) {
			log.warn("Transaction not found. lookupKey={}", approvedTransaction.getLookupKey());
			return ResponseEntity.ok(new ExceptionResponse("DECLINED", "NO_DATA_FOUND",
					"Transaction data was not found in the Golden Template for the generated key.",
					approvedTransaction.getLookupKey()));
		}

		log.info("Approved transaction | lookupKey={}, cctRequest={}, processorRequest={}",
				approvedTransaction.getLookupKey(), approvedProcessRequest.getCctRequest(),
				approvedProcessRequest.getProcessorRequest());
		// ---------- END LOOKUP ----------

		ComparisonJsonResult jsonComparisonResult;
		try {
			jsonComparisonResult = compareJson(declinedRequest, approvedTransaction);
		} catch (NoDataFoundException e) {
			log.warn("Transaction data not found. lookupKey={}", e.getLookupKey());
			return ResponseEntity
					.ok(new ExceptionResponse(e.getStatus(), e.getCode(), e.getMessage(), e.getLookupKey()));
		} catch (Exception e) {
			log.error("JSON comparison failed: {}", e.getMessage(), e);
			return ResponseEntity.ok(buildExceptionResponse("JSON_COMPARISON_FAILED", e.getMessage()));
		}

		ComparisionXmlResult xmlComparisonResult;
		try {
			xmlComparisonResult = compareXmlIfPresent(declinedRequest, approvedTransaction);
		} catch (Exception e) {
			log.error("XML comparison failed: {}", e.getMessage(), e);
			return ResponseEntity.ok(buildExceptionResponse("XML_COMPARISON_FAILED", e.getMessage()));
		}

		log.info("JSON comparison result: match={},\n mismatch={},\n validation={},\n skipped={}",
				jsonComparisonResult.getMissMatchIssue(), jsonComparisonResult.getValidationIssue());

		log.info("XML comparison result: match={},\n mismatch={},\n validation={},\n skipped={}",
				xmlComparisonResult.getXmlMissMatchIssue(), xmlComparisonResult.getXmlValidationIssue());

		ComparisonResult comparisonResult = new ComparisonResult();
		comparisonResult.setComparisonJsonResult(jsonComparisonResult);
		comparisonResult.setComparisionXmlResult(xmlComparisonResult);

		ValidationIssue validationIssue = new ValidationIssue();
		validationIssue.setRequestKey(approvedTransaction.getLookupKey());
		validationIssue.setAurusRequestValidationIssue(jsonComparisonResult.getValidationIssue());
		validationIssue.setProcessorRequestValidationIssue(xmlComparisonResult.getXmlValidationIssue());

		log.info("Validation completed successfully. lookupKey={}", approvedTransaction.getLookupKey());
		return ResponseEntity.ok(validationIssue);
	}

	// ---------------------------------------------------------------------
	// Helper methods
	// ---------------------------------------------------------------------

	private ProcessRequest decryptRequest(UserInput request) throws Exception {
		if (request.getCctRequest() == null || request.getCctRequest().isBlank()) {
			throw new BlankRequestException("cctRequest cannot be empty");
		}
		String decryptedCctResponse = aurusDecryptor.decryptor(request.getCctResponse());
		String decryptedCctRequest = aurusDecryptor.decryptor(request.getCctRequest());

		String decryptedProcessorRequest = null;
		if (request.getProcRequest() != null && !request.getProcRequest().isBlank()) {
			decryptedProcessorRequest = aurusDecryptor.decryptor(request.getProcRequest());
		} else {
			log.info("processorRequest is empty, skipping decryption");
		}

		ProcessRequest declinedRequest = new ProcessRequest();
		declinedRequest.setCctRequest(decryptedCctRequest);
		declinedRequest.setCctResponse(decryptedCctResponse);
		declinedRequest.setProcessorRequest(decryptedProcessorRequest);
		return declinedRequest;
	}

	private ComparisonJsonResult compareJson(ProcessRequest declinedRequest,
			TransactionLookupResponse approvedTransaction) throws Exception {
		String lookupKey = approvedTransaction.getLookupKey();

		String approvedJson = Optional.ofNullable(approvedTransaction.getProcessRequest())
				.map(ProcessRequest::getCctRequest).filter(json -> !json.isBlank())
				.orElseThrow(() -> new NoDataFoundException("DECLINED", "NO_DATA_FOUND", "Transaction data not found",
						lookupKey));

		String declinedJson = declinedRequest.getCctRequest();

		log.debug("declinedJson={}", declinedJson);
		log.debug("approvedJson={}", approvedJson);

		ComparisonJsonResult result = jsonComparator.compare(declinedJson, approvedJson);
		log.info("JSON comparison result: {}", result);
		return result;
	}

	private ComparisionXmlResult compareXmlIfPresent(ProcessRequest declinedRequest,
			TransactionLookupResponse approvedTransaction) throws Exception {

		log.info("Declined-Processor-Request : {}", declinedRequest.getProcessorRequest());
		String declinedXml = declinedRequest.getProcessorRequest();
		if (declinedXml == null || declinedXml.isBlank()) {
			return new ComparisionXmlResult();
		}

		ProcessRequest approvedProcessRequest = Optional.ofNullable(approvedTransaction.getProcessRequest())
				.orElseThrow(() -> new NoDataFoundException("DECLINED", "NO_DATA_FOUND",
						"Approved processor request not found", approvedTransaction.getLookupKey()));

		String approvedXml = approvedProcessRequest.getProcessorRequest();
/*
		approvedXml = """
				<?xml version="1.0" encoding="UTF-8"?><Request Version="3" ClientTimeout="30" xmlns="https://stg.dw.us.fdcnet.biz/rc"><ReqClientID><DID>00070043299621013039</DID><App>RAPIDCONNECTSRS</App><Auth>20001318192|00000001</Auth><ClientRef>2723FCDVRAU070</ClientRef></ReqClientID><Transaction><ServiceID>160</ServiceID><Payload Encoding="xml_escape"><GMF xmlns="com/fiserv/Merchant/gmfV12.04"><DebitRequest><CommonGrp><PymtType>Debit</PymtType><TxnType>Sale</TxnType><LocalDateTime>20260707050122</LocalDateTime><TrnmsnDateTime>20260707090122</TrnmsnDateTime><STAN>010507</STAN><RefNum>707050122</RefNum><TPPID>RAU070</TPPID><TermID>00000001</TermID><MerchID>318192</MerchID><MerchCatCode>5541</MerchCatCode><POSEntryMode>051</POSEntryMode><POSCondCode>00</POSCondCode><TermCatCode>12</TermCatCode><TermEntryCapablt>12</TermEntryCapablt><TxnAmt>000000000001</TxnAmt><TxnCrncy>840</TxnCrncy><TermLocInd>0</TermLocInd><CardCaptCap>1</CardCaptCap><GroupID>20001</GroupID><MerchEcho>5f31e78f-8247-486b-9b4e-5641fabb830a</MerchEcho></CommonGrp><CardGrp><Track2Data>67999989000002010=4912201148359490000</Track2Data></CardGrp><PINGrp><PINData>FFFFFFFFFFFFFFFF</PINData><KeySerialNumData>00000000000000000000</KeySerialNumData></PINGrp><AddtlAmtGrp><PartAuthrztnApprvlCapablt>1</PartAuthrztnApprvlCapablt></AddtlAmtGrp><EMVGrp><EMVData>82021800950580000080009A032607079C01005F24034912315F2A0208405F3401119F02060000000001009F03060000000000009F090200029F1A0208409F1E0835323237363431379F2608311B5006DBC083E89F2701809F3303E0F8C89F34034103029F3501229F360200429F37042FD9BC079F3901059F4104000001029F530152840BA0000000043060D05611119F10120110A50009020000000000000000000000FF9F6E07084000003030009F0607A00000000430609F0702FFC05F300202014F0BA0000000043060D0561111</EMVData><CardSeqNum>011</CardSeqNum></EMVGrp></DebitRequest></GMF></Payload></Transaction></Request>

				       			""";

		declinedXml = """
				<?xml version="1.0" encoding="UTF-8"?><Request Version="3" ClientTimeout="30" xmlns="https://prod.dw.us.fdcnet.biz/rc"><ReqClientID><DID>00179018495193182915</DID><App>RAPIDCONNECTSRS</App><Auth>20001440541|00000004</Auth><ClientRef>E365738VRAU070</ClientRef></ReqClientID><Transaction><ServiceID>160</ServiceID><Payload Encoding="xml_escape"><GMF xmlns="com/fiserv/Merchant/gmfV12.04"><DebitRequest><CommonGrp><PymtType>Debit</PymtType><TxnType>Sale</TxnType><LocalDateTime>20260712211702</LocalDateTime><TrnmsnDateTime>20260713011702</TrnmsnDateTime><STAN>072752</STAN><RefNum>712211702</RefNum><TPPID>RAU070</TPPID><TermID>00000004</TermID><MerchID>440541</MerchID><MerchCatCode>5621</MerchCatCode><POSEntryMode>071</POSEntryMode><POSCondCode>00</POSCondCode><TermCatCode>12</TermCatCode><TermEntryCapablt>12</TermEntryCapablt><TxnAmt>000000000776</TxnAmt><TxnCrncy>840</TxnCrncy><TermLocInd>0</TermLocInd><CardCaptCap>1</CardCaptCap><GroupID>20001</GroupID><MerchEcho>95a72701-7e06-4d33-9468-a5a8563fcabd</MerchEcho></CommonGrp><CardGrp><Track2Data>4124880303887507=31122010000018099995</Track2Data></CardGrp><PINGrp><PINData>D19D6BFC67B33F81</PINData><MSKeyID>CEXU211826</MSKeyID></PINGrp><AddtlAmtGrp><PartAuthrztnApprvlCapablt>1</PartAuthrztnApprvlCapablt></AddtlAmtGrp><EMVGrp><EMVData>82020060950500000000009A032607129C01005F24033112315F2A0208405F3401009F02060000000007769F03060000000000009F090200969F1A0208409F1E0832363432353138379F2608697BEDA8BA7EC8889F2701809F3303E068C89F34034203009F3501229F3602003B9F3704539B801C9F3901079F4104000000488407A00000009808409F10201F42016EA00000000010030273000000004000000000000000000000000000009F6E04238800009F0607A00000009808409F0702C0809F660436C040004F07A0000000980840</EMVData><CardSeqNum>000</CardSeqNum></EMVGrp></DebitRequest></GMF></Payload></Transaction></Request>

				""";
				*/
		log.info("processorRequest present, running XML comparison");
		log.info("approvedXml : {}", approvedXml);
		log.info("declinedXml : {}", declinedXml);

		return xmlComparator.getXmlComparator(approvedXml, declinedXml);
	}

	private ExceptionResponse buildExceptionResponse(String code, String message) {
		ExceptionResponse exceptionResponse = new ExceptionResponse();
		exceptionResponse.setCode(code);
		exceptionResponse.setMessage(message);
		return exceptionResponse;
	}

	private CustomResponse buildCustomResponse(String lookupKey, Object message, String status) {
		CustomResponse customResponse = new CustomResponse();
		customResponse.setLookupKey(lookupKey);
		customResponse.setMessage(message);
		customResponse.setStatus(status);
		return customResponse;
	}
}