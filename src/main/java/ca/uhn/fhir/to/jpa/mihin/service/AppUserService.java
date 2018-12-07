/**
 * 
 */
package ca.uhn.fhir.to.jpa.mihin.service;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

import ca.uhn.fhir.to.jpa.mihin.domain.User;

/**
 * @author Ahsan Raza
 *
 */
public interface AppUserService {

	public User getUser(String email);

	@Transactional
	public User updateUser(User user);

	public List<User> getAllUsers();

	public User getUserById(int id);

	@Transactional
	public String saveNewUser(User user) throws ParseException, Exception;

	public User getCurrentUser();

	String getPrincipal();

	@Transactional
	String retrievePassword(String email) throws NoSuchAlgorithmException;

	@Transactional
	public ModelAndView verifyResetLink(String email, String key);

	@Transactional
	public String resetPassword(String password);

	@Transactional
	public String accountResetPassword(String currentPassword, String newPassword);

	@Transactional
	public String accountResetEmail(String currentEmail, String newEmail);
}
