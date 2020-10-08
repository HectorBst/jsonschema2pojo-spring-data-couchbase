package io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;

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
		return "DOCTYPE";
	}

	@Override
	protected boolean autoIndexCreation() {
		return true;
	}
}
