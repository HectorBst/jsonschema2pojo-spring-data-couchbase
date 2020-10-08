package io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.SpringDataCouchbaseRuleFactory;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.test.NonValuedJsonSource;
import org.jsonschema2pojo.Schema;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.stream.Stream;

import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.test.TestUtil.combinatorial;
import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.test.TestUtil.nonValuedJsonValues;
import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.test.TestUtil.trueJsonValues;
import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.test.TestUtil.valuedJsonValueToBoolean;
import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.test.TestUtil.valuedJsonValues;
import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.util.Definition.CAS;
import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.util.Definition.DOCUMENT;
import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.util.Definition.FIELD;
import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.util.Definition.ID;
import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.util.Definition.ID_PREFIX;
import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.util.Definition.ID_SUFFIX;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Hector Basset
 */
@RunWith(JUnitPlatform.class)
class DefinitionTest {

	final SpringDataCouchbaseRuleFactory springDataCouchbaseRuleFactory = new SpringDataCouchbaseRuleFactory();

	static Stream<Definition> definitions() {
		return Arrays.stream(Definition.values());
	}

	static Stream<Arguments> definitionsAndValued() {
		return combinatorial(definitions(), valuedJsonValues());
	}

	static Stream<Arguments> definitionsAndNonValued() {
		return combinatorial(definitions(), nonValuedJsonValues());
	}

	@ParameterizedTest
	@MethodSource("definitionsAndValued")
	void when_test_is_on_valued_value_must_return_value(Definition definition, JsonNode value) {

		// Given
		ObjectNode content = springDataCouchbaseRuleFactory.getObjectMapper().createObjectNode();
		content.set(definition.getJsonKey(), value);
		Schema schema = new Schema(null, content, null);

		// When
		boolean result = definition.is(schema);

		// Then
		assertThat(result).isEqualTo(valuedJsonValueToBoolean(value));
	}

	@ParameterizedTest
	@MethodSource("definitionsAndNonValued")
	void when_test_is_on_non_valued_value_must_return_default(Definition definition, JsonNode value) {

		// Given
		ObjectNode content = springDataCouchbaseRuleFactory.getObjectMapper().createObjectNode();
		content.set(definition.getJsonKey(), value);
		Schema schema = new Schema(null, content, null);

		// When
		boolean result = definition.is(schema);

		// Then
		assertThat(result).isEqualTo(definition.getDefaultValueGetter().test(schema));
	}

	static Stream<Definition> falseDefaultDefinitions() {
		return Stream.of(
				CAS,
				ID,
				ID_PREFIX,
				ID_SUFFIX
		);
	}

	static Stream<Arguments> falseDefaultDefinitionsAndNonValued() {
		return combinatorial(falseDefaultDefinitions(), nonValuedJsonValues());
	}

	@ParameterizedTest
	@MethodSource("falseDefaultDefinitionsAndNonValued")
	void when_test_is_false_default_definition_on_non_valued_value_must_return_false(Definition definition, JsonNode value) {

		// Given
		ObjectNode content = springDataCouchbaseRuleFactory.getObjectMapper().createObjectNode();
		content.set(definition.getJsonKey(), value);
		Schema schema = new Schema(null, content, null);

		// When
		boolean result = definition.is(schema);

		// Then
		assertThat(result).isFalse();
	}

	@ParameterizedTest
	@NonValuedJsonSource
	void when_test_is_document_at_root_on_non_valued_value_must_return_true(JsonNode value) {

		// Given
		ObjectNode content = springDataCouchbaseRuleFactory.getObjectMapper().createObjectNode();
		content.set(DOCUMENT.getJsonKey(), value);
		Schema schema = new Schema(null, content, null);

		// When
		boolean result = DOCUMENT.is(schema);

		// Then
		assertThat(result).isTrue();
	}

	@ParameterizedTest
	@NonValuedJsonSource
	void when_test_is_document_at_non_root_on_non_valued_value_must_return_false(JsonNode value) {

		// Given
		Schema parent = new Schema(null, null, null);
		ObjectNode content = springDataCouchbaseRuleFactory.getObjectMapper().createObjectNode();
		content.set(DOCUMENT.getJsonKey(), value);
		Schema schema = new Schema(null, content, parent);

		// When
		boolean result = DOCUMENT.is(schema);

		// Then
		assertThat(result).isFalse();
	}

