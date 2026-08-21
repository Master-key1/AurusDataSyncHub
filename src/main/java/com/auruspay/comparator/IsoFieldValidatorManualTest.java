package com.auruspay.comparator;

import com.auruspay.comparator.config.IsoFieldDefinitionLoader;
import com.auruspay.comparator.model.IsoFieldDefinition;
import com.auruspay.comparator.model.ValidateResult;
import com.auruspay.util.ExtractMultipleKeywords;

import java.util.List;

/**
 * Standalone smoke test — run this as a plain Java application (no Spring
 * context required) to verify:
 *   1. The ISO field XML on the classpath loads without errors and every
 *      field you care about actually resolves to a definition.
 *   2. JsonFieldValidator.validateAll(...) produces sane results against a
 *      couple of sample declined/approved payloads.
 *
 * HOW TO RUN:
 *   - Make sure static/iso-fields-definition.xml is on the classpath (e.g.
 *     src/main/resources/static/iso-fields-definition.xml, or
 *     src/test/resources/static/iso-fields-definition.xml).
 *   - Run this class's main() method directly from your IDE, or:
 *       mvn compile exec:java -Dexec.mainClass=com.auruspay.comparator.IsoFieldValidatorManualTest
 *
 * This class has no @Component/@SpringBootApplication annotations on
 * purpose — it's meant to be a quick, dependency-free sanity check you run
 * locally, not something wired into the running application.
 */
public class IsoFieldValidatorManualTest {
	
	 private static ExtractMultipleKeywords extractMultipleKeywords;
	 

    public IsoFieldValidatorManualTest(ExtractMultipleKeywords extractMultipleKeywords) {
		super();
		this.extractMultipleKeywords = extractMultipleKeywords;
	}


	public static void main(String[] args) throws Exception {
        System.out.println("=== 1. Loading ISO field definitions ===");
        IsoFieldDefinitionLoader loader = new IsoFieldDefinitionLoader();
        loader.loadDefinitions(); // normally called by Spring's @PostConstruct; called manually here

        System.out.println();
        System.out.println("=== 2. Spot-checking specific field ids ===");
        // Add/remove ids here as needed — this is the fastest way to confirm
        // a specific field (like the "2.1 shows UNKNOWN FIELD" case) is
        // actually present in whatever XML file is on the classpath right now.
        String[] idsToCheck = {
                "0", "1.1", "2.1", "4.5", "6.9", "11.1", "12.1", "73.3", "12.271.1", "does-not-exist"
        };
        for (String id : idsToCheck) {
            IsoFieldDefinition def = loader.getField(id);
            if (def == null) {
                System.out.printf("  id=%-15s -> NOT FOUND%n", id);
            } else {
                System.out.printf("  id=%-15s -> name='%s', classType=%s, minLength=%d, maxLength=%d, value=%s%n",
                        id, def.getName(), def.getClassType(), def.getMinLength(), def.getMaxLength(), def.getValue());
            }
        }

        System.out.println();
        System.out.println("=== 3. Running JsonFieldValidator against a sample declined/approved pair ===");
       
        JsonFieldValidator validator = new JsonFieldValidator(loader, extractMultipleKeywords);

        // Sample payloads — edit these to reproduce whatever real-world case
        // you're debugging. Field "2.1" included here specifically to verify
        // the "UNKNOWN FIELD" issue is resolved.
        String declinedJson = "{"
                + "\"0\": \"1800\","
                + "\"1.1\": \"100000090522\","
                + "\"2.1\": \"C0N43N4U45RLN8R1\","
                + "\"4.5\": \"10.00\","
                + "\"6.9\": \"90210\","
                + "\"73.3\": \"9\""
                + "}";

        String approvedJson = "{"
                + "\"0\": \"1800\","
                + "\"1.1\": \"101231234030\","
                + "\"2.1\": \"809775605\","
                + "\"4.5\": \"10.00\","
                + "\"6.9\": \"90210-1234\","
                + "\"73.3\": \"1\""
                + "}";

        List<ValidateResult> results = validator.validateAll(declinedJson, approvedJson);

        System.out.println();
        System.out.printf("Total results returned: %d%n", results.size());
        System.out.println();
        for (ValidateResult r : results) {
            System.out.printf(
                    "Field=%-10s FieldName=%-25s Value=%-9s Pattern=%-9s Length=%-8s%n    Declined=%-20s Approved=%-20s%n    Summary=%s%n%n",
                    r.getField(), r.getFieldName(), r.getValue(), r.getPattern(), r.getLength(),
                    r.getDeclinedValue(), r.getApprovedValue(), r.getSummary()
            );
        }

        System.out.println("=== Done ===");
    }
}