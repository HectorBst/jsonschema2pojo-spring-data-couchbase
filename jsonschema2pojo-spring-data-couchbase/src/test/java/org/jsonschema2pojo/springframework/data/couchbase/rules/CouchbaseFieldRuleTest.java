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
import org.jsonschema2pojo.springframework.data.couchbase.util.SpringDataCouchbaseHelper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.springframework.data.couchbase.core.mapping.Field;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Hector Basset
 */
@RunWith(JUnitPlatform.class)
public class CouchbaseFieldRuleTest {

	static final ObjectMapper objectMapper = new ObjectMapper();
	final JCodeModel owner = new JCodeModel();
	final JDefinedClass clazz = owner._class("test.Test");
	final JFieldVar field = clazz.field(JMod.PRIVATE, owner.ref(String.class), "field");
	final CouchbaseFieldRule couchbaseFieldRule = new CouchbaseFieldRule(new SpringDataCouchbaseRuleFactory());

	public CouchbaseFieldRuleTest() throws JClassAlreadyExistsException {
	}

	ObjectNode getFieldPropertyNode(JsonNode fieldValue, boolean exclusivityValue) {
		ObjectNode node = objectMapper.createObjectNode();
		node.put("title", "A field");
		node.put("type", "string");
		node.set(CouchbaseFieldRule.JSON_KEY_FIELD, fieldValue);
		if (exclusivityValue) {
			node.put(SpringDataCouchbaseHelper.JSON_KEY_INTERNAL_FIELD_EXCLUSIVITY, "test");
		}
		return node;
	}

	static private Stream<Arguments> whenPropertyIsFieldArguments() {
		return Stream.of(
				Arguments.of(MissingNode.getInstance(), false),
				Arguments.of(NullNode.getInstance(), false),
				Arguments.of(BooleanNode.TRUE, false),
				Arguments.of(objectMapper.createObjectNode(), false)
		);
	}

	@ParameterizedTest
	@MethodSource("whenPropertyIsFieldArguments")
	void when_property_is_field_must_annotate(JsonNode fieldValue, boolean exclusivityValue) {

		// Given
		ObjectNode node = getFieldPropertyNode(fieldValue, exclusivityValue);

		// When
		couchbaseFieldRule.apply("test", node, null, field, null);

		// Then
		assertThat(field.annotations()).anyMatch(ann -> ann.getAnnotationClass().equals(owner.ref(Field.class)));
	}

	static private Stream<Arguments> whenPropertyIsNotFieldArguments() {
		return Stream.of(
				Arguments.of(BooleanNode.FALSE, false),
				Arguments.of(MissingNode.getInstance(), true),
				Arguments.of(NullNode.getInstance(), true)
		);
	}

	@ParameterizedTest
	@MethodSource("whenPropertyIsNotFieldArguments")
	void when_property_is_not_field_must_not_annotate(JsonNode fieldValue, boolean exclusivityValue) {

		// Given
		ObjectNode node = getFieldPropertyNode(fieldValue, exclusivityValue);

		// When
		couchbaseFieldRule.apply("test", node, null, field, null);

		// Then
		assertThat(field.annotations()).isEmpty();
	}
}
