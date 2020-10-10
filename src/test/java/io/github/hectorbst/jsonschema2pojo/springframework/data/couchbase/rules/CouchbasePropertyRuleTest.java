package io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.rules;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMod;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.SpringDataCouchbaseRuleFactory;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.GeneratedDef;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.IdAttributeDef;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.IndexDef;
import org.jsonschema2pojo.Schema;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.couchbase.core.index.QueryIndexDirection;
import org.springframework.data.couchbase.core.index.QueryIndexed;
import org.springframework.data.couchbase.core.mapping.Field;
import org.springframework.data.couchbase.core.mapping.id.GeneratedValue;
import org.springframework.data.couchbase.core.mapping.id.GenerationStrategy;
import org.springframework.data.couchbase.core.mapping.id.IdAttribute;
import org.springframework.data.couchbase.core.mapping.id.IdPrefix;
import org.springframework.data.couchbase.core.mapping.id.IdSuffix;

import java.net.URI;

import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.tests.TestsUtil.clazz;
import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.util.Definition.CAS;
import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.util.Definition.FIELD;
import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.util.Definition.ID;
import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.util.Definition.ID_PREFIX;
import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.util.Definition.ID_SUFFIX;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Hector Basset
 */
@RunWith(JUnitPlatform.class)
class CouchbasePropertyRuleTest {

	final SpringDataCouchbaseRuleFactory ruleFactory = new SpringDataCouchbaseRuleFactory();
	final CouchbasePropertyRule couchbasePropertyRule = new CouchbasePropertyRule(ruleFactory);
	final ObjectNode content = ruleFactory.getObjectMapper().createObjectNode();
	final Schema schema = new Schema(URI.create("schema"), content, null);
	final JDefinedClass clazz = clazz();
	final JFieldVar field = clazz.field(
			JMod.PRIVATE,
			clazz.owner().ref(String.class),
			"test"
	);

	CouchbasePropertyRuleTest() throws JClassAlreadyExistsException {
	}

	@Test
	void when_handle_couchbase_cas_on_non_cas_must_do_nothing() {

		// Given
		content.put(CAS.getJsonKey(), false);

		// When
		couchbasePropertyRule.handleCouchbaseCas(field, schema);

		// Then
		assertThat(field.annotations()).isEmpty();
	}

	@Test
	void when_handle_couchbase_cas_on_cas_must_annotate() {

		// Given
		content.put(CAS.getJsonKey(), true);

		// When
		couchbasePropertyRule.handleCouchbaseCas(field, schema);

		// Then
		assertThat(field.annotations())
				.hasSize(1)
				.first()
				.satisfies(ann -> assertThat(ann.getAnnotationClass()).isEqualTo(clazz.owner().ref(Version.class)));
	}

	@Test
	void when_handle_couchbase_id_on_non_id_must_do_nothing() {

		// Given
		content.put(ID.getJsonKey(), false);

		// When
		couchbasePropertyRule.handleCouchbaseId(field, schema);

		// Then
		assertThat(field.annotations()).isEmpty();
	}

	@Test
	void when_handle_couchbase_id_on_id_must_annotate() {

		// Given
		content.put(ID.getJsonKey(), true);

		// When
		couchbasePropertyRule.handleCouchbaseId(field, schema);

		// Then
		assertThat(field.annotations())
				.hasSize(1)
				.first()
				.satisfies(ann -> assertThat(ann.getAnnotationClass()).isEqualTo(clazz.owner().ref(Id.class)));
	}

	@Test
	void when_handle_id_generated_id_on_non_id_generated_must_do_nothing() {

		// Given

		// When
		couchbasePropertyRule.handleIdGenerated(field, null);

		// Then
		assertThat(field.annotations()).isEmpty();
	}

	@Test
	void when_handle_id_generated_id_on_id_generated_must_annotate() {

		// Given
		GeneratedDef generated = new GeneratedDef();

		// When
		couchbasePropertyRule.handleIdGenerated(field, generated);

		// Then
		assertThat(field.annotations())
				.hasSize(1)
				.first()
				.satisfies(ann -> assertThat(ann.getAnnotationClass()).isEqualTo(clazz.owner().ref(GeneratedValue.class)));
	}

