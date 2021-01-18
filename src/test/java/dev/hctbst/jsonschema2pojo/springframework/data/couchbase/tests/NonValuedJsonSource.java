package dev.hctbst.jsonschema2pojo.springframework.data.couchbase.tests;

import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Hector Basset
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ArgumentsSource(NonValuedJsonArgumentsProvider.class)
public @interface NonValuedJsonSource {

}
