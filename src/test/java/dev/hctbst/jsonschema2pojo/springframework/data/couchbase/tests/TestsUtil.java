package dev.hctbst.jsonschema2pojo.springframework.data.couchbase.tests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.params.provider.Arguments;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * @author Hector Basset
 */
public class TestsUtil {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final ObjectNode EMPTY_OBJECT_NODE = OBJECT_MAPPER.createObjectNode();

	public static ObjectNode emptyObjectNode() {
		return EMPTY_OBJECT_NODE.deepCopy();
	}

	public static Stream<JsonNode> nonValuedJsonValues() {
		return Stream.of(
				MissingNode.getInstance(),
				NullNode.getInstance()
		);
	}

	public static Stream<JsonNode> valuedJsonValues() {
		return Stream.of(
				BooleanNode.FALSE,
				BooleanNode.TRUE,
				emptyObjectNode()
		);
	}

	public static Stream<JsonNode> trueJsonValues() {
		return Stream.of(
				BooleanNode.TRUE,
				emptyObjectNode()
		);
	}

	public static boolean valuedJsonValueToBoolean(JsonNode value) {
		if (value.isBoolean()) {
			return value.asBoolean();
		} else if (value.isObject()) {
			return true;
		} else {
			throw new IllegalArgumentException("Unexpected JSON type: \"" + value + "\"");
		}
	}

	public static Stream<Arguments> combinatorial(Stream<?>... streams) {
		return Arrays.stream(streams)
				.map(s -> s.map(o -> {
					if (o instanceof Arguments) {
						return (Arguments) o;
					} else {
						return arguments(o);
					}
				}))
				.map(s -> s.collect(Collectors.toList()))
				.reduce((s1, s2) -> s1.stream().flatMap(a1 -> s2.stream().map(a2 -> arguments(ArrayUtils.addAll(a1.get(), a2.get())))).collect(Collectors.toList()))
				.map(Collection::stream)
				.orElseGet(Stream::empty);
	}

	public static JCodeModel codeModel() {
		return new JCodeModel();
	}

	public static JDefinedClass clazz() throws JClassAlreadyExistsException {
		return codeModel()._class("test.Test");
	}
}
