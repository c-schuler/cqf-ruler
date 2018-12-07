/**
 * 
 */
package ca.uhn.fhir.to;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import com.typesafe.config.Config;

/**
 * @author Ahsan Raza
 *
 */

//@Configuration
//@EnableResourceServer
public class OAuth2ResourceServerConfig /*extends ResourceServerConfigurerAdapter*/ {

	@Autowired
	private Config properties;

	
//
//	@Override
//	public void configure(final HttpSecurity http) throws Exception {
//		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).and().authorizeRequests()
//				.antMatchers("/baseDstu3/**").access("#oauth2.hasScope('read write')").anyRequest().permitAll();
//
//	}

	// JWT token store

//	@Override
//	public void configure(final ResourceServerSecurityConfigurer config) {
//		config.tokenServices(tokenServices());
//	}


}