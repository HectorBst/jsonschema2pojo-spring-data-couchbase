package dev.hctbst.jsonschema2pojo.springframework.data.couchbase.example;

import dev.hctbst.jsonschema2pojo.springframework.data.couchbase.example.data.AddressRepository;
import dev.hctbst.jsonschema2pojo.springframework.data.couchbase.example.data.PetRepository;
import dev.hctbst.jsonschema2pojo.springframework.data.couchbase.example.data.UserRepository;
import dev.hctbst.jsonschema2pojo.springframework.data.couchbase.example.domain.Address;
import dev.hctbst.jsonschema2pojo.springframework.data.couchbase.example.domain.Pet;
import dev.hctbst.jsonschema2pojo.springframework.data.couchbase.example.domain.User;
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
	private final PetRepository petRepository;

	@Autowired
	public Example(
			UserRepository userRepository,
			AddressRepository addressRepository,
			PetRepository petRepository
	) {
		this.userRepository = userRepository;
		this.addressRepository = addressRepository;
		this.petRepository = petRepository;
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
				.withUserId(user.getId())
				.withStreet("Example Street")
				.withZipCode("ZIP")
				.withCity("Test city");
		log.info(address.toString());
		address = addressRepository.save(address);
		log.info(address.toString());

		Pet cat = new Pet()
				.withUserId(user.getId())
				.withName("Cat Example")
				.withDescription("Description 1")
				.withType(Pet.Type.CAT);
		log.info(cat.toString());
		petRepository.save(cat);
		log.info(cat.toString());

		Pet dog = new Pet()
				.withUserId(user.getId())
				.withName("Dog Example")
				.withDescription("Description 2")
				.withType(Pet.Type.DOG);
		log.info(dog.toString());
		petRepository.save(dog);
		log.info(dog.toString());

		log.info(petRepository.findAllByUserId(user.getId()).toString());
	}
}
