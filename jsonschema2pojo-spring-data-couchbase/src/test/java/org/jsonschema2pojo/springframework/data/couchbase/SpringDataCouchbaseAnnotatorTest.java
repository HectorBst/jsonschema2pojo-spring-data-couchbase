package org.jsonschema2pojo.springframework.data.couchbase;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMod;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Hector Basset
 */
@ExtendWith(MockitoExtension.class)
public class SpringDataCouchbaseAnnotatorTest {

	final JCodeModel owner = new JCodeModel();
	final JDefinedClass generatedClass = spy(owner._class("org.test.TestClass"));
	final JFieldVar field = spy(generatedClass.field(JMod.NONE, String.class, "testField"));
	final ObjectNode jsonNode = new JsonNodeFactory(true).objectNode();
	final SpringDataCouchbaseAnnotator annotator = new SpringDataCouchbase4Annotator();

	public SpringDataCouchbaseAnnotatorTest() throws JClassAlreadyExistsException {
	}

	private JClass annotationMatch(Function<JCodeModel, JClass> annotationGetter) {
		return argThat(a -> a.fullName().equals(annotationGetter.apply(owner).fullName()));
	}

	static private Stream<Arguments> whenDocumentArguments() {
		return Stream.of(
				Arguments.of(true, true),
				Arguments.of(false, false),
				Arguments.of(null, false)
		);
	}

	@ParameterizedTest
	@MethodSource("whenDocumentArguments")
	void when_document(Boolean valueInSchema, boolean mustAnnotate) {

		// Given
		if (valueInSchema != null) {
			jsonNode.put(SpringDataCouchbaseAnnotator.JSON_KEY_DOCUMENT, valueInSchema);
		}

		// When
		annotator.propertyInclusion(generatedClass, jsonNode);

		// Then
		if (mustAnnotate) {
			verify(generatedClass, times(1)).annotate(annotationMatch(annotator::documentAnnotation));
		} else {
			verify(generatedClass, times(0)).annotate(any(JClass.class));
		}
	}

	private enum FieldAnnotation {
		NONE,
		ID,
		CAS,
		FIELD,
		EXCEPTION
	}

	static private Stream<Arguments> whenFieldArguments() {
		return Stream.of(
				Arguments.of(true, true, true, FieldAnnotation.EXCEPTION),
				Arguments.of(true, true, false, FieldAnnotation.EXCEPTION),
				Arguments.of(true, true, null, FieldAnnotation.EXCEPTION),
				Arguments.of(true, false, true, FieldAnnotation.EXCEPTION),
				Arguments.of(true, false, false, FieldAnnotation.ID),
				Arguments.of(true, false, null, FieldAnnotation.ID),
				Arguments.of(true, null, true, FieldAnnotation.EXCEPTION),
				Arguments.of(true, null, false, FieldAnnotation.ID),
				Arguments.of(true, null, null, FieldAnnotation.ID),//normal case: id
				Arguments.of(false, true, true, FieldAnnotation.EXCEPTION),
				Arguments.of(false, true, false, FieldAnnotation.CAS),
				Arguments.of(false, true, null, FieldAnnotation.CAS),
				Arguments.of(false, false, true, FieldAnnotation.FIELD),
				Arguments.of(false, false, false, FieldAnnotation.NONE),
				Arguments.of(false, false, null, FieldAnnotation.FIELD),
				Arguments.of(false, null, true, FieldAnnotation.FIELD),
				Arguments.of(false, null, false, FieldAnnotation.NONE),
				Arguments.of(false, null, null, FieldAnnotation.FIELD),
				Arguments.of(null, true, true, FieldAnnotation.EXCEPTION),
				Arguments.of(null, true, false, FieldAnnotation.CAS),
				Arguments.of(null, true, null, FieldAnnotation.CAS),//normal case: cas
				Arguments.of(null, false, true, FieldAnnotation.FIELD),
				Arguments.of(null, false, false, FieldAnnotation.NONE),
				Arguments.of(null, false, null, FieldAnnotation.FIELD),
				Arguments.of(null, null, true, FieldAnnotation.FIELD),
				Arguments.of(null, null, false, FieldAnnotation.NONE),
				Arguments.of(null, null, null, FieldAnnotation.FIELD)//normal case: field
		);
	}

	@ParameterizedTest
	@MethodSource("whenFieldArguments")
	void when_field(Boolean idInSchema, Boolean casInSchema, Boolean fieldInSchema, FieldAnnotation annotation) {

		// Given
		if (idInSchema != null) {
			jsonNode.put(SpringDataCouchbaseAnnotator.JSON_KEY_ID, idInSchema);
		}
		if (casInSchema != null) {
			jsonNode.put(SpringDataCouchbaseAnnotator.JSON_KEY_CAS, casInSchema);
		}
		if (fieldInSchema != null) {
			jsonNode.put(SpringDataCouchbaseAnnotator.JSON_KEY_FIELD, fieldInSchema);
		}

		// When
		IllegalArgumentException exception = null;
		try {
			annotator.propertyField(field, generatedClass, field.name(), jsonNode);
		} catch (IllegalArgumentException e) {
			exception = e;
		}

		// Then
		switch (annotation) {
			case NONE:
				verify(field, times(0)).annotate(any(JClass.class));
				break;
			case ID:
				verify(field, times(1)).annotate(annotationMatch(annotator::idAnnotation));
				break;
			case CAS:
				verify(field, times(1)).annotate(annotationMatch(annotator::versionAnnotation));
				break;
			case FIELD:
				verify(field, times(1)).annotate(annotationMatch(annotator::fieldAnnotation));
				break;
			case EXCEPTION:
				assertThat(exception).isNotNull();
				break;
			default:
				throw new IllegalArgumentException("Unexpected FieldAnnotation enum value: " + annotation);
		}
	}
}
