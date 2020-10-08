package io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.rules;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.SpringDataCouchbaseRuleFactory;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.CompositeIndexDef;
import org.jsonschema2pojo.Schema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.springframework.data.couchbase.core.index.CompositeQueryIndex;
import org.springframework.data.couchbase.core.mapping.Document;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.test.TestUtil.clazz;
import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.test.TestUtil.emptyObjectNode;
import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.util.Definition.DOCUMENT;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Hector Basset
 */
@RunWith(JUnitPlatform.class)
class CouchbaseObjectRuleTest {

	private static final String TEST_NODE_NAME = "test";

	final SpringDataCouchbaseRuleFactory ruleFactory = new SpringDataCouchbaseRuleFactory();
	final CouchbaseObjectRule couchbaseObjectRule = new CouchbaseObjectRule(ruleFactory);
	final JDefinedClass clazz = clazz();

	public CouchbaseObjectRuleTest() throws JClassAlreadyExistsException {
	}

	@Test
	void when_handle_couchbase_document_on_non_document_must_do_nothing() {

		// Given
		ObjectNode content = emptyObjectNode();
		content.put(DOCUMENT.getJsonKey(), false);
		Schema schema = new Schema(null, content, null);

		// When
		couchbaseObjectRule.handleCouchbaseDocument(TEST_NODE_NAME, clazz, schema);

		// Then
		assertThat(clazz.annotations()).isEmpty();
	}

	@Test
	void when_handle_couchbase_document_on_document_must_annotate() {

		// Given
		ObjectNode content = emptyObjectNode();
		content.put(DOCUMENT.getJsonKey(), true);
		Schema schema = new Schema(null, content, null);

		// When
		couchbaseObjectRule.handleCouchbaseDocument(TEST_NODE_NAME, clazz, schema);

		// Then
		assertThat(clazz.annotations())
				.hasSize(1)
				.first()
				.satisfies(ann -> assertThat(ann.getAnnotationClass()).isEqualTo(clazz.owner().ref(Document.class)));
	}

	@Test
	void when_handle_couchbase_document_on_document_with_params_must_annotate() {

		// Given
		ObjectNode document = emptyObjectNode();
		document.put("expiry", 1);
		document.put("expiryExpression", "test");
		document.put("expiryUnit", TimeUnit.HOURS.name());
		document.put("touchOnRead", true);
		ObjectNode content = emptyObjectNode();
		content.set(DOCUMENT.getJsonKey(), document);
		Schema schema = new Schema(null, content, null);

		// When
		couchbaseObjectRule.handleCouchbaseDocument(TEST_NODE_NAME, clazz, schema);

		// Then
		assertThat(clazz.annotations())
				.hasSize(1)
				.first()
				.satisfies(ann -> assertThat(ann.getAnnotationClass()).isEqualTo(clazz.owner().ref(Document.class)))
				.satisfies(ann -> assertThat(ann.getAnnotationMembers().get("expiry")).isNotNull())
				.satisfies(ann -> assertThat(ann.getAnnotationMembers().get("expiryExpression")).isNotNull())
				.satisfies(ann -> assertThat(ann.getAnnotationMembers().get("expiryUnit")).isNotNull())
				.satisfies(ann -> assertThat(ann.getAnnotationMembers().get("touchOnRead")).isNotNull());
	}

	@Test
	void when_handle_document_composite_indexes_on_composite_indexes_must_annotate() {

		// Give
		List<CompositeIndexDef> compositeIndexes = singletonList(
				new CompositeIndexDef(
						asList("field1", "field2"),
						"test"
				)
		);

		// When
		couchbaseObjectRule.handleDocumentCompositeIndexes(TEST_NODE_NAME, clazz, compositeIndexes);

		// Then
		assertThat(clazz.annotations())
				.hasSize(1)
				.first()
				.satisfies(ann -> assertThat(ann.getAnnotationClass()).isEqualTo(clazz.owner().ref(CompositeQueryIndex.class)))
				.satisfies(ann -> assertThat(ann.getAnnotationMembers().get("fields")).isInstanceOfSatisfying(JAnnotationArrayMember.class,
						m -> assertThat(m.annotations()).hasSize(2)
				))
				.satisfies(ann -> assertThat(ann.getAnnotationMembers().get("name")).isNotNull());
	}

	@ParameterizedTest
	@NullAndEmptySource
	void when_handle_document_composite_indexes_on_no_composite_indexes_must_annotate(List<CompositeIndexDef> compositeIndexes) {

		// Give

		// When
		couchbaseObjectRule.handleDocumentCompositeIndexes(TEST_NODE_NAME, clazz, compositeIndexes);

		// Then
		assertThat(clazz.annotations()).isEmpty();
	}
}
