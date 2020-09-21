package org.jsonschema2pojo.springframework.data.couchbase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import org.jsonschema2pojo.AbstractAnnotator;
import org.jsonschema2pojo.springframework.data.couchbase.definition.Cas;
import org.jsonschema2pojo.springframework.data.couchbase.definition.CompositeIndex;
import org.jsonschema2pojo.springframework.data.couchbase.definition.Document;
import org.jsonschema2pojo.springframework.data.couchbase.definition.Field;
import org.jsonschema2pojo.springframework.data.couchbase.definition.Id;
import org.jsonschema2pojo.springframework.data.couchbase.definition.Index;
import org.jsonschema2pojo.springframework.data.couchbase.definition.Join;
import org.jsonschema2pojo.springframework.data.couchbase.deser.DefaultDefinitionDeserializerModifier;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Abstract annotator doing most of the work, except what is specific to the version of Spring Data Couchbase version being used.
 *
 * @author Hector Basset
 */
public abstract class SpringDataCouchbaseAnnotator extends AbstractAnnotator {

	private final ObjectMapper mapper = new ObjectMapper();

	{
		SimpleModule module = new SimpleModule();

		module.setDeserializerModifier(new DefaultDefinitionDeserializerModifier());

		mapper.registerModule(module);
	}

	protected static final String DOCUMENT_ANNOTATION = "org.springframework.data.couchbase.core.mapping.Document";
	protected static final String FETCH_TYPE_ENUM = "org.springframework.data.couchbase.core.query.FetchType";
	protected static final String GENERATED_VALUE_ANNOTATION = "org.springframework.data.couchbase.core.mapping.id.GeneratedValue";
	protected static final String GENERATION_STRATEGY_ENUM = "org.springframework.data.couchbase.core.mapping.id.GenerationStrategy";
	protected static final String HASH_SIDE_ENUM = "org.springframework.data.couchbase.core.query.HashSide";
	protected static final String ID_ATTRIBUTE_ANNOTATION = "org.springframework.data.couchbase.core.mapping.id.IdAttribute";
	protected static final String ID_PREFIX_ANNOTATION = "org.springframework.data.couchbase.core.mapping.id.IdPrefix";
	protected static final String ID_SUFFIX_ANNOTATION = "org.springframework.data.couchbase.core.mapping.id.IdSuffix";
	protected static final String N1QL_JOIN_ANNOTATION = "org.springframework.data.couchbase.core.query.N1qlJoin";
	protected static final String VERSION_ANNOTATION = "org.springframework.data.annotation.Version";

	protected static final String JSON_KEY_CAS = "x-cb-cas";
	protected static final String JSON_KEY_DOCUMENT = "x-cb-document";
	protected static final String JSON_KEY_FIELD = "x-cb-field";
	protected static final String JSON_KEY_ID = "x-cb-id";
	protected static final String JSON_KEY_JOIN = "x-cb-join";

	@Override
	public void propertyInclusion(JDefinedClass clazz, JsonNode schema) {
		Optional.of(schema.path(JSON_KEY_DOCUMENT))
				.map(node -> mapper.convertValue(node, Document.class))
				.ifPresent(document -> {
					JAnnotationUse annotation = clazz.annotate(documentAnnotation(clazz.owner()));

					Optional.ofNullable(document.getExpiry())
							.ifPresent(expiry -> annotation.param("expiry", expiry));
					Optional.ofNullable(document.getExpiryExpression())
							.ifPresent(expiryExpression -> annotation.param("expiryExpression", expiryExpression));
					Optional.ofNullable(document.getExpiryUnit())
							.ifPresent(expiryUnit -> annotation.param("expiryUnit", TimeUnit.valueOf(expiryUnit.name())));
					Optional.ofNullable(document.getTouchOnRead())
							.ifPresent(touchOnRead -> annotation.param("touchOnRead", touchOnRead));
					Optional.ofNullable(document.getCompositeIndexes())
							.ifPresent(indexes -> compositeQueryIndexesAnnotate(clazz, indexes));
				});
	}

