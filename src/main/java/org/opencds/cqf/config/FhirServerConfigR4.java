package org.opencds.cqf.config;

import java.io.File;
import java.util.Properties;

import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import ca.uhn.fhir.jpa.config.BaseJavaConfigR4;
import ca.uhn.fhir.jpa.dao.DaoConfig;
import ca.uhn.fhir.jpa.search.DatabaseBackedPagingProvider;
import ca.uhn.fhir.jpa.search.LuceneSearchMappingFactory;
import ca.uhn.fhir.rest.server.interceptor.RequestValidatingInterceptor;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import org.apache.commons.dbcp2.BasicDataSource;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

@Configuration
@ComponentScan(basePackages = "ca.uhn.fhir.to.jpa.mihin")
@EnableJpaRepositories("ca.uhn.fhir.to.jpa.mihin.repository")
@Import(FhirServerConfigCommon.class)
@EnableTransactionManagement()
public class FhirServerConfigR4 extends BaseJavaConfigR4 {

	@Autowired
	private Config properties;

	@Autowired
	private ResourceLoader resourceLoader;
	
    @Bean()
    public DaoConfig daoConfig() {
        DaoConfig retVal = new DaoConfig();
        retVal.setAllowMultipleDelete(true);
        retVal.setAllowInlineMatchUrlReferences(true);
        retVal.setAllowExternalReferences(true);
        retVal.getTreatBaseUrlsAsLocal().add("http://measure.eval.kanvix.com/cqf-ruler/baseR4");
        retVal.getTreatBaseUrlsAsLocal().add("https://measure.eval.kanvix.com/cqf-ruler/baseR4");
        retVal.setCountSearchResultsUpTo(50000);
        retVal.setIndexMissingFields(DaoConfig.IndexEnabledEnum.ENABLED);
        retVal.setFetchSizeDefaultMaximum(10000);
        retVal.setAllowMultipleDelete(true);
        return retVal;
    }

    @Override
    @Bean(autowire = Autowire.BY_TYPE)
    public DatabaseBackedPagingProvider databaseBackedPagingProvider() {
        DatabaseBackedPagingProvider retVal = super.databaseBackedPagingProvider();
        retVal.setDefaultPageSize(20);
        retVal.setMaximumPageSize(500);
        return retVal;
    }

    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean retVal = new LocalContainerEntityManagerFactoryBean();
        retVal.setPersistenceUnitName("PU_HapiFhirJpaR4");
        try {
			retVal.setDataSource(dataSource());
    	} catch( NamingException ne ) {
	    	System.err.println( "ConfigR4 entityManagerFactory() threw NamingException: " + ne.getMessage() );
	    	//System.err.println( "stack trace:" );
	    	//System.err.println( " ", ne );
    	}
		
