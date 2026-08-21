package com.auruspay.comparator.controller;

import com.auruspay.comparator.XmlComparator;
import com.auruspay.comparator.XmlComparators;
import com.auruspay.comparator.controller.XmlCompareRequest;
import com.auruspay.comparator.model.ComparisionXmlResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes {@link XmlComparator} over HTTP: accepts the Approved and Declined
 * GMF XML payloads and returns the field-by-field comparison result.
 */
@RestController
@RequestMapping("/api/xml-comparator")
public class XmlComparatorController {

    private static final Logger log = LoggerFactory.getLogger(XmlComparatorController.class);

    private final XmlComparators xmlComparator;

    @Autowired
    public XmlComparatorController(XmlComparators xmlComparator) {
        this.xmlComparator = xmlComparator;
    }

    /**
     * POST /api/xml-comparator/compare
     * Body: { "approvedXml": "...", "declinedXml": "..." }
     */
  //  @PostMapping(value = "/compare", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  //  public ResponseEntity<?> compare(@RequestBody XmlCompareRequest request) {
    	@GetMapping("/xmlcompare") 
    	public ResponseEntity<?> compare() {
/*
        if (request == null
                || request.getApprovedXml() == null || request.getApprovedXml().isBlank()
                || request.getDeclinedXml() == null || request.getDeclinedXml().isBlank()) {
            return ResponseEntity.badRequest()
                    .body("Both approvedXml and declinedXml are required.");
        }
        */
        String approvedXml = """
        		<?xml version="1.0" encoding="UTF-8"?>
<Request Version="3" ClientTimeout="30" xmlns="https://prod.dw.us.fdcnet.biz/rc">
    <ReqClientID>
        <DID>00284285339973382121</DID>
        <App>RAPIDCONNECTSRS</App>
        <Auth>20001314122|00000004</Auth>
        <ClientRef>D722A0AVRAU070</ClientRef>
    </ReqClientID>
    <Transaction>
        <ServiceID>160</ServiceID>
        <Payload Encoding="xml_escape">
            <GMF xmlns="com/fiserv/Merchant/gmfV12.04">
                <DebitRequest>
                    <CommonGrp>
                        <PymtType>Debit</PymtType>
                        <TxnType>Sale</TxnType>
                        <LocalDateTime>20260711214420</LocalDateTime>
                        <TrnmsnDateTime>20260712014420</TrnmsnDateTime>
                        <STAN>094601</STAN>
                        <RefNum>711214420</RefNum>
                        <TPPID>RAU070</TPPID>
                        <TermID>00000004</TermID>
                        <MerchID>314122</MerchID>
                        <MerchCatCode>5621</MerchCatCode>
                        <POSEntryMode>051</POSEntryMode>
                        <POSCondCode>00</POSCondCode>
                        <TermCatCode>12</TermCatCode>
                        <TermEntryCapablt>12</TermEntryCapablt>
                        <TxnAmt>000000014495</TxnAmt>
                        <TxnCrncy>840</TxnCrncy>
                        <TermLocInd>0</TermLocInd>
                        <CardCaptCap>1</CardCaptCap>
                        <GroupID>20001</GroupID>
                        <MerchEcho>9795fb28-c600-4213-b2b4-df1d7c92f93a</MerchEcho>
                    </CommonGrp>
                    <CardGrp>
                        <Track2Data>5143773843009118=270120100100693</Track2Data>
                    </CardGrp>
                    <PINGrp>
                        <PINData>8A0A37B3999882BC</PINData>
                        <MSKeyID>CEXU211826</MSKeyID>
                    </PINGrp>
                    <AddtlAmtGrp>
                        <PartAuthrztnApprvlCapablt>1</PartAuthrztnApprvlCapablt>
                    </AddtlAmtGrp>
                    <EMVGrp>
                        <EMVData>82021800950580000480009A032607119C01005F24032701315F2A0208405F3401019F02060000000000019F03060000000000009F090200029F1A0208409F1E0832373137353333349F260876E4C900C138F4219F2701809F3303E0F8C89F34034203009F3501229F360204D19F37045DAA78C09F3901059F4104000001098407A00000000422039F10120110A00001220000000000000000000000FF9F0607A00000000422034F07A0000000042203</EMVData>
                        <CardSeqNum>001</CardSeqNum>
                    </EMVGrp>
                </DebitRequest>
            </GMF>
        </Payload>
    </Transaction>
</Request>
        			""";

        		String declinedXml = """
        		<?xml version="1.0" encoding="UTF-8"?>
        		<Request Version="3" ClientTimeout="30" xmlns="https://stg.dw.us.fdcnet.biz/rc">
        		    <ReqClientID>
        		        <DID>00066128857378836188</DID>
        		        <App>RAPIDCONNECTSRS</App>
        		        <Auth>20001317621|00000001</Auth>
        		        <ClientRef>4F9B126VRAU070</ClientRef>
        		    </ReqClientID>
        		    <Transaction>
        		        <ServiceID>160</ServiceID>
        		        <Payload Encoding="xml_escape">
        		            <GMF xmlns="com/fiserv/Merchant/gmfV12.04">
        		                <CreditRequest>
        		                    <CommonGrp>
        		                        <PymtType>Credit</PymtType>
        		                        <TxnType>Authorization</TxnType>
        		                        <LocalDateTime>20260725114208</LocalDateTime>
        		                        <TrnmsnDateTime>20260725154208</TrnmsnDateTime>
        		                        <STAN>076536</STAN>
        		                        <RefNum>725114208</RefNum>
        		                        <OrderNum>725154208</OrderNum>
        		                        <TPPID>RAU070</TPPID>
        		                        <TermID>00000001</TermID>
        		                        <MerchID>317621</MerchID>
        		                        <MerchCatCode>5541</MerchCatCode>
        		                        <POSEntryMode>071</POSEntryMode>
        		                        <POSCondCode>02</POSCondCode>
        		                        <TermCatCode>12</TermCatCode>
        		                        <TermEntryCapablt>12</TermEntryCapablt>
        		                        <TxnAmt>000000000500</TxnAmt>
        		                        <TxnCrncy>840</TxnCrncy>
        		                        <TermLocInd>0</TermLocInd>
        		                        <CardCaptCap>1</CardCaptCap>
        		                        <GroupID>20001</GroupID>
        		                        <MerchEcho>973f00f4-350e-4c9a-9319-20e45c74dbee</MerchEcho>
        		                        <TranInit>Customer</TranInit>
        		                    </CommonGrp>
        		                    <CardGrp>
        		                        <Track2Data>374245001741007=301220117021234500000</Track2Data>
        		                        <CardType>Amex</CardType>
        		                    </CardGrp>
        		                    <AddtlAmtGrp>
        		                        <PartAuthrztnApprvlCapablt>0</PartAuthrztnApprvlCapablt>
        		                    </AddtlAmtGrp>
        		                    <EMVGrp>
        		                        <EMVData>82021C80950580000080009A032607259C01005F24033012315F2A0208405F3401009F02060000000000019F03060000000000009F090200019F1A0208409F1E0830373837343734369F26088E1A2261C97F2CDB9F2701809F3303E068C89F34031F03009F3501229F360200159F370442AA15AA9F3901079F410201049F5301528408A0000000250104029F100706020103A0B0009F6E04D8E000039F0606A000000025019F07023D004F08A0000000250104025F30020201</EMVData>
        		                        <CardSeqNum>000</CardSeqNum>
        		                    </EMVGrp>
        		                </CreditRequest>
        		            </GMF>
        		        </Payload>
        		    </Transaction>
        		</Request>
        		""";
        try {
            ComparisionXmlResult result = xmlComparator.getXmlComparator(approvedXml,declinedXml);
                 //   request.getApprovedXml(), request.getDeclinedXml());
            return ResponseEntity.ok(result.getXmlValidationIssue());
        } catch (Exception e) {
            log.error("XML comparison failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to compare XML payloads: " + e.getMessage());
        }
    }
}