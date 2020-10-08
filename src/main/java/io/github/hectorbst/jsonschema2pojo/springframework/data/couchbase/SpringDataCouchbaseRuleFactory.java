package io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.deser.DefaultDefinitionDeserializerModifier;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.rules.CouchbaseObjectRule;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.rules.CouchbasePropertyRule;
import org.jsonschema2pojo.rules.Rule;
import org.jsonschema2pojo.rules.RuleFactory;
import org.jsonschema2pojo.util.ParcelableHelper;

/**
 * The entry point of the Spring Data Couchbase jsonschema2pojo extension.
 * <p>
 * Allows the use of additional {@link Rule}s applied to handle Couchbase related elements (e.g. when a schema is marked
 * as document or a property as id).
 *
 * @author Hector Basset
 */
public class SpringDataCouchbaseRuleFactory extends RuleFactory {

	private final ParcelableHelper parcelableHelper = new ParcelableHelper();
	private final ObjectMapper objectMapper = new ObjectMapper();

	public SpringDataCouchbaseRuleFactory() {
		SimpleModule module = new SimpleModule();
		module.setDeserializerModifier(new DefaultDefinitionDeserializerModifier());
		objectMapper.registerModule(module);
	}

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public ParcelableHelper getParcelableHelper() {
		return parcelableHelper;
	}

	@Override
	public Rule<JPackage, JType> getObjectRule() {
		return new CouchbaseObjectRule(this);
	}

	@Override
	public Rule<JDefinedClass, JDefinedClass> getPropertyRule() {
		return new CouchbasePropertyRule(this);
	}
}
