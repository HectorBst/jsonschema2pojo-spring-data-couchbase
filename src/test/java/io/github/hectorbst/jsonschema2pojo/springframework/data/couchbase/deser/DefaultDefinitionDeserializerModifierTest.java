package io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.deser;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.CompositeIndexDef;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.Serializable;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Hector Basset
 */
@RunWith(JUnitPlatform.class)
@ExtendWith(MockitoExtension.class)
class DefaultDefinitionDeserializerModifierTest {

	final DefaultDefinitionDeserializerModifier defaultDefinitionDeserializerModifier = new DefaultDefinitionDeserializerModifier();
	final JsonDeserializer<?> defaultJsonDeserializer = mock(StdDeserializer.class);
	final BeanDescription beanDescription = mock(BeanDescription.class);

	static Stream<Class<? extends Serializable>> defaultDefinitionsClasses() {
		return DefaultDefinitionDeserializerModifier.DEFAULT_DEFINITIONS.keySet().stream();
	}

	@ParameterizedTest
	@MethodSource("defaultDefinitionsClasses")
	@SuppressWarnings("unchecked")
	void when_definition_with_default_must_return_custom_deserializer(Class<?> clazz) {

		// Given
		when(beanDescription.getBeanClass()).thenReturn((Class) clazz);

		// When
		JsonDeserializer<?> jsonDeserializer = defaultDefinitionDeserializerModifier.modifyDeserializer(null, beanDescription, defaultJsonDeserializer);

		// Then
		assertThat(jsonDeserializer).isInstanceOf(DefaultDefinitionDeserializer.class);
	}

	static Stream<Class<?>> classesWithoutDefaultDefinitions() {
		return Stream.of(
				CompositeIndexDef.class,
				String.class
		);
	}

	@ParameterizedTest
	@MethodSource("classesWithoutDefaultDefinitions")
	@SuppressWarnings("unchecked")
	void when_type_not_being_definition_with_default_must_return_native_deserializer(Class<?> clazz) {

		// Given
		when(beanDescription.getBeanClass()).thenReturn((Class) clazz);

		// When
		JsonDeserializer<?> jsonDeserializer = defaultDefinitionDeserializerModifier.modifyDeserializer(null, beanDescription, defaultJsonDeserializer);

		// Then
		assertThat(jsonDeserializer).isEqualTo(defaultJsonDeserializer);
	}
}
