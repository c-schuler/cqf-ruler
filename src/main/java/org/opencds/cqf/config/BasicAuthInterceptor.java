/**
 * 
 */
//package ca.uhn.fhir.jpa.demo;
package org.opencds.cqf.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.interceptor.InterceptorAdapter;
import ca.uhn.fhir.to.jpa.mihin.domain.User;
import ca.uhn.fhir.to.jpa.mihin.service.AppUserService;

/**
 * @author Ahsan Raza
 *
 */
public class BasicAuthInterceptor extends InterceptorAdapter {

	/**
	 * This interceptor implements HTTP Basic Auth, which specifies that a username
	 * and password are provided in a header called Authorization.
	 */
	@Autowired
	private AppUserService appUserService;

	@Override
	public boolean incomingRequestPostProcessed(RequestDetails theRequestDetails, HttpServletRequest theRequest,
			HttpServletResponse theResponse) throws AuthenticationException {
		String authHeader = theRequest.getHeader("BasicAuth");
		// String authHeader2 = theRequest.getHeader("Authorization");
		String goodHeader;
		// The format of the header must be:
		// Authorization: Basic [base64 of username:password]
		if (authHeader != null && authHeader.startsWith("Basic ") == true) {
			goodHeader = authHeader;
		}
		// } else if (authHeader2 != null && authHeader2.startsWith("Basic ") == true) {
		// goodHeader = authHeader2;
		// }
		else {
			throw new AuthenticationException("Missing or invalid Authorization header");
		}

		String base64 = goodHeader.substring("Basic ".length());
		String base64decoded = new String(Base64.decodeBase64(base64));
		String[] parts = base64decoded.split("\\:");
		String username = parts[0];
		String password = parts[1];

		// Here we test for a hardcoded username & password. This is not typically how
		// you would implement this in a production system of course..

		User user = appUserService.getUser(username);
		if (user == null) {
			throw new UsernameNotFoundException(username);
		} else {
			BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			if (passwordEncoder.matches(password, user.getPassword())) {
				return true;
			} else {
				throw new AuthenticationException("Invalid username or password");
			}
		}
	}

	public void basicAuthInterceptorRealm() {
		AuthenticationException ex = new AuthenticationException();
		ex.addAuthenticateHeaderForRealm("myRealm");
		throw ex;
	}

}
