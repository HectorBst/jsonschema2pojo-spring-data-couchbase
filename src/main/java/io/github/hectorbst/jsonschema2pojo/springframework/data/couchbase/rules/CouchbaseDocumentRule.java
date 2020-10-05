package io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JDefinedClass;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.SpringDataCouchbaseRuleFactory;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.CompositeIndexDef;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.DocumentDef;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.rules.Rule;
import org.springframework.data.couchbase.core.index.CompositeQueryIndex;
import org.springframework.data.couchbase.core.mapping.Document;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author Hector Basset
 */
public class CouchbaseDocumentRule implements Rule<JDefinedClass, JDefinedClass> {

	private final Logger log = Logger.getLogger(getClass().getName());

	protected static final String JSON_KEY_DOCUMENT = "x-cb-document";

	protected final SpringDataCouchbaseRuleFactory ruleFactory;

	public CouchbaseDocumentRule(SpringDataCouchbaseRuleFactory ruleFactory) {
		this.ruleFactory = ruleFactory;
	}

	@Override
	public JDefinedClass apply(String nodeName, JsonNode node, JsonNode parent, JDefinedClass clazz, Schema schema) {
		Optional.of(node.path(JSON_KEY_DOCUMENT))
				.map(n -> ruleFactory.getObjectMapper().convertValue(n, DocumentDef.class))
				.ifPresent(document -> {
					JAnnotationUse annotation = clazz.annotate(Document.class);

					Optional.ofNullable(document.getExpiry())
							.ifPresent(expiry -> annotation.param("expiry", expiry));

					Optional.ofNullable(document.getExpiryExpression())
							.ifPresent(expiryExpression -> annotation.param("expiryExpression", expiryExpression));

					Optional.ofNullable(document.getExpiryUnit())
							.ifPresent(expiryUnit -> annotation.param("expiryUnit", TimeUnit.valueOf(expiryUnit.name())));

					Optional.ofNullable(document.getTouchOnRead())
							.ifPresent(touchOnRead -> annotation.param("touchOnRead", touchOnRead));

					handleCompositeIndexes(nodeName, clazz, document.getCompositeIndexes());
				});

		return clazz;
	}

	protected void handleCompositeIndexes(String nodeName, JDefinedClass clazz, List<CompositeIndexDef> compositeIndexes) {
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
