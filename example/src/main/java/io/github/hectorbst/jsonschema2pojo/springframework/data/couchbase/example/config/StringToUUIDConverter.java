package io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.example.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.util.UUID;

/**
 * @author Hector Basset
 */
@ReadingConverter
public class StringToUUIDConverter implements Converter<String, UUID> {

	@Override
	public UUID convert(String source) {
		return UUID.fromString(source);
	}
}
