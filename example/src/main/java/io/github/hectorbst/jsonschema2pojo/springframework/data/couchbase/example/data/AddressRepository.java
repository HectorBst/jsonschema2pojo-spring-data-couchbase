package io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.example.data;

import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.example.domain.Address;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * @author Hector Basset
 */
public interface AddressRepository extends PagingAndSortingRepository<Address, String> {

}
