package ca.uhn.fhir.to.jpa.mihin.serviceImpl;

import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import ca.uhn.fhir.to.jpa.mihin.domain.User;
import ca.uhn.fhir.to.jpa.mihin.repository.UserRepository;
import ca.uhn.fhir.to.jpa.mihin.service.AppUserService;

/**
 * @author Ahsan Raza
 *
 */
@Service("appUserService")
public class AppUserServiceImpl implements AppUserService {

	@Autowired
	private UserRepository userRepository;
	
	@Override
	public User getUser(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public User updateUser(User user) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<User> getAllUsers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User getUserById(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String saveNewUser(User user) throws ParseException, Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User getCurrentUser() {
		return userRepository.findByEmail(getPrincipal());
	}

	@Override
	public String getPrincipal() {
		String userName = null;
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if (principal instanceof UserDetails) {
			userName = ((UserDetails) principal).getUsername();
		} else {
			userName = principal.toString();
		}
		return userName;
	}

	@Override
	public String retrievePassword(String email) throws NoSuchAlgorithmException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ModelAndView verifyResetLink(String email, String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String resetPassword(String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String accountResetPassword(String currentPassword, String newPassword) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String accountResetEmail(String currentEmail, String newEmail) {
		// TODO Auto-generated method stub
		return null;
	}

}
