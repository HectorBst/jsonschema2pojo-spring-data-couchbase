package io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.SpringDataCouchbaseRuleFactory;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.DocumentDef;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.FieldDef;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.IdDef;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.IdPrefixDef;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.IdSuffixDef;
import org.jsonschema2pojo.Schema;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.Predicate;

/**
 * @author Hector Basset
 */
public enum Definition {

	DOCUMENT("document", schema -> schema.getParent() == schema, DocumentDef.class),
	CAS("cas", schema -> false, null),
	ID("id", schema -> false, IdDef.class),
	ID_PREFIX("idPrefix", schema -> false, IdPrefixDef.class),
	ID_SUFFIX("idSuffix", schema -> false, IdSuffixDef.class),
	FIELD("field", schema ->
			!CAS.is(schema) &&
					!DOCUMENT.is(schema) &&
					!ID.is(schema) &&
					!ID_PREFIX.is(schema) &&
					!ID_SUFFIX.is(schema) &&
					(schema != schema.getParent()) &&
					(DOCUMENT.is(schema.getParent()) || Definition.valueOf("FIELD").is(schema.getParent())),
			FieldDef.class
	);

	protected static final String JSON_KEY_PREFIX = "x-cb-";

	Definition(
			String jsonKeySuffix,
			Predicate<Schema> defaultValueGetter,
			Class<? extends Serializable> pojoClass
	) {
		this.jsonKey = JSON_KEY_PREFIX + jsonKeySuffix;
		this.defaultValueGetter = defaultValueGetter;
		this.pojoClass = pojoClass;
	}

	private final String jsonKey;
	private final Predicate<Schema> defaultValueGetter;
	private final Class<? extends Serializable> pojoClass;

	public static void setAllMissingValues(Schema schema) {
		Arrays.stream(values()).forEach(def -> def.setMissing(schema));
	}

	public String getJsonKey() {
		return jsonKey;
	}

	public Predicate<Schema> getDefaultValueGetter() {
		return defaultValueGetter;
	}

	public boolean is(Schema schema) {
		JsonNode value = schema.getContent().path(jsonKey);

		if (value.isMissingNode() || value.isNull()) {
			return defaultValueGetter.test(schema);
		} else if (value.isBoolean()) {
			return value.asBoolean();
		} else if (value.isObject()) {
			return true;
		} else {
			throw new IllegalArgumentException("Invalid JSON value: \"" + value + "\", expecting a boolean or an object");
		}
	}

	public void setMissing(Schema schema) {
		ObjectNode content = (ObjectNode) schema.getContent();
		JsonNode value = content.path(jsonKey);

		if (value.isMissingNode() || value.isNull()) {
			content.put(jsonKey, defaultValueGetter.test(schema));
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends Serializable> T get(Schema schema, SpringDataCouchbaseRuleFactory ruleFactory) {
		return (T) ruleFactory.getObjectMapper().convertValue(schema.getContent().get(jsonKey), pojoClass);
	}
}
