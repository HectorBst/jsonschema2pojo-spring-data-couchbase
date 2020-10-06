package io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.example;

import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.example.data.AddressRepository;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.example.data.PurchaseRepository;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.example.data.UserRepository;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.example.domain.Address;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.example.domain.Purchase;
import io.github.hectorbst.jsonschema2pojo.springframework.data.couchbase.example.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Hector Basset
 */
@Component
public class Example {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final UserRepository userRepository;
	private final AddressRepository addressRepository;
	private final PurchaseRepository purchaseRepository;

	@Autowired
	public Example(UserRepository userRepository, AddressRepository addressRepository, PurchaseRepository purchaseRepository) {
		this.userRepository = userRepository;
		this.addressRepository = addressRepository;
		this.purchaseRepository = purchaseRepository;
	}

	@PostConstruct
	public void example() {

		User user = new User()
				.withFirstName("Example")
				.withLastName("User")
				.withEmailAddress("example@test.test");
		log.info(user.toString());
		user = userRepository.save(user);
		log.info(user.toString());

		Address address = new Address()
				.withOwnerId(user.getId())
				.withStreet("Example Street")
				.withZipCode("ZIP")
				.withCity("Test city");
		log.info(address.toString());
		address = addressRepository.save(address);
		log.info(address.toString());

		Purchase purchase1 = new Purchase()
				.withBuyerId(user.getId())
				.withTitle("Purchase 1")
				.withDescription("Description 1");
		log.info(purchase1.toString());
		purchaseRepository.save(purchase1);
		log.info(purchase1.toString());

		Purchase purchase2 = new Purchase()
				.withBuyerId(user.getId())
				.withTitle("Purchase 2")
				.withDescription("Description 2");
		log.info(purchase2.toString());
		purchaseRepository.save(purchase2);
		log.info(purchase2.toString());
	}
}
