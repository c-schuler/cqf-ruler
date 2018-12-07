/**
 * 
 */
package ca.uhn.fhir.to.jpa.mihin.security;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.WebUtils;

/**
 * @author Ahsan Raza
 *
 */
public class CustomFilter extends GenericFilterBean {
	private final static Logger logger = LoggerFactory.getLogger(CustomFilter.class);

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		HttpSession session = httpRequest.getSession(true);
		String uiPath = "/baseDstu3/";
		if (httpRequest.getRequestURI().contains(uiPath)) {
			String hToken = extractHeaderToken(httpRequest);
			if (hToken == null) {
				logger.debug("Token not found. Trying session.");
				// String sessionToken =
				// httpRequest.getSession().getAttribute("token").toString();
				BufferedReader br = null;
				FileReader fr = null;
				String sessionToken = "";

				// br = new BufferedReader(new FileReader(FILENAME));
				fr = new FileReader(System.getProperty("catalina.base")+"/target/jwt");
				br = new BufferedReader(fr);
				String sCurrentLine;
				while ((sCurrentLine = br.readLine()) != null) {
					sessionToken = sessionToken + sCurrentLine;
				}

				if (sessionToken != "") {
					CustomHttpServletRequestWrapper wrapperRequest = new CustomHttpServletRequestWrapper(httpRequest);
					wrapperRequest.addHeader("Authorization", OAuth2AccessToken.BEARER_TYPE + " " + sessionToken);
					chain.doFilter(wrapperRequest, response);
				} else {
					throw new IllegalArgumentException("No access token provided.");
				}
			} else {
				chain.doFilter(request, response);
			}
		} else {
			chain.doFilter(request, response);
		}
	}

	/**
	 * Extract the OAuth bearer token from a header.
	 * 
	 * @param request
	 *            The request.
	 * @return The token, or null if no OAuth authorization header was supplied.
	 */
	protected String extractHeaderToken(HttpServletRequest request) {
		Enumeration<String> headers = request.getHeaders("Authorization");
		while (headers.hasMoreElements()) { // typically there is only one (most servers enforce that)
			String value = headers.nextElement();
			if ((value.toLowerCase().startsWith(OAuth2AccessToken.BEARER_TYPE.toLowerCase()))) {
				String authHeaderValue = value.substring(OAuth2AccessToken.BEARER_TYPE.length()).trim();
				// Add this here for the auth details later. Would be better to change the
				// signature of this method.
				// request.setAttribute(OAuth2AuthenticationDetails.ACCESS_TOKEN_TYPE,
				// value.substring(0, OAuth2AccessToken.BEARER_TYPE.length()).trim());
				int commaIndex = authHeaderValue.indexOf(',');
				if (commaIndex > 0) {
					authHeaderValue = authHeaderValue.substring(0, commaIndex);
				}
				return authHeaderValue;
			}
		}

		return null;
	}
}
