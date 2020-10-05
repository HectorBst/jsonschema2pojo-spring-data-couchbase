package io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.SpringDataCouchbaseRuleFactory;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.rules.ObjectRule;

/**
 * Override of the default {@link ObjectRule} to apply Couchbase related elements.
 *
 * @author Hector Basset
 */
public class SpringDataCouchbaseObjectRule extends ObjectRule {

	private final SpringDataCouchbaseRuleFactory ruleFactory;

	public SpringDataCouchbaseObjectRule(SpringDataCouchbaseRuleFactory ruleFactory) {
		super(ruleFactory, ruleFactory.getParcelableHelper(), ruleFactory.getReflectionHelper());
		this.ruleFactory = ruleFactory;
	}

	@Override
	public JType apply(String nodeName, JsonNode node, JsonNode parent, JPackage jPackage, Schema schema) {
		JType type = super.apply(nodeName, node, parent, jPackage, schema);

		if (type instanceof JDefinedClass) {
			JDefinedClass clazz = (JDefinedClass) type;

			ruleFactory.getCouchbaseDocumentRule().apply(nodeName, node, parent, clazz, schema);
		}

		return type;
	}
}
