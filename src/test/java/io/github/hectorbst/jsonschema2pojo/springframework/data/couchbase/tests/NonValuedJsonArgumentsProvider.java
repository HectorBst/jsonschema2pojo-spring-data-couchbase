package io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.tests;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.tests.TestsUtil.nonValuedJsonValues;

/**
 * @author Hector Basset
 */
class NonValuedJsonArgumentsProvider implements ArgumentsProvider {

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		return nonValuedJsonValues().map(Arguments::arguments);
	}
}
