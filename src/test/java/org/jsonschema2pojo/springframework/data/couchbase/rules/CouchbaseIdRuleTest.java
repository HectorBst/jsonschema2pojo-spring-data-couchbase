package org.jsonschema2pojo.springframework.data.couchbase.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMod;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.id.GeneratedValue;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Hector Basset
 */
@RunWith(JUnitPlatform.class)
class CouchbaseIdRuleTest {

	static final ObjectMapper objectMapper = new ObjectMapper();
	final JCodeModel owner = new JCodeModel();
	final JDefinedClass clazz = owner._class("test.Test");
	final JFieldVar field = clazz.field(JMod.PRIVATE, owner.ref(String.class), "id");
	final CouchbaseIdRule couchbaseIdRule = new CouchbaseIdRule(new SpringDataCouchbaseRuleFactory());

	public CouchbaseIdRuleTest() throws JClassAlreadyExistsException {
	}

	ObjectNode getIdPropertyNode(JsonNode idValue) {
		ObjectNode node = objectMapper.createObjectNode();
		node.put("title", "Entity id");
		node.put("type", "string");
		node.set(CouchbaseIdRule.JSON_KEY_ID, idValue);
		return node;
	}

	static private Stream<JsonNode> whenPropertyIsIdArguments() {
		return Stream.of(
				BooleanNode.TRUE,
				objectMapper.createObjectNode()
		);
	}

	@ParameterizedTest
	@MethodSource("whenPropertyIsIdArguments")
	void when_property_is_id_must_annotate(JsonNode idValue) {

		// Given
		ObjectNode node = getIdPropertyNode(idValue);

		// When
		couchbaseIdRule.apply("test", node, null, field, null);

		// Then
		assertThat(field.annotations()).anyMatch(ann -> ann.getAnnotationClass().equals(owner.ref(Id.class)));
	}

	static private Stream<JsonNode> whenPropertyIsNotIdArguments() {
		return Stream.of(
				MissingNode.getInstance(),
				NullNode.getInstance(),
				BooleanNode.FALSE
		);
	}

	@ParameterizedTest
	@MethodSource("whenPropertyIsNotIdArguments")
	void when_property_is_not_id_must_not_annotate(JsonNode idValue) {

		// Given
		ObjectNode node = getIdPropertyNode(idValue);

		// When
		couchbaseIdRule.apply("test", node, null, field, null);

		// Then
		assertThat(field.annotations()).isEmpty();
	}

	ObjectNode getIdPropertyNodeWithGenerated(JsonNode generatedValue) {
		ObjectNode node = objectMapper.createObjectNode();
		node.set("generated", generatedValue);
		return getIdPropertyNode(node);
	}

	static private Stream<JsonNode> whenIdIsGeneratedArguments() {
		return Stream.of(
				BooleanNode.TRUE,
				objectMapper.createObjectNode()
		);
	}

	@ParameterizedTest
	@MethodSource("whenIdIsGeneratedArguments")
	void when_id_is_generated_must_annotate(JsonNode generatedValue) {

		// Given
		ObjectNode node = getIdPropertyNodeWithGenerated(generatedValue);

		// When
		couchbaseIdRule.apply("test", node, null, field, null);

		// Then
		assertThat(field.annotations()).anyMatch(ann -> ann.getAnnotationClass().equals(owner.ref(GeneratedValue.class)));
	}

	static private Stream<JsonNode> whenIdIsNotGeneratedArguments() {
		return Stream.of(
				MissingNode.getInstance(),
				NullNode.getInstance(),
				BooleanNode.FALSE
		);
	}

	@ParameterizedTest
	@MethodSource("whenIdIsNotGeneratedArguments")
	void when_id_is_not_generated_must_annotate(JsonNode generatedValue) {

		// Given
		ObjectNode node = getIdPropertyNodeWithGenerated(generatedValue);

		// When
		couchbaseIdRule.apply("test", node, null, field, null);

		// Then
		assertThat(field.annotations()).noneMatch(ann -> ann.getAnnotationClass().equals(owner.ref(GeneratedValue.class)));
	}
}
