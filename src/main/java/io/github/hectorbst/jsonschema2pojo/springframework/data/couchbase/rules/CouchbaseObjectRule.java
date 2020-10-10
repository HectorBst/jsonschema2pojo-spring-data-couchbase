package io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.SpringDataCouchbaseRuleFactory;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.CompositeIndexDef;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.DocumentDef;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.util.Definition;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.rules.ObjectRule;
import org.springframework.data.couchbase.core.index.CompositeQueryIndex;
import org.springframework.data.couchbase.core.mapping.Document;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.util.Definition.DOCUMENT;

/**
 * Override of the default {@link ObjectRule} to apply Couchbase related elements.
 *
 * @author Hector Basset
 */
public class CouchbaseObjectRule extends ObjectRule {

	private final Logger log = Logger.getLogger(getClass().getName());

	private final SpringDataCouchbaseRuleFactory ruleFactory;

	public CouchbaseObjectRule(SpringDataCouchbaseRuleFactory ruleFactory) {
		super(ruleFactory, ruleFactory.getParcelableHelper(), ruleFactory.getReflectionHelper());
		this.ruleFactory = ruleFactory;
	}

	@Override
	public JType apply(String nodeName, JsonNode node, JsonNode parent, JPackage jPackage, Schema schema) {
		Definition.fillAllMissingValues(schema);

		JType type = super.apply(nodeName, node, parent, jPackage, schema);

		type = handleCouchbase(nodeName, type, schema);

		return type;
	}

	protected JType handleCouchbase(String nodeName, JType type, Schema schema) {
		return handleCouchbaseDocument(nodeName, type, schema);
	}

	protected JType handleCouchbaseDocument(String nodeName, JType type, Schema schema) {
		if ((type instanceof JDefinedClass) && DOCUMENT.is(schema)) {
			JDefinedClass clazz = (JDefinedClass) type;
			DocumentDef document = DOCUMENT.get(schema, ruleFactory);

			JAnnotationUse annotation = clazz.annotate(Document.class);

			Optional.ofNullable(document.getExpiry())
					.ifPresent(expiry -> annotation.param("expiry", expiry));

			Optional.ofNullable(document.getExpiryExpression())
					.ifPresent(expiryExpression -> annotation.param("expiryExpression", expiryExpression));

			Optional.ofNullable(document.getExpiryUnit())
					.ifPresent(expiryUnit -> annotation.param("expiryUnit", expiryUnit));

			Optional.ofNullable(document.getTouchOnRead())
					.ifPresent(touchOnRead -> annotation.param("touchOnRead", touchOnRead));

			handleDocumentCompositeIndexes(nodeName, clazz, document.getCompositeIndexes());
		}

		return type;
	}

	protected void handleDocumentCompositeIndexes(String nodeName, JDefinedClass clazz, List<CompositeIndexDef> compositeIndexes) {
		Optional.ofNullable(compositeIndexes)
				.ifPresent(indexes -> indexes.forEach(index -> {
					JAnnotationUse ann = clazz.annotate(CompositeQueryIndex.class);

					if (index.getFields() == null || index.getFields().isEmpty()) {
						String message = String.format(
								"Error for Couchbase document \"%s\": fields are required for composite indexes",
								nodeName
						);
						throw new IllegalArgumentException(message);
					}
					if (index.getFields().size() == 1) {
						String message = String.format(
								"Couchbase document \"%s\": composite index with only one field \"%s\" found, you should probably prefer index metadata on concerned field",
								nodeName,
								index.getFields().get(0)
						);
						log.warning(message);
					}
					JAnnotationArrayMember fieldsAnnotation = ann.paramArray("fields");
					index.getFields().forEach(fieldsAnnotation::param);

					Optional.ofNullable(index.getName())
							.ifPresent(name -> ann.param("name", name));
				}));
	}
}
