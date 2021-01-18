package dev.hctbst.jsonschema2pojo.springframework.data.couchbase.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.hctbst.jsonschema2pojo.springframework.data.couchbase.SpringDataCouchbaseRuleFactory;
import dev.hctbst.jsonschema2pojo.springframework.data.couchbase.tests.NonValuedJsonSource;
import dev.hctbst.jsonschema2pojo.springframework.data.couchbase.tests.ValuedJsonSource;
import org.jsonschema2pojo.Schema;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.net.URI;
import java.util.stream.Stream;

import static dev.hctbst.jsonschema2pojo.springframework.data.couchbase.tests.TestsUtil.combinatorial;
import static dev.hctbst.jsonschema2pojo.springframework.data.couchbase.tests.TestsUtil.nonValuedJsonValues;
import static dev.hctbst.jsonschema2pojo.springframework.data.couchbase.tests.TestsUtil.trueJsonValues;
import static dev.hctbst.jsonschema2pojo.springframework.data.couchbase.tests.TestsUtil.valuedJsonValueToBoolean;
import static dev.hctbst.jsonschema2pojo.springframework.data.couchbase.tests.TestsUtil.valuedJsonValues;
import static dev.hctbst.jsonschema2pojo.springframework.data.couchbase.util.Definition.CAS;
import static dev.hctbst.jsonschema2pojo.springframework.data.couchbase.util.Definition.DOCUMENT;
import static dev.hctbst.jsonschema2pojo.springframework.data.couchbase.util.Definition.FIELD;
import static dev.hctbst.jsonschema2pojo.springframework.data.couchbase.util.Definition.ID;
import static dev.hctbst.jsonschema2pojo.springframework.data.couchbase.util.Definition.ID_PREFIX;
import static dev.hctbst.jsonschema2pojo.springframework.data.couchbase.util.Definition.ID_SUFFIX;
import static java.util.Arrays.stream;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Hector Basset
 */
@RunWith(JUnitPlatform.class)
class DefinitionTest {

	final SpringDataCouchbaseRuleFactory ruleFactory = new SpringDataCouchbaseRuleFactory();
	final ObjectNode parentContent = ruleFactory.getObjectMapper().createObjectNode();
	final Schema parent = new Schema(URI.create("parent"), parentContent, null);
	final ObjectNode content = ruleFactory.getObjectMapper().createObjectNode();
	final Schema schema = new Schema(URI.create("schema"), content, parent);

	static Stream<Definition> definitions() {
		return stream(Definition.values());
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
		content.set(definition.getJsonKey(), value);

		// When
		boolean result = definition.is(schema);

		// Then
		assertThat(result).isEqualTo(valuedJsonValueToBoolean(value));
	}

	@ParameterizedTest
	@MethodSource("definitionsAndNonValued")
	void when_test_is_on_non_valued_value_must_return_default(Definition definition, JsonNode value) {

		// Given
		content.set(definition.getJsonKey(), value);

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
		content.set(definition.getJsonKey(), value);

		// When
		boolean result = definition.is(schema);

		// Then
		assertThat(result).isFalse();
	}

	@ParameterizedTest
	@NonValuedJsonSource
	void when_test_is_document_on_non_valued_value_at_root_must_return_true(JsonNode value) {

		// Given
		parentContent.set(DOCUMENT.getJsonKey(), value);

		// When
		boolean result = DOCUMENT.is(parent);

		// Then
		assertThat(result).isTrue();
	}

	@ParameterizedTest
	@NonValuedJsonSource
	void when_test_is_document_on_non_valued_value_at_non_root_must_return_false(JsonNode value) {

		// Given
		content.set(DOCUMENT.getJsonKey(), value);

		// When
		boolean result = DOCUMENT.is(schema);

		// Then
		assertThat(result).isFalse();
	}

	@ParameterizedTest
	@NonValuedJsonSource
	void when_test_is_field_on_non_valued_value_at_root_must_return_false(JsonNode value) {

		// Given
		parentContent.set(FIELD.getJsonKey(), value);

		// When
		boolean result = FIELD.is(parent);

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
	void when_test_is_field_on_non_valued_value_at_non_root_within_document_or_field_must_return_true(JsonNode value, Definition parentDef, JsonNode parentDefValue) {

		// Given
		parentContent.set(parentDef.getJsonKey(), parentDefValue);
		content.set(FIELD.getJsonKey(), value);

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
		content.set(FIELD.getJsonKey(), value);
		content.set(otherDef.getJsonKey(), otherDefValue);

		// When
		boolean result = FIELD.is(schema);

		// Then
		assertThat(result).isFalse();
	}

	@ParameterizedTest
	@NonValuedJsonSource
	void when_test_is_field_on_non_valued_value_at_non_root_within_non_document_and_non_field_must_return_false(JsonNode value) {

		// Given
		parentContent.put(DOCUMENT.getJsonKey(), false);
		parentContent.put(FIELD.getJsonKey(), false);
		content.set(FIELD.getJsonKey(), value);

		// When
		boolean result = FIELD.is(schema);

		// Then
		assertThat(result).isFalse();
	}

	@ParameterizedTest
	@MethodSource("definitionsAndValued")
	void when_fill_missing_value_on_valued_values_must_do_nothing(Definition definition, JsonNode value) {

		// Given
		content.set(definition.getJsonKey(), value);
		ObjectNode contentCopy = content.deepCopy();

		// When
		definition.fillMissingValue(schema);

		// Then
		assertThat(content).isEqualTo(contentCopy);
	}

	@ParameterizedTest
	@MethodSource("definitionsAndNonValued")
	void when_fill_missing_value_on_non_valued_values_must_set_it(Definition definition, JsonNode value) {

		// Given
		content.set(definition.getJsonKey(), value);

		// When
		definition.fillMissingValue(schema);

		// Then
		assertThat(content.path(definition.getJsonKey()).isBoolean()).isTrue();
	}

	@ParameterizedTest
	@NonValuedJsonSource
	void when_fill_all_missing_values_on_non_valued_values_must_set_all(JsonNode value) {

		// Given
		definitions().forEach(def -> content.set(def.getJsonKey(), value));

		// When
		Definition.fillAllMissingValues(schema);

		// Then
		definitions().forEach(def -> assertThat(content.path(def.getJsonKey()).isBoolean()).isTrue());
	}

	@ParameterizedTest
	@ValuedJsonSource
	void when_fill_all_missing_values_on_valued_values_must_do_nothing(JsonNode value) {

		// Given
		definitions().forEach(def -> content.set(def.getJsonKey(), value));

		// When
		Definition.fillAllMissingValues(schema);

		// Then
		definitions().forEach(def -> assertThat(content.path(def.getJsonKey())).isEqualTo(value));
	}
}
