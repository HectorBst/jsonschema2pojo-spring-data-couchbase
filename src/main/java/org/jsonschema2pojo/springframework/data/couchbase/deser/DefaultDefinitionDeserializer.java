package org.jsonschema2pojo.springframework.data.couchbase.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.io.Serializable;

/**
 * Custom deserializer returning a default POJO when only a true boolean is specified in schema.
 *
 * @author Hector Basset
 */
public class DefaultDefinitionDeserializer<T extends Serializable> extends StdDeserializer<T> implements ResolvableDeserializer {

	private final StdDeserializer<?> defaultDeserializer;
	private final T defaultDefinition;

	public DefaultDefinitionDeserializer(StdDeserializer<?> defaultDeserializer, T defaultDefinition) {
		super(defaultDeserializer);
		this.defaultDeserializer = defaultDeserializer;
		this.defaultDefinition = defaultDefinition;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		if (p.getCurrentToken() == null || (p.getCurrentToken().isBoolean() && !p.getBooleanValue())) {
			return null;
		} else if (p.getCurrentToken().isBoolean() && p.getBooleanValue()) {
			return defaultDefinition;
		} else {
			return (T) defaultDeserializer.deserialize(p, ctxt);
		}
	}

	@Override
	public void resolve(DeserializationContext ctxt) throws JsonMappingException {
		((ResolvableDeserializer) defaultDeserializer).resolve(ctxt);
	}
}