	@Override
	public void propertyField(JFieldVar fieldVar, JDefinedClass clazz, String propertyName, JsonNode propertyNode) {
		Optional<Id> idOpt = Optional.of(propertyNode.path(JSON_KEY_ID))
				.map(node -> mapper.convertValue(node, Id.class));
		Optional<Cas> casOpt = Optional.of(propertyNode.path(JSON_KEY_CAS))
				.map(node -> mapper.convertValue(node, Cas.class));
		Optional<Join> joinOpt = Optional.of(propertyNode.path(JSON_KEY_JOIN))
				.map(node -> mapper.convertValue(node, Join.class));
		boolean fieldIsTrueIfMissing = !idOpt.isPresent() && !casOpt.isPresent() && !joinOpt.isPresent();
		Optional<Field> fieldOpt = Optional.of(propertyNode.path(JSON_KEY_FIELD))
				.map(node -> {
					if (node.isMissingNode() && fieldIsTrueIfMissing) {
						return mapper.getNodeFactory().booleanNode(true);
					} else {
						return node;
					}
				})
				.map(node -> mapper.convertValue(node, Field.class));
		checkIncoherentValues(propertyName, idOpt.isPresent(), casOpt.isPresent(), fieldOpt.isPresent());

		idOpt.ifPresent(id -> {
			fieldVar.annotate(idAnnotation(clazz.owner()));

			Optional.ofNullable(id.getGenerated())
					.ifPresent(generatedValue -> {
						JAnnotationUse annotation = fieldVar.annotate(generatedValueAnnotation(clazz.owner()));

						Optional.ofNullable(generatedValue.getDelimiter())
								.ifPresent(delimiter -> annotation.param("delimiter", delimiter));
						Optional.ofNullable(generatedValue.getStrategy())
								.ifPresent(strategy -> annotation.param("strategy", generationStrategyEnum(clazz.owner()).staticRef(strategy.name())));
					});
		});

		casOpt.ifPresent(cas -> {
			fieldVar.annotate(versionAnnotation(clazz.owner()));
		});

		joinOpt.ifPresent(join -> {
			JAnnotationUse annotation = fieldVar.annotate(n1qlJoinAnnotation(clazz.owner()));

			if (join.getOn() == null || join.getOn().isEmpty()) {
				throw new IllegalArgumentException("On is required for join");
			}
			annotation.param("on", join.getOn());
			Optional.ofNullable(join.getFetchType())
					.ifPresent(fetchType -> annotation.param("fetchType", fetchTypeEnum(clazz.owner()).staticRef(fetchType.name())));
			Optional.ofNullable(join.getWhere())
					.ifPresent(where -> annotation.param("where", where));
			Optional.ofNullable(join.getIndex())
					.ifPresent(index -> annotation.param("index", index));
			Optional.ofNullable(join.getRightIndex())
					.ifPresent(rightIndex -> annotation.param("rightIndex", rightIndex));
			Optional.ofNullable(join.getHashSide())
					.ifPresent(hashSide -> annotation.param("hashside", hashSideEnum(clazz.owner()).staticRef(hashSide.name())));
			Optional.ofNullable(join.getKeys())
					.ifPresent(keys -> {
						JAnnotationArrayMember keysAnnotation = annotation.paramArray("keys");
						keys.forEach(keysAnnotation::param);
					});
		});

		fieldOpt.ifPresent(field -> {
			JAnnotationUse annotation = fieldVar.annotate(fieldAnnotation(clazz.owner()));

			Optional.ofNullable(field.getName())
					.ifPresent(name -> fieldAnnotationName(annotation, name));
			Optional.ofNullable(field.getOrder())
					.ifPresent(order -> fieldAnnotationOrder(annotation, order));
			Optional.ofNullable(field.getIdPrefix())
					.ifPresent(idPrefix -> {
						JAnnotationUse ann = fieldVar.annotate(idPrefixAnnotation(clazz.owner()));

						Optional.ofNullable(idPrefix.getOrder())
								.ifPresent(order -> ann.param("order", order));
					});
			Optional.ofNullable(field.getIdAttribute())
					.ifPresent(idAttribute -> {
						JAnnotationUse ann = fieldVar.annotate(idAttributeAnnotation(clazz.owner()));

						Optional.ofNullable(idAttribute.getOrder())
								.ifPresent(order -> ann.param("order", order));
					});
			Optional.ofNullable(field.getIdSuffix())
					.ifPresent(idSuffix -> {
						JAnnotationUse ann = fieldVar.annotate(idSuffixAnnotation(clazz.owner()));

						Optional.ofNullable(idSuffix.getOrder())
								.ifPresent(order -> ann.param("order", order));
					});
			Optional.ofNullable(field.getIndex())
					.ifPresent(index -> queryIndexedAnnotate(clazz.owner(), fieldVar, index));
		});
	}