        //retVal.setPackagesToScan("ca.uhn.fhir.jpa.entity");
        //retVal.setPackagesToScan("ca.uhn.fhir.jpa.entity", "ca.uhn.fhir.to.jpa.mihin.domain");
        retVal.setPackagesToScan( new String[] { "ca.uhn.fhir.jpa.entity", "ca.uhn.fhir.to.jpa.mihin.domain" } );
        retVal.setPersistenceProvider(new HibernatePersistenceProvider());
        retVal.setJpaProperties(jpaProperties());
        return retVal;
    }

    @Bean
    @Lazy
    public RequestValidatingInterceptor requestValidatingInterceptor() {
        RequestValidatingInterceptor requestValidator = new RequestValidatingInterceptor();
        requestValidator.setFailOnSeverity(null);
        requestValidator.setAddResponseHeaderOnSeverity(null);
        requestValidator.setAddResponseOutcomeHeaderOnSeverity(ResultSeverityEnum.INFORMATION);
        requestValidator.addValidatorModule(instanceValidatorR4());
        requestValidator.setIgnoreValidatorExceptions(true);

        return requestValidator;
    }

    @Bean()
    public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager retVal = new JpaTransactionManager();
        retVal.setEntityManagerFactory(entityManagerFactory);
        return retVal;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    // Derby config
    /*
    @Bean(name = "myPersistenceDataSourceR4", destroyMethod = "close")
    public DataSource dataSource() {
        BasicDataSource retVal = new BasicDataSource();
        retVal.setDriver(new org.apache.derby.jdbc.EmbeddedDriver());
        retVal.setUrl("jdbc:derby:directory:target/r4;create=true");
        retVal.setUsername("");
        retVal.setPassword("");
        return retVal;
    }
	*/
	
	@Bean()
	public DataSource dataSource() throws NamingException {
		BasicDataSource retVal = new BasicDataSource();
		retVal.setDriverClassName(properties.getString("org.mihin.fhirpit.driverClassName"));
		// NOTE: per the dbcp2 javadoc,  this method currently has no effect once the pool has been initialized.
		// So, trying to set three of them in one application may not be possible.
		System.out.println("FhirServerConfigR4: CQF-RULER fhirpit url: " + properties.getString("org.mihin.fhirpit.url.r4") );
		//retVal.setUrl( (String) properties.getString("org.mihin.fhirpit.url"));
		retVal.setUrl( (String) properties.getString("org.mihin.fhirpit.url.r4"));
		retVal.setUsername(properties.getString("org.mihin.fhirpit.username"));
		retVal.setPassword(properties.getString("org.mihin.fhirpit.password"));
		retVal.setMaxIdle(Integer.parseInt(properties.getString("org.mihin.fhirpit.minIdle")));
		retVal.setMinIdle(Integer.parseInt(properties.getString("org.mihin.fhirpit.maxIdle")));
		retVal.setMaxOpenPreparedStatements(
				Integer.parseInt(properties.getString("org.mihin.fhirpit.maxOpenPreparedStatements")));
		return retVal;
	}

    // MySql config
	@SuppressWarnings("deprecation")
    private Properties jpaProperties() {
        Properties extraProperties = new Properties();
        //extraProperties.put("hibernate.dialect", org.hibernate.dialect.DerbyTenSevenDialect.class.getName());
        //extraProperties.put("hibernate.dialect", org.hibernate.dialect.MySQL5InnoDBDialect.class.getName());
        extraProperties.put("hibernate.dialect", org.hibernate.dialect.MySQL57Dialect.class.getName());
        extraProperties.put("hibernate.format_sql", "true");
        extraProperties.put("hibernate.show_sql", "false");
        extraProperties.put("hibernate.hbm2ddl.auto", "create");
        //extraProperties.put("hibernate.hbm2ddl.auto", "update");
        extraProperties.put("hibernate.jdbc.batch_size", "20");
        extraProperties.put("hibernate.cache.use_query_cache", "false");
        extraProperties.put("hibernate.cache.use_second_level_cache", "false");
        extraProperties.put("hibernate.cache.use_structured_entries", "false");
        extraProperties.put("hibernate.cache.use_minimal_puts", "false");
        extraProperties.put("hibernate.search.model_mapping", LuceneSearchMappingFactory.class.getName());
        extraProperties.put("hibernate.search.default.directory_provider", "filesystem");
        extraProperties.put("hibernate.search.default.indexBase", "target/lucenefiles_r4");
        extraProperties.put("hibernate.search.lucene_version", "LUCENE_CURRENT");
//		extraProperties.put("hibernate.search.default.worker.execution", "async");
        return extraProperties;
    }

    // H2 Config
//    @Bean(name = "myPersistenceDataSourceR4", destroyMethod = "close")
//    public DataSource dataSource() {
//        Path path = Paths.get("target/r4").toAbsolutePath();
//        BasicDataSource retVal = new BasicDataSource();
//        retVal.setDriver(new org.h2.Driver());
//        retVal.setUrl("jdbc:h2:file:" + path.toString() + ";create=true;MV_STORE=FALSE;MVCC=FALSE");
//        retVal.setUsername("");
//        retVal.setPassword("");
//        return retVal;
//    }

    // H2 config
//    private Properties jpaProperties() {
//        Properties extraProperties = new Properties();
//        extraProperties.put("hibernate.dialect", org.hibernate.dialect.H2Dialect.class.getName());
//        extraProperties.put("hibernate.format_sql", "true");
//        extraProperties.put("hibernate.show_sql", "false");
//        extraProperties.put("hibernate.hbm2ddl.auto", "update");
//        extraProperties.put("hibernate.jdbc.batch_size", "20");
//        extraProperties.put("hibernate.cache.use_query_cache", "false");
//        extraProperties.put("hibernate.cache.use_second_level_cache", "false");
//        extraProperties.put("hibernate.cache.use_structured_entries", "false");
//        extraProperties.put("hibernate.cache.use_minimal_puts", "false");
//        extraProperties.put("hibernate.search.model_mapping", LuceneSearchMappingFactory.class.getName());
//        extraProperties.put("hibernate.search.default.directory_provider", "filesystem");
//        extraProperties.put("hibernate.search.default.indexBase", "target/lucenefiles_r4");
//        extraProperties.put("hibernate.search.lucene_version", "LUCENE_CURRENT");
////		extraProperties.put("hibernate.search.default.worker.execution", "async");
//        return extraProperties;
//    }

	@Bean
	public Config properties() throws Exception {
		Config conf = ConfigFactory
				.parseFile(new File(System.getProperty("catalina.base") + System.getProperty("file.separator")
						+ "properties" + System.getProperty("file.separator") + "cqf-ruler.properties"));
		return conf;
	}

	private Config addNewPropertyToConfig(Config newConf, String decryptedString, String property) {
		ConfigValue cv = ConfigValueFactory.fromAnyRef(decryptedString);
		newConf = newConf.withValue(property, cv);
		return newConf;
	}

}
