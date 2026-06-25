package com.auruspay.comparator.config;

import com.auruspay.comparator.model.XmlFieldDefinition;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Component
public class XmlFieldDefinitionLoader {

	private final Map<String, XmlFieldDefinition> fieldDefinitions = new HashMap<>();

	@PostConstruct
	public void loadDefinitions() {

		try (InputStream is = getClass().getClassLoader().getResourceAsStream("xmlFields.xml")) {

			if (is == null) {
				throw new RuntimeException("xmlFields.xml not found in resources folder");
			}

			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);

			doc.getDocumentElement().normalize();

			NodeList nl = doc.getElementsByTagName("xmlfield");

			for (int i = 0; i < nl.getLength(); i++) {

				Element el = (Element) nl.item(i);

				XmlFieldDefinition def = new XmlFieldDefinition();

				def.setId(el.getAttribute("id"));

				def.setPattern(el.getAttribute("pattern"));

				def.setClassType(el.getAttribute("classType"));

				def.setFailedMsg(el.getAttribute("failedMsg"));

				fieldDefinitions.put(def.getId(), def);
			}

			System.out.println("Loaded XML Fields : " + fieldDefinitions.size());

		} catch (Exception e) {

			e.printStackTrace();

			throw new RuntimeException("Failed to load xmlFields.xml", e);
		}
	}

	public XmlFieldDefinition getDefinition(String id) {

		return fieldDefinitions.get(id);
	}

	public Map<String, XmlFieldDefinition> getAllDefinitions() {

		return fieldDefinitions;
	}

}