	@Test
	void when_handle_id_generated_id_on_id_generated_with_params_must_annotate() {

		// Given
		GeneratedDef generated = new GeneratedDef("::", GenerationStrategy.UNIQUE);

		// When
		couchbasePropertyRule.handleIdGenerated(field, generated);

		// Then
		assertThat(field.annotations())
				.hasSize(1)
				.first()
				.satisfies(ann -> assertThat(ann.getAnnotationClass()).isEqualTo(clazz.owner().ref(GeneratedValue.class)))
				.satisfies(ann -> assertThat(ann.getAnnotationMembers()).containsOnlyKeys("delimiter", "strategy"));
	}

	@Test
	void when_handle_couchbase_id_prefix_on_non_id_prefix_must_do_nothing() {

		// Given
		content.put(ID_PREFIX.getJsonKey(), false);

		// When
		couchbasePropertyRule.handleCouchbaseIdPrefix(field, schema);

		// Then
		assertThat(field.annotations()).isEmpty();
	}

	@Test
	void when_handle_couchbase_id_prefix_on_id_prefix_must_annotate() {

		// Given
		content.put(ID_PREFIX.getJsonKey(), true);

		// When
		couchbasePropertyRule.handleCouchbaseIdPrefix(field, schema);

		// Then
		assertThat(field.annotations())
				.hasSize(1)
				.first()
				.satisfies(ann -> assertThat(ann.getAnnotationClass()).isEqualTo(clazz.owner().ref(IdPrefix.class)));
	}

	@Test
	void when_handle_couchbase_id_prefix_on_id_prefix_with_params_must_annotate() {

		// Given
		ObjectNode node = content.with(ID_PREFIX.getJsonKey());
		node.put("order", 1);

		// When
		couchbasePropertyRule.handleCouchbaseIdPrefix(field, schema);

		// Then
		assertThat(field.annotations())
				.hasSize(1)
				.first()
				.satisfies(ann -> assertThat(ann.getAnnotationClass()).isEqualTo(clazz.owner().ref(IdPrefix.class)))
				.satisfies(ann -> assertThat(ann.getAnnotationMembers()).containsOnlyKeys("order"));
	}

	@Test
	void when_handle_couchbase_id_suffix_on_non_id_suffix_must_do_nothing() {

		// Given
		content.put(ID_SUFFIX.getJsonKey(), false);

		// When
		couchbasePropertyRule.handleCouchbaseIdSuffix(field, schema);

		// Then
		assertThat(field.annotations()).isEmpty();
	}

	@Test
	void when_handle_couchbase_id_suffix_on_id_suffix_must_annotate() {

		// Given
		content.put(ID_SUFFIX.getJsonKey(), true);

		// When
		couchbasePropertyRule.handleCouchbaseIdSuffix(field, schema);

		// Then
		assertThat(field.annotations())
				.hasSize(1)
				.first()
				.satisfies(ann -> assertThat(ann.getAnnotationClass()).isEqualTo(clazz.owner().ref(IdSuffix.class)));
	}

	@Test
	void when_handle_couchbase_id_suffix_on_id_suffix_with_params_must_annotate() {

		// Given
		ObjectNode node = content.with(ID_SUFFIX.getJsonKey());
		node.put("order", 1);

		// When
		couchbasePropertyRule.handleCouchbaseIdSuffix(field, schema);

		// Then
		assertThat(field.annotations())
				.hasSize(1)
				.first()
				.satisfies(ann -> assertThat(ann.getAnnotationClass()).isEqualTo(clazz.owner().ref(IdSuffix.class)))
				.satisfies(ann -> assertThat(ann.getAnnotationMembers()).containsOnlyKeys("order"));
	}

