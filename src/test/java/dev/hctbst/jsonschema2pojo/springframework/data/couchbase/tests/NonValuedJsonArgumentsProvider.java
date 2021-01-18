package dev.hctbst.jsonschema2pojo.springframework.data.couchbase.tests;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

/**
 * @author Hector Basset
 */
class NonValuedJsonArgumentsProvider implements ArgumentsProvider {

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		return TestsUtil.nonValuedJsonValues().map(Arguments::arguments);
	}
}
