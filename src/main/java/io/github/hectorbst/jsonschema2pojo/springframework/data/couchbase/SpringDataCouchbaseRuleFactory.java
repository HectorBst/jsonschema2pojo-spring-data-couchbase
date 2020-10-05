package io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.deser.DefaultDefinitionDeserializerModifier;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.rules.CouchbaseCasRule;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.rules.CouchbaseDocumentRule;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.rules.CouchbaseFieldRule;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.rules.CouchbaseIdRule;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.rules.CouchbaseJoinRule;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.rules.SpringDataCouchbaseObjectRule;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.rules.SpringDataCouchbasePropertyRule;
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
		return new SpringDataCouchbaseObjectRule(this);
	}

	@Override
	public Rule<JDefinedClass, JDefinedClass> getPropertyRule() {
		return new SpringDataCouchbasePropertyRule(this);
	}

	public Rule<JDefinedClass, JDefinedClass> getCouchbaseDocumentRule() {
		return new CouchbaseDocumentRule(this);
	}

	public Rule<JFieldVar, JFieldVar> getCouchbaseIdRule() {
		return new CouchbaseIdRule(this);
	}

	public Rule<JFieldVar, JFieldVar> getCouchbaseCasRule() {
		return new CouchbaseCasRule(this);
	}

	public Rule<JFieldVar, JFieldVar> getCouchbaseJoinRule() {
		return new CouchbaseJoinRule(this);
	}

	public Rule<JFieldVar, JFieldVar> getCouchbaseFieldRule() {
		return new CouchbaseFieldRule(this);
	}
}
