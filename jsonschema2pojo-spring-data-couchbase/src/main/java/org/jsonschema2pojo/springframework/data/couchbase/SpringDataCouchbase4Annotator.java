package org.jsonschema2pojo.springframework.data.couchbase;

import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import org.jsonschema2pojo.springframework.data.couchbase.definition.CompositeIndex;
import org.jsonschema2pojo.springframework.data.couchbase.definition.Index;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Annotator for Spring Data Couchbase 4.X / Spring Boot <= 2.3.X.
 *
 * @author Hector Basset
 */
public class SpringDataCouchbase4Annotator extends SpringDataCouchbaseAnnotator {

	private final Logger log = Logger.getLogger(getClass().getName());

	protected static final String COMPOSITE_QUERY_INDEX_ANNOTATION = "org.springframework.data.couchbase.core.index.CompositeQueryIndex";
	protected static final String FIELD_ANNOTATION = "org.springframework.data.couchbase.core.mapping.Field";
	protected static final String ID_ANNOTATION = "org.springframework.data.annotation.Id";
	protected static final String QUERY_INDEXED_ANNOTATION = "org.springframework.data.couchbase.core.index.QueryIndexed";
	protected static final String QUERY_INDEX_DIRECTION_ENUM = "org.springframework.data.couchbase.core.index.QueryIndexDirection";

	protected JClass compositeQueryIndexAnnotation(JCodeModel owner) {
		return owner.directClass(COMPOSITE_QUERY_INDEX_ANNOTATION);
	}

	@Override
	protected void compositeQueryIndexesAnnotate(JDefinedClass clazz, List<CompositeIndex> compositeIndexes) {
		compositeIndexes.forEach(index -> {
			JAnnotationUse annotation = clazz.annotate(compositeQueryIndexAnnotation(clazz.owner()));

			if (index.getFields() == null || index.getFields().isEmpty()) {
				throw new IllegalArgumentException("Fields are required for composite indexes");
			}
			if (index.getFields().size() == 1) {
				log.warning("Composite index with only one field found, prefer index metadata on concerned field");
			}
			JAnnotationArrayMember fieldsAnnotation = annotation.paramArray("fields");
			index.getFields().forEach(fieldsAnnotation::param);
			Optional.ofNullable(index.getName())
					.ifPresent(name -> annotation.param("name", name));
		});
	}

	@Override
	protected JClass fieldAnnotation(JCodeModel owner) {
		return owner.directClass(FIELD_ANNOTATION);
	}

	@Override
	protected void fieldAnnotationName(JAnnotationUse annotation, String name) {
		annotation.param("name", name);
	}

	@Override
	protected void fieldAnnotationOrder(JAnnotationUse annotation, int order) {
		annotation.param("order", order);
	}

	@Override
	protected JClass idAnnotation(JCodeModel owner) {
		return owner.directClass(ID_ANNOTATION);
	}

	@Override
	protected void queryIndexedAnnotate(JCodeModel owner, JFieldVar fieldVar, Index index) {
		JAnnotationUse annotation = fieldVar.annotate(queryIndexedAnnotation(owner));

		Optional.ofNullable(index.getDirection())
				.ifPresent(direction -> annotation.param("direction", queryIndexDirectionEnum(owner).staticRef(direction.name())));
		Optional.ofNullable(index.getName())
				.ifPresent(name -> annotation.param("name", name));
	}

	protected JClass queryIndexedAnnotation(JCodeModel owner) {
		return owner.directClass(QUERY_INDEXED_ANNOTATION);
	}

	protected JClass queryIndexDirectionEnum(JCodeModel owner) {
		return owner.directClass(QUERY_INDEX_DIRECTION_ENUM);
	}
}
