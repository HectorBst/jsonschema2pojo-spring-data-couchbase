package io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.rules;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMod;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.SpringDataCouchbaseRuleFactory;
import org.jsonschema2pojo.Schema;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.couchbase.core.mapping.Field;
import org.springframework.data.couchbase.core.mapping.id.IdPrefix;
import org.springframework.data.couchbase.core.mapping.id.IdSuffix;

import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.test.TestUtil.clazz;
import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.test.TestUtil.emptyObjectNode;
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

	private static final String TEST_NODE_NAME = "test";

	final SpringDataCouchbaseRuleFactory ruleFactory = new SpringDataCouchbaseRuleFactory();
	final CouchbasePropertyRule couchbasePropertyRule = new CouchbasePropertyRule(ruleFactory);
	final JDefinedClass clazz = clazz();
	final JFieldVar field = clazz.field(
			JMod.PRIVATE,
			clazz.owner().ref(String.class),
			TEST_NODE_NAME
	);

	CouchbasePropertyRuleTest() throws JClassAlreadyExistsException {
	}

	@Test
	void when_handle_couchbase_cas_on_non_cas_must_do_nothing() {

		// Given
		ObjectNode content = emptyObjectNode();
		content.put(CAS.getJsonKey(), false);
		Schema schema = new Schema(null, content, null);

		// When
		couchbasePropertyRule.handleCouchbaseCas(clazz, field, schema);

		// Then
		assertThat(field.annotations()).isEmpty();
	}

	@Test
	void when_handle_couchbase_cas_on_cas_must_annotate() {

		// Given
		ObjectNode content = emptyObjectNode();
		content.put(CAS.getJsonKey(), true);
		Schema schema = new Schema(null, content, null);

		// When
		couchbasePropertyRule.handleCouchbaseCas(clazz, field, schema);

		// Then
		assertThat(field.annotations())
				.hasSize(1)
				.first()
				.satisfies(ann -> assertThat(ann.getAnnotationClass()).isEqualTo(clazz.owner().ref(Version.class)));
	}

	@Test
	void when_handle_couchbase_id_on_non_id_must_do_nothing() {

		// Given
		ObjectNode content = emptyObjectNode();
		content.put(ID.getJsonKey(), false);
		Schema schema = new Schema(null, content, null);

		// When
		couchbasePropertyRule.handleCouchbaseId(clazz, field, schema);

		// Then
		assertThat(field.annotations()).isEmpty();
	}

	@Test
	void when_handle_couchbase_id_on_id_must_annotate() {

		// Given
		ObjectNode content = emptyObjectNode();
		content.put(ID.getJsonKey(), true);
		Schema schema = new Schema(null, content, null);

		// When
		couchbasePropertyRule.handleCouchbaseId(clazz, field, schema);

		// Then
		assertThat(field.annotations())
				.hasSize(1)
				.first()
				.satisfies(ann -> assertThat(ann.getAnnotationClass()).isEqualTo(clazz.owner().ref(Id.class)));
	}

	@Test
	void when_handle_couchbase_id_prefix_on_non_id_prefix_must_do_nothing() {

		// Given
		ObjectNode content = emptyObjectNode();
		content.put(ID_PREFIX.getJsonKey(), false);
		Schema schema = new Schema(null, content, null);

		// When
		couchbasePropertyRule.handleCouchbaseIdPrefix(clazz, field, schema);

		// Then
		assertThat(field.annotations()).isEmpty();
	}

	@Test
	void when_handle_couchbase_id_prefix_on_id_prefix_must_annotate() {

		// Given
		ObjectNode content = emptyObjectNode();
		content.put(ID_PREFIX.getJsonKey(), true);
		Schema schema = new Schema(null, content, null);

		// When
		couchbasePropertyRule.handleCouchbaseIdPrefix(clazz, field, schema);

		// Then
		assertThat(field.annotations())
				.hasSize(1)
				.first()
				.satisfies(ann -> assertThat(ann.getAnnotationClass()).isEqualTo(clazz.owner().ref(IdPrefix.class)));
	}

	@Test
	void when_handle_couchbase_id_suffix_on_non_id_suffix_must_do_nothing() {

		// Given
		ObjectNode content = emptyObjectNode();
		content.put(ID_SUFFIX.getJsonKey(), false);
		Schema schema = new Schema(null, content, null);

		// When
		couchbasePropertyRule.handleCouchbaseIdSuffix(clazz, field, schema);

		// Then
		assertThat(field.annotations()).isEmpty();
	}

	@Test
	void when_handle_couchbase_id_suffix_on_id_suffix_must_annotate() {

		// Given
		ObjectNode content = emptyObjectNode();
		content.put(ID_SUFFIX.getJsonKey(), true);
		Schema schema = new Schema(null, content, null);

		// When
		couchbasePropertyRule.handleCouchbaseIdSuffix(clazz, field, schema);

		// Then
		assertThat(field.annotations())
				.hasSize(1)
				.first()
				.satisfies(ann -> assertThat(ann.getAnnotationClass()).isEqualTo(clazz.owner().ref(IdSuffix.class)));
	}

	@Test
	void when_handle_couchbase_field_on_non_field_must_do_nothing() {

		// Given
		ObjectNode content = emptyObjectNode();
		content.put(FIELD.getJsonKey(), false);
		Schema schema = new Schema(null, content, null);

		// When
		couchbasePropertyRule.handleCouchbaseField(clazz, field, schema);

		// Then
		assertThat(field.annotations()).isEmpty();
	}

	@Test
	void when_handle_couchbase_field_on_field_must_annotate() {

		// Given
		ObjectNode content = emptyObjectNode();
		content.put(FIELD.getJsonKey(), true);
		Schema schema = new Schema(null, content, null);

		// When
		couchbasePropertyRule.handleCouchbaseField(clazz, field, schema);

		// Then
		assertThat(field.annotations())
				.hasSize(1)
				.first()
				.satisfies(ann -> assertThat(ann.getAnnotationClass()).isEqualTo(clazz.owner().ref(Field.class)));
	}
}
