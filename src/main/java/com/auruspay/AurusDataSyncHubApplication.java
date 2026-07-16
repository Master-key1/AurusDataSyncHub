package com.auruspay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AurusDataSyncHubApplication {

	public static void main(String[] args) {
		   System.setProperty("PID", String.valueOf(ProcessHandle.current().pid()));
		SpringApplication.run(AurusDataSyncHubApplication.class, args);
	}

}

/*

package com.auruspay;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.auruspay.comparator.CctComparator;
import com.auruspay.comparator.XmlComparator;
import com.auruspay.dto.ProcessRequest;
import com.auruspay.service.TransactionLookupService;

@SpringBootApplication
public class AurusDataSyncHubApplication {

    public static void main(String[] args) {

        ConfigurableApplicationContext ctx =
                SpringApplication.run(AurusDataSyncHubApplication.class, args);

        // ✅ GET BEANS FROM SPRING CONTEXT
        TransactionLookupService lookupService =
                ctx.getBean(TransactionLookupService.class);

        XmlComparator xmlComparator =
                ctx.getBean(XmlComparator.class);

        CctComparator cctComparator =
                ctx.getBean(CctComparator.class);

        try {

            ProcessRequest declinedRequest = new ProcessRequest();
            
            declinedRequest.setCctRequest("\n"
            		+ "{\"6.9\":\"12345\",\"4.11\":\"150.00\",\"4.13\":\"134001\",\"4.15\":\"1\",\"14.13\":\"1\",\"3.43\":\"6\",\"4.20\":\"840\",\"4.21\":\"840\",\"4.26\":\"1\",\"4.113\":\"1\",\"12.1\":\"0000002\",\"1.1\":\"100000139336\",\"1.2\":\"79794\",\"1.3\":\"59806551\",\"3.1\":\"17\",\"3.2\":\"AESDK\",\"3.3\":\"Reg01\",\"5.1\":\"0000002\",\"3.4\":\"00\",\"4.18\":\"05142026\",\"3.5\":\"26.03.065.001\",\"4.19\":\"202022\",\"5.3\":\"0000002\",\"7.1\":\"0.00\",\"3.6\":\"1.8\",\"7.2\":\"0.00\",\"3.7\":\"5.3.18-57-default\",\"7.3\":\"0.00\",\"3.8\":\"1\",\"7.5\":\"%1F%1F%1D\",\"5.8\":\"5.0874\",\"5.9\":\"01\",\"4.32\":\"3\",\"7.9\":\"%1F%1F%1D\",\"12.55\":\"10001\",\"4.79\":\"1\",\"4.38\":\"26.03.065.001\",\"3.21\":\"1.91\",\"4.42\":\"P2PE NOT SUPPORTED\",\"5.13\":\"01\",\"5.14\":\"01\",\"4.40\":\"00\",\"2.1\":\"C0N43N4U45RLN8R1\",\"2.2\":\"60:33:4b:10:42:98\",\"2.3\":\"192.168.255.243\",\"4.1\":\"3\",\"4.2\":\"000002\",\"4.3\":\"4\",\"4.4\":\"4761730111160043%5EUAT+USA%2FTEST+CARD+02++++++%5E3112201114380440000000000000000%7E4761730111160043%3D31122011303130600000\",\"4.5\":\"150.00\",\"4.6\":\"0.00\",\"4.7\":\"0.00\"}");

            declinedRequest.setProcessorRequest("<GMF xmlns=\"com/fiserv/Merchant/gmfV10.02\"><CreditRequest><CommonGrp><PymtType>Credit</PymtType><TxnType>Authorization</TxnType><LocalDateTime>20260514202026</LocalDateTime><TrnmsnDateTime>20260515002026</TrnmsnDateTime><STAN>088789</STAN><RefNum>514202026</RefNum><OrderNum>0000002</OrderNum><TPPID>RAU053</TPPID><TermID>00000001</TermID><MerchID>RD13317808</MerchID><MerchCatCode>5294</MerchCatCode><POSEntryMode>901</POSEntryMode><POSCondCode>00</POSCondCode><TermCatCode>05</TermCatCode><TermEntryCapablt>12</TermEntryCapablt><TxnAmt>000000015000</TxnAmt><TxnCrncy>840</TxnCrncy><TermLocInd>0</TermLocInd><CardCaptCap>1</CardCaptCap><GroupID>20001</GroupID><POSID>0001</POSID><MerchEcho>4936cd0c-f0cb-4c19-8622-b3a248028445</MerchEcho></CommonGrp><CardGrp><Track2Data>4761730111160043=31122011303130600000</Track2Data><CardType>Visa</CardType></CardGrp><AddtlAmtGrp><PartAuthrztnApprvlCapablt>1</PartAuthrztnApprvlCapablt></AddtlAmtGrp><VisaGrp><ACI>Y</ACI></VisaGrp><CustInfoGrp><AVSBillingPostalCode>12345</AVSBillingPostalCode></CustInfoGrp></CreditRequest></GMF>");
            declinedRequest.setProcessorResponse("FDk<GMF xmlns=\"com/fiserv/Merchant/gmfV10.02\"><CreditResponse><CommonGrp><PymtType>Credit</PymtType><TxnType>Authorization</TxnType><LocalDateTime>20260514202026</LocalDateTime><TrnmsnDateTime>20260515002026</TrnmsnDateTime><STAN>088789</STAN><RefNum>514202026</RefNum><OrderNum>0000002</OrderNum><TermID>00000001</TermID><MerchID>RD13317808</MerchID><TxnAmt>000000015000</TxnAmt><TxnCrncy>840</TxnCrncy><POSID>0001</POSID><MerchEcho>4936cd0c-f0cb-4c19-8622-b3a248028445</MerchEcho><AcctTypeID>Unspecified</AcctTypeID></CommonGrp><CardGrp><AVSResultCode>N</AVSResultCode></CardGrp><VisaGrp><CardLevelResult>A </CardLevelResult><SourceReasonCode>4</SourceReasonCode><TransID>07561350122733650112</TransID></VisaGrp><RespGrp><RespCode>721</RespCode><AddtlRespData>INVALID ZIP  BE</AddtlRespData><AthNtwkID>02</AthNtwkID><AthNtwkNm>VISA</AthNtwkNm></RespGrp></CreditResponse></GMF>FD");
            declinedRequest.setCctResponse("{\"4.11\":\"150.00\",\"4.13\":\"134001\",\"71.1\":\"0\",\"72.18\":\"476173XXXXXX0043\",\"72.16\":\"\",\"71.2\":\"0\",\"72.17\":\"01\",\"72.14\":\"0043\",\"72.12\":\"\",\"72.13\":\"\",\"72.10\":\"0\",\"72.11\":\"0.00\",\"72.55\":\"\",\"72.132\":\"1\",\"72.133\":\"0\",\"72.127\":\"00000001\",\"72.129\":\"0\",\"4.20\":\"840\",\"4.21\":\"840\",\"12.89\":\"\",\"72.28\":\"RD13317808\",\"72.69\":\"VISA\",\"71.11\":\"\",\"72.67\":\"N\",\"4.113\":\"1\",\"72.21\":\"721\",\"75.13\":\"1231\",\"4.4.4\":\"UAT USA TEST CARD 02\",\"12.1\":\"0000002\",\"1.1\":\"100000139336\",\"1.2\":\"79794\",\"1.3\":\"59806551\",\"5.1\":\"0000002\",\"71.5\":\"\",\"3.4\":\"00\",\"4.18\":\"05142026\",\"71.4\":\"4761730111160043\",\"4.19\":\"202027\",\"5.3\":\"0000002\",\"7.9\":\"%1F%1F%1D\",\"72.2\":\"006\",\"72.1\":\"VIC\",\"4.102\":\"514202026\",\"4.4.2\":\"E+JSqCMvjaFlW9x8JhxWng==\",\"3.21\":\"1.91\",\"72.109\":\"C\",\"4.29\":\"\",\"12.64\":\"30\",\"5.13\":\"01\",\"72.87\":\"\",\"4.40\":\"00\",\"72.41\":\"0\",\"72.84\":\"0000002\",\"11.1\":\"4936cd0c-f0cb-4c19-8622-b3a248028445\",\"4.1\":\"3\",\"4.2\":\"000002\",\"72.4\":\"0.00\",\"4.3\":\"2\",\"72.3\":\"000000\",\"72.6\":\"INVALID ZIP CODE\",\"4.5\":\"150.00\",\"72.5\":\"0.00\",\"4.6\":\"0.00\",\"72.7\":\"N\",\"72.9\":\"292261347322755443\"}");
            // ================= FETCH APPROVED DATA =================
            ProcessRequest approvedRequest =
                    lookupService.lookupTransaction(declinedRequest);

            // ================= XML COMPARISON =================
            String approvedXml = approvedRequest.getProcessorRequest();
            String declinedXml = declinedRequest.getProcessorRequest();

            List<Map<String, String>> xmlComparedData =
                    xmlComparator.getXmlComparator(approvedXml, declinedXml);

            // ================= JSON/CCT COMPARISON =================
            String approvedJson = approvedRequest.getCctRequest();
            String declinedJson = declinedRequest.getCctRequest();

            List<Map<String, String>> cctComparedData =
                    cctComparator.compare(declinedJson, approvedJson);

            // ================= OUTPUT =================
            System.out.println("\n===== XML COMPARISON =====");
            xmlComparedData.forEach(System.out::println);

            System.out.println("\n===== CCT COMPARISON =====");
            cctComparedData.forEach(System.out::println);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

*/