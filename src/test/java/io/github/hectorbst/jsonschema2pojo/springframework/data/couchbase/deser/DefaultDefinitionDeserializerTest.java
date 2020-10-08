package io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.Serializable;
import java.util.stream.Stream;

import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.test.TestUtil.combinatorial;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Hector Basset
 */
@RunWith(JUnitPlatform.class)
@ExtendWith(MockitoExtension.class)
class DefaultDefinitionDeserializerTest {

	final StdDeserializer<?> defaultJsonDeserializer = mock(StdDeserializer.class);

	static Stream<? extends Serializable> defaultDefinitions() {
		return DefaultDefinitionDeserializerModifier.DEFAULT_DEFINITIONS.values().stream();
	}

	private JsonParser getBooleanOrNullJsonParserMock(Boolean value) throws IOException {
		JsonParser jsonParser = mock(JsonParser.class);
		if (value != null) {
			JsonToken jsonToken = value ? JsonToken.VALUE_TRUE : JsonToken.VALUE_FALSE;
			when(jsonParser.getCurrentToken()).thenReturn(jsonToken);
			when(jsonParser.getBooleanValue()).thenReturn(value);
		}
		return jsonParser;
	}

	static Stream<Boolean> nullEquivalentTokenValues() {
		return Stream.of(
				null,
				false
		);
	}

	static Stream<Arguments> defaultDefinitionsAndNullEquivalentTokenValues() {
		return combinatorial(defaultDefinitions(), nullEquivalentTokenValues());
	}

	@ParameterizedTest
	@MethodSource("defaultDefinitionsAndNullEquivalentTokenValues")
	<T extends Serializable> void when_false_or_null_must_return_null(T defaultDef, Boolean value) throws IOException {

		// Given
		DefaultDefinitionDeserializer<T> defaultDefinitionDeserializer = new DefaultDefinitionDeserializer<>(defaultJsonDeserializer, defaultDef);
		JsonParser jsonParser = getBooleanOrNullJsonParserMock(value);
		DeserializationContext deserializationContext = mock(DeserializationContext.class);

		// When
		T result = defaultDefinitionDeserializer.deserialize(jsonParser, deserializationContext);

		// Then
		assertThat(result).isNull();
	}

	@ParameterizedTest
	@MethodSource("defaultDefinitions")
	<T extends Serializable> void when_true_must_return_default(T defaultDef) throws IOException {

		// Given
		DefaultDefinitionDeserializer<T> defaultDefinitionDeserializer = new DefaultDefinitionDeserializer<>(defaultJsonDeserializer, defaultDef);
		JsonParser jsonParser = getBooleanOrNullJsonParserMock(true);
		DeserializationContext deserializationContext = mock(DeserializationContext.class);

		// When
		T result = defaultDefinitionDeserializer.deserialize(jsonParser, deserializationContext);

		// Then
		assertThat(result).isEqualTo(defaultDef);
	}

	@ParameterizedTest
	@MethodSource("defaultDefinitions")
	<T extends Serializable> void when_object_must_deserialize(T defaultDef) throws IOException {

		// Given
		DefaultDefinitionDeserializer<T> defaultDefinitionDeserializer = new DefaultDefinitionDeserializer<>(defaultJsonDeserializer, defaultDef);
		JsonParser jsonParser = mock(JsonParser.class);
		when(jsonParser.getCurrentToken()).thenReturn(JsonToken.START_OBJECT);
		DeserializationContext deserializationContext = mock(DeserializationContext.class);

		// When
		defaultDefinitionDeserializer.deserialize(jsonParser, deserializationContext);

		// Then
		verify(defaultJsonDeserializer, times(1)).deserialize(jsonParser, deserializationContext);
	}
}
