package com.auruspay.comparator.validation;

import java.lang.reflect.Field;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DTOMapper {
	  private static final Logger log =
	            LoggerFactory.getLogger(DTOMapper.class);

	public static <T> T mapToDTO(Map<String, String> map, Class<T> clazz) {
		try {

			log.info("Starting DTO mapping for class: {}", clazz.getSimpleName());

			T dto = clazz.getDeclaredConstructor().newInstance();

			for (Field field : clazz.getDeclaredFields()) {

				field.setAccessible(true);

				String fieldName = field.getName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();

				for (Map.Entry<String, String> entry : map.entrySet()) {

					String tagName = entry.getKey().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();

					if (fieldName.equals(tagName)) {

						log.info(" [{}] -> [{}] = [{}]", entry.getKey(), field.getName(),
								entry.getValue());

						field.set(dto, entry.getValue());
						break;
					}
				}
			}

			log.info("DTO mapping completed successfully: {}.",dto.getClass());

			
			return dto;

		} catch (Exception e) {

			log.error("Error while mapping XML data to DTO: {}", clazz.getSimpleName(), e);

			throw new RuntimeException(e);
		}
	}
}