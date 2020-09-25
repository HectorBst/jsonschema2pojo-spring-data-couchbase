package org.jsonschema2pojo.springframework.data.couchbase.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JFieldVar;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.rules.Rule;
import org.jsonschema2pojo.springframework.data.couchbase.definitions.FieldDef;
import org.jsonschema2pojo.springframework.data.couchbase.definitions.IndexDef;
import org.springframework.data.couchbase.core.index.QueryIndexDirection;
import org.springframework.data.couchbase.core.index.QueryIndexed;
import org.springframework.data.couchbase.core.mapping.Field;
import org.springframework.data.couchbase.core.mapping.id.IdAttribute;
import org.springframework.data.couchbase.core.mapping.id.IdPrefix;
import org.springframework.data.couchbase.core.mapping.id.IdSuffix;

import java.util.Optional;

import static org.jsonschema2pojo.springframework.data.couchbase.util.SpringDataCouchbaseHelper.JSON_KEY_INTERNAL_FIELD_EXCLUSIVITY;
import static org.jsonschema2pojo.springframework.data.couchbase.util.SpringDataCouchbaseHelper.handleFieldMetadataExclusivity;

/**
 * @author Hector Basset
 */
public class CouchbaseFieldRule implements Rule<JFieldVar, JFieldVar> {

	protected static final String JSON_KEY_FIELD = "x-cb-field";

	protected final SpringDataCouchbaseRuleFactory ruleFactory;

	public CouchbaseFieldRule(SpringDataCouchbaseRuleFactory ruleFactory) {
		this.ruleFactory = ruleFactory;
	}

	@Override
	public JFieldVar apply(String nodeName, JsonNode node, JsonNode parent, JFieldVar field, Schema schema) {
		Optional.of(node.path(JSON_KEY_FIELD))
				.map(n -> {
					//Unlike other property metadata, field is true by default if all others are false, so we return
					//true if the value is null or missing
					if ((n.isMissingNode() || n.isNull()) && node.get(JSON_KEY_INTERNAL_FIELD_EXCLUSIVITY) == null) {
						return ruleFactory.getObjectMapper().getNodeFactory().booleanNode(true);
					} else {
						return n;
					}
				})
				.map(n -> ruleFactory.getObjectMapper().convertValue(n, FieldDef.class))
				.ifPresent(fieldDef -> {
					handleFieldMetadataExclusivity(nodeName, node, "field");

					JAnnotationUse annotation = field.annotate(Field.class);

					Optional.ofNullable(fieldDef.getName())
							.ifPresent(name -> annotation.param("name", name));

					Optional.ofNullable(fieldDef.getOrder())
							.ifPresent(order -> annotation.param("order", order));

					Optional.ofNullable(fieldDef.getIdPrefix())
							.ifPresent(idPrefix -> {
								JAnnotationUse ann = field.annotate(IdPrefix.class);

								Optional.ofNullable(idPrefix.getOrder())
										.ifPresent(order -> ann.param("order", order));
							});

					Optional.ofNullable(fieldDef.getIdAttribute())
							.ifPresent(idAttribute -> {
								JAnnotationUse ann = field.annotate(IdAttribute.class);

								Optional.ofNullable(idAttribute.getOrder())
										.ifPresent(order -> ann.param("order", order));
							});

					Optional.ofNullable(fieldDef.getIdSuffix())
							.ifPresent(idSuffix -> {
								JAnnotationUse ann = field.annotate(IdSuffix.class);

								Optional.ofNullable(idSuffix.getOrder())
										.ifPresent(order -> ann.param("order", order));
							});

					handleIndex(field, fieldDef.getIndex());
				});

		return field;
	}

	protected void handleIndex(JFieldVar field, IndexDef indexDef) {
		Optional.ofNullable(indexDef)
				.ifPresent(index -> {
					JCodeModel owner = field.type().owner();

					JAnnotationUse ann = field.annotate(QueryIndexed.class);

					Optional.ofNullable(index.getDirection())
							.ifPresent(direction -> ann.param("direction", owner.ref(QueryIndexDirection.class).staticRef(direction.name())));

					Optional.ofNullable(index.getName())
							.ifPresent(name -> ann.param("name", name));
				});
	}
}
