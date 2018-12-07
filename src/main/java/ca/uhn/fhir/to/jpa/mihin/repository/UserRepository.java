/**
 * 
 */
package ca.uhn.fhir.to.jpa.mihin.repository;


import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ca.uhn.fhir.to.jpa.mihin.domain.User;

/**
 * @author Ahsan Raza
 *
 */
@Repository("userRepository")
//@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
	User findByEmail(String email);
}
