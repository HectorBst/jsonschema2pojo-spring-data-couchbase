package io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.deser;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.CasDef;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.DocumentDef;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.FieldDef;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.GeneratedDef;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.IdAttributeDef;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.IdDef;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.IdPrefixDef;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.IdSuffixDef;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.IndexDef;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Jackson deserializer modifier to use a custom deserializer for some definition POJO classes.
 *
 * @author Hector Basset
 */
public class DefaultDefinitionDeserializerModifier extends BeanDeserializerModifier {

	private final Map<Class<?>, Object> defaultDefinitions = Collections.unmodifiableMap(Stream.of(
			CasDef.class,
			DocumentDef.class,
			FieldDef.class,
			GeneratedDef.class,
			IdDef.class,
			IdAttributeDef.class,
			IdPrefixDef.class,
			IdSuffixDef.class,
			IndexDef.class
	).collect(Collectors.toMap(
			Function.identity(),
			clazz -> {
				try {
					return clazz.getConstructor().newInstance();
				} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
	)));

	@Override
	public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {

		if (defaultDefinitions.containsKey(beanDesc.getBeanClass())) {
			return new DefaultDefinitionDeserializer<>((StdDeserializer<?>) deserializer, (Serializable) defaultDefinitions.get(beanDesc.getBeanClass()));
		}

		return deserializer;
	}
}
