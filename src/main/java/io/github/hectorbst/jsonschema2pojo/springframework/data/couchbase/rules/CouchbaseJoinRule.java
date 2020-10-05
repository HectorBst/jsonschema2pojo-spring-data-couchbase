package io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.rules;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JFieldVar;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.SpringDataCouchbaseRuleFactory;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.definitions.JoinDef;
import org.jsonschema2pojo.Schema;
import org.jsonschema2pojo.rules.Rule;
import org.springframework.data.couchbase.core.query.FetchType;
import org.springframework.data.couchbase.core.query.HashSide;
import org.springframework.data.couchbase.core.query.N1qlJoin;

import java.util.Optional;

import static io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.util.SpringDataCouchbaseHelper.handleFieldMetadataExclusivity;

/**
 * @author Hector Basset
 */
public class CouchbaseJoinRule implements Rule<JFieldVar, JFieldVar> {

	protected static final String JSON_KEY_JOIN = "x-cb-join";

	protected final SpringDataCouchbaseRuleFactory ruleFactory;

	public CouchbaseJoinRule(SpringDataCouchbaseRuleFactory ruleFactory) {
		this.ruleFactory = ruleFactory;
	}

	@Override
	public JFieldVar apply(String nodeName, JsonNode node, JsonNode parent, JFieldVar field, Schema schema) {
		Optional.of(node.path(JSON_KEY_JOIN))
				.map(n -> ruleFactory.getObjectMapper().convertValue(n, JoinDef.class))
				.ifPresent(join -> {
					handleFieldMetadataExclusivity(nodeName, node, "join");

					JCodeModel owner = field.type().owner();

					JAnnotationUse annotation = field.annotate(N1qlJoin.class);

					if (join.getOn() == null || join.getOn().isEmpty()) {
						throw new IllegalArgumentException("On is required for join");
					}
					annotation.param("on", join.getOn());

					Optional.ofNullable(join.getFetchType())
							.ifPresent(fetchType -> annotation.param("fetchType", owner.ref(FetchType.class).staticRef(fetchType.name())));

					Optional.ofNullable(join.getWhere())
							.ifPresent(where -> annotation.param("where", where));

					Optional.ofNullable(join.getIndex())
							.ifPresent(index -> annotation.param("index", index));

					Optional.ofNullable(join.getRightIndex())
							.ifPresent(rightIndex -> annotation.param("rightIndex", rightIndex));

					Optional.ofNullable(join.getHashSide())
							.ifPresent(hashSide -> annotation.param("hashside", owner.ref(HashSide.class).staticRef(hashSide.name())));

					Optional.ofNullable(join.getKeys())
							.ifPresent(keys -> {
								JAnnotationArrayMember keysAnnotation = annotation.paramArray("keys");
								keys.forEach(keysAnnotation::param);
							});
				});

		return field;
	}
}
