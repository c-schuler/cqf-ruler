package org.opencds.cqf.config;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;
import org.apache.commons.io.IOUtils;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import ca.uhn.fhir.rest.server.interceptor.IServerInterceptor;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@Configuration
//@EnableTransactionManagement()
//@ComponentScan(basePackages = "ca.uhn.fhir.to.jpa.mihin")
//@EnableJpaRepositories("ca.uhn.fhir.to.jpa.mihin.repository")
//@EnableJpaRepositories("ca.uhn.fhir.to.jpa.mihin")
@ImportResource("classpath:spring-security.xml")
public class FhirServerConfigCommon {

	@Autowired
	private Config properties;
	
	@Autowired
	private ResourceLoader resourceLoader;
	
    @Bean
    public IServerInterceptor loggingInterceptor() {
        LoggingInterceptor retVal = new LoggingInterceptor();
        retVal.setLoggerName("cqfruler.access");
        retVal.setMessageFormat(
                "Path[${servletPath}] Source[${requestHeader.x-forwarded-for}] Operation[${operationType} ${operationName} ${idOrResourceName}] UA[${requestHeader.user-agent}] Params[${requestParameters}] ResponseEncoding[${responseEncodingNoDefault}]");
        retVal.setLogExceptions(true);
        retVal.setErrorMessageFormat("ERROR - ${requestVerb} ${requestUrl}");
        return retVal;
    }

    @Bean
    public IServerInterceptor requestLoggingInterceptor() {
        LoggingInterceptor retVal = new LoggingInterceptor();
        retVal.setLoggerName("cqfruler.request");
        retVal.setMessageFormat("${requestVerb} ${servletPath} -\n${requestBodyFhir}");
        retVal.setLogExceptions(false);
        return retVal;
    }
    
    /*
	@Bean
	@Primary
	public DefaultTokenServices customTokenServices() {
		final DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
		defaultTokenServices.setTokenStore(tokenStore());
		return defaultTokenServices;
	}
	
	@Bean
	public TokenStore tokenStore() {
		return new JwtTokenStore(accessTokenConverter());
	}

	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		final JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
		final Resource fileResource = resourceLoader.getResource(
				"file:" + System.getProperty("catalina.base") + System.getProperty("file.separator") + "properties"
						+ System.getProperty("file.separator") + properties.getString("org.mihin.pit-ct1.key"));
		String publicKey = null;
		try {
			publicKey = IOUtils.toString(fileResource.getInputStream(), Charset.defaultCharset());
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		converter.setVerifierKey(publicKey);
		return converter;
	}
	*/
}
