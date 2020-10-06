package io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.core.convert.CouchbaseCustomConversions;
import org.springframework.data.couchbase.core.mapping.CouchbaseMappingContext;

import java.util.List;

/**
 * @author Hector Basset
 */
@Configuration
public class CouchbaseConfig extends AbstractCouchbaseConfiguration {

	@Override
	public String getConnectionString() {
		return "127.0.0.1";
	}

	@Override
	public String getUserName() {
		return "example";
	}

	@Override
	public String getPassword() {
		return "password";
	}

	@Override
	public String getBucketName() {
		return "example";
	}

	@Override
	public String typeKey() {
		return "type";
	}

	@Override
	public CouchbaseMappingContext couchbaseMappingContext(CustomConversions customConversions) throws Exception {
		CouchbaseMappingContext couchbaseMappingContext = super.couchbaseMappingContext(customConversions);
		couchbaseMappingContext.setAutoIndexCreation(true);
		return couchbaseMappingContext;
	}

	@Override
	public CustomConversions customConversions() {
		return new CouchbaseCustomConversions(List.of(
				new UUIDToStringConverter(), new StringToUUIDConverter()
		));
	}
}
