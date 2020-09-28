package org.jsonschema2pojo.springframework.data.couchbase.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JFieldVar;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.rules.Rule;
import org.jsonschema2pojo.springframework.data.couchbase.definitions.CasDef;
import org.springframework.data.annotation.Version;

import java.util.Optional;

import static org.jsonschema2pojo.springframework.data.couchbase.util.SpringDataCouchbaseHelper.handleFieldMetadataExclusivity;

/**
 * @author Hector Basset
 */
public class CouchbaseCasRule implements Rule<JFieldVar, JFieldVar> {

	protected static final String JSON_KEY_CAS = "x-cb-cas";

	protected final SpringDataCouchbaseRuleFactory ruleFactory;

	public CouchbaseCasRule(SpringDataCouchbaseRuleFactory ruleFactory) {
		this.ruleFactory = ruleFactory;
	}

	@Override
	public JFieldVar apply(String nodeName, JsonNode node, JsonNode parent, JFieldVar field, Schema schema) {
		Optional.of(node.path(JSON_KEY_CAS))
				.map(n -> ruleFactory.getObjectMapper().convertValue(n, CasDef.class))
				.ifPresent(id -> {
					handleFieldMetadataExclusivity(nodeName, node, "cas");

					field.annotate(Version.class);
				});

		return field;
	}
}
