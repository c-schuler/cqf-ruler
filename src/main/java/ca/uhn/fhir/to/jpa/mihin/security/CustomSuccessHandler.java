/**
 * 
 */
package ca.uhn.fhir.to.jpa.mihin.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * @author Ahsan Raza
 *
 */
@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	/*
	 * @Autowired private UserLoginDAO userLoginDAO;
	 */
	/*
	 * @Autowired private AppUserService appUserService;
	 */
	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {
		handle(request, response, authentication);
		clearAuthenticationAttributes(request);
	}

	@Override
	protected void handle(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException {
		String targetUrl = determineTargetUrl(request, authentication);

		if (response.isCommitted()) {
			System.out.println("Can't redirect");
			return;
		}

		redirectStrategy.sendRedirect(request, response, targetUrl);
	}

	protected String determineTargetUrl(HttpServletRequest request, Authentication authentication) {
		String url = "";

		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

		List<String> roles = new ArrayList<String>();

		for (GrantedAuthority a : authorities) {
			roles.add(a.getAuthority());
		}

		if (isAdmin(roles)) {
			url = "/admin.htm";
		} else if (roles.size() > 0) {
			url = "/";
		} else {
			url = "/error-login.htm";
		}

		return url;
	}

	public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
		this.redirectStrategy = redirectStrategy;
	}

	protected RedirectStrategy getRedirectStrategy() {
		return redirectStrategy;
	}

	/*
	 * private boolean isConsumer(List<String> roles) { if
	 * (roles.contains("ROLE_CONSUMER")) { return true; } return false; }
	 */

	private boolean isAdmin(List<String> roles) {
		if (roles.contains("ROLE_ADMIN")) {
			return true;
		}
		return false;
	}

	/*
	 * private boolean isDba(List<String> roles) { if (roles.contains("ROLE_DBA")) {
	 * return true; } return false; }
	 */

	/*
	 * private void logUserLogin(HttpServletRequest request, Authentication
	 * authentication) { try { // log login UserLoginLog userLoginLog = new
	 * UserLoginLog();
	 * userLoginLog.setUserId(appUserService.getCurrentUser().getId()); String
	 * xForwardedForHeader = request.getHeader("X-Forwarded-For"); if
	 * (xForwardedForHeader != null) userLoginLog.setIpAddress(xForwardedForHeader);
	 * else userLoginLog.setIpAddress(request.getRemoteAddr()); if
	 * (request.getHeader("User-Agent") != null)
	 * userLoginLog.setUserAgent(request.getHeader("User-Agent").toString());
	 * userLoginLog.setSessionToken(request.getSession().getId());
	 * 
	 * userLoginLog.setCreateDate(new Date()); userLoginLog.setUpdateDate(new
	 * Date()); request.getSession(); userLoginDAO.saveUserLoginLog(userLoginLog); }
	 * catch (Exception e) {
	 * 
	 * } }
	 */
}