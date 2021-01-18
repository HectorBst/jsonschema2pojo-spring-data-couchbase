package dev.hctbst.jsonschema2pojo.springframework.data.couchbase.example.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.util.UUID;

/**
 * @author Hector Basset
 */
@WritingConverter
public class UUIDToStringConverter implements Converter<UUID, String> {

	@Override
	public String convert(UUID source) {
		return source.toString();
	}
}