	protected void checkIncoherentValues(String fieldName, boolean id, boolean cas, boolean field) {
		long truesCount = Stream.of(id, cas, field)
				.filter(b -> b)
				.count();

		if (truesCount > 1) {
			String message = String.format("Incoherent Couchbase id/cas/field combination for field %s, at most one can be true (respectively got %s/%s/%s)", fieldName, id, cas, field);
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 * Handle document level composite indexes annotations.
	 */
	protected abstract void compositeQueryIndexesAnnotate(JDefinedClass clazz, List<CompositeIndex> compositeIndexes);

	protected JClass documentAnnotation(JCodeModel owner) {
		return owner.directClass(DOCUMENT_ANNOTATION);
	}

	/**
	 * Returns correct Field annotation class according to the Spring Data Couchbase version being used.
	 */
	protected abstract JClass fieldAnnotation(JCodeModel owner);

	/**
	 * Handle Field annotation name parameter.
	 */
	protected abstract void fieldAnnotationName(JAnnotationUse annotation, String name);

	/**
	 * Handle Field annotation order parameter.
	 */
	protected abstract void fieldAnnotationOrder(JAnnotationUse annotation, int order);

	protected JClass fetchTypeEnum(JCodeModel owner) {
		return owner.directClass(FETCH_TYPE_ENUM);
	}

	protected JClass generatedValueAnnotation(JCodeModel owner) {
		return owner.directClass(GENERATED_VALUE_ANNOTATION);
	}

	protected JClass generationStrategyEnum(JCodeModel owner) {
		return owner.directClass(GENERATION_STRATEGY_ENUM);
	}

	protected JClass hashSideEnum(JCodeModel owner) {
		return owner.directClass(HASH_SIDE_ENUM);
	}

	/**
	 * Returns correct Id annotation class according to the Spring Data Couchbase version being used.
	 */
	protected abstract JClass idAnnotation(JCodeModel owner);

	protected JClass idAttributeAnnotation(JCodeModel owner) {
		return owner.directClass(ID_ATTRIBUTE_ANNOTATION);
	}

	protected JClass idPrefixAnnotation(JCodeModel owner) {
		return owner.directClass(ID_PREFIX_ANNOTATION);
	}

	protected JClass idSuffixAnnotation(JCodeModel owner) {
		return owner.directClass(ID_SUFFIX_ANNOTATION);
	}

	protected JClass n1qlJoinAnnotation(JCodeModel owner) {
		return owner.directClass(N1QL_JOIN_ANNOTATION);
	}

	/**
	 * Handle field level indexes annotations.
	 */
	protected abstract void queryIndexedAnnotate(JCodeModel owner, JFieldVar fieldVar, Index index);

	protected JClass versionAnnotation(JCodeModel owner) {
		return owner.directClass(VERSION_ANNOTATION);
	}
}
