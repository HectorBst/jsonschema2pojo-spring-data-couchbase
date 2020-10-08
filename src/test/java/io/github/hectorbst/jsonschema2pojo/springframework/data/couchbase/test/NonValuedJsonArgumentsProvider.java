package io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.test;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.test.TestUtil.nonValuedJsonValues;

/**
 * @author Hector Basset
 */
public class NonValuedJsonArgumentsProvider implements ArgumentsProvider {

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		return nonValuedJsonValues().map(Arguments::arguments);
	}
}
