package dev.hctbst.jsonschema2pojo.springframework.data.couchbase.example.data;

import com.couchbase.client.java.query.QueryScanConsistency;
import dev.hctbst.jsonschema2pojo.springframework.data.couchbase.example.domain.Pet;
import org.springframework.data.couchbase.repository.ScanConsistency;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * @author Hector Basset
 */
public interface PetRepository extends PagingAndSortingRepository<Pet, String> {

	@ScanConsistency(query = QueryScanConsistency.REQUEST_PLUS)
	List<Pet> findAllByUserId(String userId);
}
