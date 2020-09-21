package org.jsonschema2pojo.springframework.data.couchbase.deser;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.jsonschema2pojo.springframework.data.couchbase.definition.Cas;
import org.jsonschema2pojo.springframework.data.couchbase.definition.Document;
import org.jsonschema2pojo.springframework.data.couchbase.definition.Field;
import org.jsonschema2pojo.springframework.data.couchbase.definition.Generated;
import org.jsonschema2pojo.springframework.data.couchbase.definition.Id;
import org.jsonschema2pojo.springframework.data.couchbase.definition.IdAttribute;
import org.jsonschema2pojo.springframework.data.couchbase.definition.IdPrefix;
import org.jsonschema2pojo.springframework.data.couchbase.definition.IdSuffix;
import org.jsonschema2pojo.springframework.data.couchbase.definition.Index;

import java.lang.reflect.InvocationTargetException;
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

	private final Map<Class<?>, Object> defaultDefinitions = Stream.of(
			Cas.class,
			Document.class,
			Field.class,
			Generated.class,
			Id.class,
			IdAttribute.class,
			IdPrefix.class,
			IdSuffix.class,
			Index.class
	).collect(Collectors.toMap(
			Function.identity(),
			clazz -> {
				try {
					return clazz.getConstructor().newInstance();
				} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
	));

	@Override
	public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {

		if (defaultDefinitions.containsKey(beanDesc.getBeanClass())) {
			return new DefaultDefinitionDeserializer<>((StdDeserializer<?>) deserializer, defaultDefinitions.get(beanDesc.getBeanClass()));
		}

		return deserializer;
	}
}
