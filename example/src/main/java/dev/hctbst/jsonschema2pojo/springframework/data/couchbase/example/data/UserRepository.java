package dev.hctbst.jsonschema2pojo.springframework.data.couchbase.example.data;

import dev.hctbst.jsonschema2pojo.springframework.data.couchbase.example.domain.User;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author Hector Basset
 */
public interface UserRepository extends PagingAndSortingRepository<User, String> {

}
