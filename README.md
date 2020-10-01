[![GitHub Workflow Status](https://img.shields.io/github/workflow/status/hectorbst/jsonschema2pojo-spring-data-couchbase/Build?label=Build)](https://github.com/HectorBst/jsonschema2pojo-spring-data-couchbase/actions?query=workflow%3ABuild)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.hectorbst/jsonschema2pojo-spring-data-couchbase?label=Maven%20Central)](https://search.maven.org/artifact/io.github.hectorbst/jsonschema2pojo-spring-data-couchbase)
[![GitHub](https://img.shields.io/github/license/hectorbst/jsonschema2pojo-spring-data-couchbase?label=Licence)](LICENSE)

# jsonschema2pojo-spring-data-couchbase

This project is a [*jsonschema2pojo*](https://github.com/joelittlejohn/jsonschema2pojo) extension dedicated to
[*Spring Data Couchbase*](https://docs.spring.io/spring-data/couchbase/docs/current/reference/html) entities generation.

## Features

### Couchbase document

At the schema of an object level, it is possible to define a POJO as being a Couchbase document using the custom JSON
property `x-cb-document`.

* If missing, the value of this property is `false`.
* The `true` value is equivalent to `{}`.
* The schema of the content of this custom property is available [here](src/main/resources/schema/document.json).

This property is responsible for generating the [`Document`](https://docs.spring.io/spring-data/couchbase/docs/current/api/org/springframework/data/couchbase/core/mapping/Document.html)
annotation.

E.g., this schema:
```json
{
	"title": "Sample entity",
	"type": "object",
	"x-cb-document": true,
	"properties": {
		"..."
	}
}
```
Will produce:
```java
@Document
public class Entity {
	...
}
```

Some sub-properties are available to manage the annotation parameters (detailed in the annotation's documentation).

E.g., this schema:
```json
{
	"title": "Sample entity",
	"type": "object",
	"x-cb-document": {
		"expiry": 2,
		"expiryUnit": "MINUTES",
		"touchOnRead": true
	},
	"properties": {
		"..."
	}
}
```
Will produce:
```java
@Document(expiry = 2, expiryUnit = TimeUnit.MINUTES, touchOnRead = true)
public class Entity {
	...
}
```

### Document id

At the property of an object level, it is possible to define a field as being a document id using the custom JSON
property `x-cb-id`.

* If missing, the value of this property is `false`.
* The `true` value is equivalent to `{}`.
* The schema of the content of this custom property is available [here](src/main/resources/schema/id.json).

This property is responsible for generating the [`Id`](https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/annotation/Id.html)
annotation.

E.g., this schema:
```json
{
	"..."
	"properties": {
		"..."
		"id": {
			"title": "Entity id",
			"type": "string",
			"format": "uuid",
			"x-cb-id": true
		},
		"..."
	}
}
```
Will produce:
```java
@Id
private UUID id;
```

A sub-property `generated` is available to manage the generating of the [`GeneratedValue`](https://docs.spring.io/spring-data/couchbase/docs/current/api/org/springframework/data/couchbase/core/mapping/id/GeneratedValue.html)
annotation and its parameters (detailed in the annotation's documentation).

E.g., this schema:
```json
{
	"..."
	"properties": {
		"..."
		"id": {
			"title": "Entity id",
			"type": "string",
			"format": "uuid",
			"x-cb-id": {
				"generated": true
			}
		},
		"..."
	}
}
```
Will produce:
```java
@Id
@GeneratedValue
private UUID id;
```

And this schema:
```json
{
	"..."
	"properties": {
		"..."
		"id": {
			"title": "Entity id",
			"type": "string",
			"format": "uuid",
			"x-cb-id": {
				"generated": {
					"delimiter": "::",
					"strategy": "USE_ATTRIBUTES"
				}
			}
		},
		"..."
	}
}
```
Will produce:
```java
@Id
@GeneratedValue(delimiter = "::", strategy = GenerationStrategy.USE_ATTRIBUTES)
private String id;
```

### Document CAS

At the property of an object level, it is possible to define a field as being a document CAS (Compare And Swap) using
the custom JSON property `x-cb-cas`.

If missing, the value of this property is `false`.

This property is responsible for generating the [`Version`](https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/annotation/Version.html)
annotation.

Note that the type of a CAS field must be `Long` or `long`. This can be achieved through a `formatTypeMapping` or the
`useLongIntegers` option.

E.g., this schema:
```json
{
	"..."
	"properties": {
		"..."
		"cas": {
			"title": "Couchbase CAS",
			"type": "integer",
			"format": "int64",
			"x-cb-cas": true
		},
		"..."
	}
}
```
Will produce:
```java
@Version
private Long cas;
```

### Document field

At the property of an object level, it is possible to define a field as being a document id using the custom JSON
property `x-cb-field`.

* If missing and if the field is not already marked as being an id, a cas or a join, the value of this property is
`true`.
* If missing and if the field is already marked as being an id, a cas or a join, the value of this property is
`false`.
* The `true` value is equivalent to `{}`.
* The schema of the content of this custom property is available [here](src/main/resources/schema/field.json).

This property is responsible for generating the [`Field`](https://docs.spring.io/spring-data/couchbase/docs/current/api/org/springframework/data/couchbase/core/mapping/Field.html)
annotation.

E.g., this schema:
```json
{
	"..."
	"properties": {
		"..."
		"field": {
			"title": "A field",
			"type": "string"
		},
		"..."
	}
}
```
Will produce:
```java
@Field
private String field;
```

Some sub-properties are available to manage the annotation parameters (detailed in the annotation's documentation).

E.g., this schema:
```json
{
	"..."
	"properties": {
		"..."
		"field": {
			"title": "A field",
			"type": "string",
			"x-cb-field": {
				"name": "field_",
				"order": 5
			}
		},
		"..."
	}
}
```
Will produce:
```java
@Field(name = "field_", order = 5)
private String field;
```

Some sub-properties are available to manage the generating of the annotations targeted at building the document id using the field value, and their parameters (detailed in the annotation's documentation).
* `idPrefix` for the [`IdPrefix`](https://docs.spring.io/spring-data/couchbase/docs/current/api/org/springframework/data/couchbase/core/mapping/id/IdPrefix.html)
annotation.
* `idAttribute` for the [`IdAttribute`](https://docs.spring.io/spring-data/couchbase/docs/current/api/org/springframework/data/couchbase/core/mapping/id/IdAttribute.html)
annotation.
* `idSuffix` for the [`IdSuffix`](https://docs.spring.io/spring-data/couchbase/docs/current/api/org/springframework/data/couchbase/core/mapping/id/IdSuffix.html)
annotation.

E.g., this schema:
```json
{
	"..."
	"properties": {
		"..."
		"field": {
			"title": "A field",
			"type": "string",
			"x-cb-field": {
				"idPrefix": true
			}
		},
		"..."
	}
}
```
Will produce:
```java
@Field
@IdPrefix
private String field;
```
And this schema:
```json
{
	"..."
	"properties": {
		"..."
		"field": {
			"title": "A field",
			"type": "string",
			"x-cb-field": {
				"idAttribute": {
					"order": 2
				}
			}
		},
		"..."
	}
}
```
Will produce:
```java
@Field
@IdAttribute(order = 2)
private String field;
```

## Maven configuration

Here is an example of how the extension can be added to the jsonschema2pojo Maven plugin.

```xml
<plugin>
    <groupId>org.jsonschema2pojo</groupId>
    <artifactId>jsonschema2pojo-maven-plugin</artifactId>
    <version>${jsonschema2pojo.version}</version>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
            <configuration>
                ...
                <!-- Extension RuleFactory -->
                <customRuleFactory>
                    org.jsonschema2pojo.springframework.data.couchbase.rules.SpringDataCouchbaseRuleFactory
                </customRuleFactory>
            </configuration>
        </execution>
    </executions>
    <dependencies>
        <!-- Extension dependency -->
        <dependency>
            <groupId>com.github.hectorbst.jsonschema2pojo</groupId>
            <artifactId>jsonschema2pojo-spring-data-couchbase</artifactId>
            <version>${jsonschema2pojo-spring-data-couchbase.version}</version>
        </dependency>
        <!-- Spring Data Couchbase dependency -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-couchbase</artifactId>
            <version>${spring-boot.version}</version>
        </dependency>
    </dependencies>
</plugin>
```

A more complete example is available [here](example).

## License

This project is released under version 2.0 of the [Apache License](https://www.apache.org/licenses/LICENSE-2.0).
