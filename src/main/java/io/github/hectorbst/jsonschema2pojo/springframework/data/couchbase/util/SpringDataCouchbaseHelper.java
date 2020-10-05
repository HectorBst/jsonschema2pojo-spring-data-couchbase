package io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Optional;

/**
 * @author Hector Basset
 */
public interface SpringDataCouchbaseHelper {

	String JSON_KEY_INTERNAL_FIELD_EXCLUSIVITY = "x-cb-internal-field-exclusivity";

	static void handleFieldMetadataExclusivity(String fieldName, JsonNode node, String actual) {
		Optional.ofNullable(node.get(JSON_KEY_INTERNAL_FIELD_EXCLUSIVITY))
				.map(JsonNode::asText)
				.ifPresent(old -> {
					String message = String.format(
							"Incoherent Couchbase id/cas/join/field combination for property \"%s\", a property cannot be \"%s\" and \"%s\"",
							fieldName,
							old,
							actual
					);
					throw new IllegalArgumentException(message);
				});

		((ObjectNode) node).put(JSON_KEY_INTERNAL_FIELD_EXCLUSIVITY, actual);
	}
}