	@ParameterizedTest
	@NonValuedJsonSource
	void when_test_is_field_at_root_on_non_valued_value_must_return_false(JsonNode value) {

		// Given
		ObjectNode content = springDataCouchbaseRuleFactory.getObjectMapper().createObjectNode();
		content.set(FIELD.getJsonKey(), value);
		Schema schema = new Schema(null, content, null);

		// When
		boolean result = FIELD.is(schema);

		// Then
		assertThat(result).isFalse();
	}

	static Stream<Definition> parentDefinition() {
		return Stream.of(
				DOCUMENT,
				FIELD
		);
	}

	static Stream<Arguments> nonValuedAndParentDefAndParentDefValue() {
		return combinatorial(nonValuedJsonValues(), parentDefinition(), trueJsonValues());
	}

	@ParameterizedTest
	@MethodSource("nonValuedAndParentDefAndParentDefValue")
	void when_test_is_field_at_non_root_within_document_or_field_on_non_valued_value_must_return_true(JsonNode value, Definition parentDef, JsonNode parentDefValue) {

		// Given
		ObjectNode parentContent = springDataCouchbaseRuleFactory.getObjectMapper().createObjectNode();
		parentContent.set(parentDef.getJsonKey(), parentDefValue);
		Schema parent = new Schema(null, parentContent, null);
		ObjectNode content = springDataCouchbaseRuleFactory.getObjectMapper().createObjectNode();
		content.set(FIELD.getJsonKey(), value);
		Schema schema = new Schema(null, content, parent);

		// When
		boolean result = FIELD.is(schema);

		// Then
		assertThat(result).isTrue();
	}

	static Stream<Definition> definitionsButField() {
		return definitions().filter(def -> def != FIELD);
	}

	static Stream<Arguments> nonValuedAndDefinitionsButFieldAndOtherDefValue() {
		return combinatorial(nonValuedJsonValues(), definitionsButField(), trueJsonValues());
	}

	@ParameterizedTest
	@MethodSource("nonValuedAndDefinitionsButFieldAndOtherDefValue")
	void when_test_is_field_with_any_other_definition_on_non_valued_value_must_return_false(JsonNode value, Definition otherDef, JsonNode otherDefValue) {

		// Given
		Schema parent = new Schema(null, springDataCouchbaseRuleFactory.getObjectMapper().createObjectNode(), null);
		ObjectNode content = springDataCouchbaseRuleFactory.getObjectMapper().createObjectNode();
		content.set(FIELD.getJsonKey(), value);
		content.set(otherDef.getJsonKey(), otherDefValue);
		Schema schema = new Schema(null, content, parent);

		// When
		boolean result = FIELD.is(schema);

		// Then
		assertThat(result).isFalse();
	}

	@ParameterizedTest
	@NonValuedJsonSource
	void when_test_is_field_within_non_document_and_non_field_on_non_valued_value_must_return_false(JsonNode value) {

		// Given
		ObjectNode parentContent = springDataCouchbaseRuleFactory.getObjectMapper().createObjectNode();
		parentContent.put(DOCUMENT.getJsonKey(), false);
		parentContent.put(FIELD.getJsonKey(), false);
		Schema parent = new Schema(null, parentContent, null);
		ObjectNode content = springDataCouchbaseRuleFactory.getObjectMapper().createObjectNode();
		content.set(FIELD.getJsonKey(), value);
		Schema schema = new Schema(null, content, parent);

		// When
		boolean result = FIELD.is(schema);

		// Then
		assertThat(result).isFalse();
	}

	@ParameterizedTest
	@MethodSource("definitionsAndValued")
	void when_set_missing_on_valued_values_must_do_nothing(Definition definition, JsonNode value) {

		// Given
		ObjectNode content = springDataCouchbaseRuleFactory.getObjectMapper().createObjectNode();
		content.set(definition.getJsonKey(), value);
		ObjectNode contentCopy = content.deepCopy();
		Schema schema = new Schema(null, content, null);

		// When
		definition.setMissing(schema);

		// Then
		assertThat(content).isEqualTo(contentCopy);
	}

	@ParameterizedTest
	@MethodSource("definitionsAndNonValued")
	void when_set_missing_on_non_valued_values_must_set_it(Definition definition, JsonNode value) {

		// Given
		ObjectNode content = springDataCouchbaseRuleFactory.getObjectMapper().createObjectNode();
		content.set(definition.getJsonKey(), value);
		Schema schema = new Schema(null, content, null);

		// When
		definition.setMissing(schema);

		// Then
		assertThat(content.path(definition.getJsonKey()).isBoolean()).isTrue();
	}
}
