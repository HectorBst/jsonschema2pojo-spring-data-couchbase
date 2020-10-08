package io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.SpringDataCouchbaseRuleFactory;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.FieldDef;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.GeneratedDef;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.IdAttributeDef;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.IdDef;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.IdPrefixDef;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.IdSuffixDef;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.IndexDef;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.util.Definition;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.rules.PropertyRule;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.couchbase.core.index.QueryIndexed;
import org.springframework.data.couchbase.core.mapping.Field;
import org.springframework.data.couchbase.core.mapping.id.GeneratedValue;
import org.springframework.data.couchbase.core.mapping.id.IdAttribute;
import org.springframework.data.couchbase.core.mapping.id.IdPrefix;
import org.springframework.data.couchbase.core.mapping.id.IdSuffix;

import java.util.Optional;

import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.util.Definition.CAS;
import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.util.Definition.FIELD;
import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.util.Definition.ID;
import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.util.Definition.ID_PREFIX;
import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.util.Definition.ID_SUFFIX;

/**
 * Override of the default {@link PropertyRule} to apply Couchbase related elements.
 *
 * @author Hector Basset
 */
public class CouchbasePropertyRule extends PropertyRule {

	private final SpringDataCouchbaseRuleFactory ruleFactory;

	public CouchbasePropertyRule(SpringDataCouchbaseRuleFactory ruleFactory) {
		super(ruleFactory);
		this.ruleFactory = ruleFactory;
	}

	@Override
	public JDefinedClass apply(String nodeName, JsonNode node, JsonNode parent, JDefinedClass clazz, Schema schema) {
		Definition.setAllMissingValues(schema);

		clazz = super.apply(nodeName, node, parent, clazz, schema);

		String propertyName = ruleFactory.getNameHelper().getPropertyName(nodeName, node);
		JFieldVar field = clazz.fields().get(propertyName);

		handleCouchbase(clazz, field, schema);

		return clazz;
	}

	protected void handleCouchbase(JDefinedClass clazz, JFieldVar field, Schema schema) {
		handleCouchbaseCas(clazz, field, schema);
		handleCouchbaseId(clazz, field, schema);
		handleCouchbaseField(clazz, field, schema);
		handleCouchbaseIdPrefix(clazz, field, schema);
		handleCouchbaseIdSuffix(clazz, field, schema);
	}

	protected void handleCouchbaseCas(JDefinedClass clazz, JFieldVar field, Schema schema) {
		if (field != null && CAS.is(schema)) {
			field.annotate(Version.class);
		}
	}

	protected void handleCouchbaseField(JDefinedClass clazz, JFieldVar field, Schema schema) {
		if (field != null && FIELD.is(schema)) {
			FieldDef fieldDef = FIELD.get(schema, ruleFactory);

			JAnnotationUse annotation = field.annotate(Field.class);

			Optional.ofNullable(fieldDef.getName())
					.ifPresent(name -> annotation.param("name", name));

			Optional.ofNullable(fieldDef.getOrder())
					.ifPresent(order -> annotation.param("order", order));

			handleFieldIdAttribute(clazz, field, fieldDef.getIdAttribute());

			handleFieldIndex(clazz, field, fieldDef.getIndex());
		}
	}

	protected void handleFieldIdAttribute(JDefinedClass clazz, JFieldVar field, IdAttributeDef idAttributeDef) {
		Optional.ofNullable(idAttributeDef)
				.ifPresent(idAttribute -> {
					JAnnotationUse annotation = field.annotate(IdAttribute.class);

					Optional.ofNullable(idAttribute.getOrder())
							.ifPresent(order -> annotation.param("order", order));
				});
	}

	protected void handleFieldIndex(JDefinedClass clazz, JFieldVar field, IndexDef indexDef) {
		Optional.ofNullable(indexDef)
				.ifPresent(index -> {
					JAnnotationUse annotation = field.annotate(QueryIndexed.class);

					Optional.ofNullable(index.getDirection())
							.ifPresent(direction -> annotation.param("direction", direction));

					Optional.ofNullable(index.getName())
							.ifPresent(name -> annotation.param("name", name));
				});
	}

	protected void handleCouchbaseId(JDefinedClass clazz, JFieldVar field, Schema schema) {
		if (field != null && ID.is(schema)) {
			IdDef id = ID.get(schema, ruleFactory);

			field.annotate(Id.class);

			handleIdGenerated(clazz, field, id.getGenerated());
		}
	}

	protected void handleIdGenerated(JDefinedClass clazz, JFieldVar field, GeneratedDef generatedDef) {
		Optional.ofNullable(generatedDef)
				.ifPresent(generated -> {
					JAnnotationUse annotation = field.annotate(GeneratedValue.class);

					Optional.ofNullable(generated.getDelimiter())
							.ifPresent(delimiter -> annotation.param("delimiter", delimiter));

					Optional.ofNullable(generated.getStrategy())
							.ifPresent(strategy -> annotation.param("strategy", strategy));
				});
	}

	protected void handleCouchbaseIdPrefix(JDefinedClass clazz, JFieldVar field, Schema schema) {
		if (field != null && ID_PREFIX.is(schema)) {
			IdPrefixDef idPrefix = ID_PREFIX.get(schema, ruleFactory);

			JAnnotationUse annotation = field.annotate(IdPrefix.class);

			Optional.ofNullable(idPrefix.getOrder())
					.ifPresent(order -> annotation.param("order", order));
		}
	}

	protected void handleCouchbaseIdSuffix(JDefinedClass clazz, JFieldVar field, Schema schema) {
		if (field != null && ID_SUFFIX.is(schema)) {
			IdSuffixDef idSuffix = ID_SUFFIX.get(schema, ruleFactory);

			JAnnotationUse annotation = field.annotate(IdSuffix.class);

			Optional.ofNullable(idSuffix.getOrder())
					.ifPresent(order -> annotation.param("order", order));
		}
	}
}
