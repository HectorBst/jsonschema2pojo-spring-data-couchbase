package io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JFieldVar;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.SpringDataCouchbaseRuleFactory;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.GeneratedDef;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.IdDef;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.rules.Rule;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.id.GeneratedValue;
import org.springframework.data.couchbase.core.mapping.id.GenerationStrategy;

import java.util.Optional;

import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.util.SpringDataCouchbaseHelper.handleFieldMetadataExclusivity;

/**
 * @author Hector Basset
 */
public class CouchbaseIdRule implements Rule<JFieldVar, JFieldVar> {

	protected static final String JSON_KEY_ID = "x-cb-id";

	protected final SpringDataCouchbaseRuleFactory ruleFactory;

	public CouchbaseIdRule(SpringDataCouchbaseRuleFactory ruleFactory) {
		this.ruleFactory = ruleFactory;
	}

	@Override
	public JFieldVar apply(String nodeName, JsonNode node, JsonNode parent, JFieldVar field, Schema schema) {
		Optional.of(node.path(JSON_KEY_ID))
				.map(n -> ruleFactory.getObjectMapper().convertValue(n, IdDef.class))
				.ifPresent(id -> {
					handleFieldMetadataExclusivity(nodeName, node, "id");

					field.annotate(Id.class);

					handleGeneratedValue(field, id.getGenerated());
				});

		return field;
	}

	protected void handleGeneratedValue(JFieldVar field, GeneratedDef generated) {
		Optional.ofNullable(generated)
				.ifPresent(generatedValue -> {
					JCodeModel owner = field.type().owner();

					JAnnotationUse annotation = field.annotate(GeneratedValue.class);

					Optional.ofNullable(generatedValue.getDelimiter())
							.ifPresent(delimiter -> annotation.param("delimiter", delimiter));

					Optional.ofNullable(generatedValue.getStrategy())
							.ifPresent(strategy -> annotation.param("strategy", owner.ref(GenerationStrategy.class).staticRef(strategy.name())));
				});
	}
}
