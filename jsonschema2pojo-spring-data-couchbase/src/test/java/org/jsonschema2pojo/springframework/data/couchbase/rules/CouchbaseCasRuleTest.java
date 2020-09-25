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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.annotation.Version;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Hector Basset
 */
@ExtendWith(MockitoExtension.class)
public class CouchbaseCasRuleTest {

	static final ObjectMapper objectMapper = new ObjectMapper();
	final JCodeModel owner = new JCodeModel();
	final JDefinedClass clazz = owner._class("test.Test");
	final JFieldVar field = spy(clazz.field(JMod.PRIVATE, owner.LONG, "cas"));
	final CouchbaseCasRule couchbaseCasRule = new CouchbaseCasRule(new SpringDataCouchbaseRuleFactory());

	public CouchbaseCasRuleTest() throws JClassAlreadyExistsException {
	}

	ObjectNode getCasPropertyNode(JsonNode casValue) {
		ObjectNode node = objectMapper.createObjectNode();
		node.put("title", "Couchbase CAS");
		node.put("type", "integer");
		node.put("format", "int64");
		node.set(CouchbaseCasRule.JSON_KEY_CAS, casValue);
		return node;
	}

	static private Stream<JsonNode> whenPropertyIsCasArguments() {
		return Stream.of(
				BooleanNode.TRUE,
				objectMapper.createObjectNode()
		);
	}

	@ParameterizedTest
	@MethodSource("whenPropertyIsCasArguments")
	void when_property_is_cas_must_annotate(JsonNode casValue) {

		// Given
		ObjectNode node = getCasPropertyNode(casValue);

		// When
		couchbaseCasRule.apply("test", node, null, field, null);

		// Then
		verify(field, times(1)).annotate(Version.class);
	}

	static private Stream<JsonNode> whenPropertyIsNotCasArguments() {
		return Stream.of(
				MissingNode.getInstance(),
				NullNode.getInstance(),
				BooleanNode.FALSE
		);
	}

	@ParameterizedTest
	@MethodSource("whenPropertyIsNotCasArguments")
	@SuppressWarnings("unchecked")
	void when_property_is_not_cas_must_not_annotate(JsonNode casValue) {

		// Given
		ObjectNode node = getCasPropertyNode(casValue);

		// When
		couchbaseCasRule.apply("test", node, null, field, null);

		// Then
		verify(field, times(0)).annotate(any(Class.class));
	}
}
