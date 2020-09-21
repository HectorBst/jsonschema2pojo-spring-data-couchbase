package org.jsonschema2pojo.springframework.data.couchbase;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import org.jsonschema2pojo.springframework.data.couchbase.definition.CompositeIndex;
import org.jsonschema2pojo.springframework.data.couchbase.definition.Index;

import java.util.List;
import java.util.logging.Logger;

/**
 * Annotator for Spring Data Couchbase 3.X / Spring Boot <= 2.2.X.
 *
 * @author Hector Basset
 */
public class SpringDataCouchbase3Annotator extends SpringDataCouchbaseAnnotator {

	private final Logger log = Logger.getLogger(getClass().getName());

	protected static final String FIELD_ANNOTATION = "com.couchbase.client.java.repository.annotation.Field";
	protected static final String ID_ANNOTATION = "com.couchbase.client.java.repository.annotation.Id";

	@Override
	protected void compositeQueryIndexesAnnotate(JDefinedClass clazz, List<CompositeIndex> compositeIndexes) {
		log.warning("document composite indexes are not supported in Spring Data Couchbase 3.X");
	}

	@Override
	protected JClass fieldAnnotation(JCodeModel owner) {
		return owner.directClass(FIELD_ANNOTATION);
	}

	@Override
	protected void fieldAnnotationName(JAnnotationUse annotation, String name) {
		annotation.param("value", name);
	}

	@Override
	protected void fieldAnnotationOrder(JAnnotationUse annotation, int order) {
		log.warning("field order is not supported in Spring Data Couchbase 3.X");
	}

	@Override
	protected JClass idAnnotation(JCodeModel owner) {
		return owner.directClass(ID_ANNOTATION);
	}

	@Override
	protected void queryIndexedAnnotate(JCodeModel owner, JFieldVar fieldVar, Index index) {
		log.warning("field indexes are not supported in Spring Data Couchbase 3.X");
	}
}