	@Test
	void when_handle_couchbase_field_on_non_field_must_do_nothing() {

		// Given
		content.put(FIELD.getJsonKey(), false);

		// When
		couchbasePropertyRule.handleCouchbaseField(field, schema);

		// Then
		assertThat(field.annotations()).isEmpty();
	}

	@Test
	void when_handle_couchbase_field_on_field_must_annotate() {

		// Given
		content.put(FIELD.getJsonKey(), true);

		// When
		couchbasePropertyRule.handleCouchbaseField(field, schema);

		// Then
		assertThat(field.annotations())
				.hasSize(1)
				.first()
				.satisfies(ann -> assertThat(ann.getAnnotationClass()).isEqualTo(clazz.owner().ref(Field.class)));
	}

	@Test
	void when_handle_couchbase_field_on_field_with_params_must_annotate() {

		// Given
		ObjectNode node = content.with(FIELD.getJsonKey());
		node.put("name", "test");
		node.put("order", 1);

		// When
		couchbasePropertyRule.handleCouchbaseField(field, schema);

		// Then
		assertThat(field.annotations())
				.hasSize(1)
				.first()
				.satisfies(ann -> assertThat(ann.getAnnotationClass()).isEqualTo(clazz.owner().ref(Field.class)))
				.satisfies(ann -> assertThat(ann.getAnnotationMembers()).containsOnlyKeys("name", "order"));
	}

	@Test
	void when_handle_field_id_attribute_on_non_field_id_attribute_must_do_nothing() {

		// Given

		// When
		couchbasePropertyRule.handleFieldIdAttribute(field, null);

		// Then
		assertThat(field.annotations()).isEmpty();
	}

	@Test
	void when_handle_field_id_attribute_on_field_id_attribute_must_annotate() {

		// Given
		IdAttributeDef idAttribute = new IdAttributeDef();

		// When
		couchbasePropertyRule.handleFieldIdAttribute(field, idAttribute);

		// Then
		assertThat(field.annotations())
				.hasSize(1)
				.first()
				.satisfies(ann -> assertThat(ann.getAnnotationClass()).isEqualTo(clazz.owner().ref(IdAttribute.class)));
	}

	@Test
	void when_handle_field_id_attribute_on_field_id_attribute_with_params_must_annotate() {

		// Given
		IdAttributeDef idAttribute = new IdAttributeDef(1);

		// When
		couchbasePropertyRule.handleFieldIdAttribute(field, idAttribute);

		// Then
		assertThat(field.annotations())
				.hasSize(1)
				.first()
				.satisfies(ann -> assertThat(ann.getAnnotationClass()).isEqualTo(clazz.owner().ref(IdAttribute.class)))
				.satisfies(ann -> assertThat(ann.getAnnotationMembers()).containsOnlyKeys("order"));
	}

	@Test
	void when_handle_field_index_on_non_field_index_must_do_nothing() {

		// Given

		// When
		couchbasePropertyRule.handleFieldIndex(field, null);

		// Then
		assertThat(field.annotations()).isEmpty();
	}

	@Test
	void when_handle_field_index_on_field_index_must_annotate() {

		// Given
		IndexDef index = new IndexDef();

		// When
		couchbasePropertyRule.handleFieldIndex(field, index);

		// Then
		assertThat(field.annotations())
				.hasSize(1)
				.first()
				.satisfies(ann -> assertThat(ann.getAnnotationClass()).isEqualTo(clazz.owner().ref(QueryIndexed.class)));
	}

	@Test
	void when_handle_field_index_on_field_index_with_params_must_annotate() {

		// Given
		IndexDef index = new IndexDef(QueryIndexDirection.ASCENDING, "test");

		// When
		couchbasePropertyRule.handleFieldIndex(field, index);

		// Then
		assertThat(field.annotations())
				.hasSize(1)
				.first()
				.satisfies(ann -> assertThat(ann.getAnnotationClass()).isEqualTo(clazz.owner().ref(QueryIndexed.class)))
				.satisfies(ann -> assertThat(ann.getAnnotationMembers()).containsOnlyKeys("direction", "name"));
	}
}
