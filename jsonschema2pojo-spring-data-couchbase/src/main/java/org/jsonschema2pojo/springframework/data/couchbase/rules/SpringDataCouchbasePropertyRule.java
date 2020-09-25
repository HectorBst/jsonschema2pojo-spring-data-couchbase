package org.jsonschema2pojo.springframework.data.couchbase.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.rules.PropertyRule;

/**
 * Override of the default {@link PropertyRule} to apply Couchbase related elements.
 *
 * @author Hector Basset
 */
public class SpringDataCouchbasePropertyRule extends PropertyRule {

	private final SpringDataCouchbaseRuleFactory ruleFactory;

	public SpringDataCouchbasePropertyRule(SpringDataCouchbaseRuleFactory ruleFactory) {
		super(ruleFactory);
		this.ruleFactory = ruleFactory;
	}

	@Override
	public JDefinedClass apply(String nodeName, JsonNode node, JsonNode parent, JDefinedClass clazz, Schema schema) {
		clazz = super.apply(nodeName, node, parent, clazz, schema);

		String propertyName = ruleFactory.getNameHelper().getPropertyName(nodeName, node);
		JFieldVar field = clazz.fields().get(propertyName);

		if (field != null) {
			ruleFactory.getCouchbaseIdRule().apply(nodeName, node, parent, field, schema);

			ruleFactory.getCouchbaseCasRule().apply(nodeName, node, parent, field, schema);

			ruleFactory.getCouchbaseJoinRule().apply(nodeName, node, parent, field, schema);

			ruleFactory.getCouchbaseFieldRule().apply(nodeName, node, parent, field, schema);
		}

		return clazz;
	}
}
