package org.opencds.cqf.config;

import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.to.FhirTesterMvcConfig;
import ca.uhn.fhir.to.TesterConfig;
import org.opencds.cqf.config.mvc.SubscriptionController;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(FhirTesterMvcConfig.class)
public class BaseFhirTesterConfig {

    @Bean
    public TesterConfig testerConfig() {
        TesterConfig config = new TesterConfig();
        config
                .addServer()
                .withId("home_r4")
                .withFhirVersion(FhirVersionEnum.R4)
                //.withBaseUrl(System.getProperty("fhir.baseurl.r4"))
                //.withBaseUrl("cqf-ruler/baseR4")
                .withBaseUrl("baseR4")
                .withName("CQF Ruler Server (R4 FHIR)")
                .addServer()
                .withId("home_stu3")
                .withFhirVersion(FhirVersionEnum.DSTU3)
                //.withBaseUrl(System.getProperty("fhir.baseurl.dstu3"))
                //.withBaseUrl("cqf-ruler/baseDstu3")
                .withBaseUrl("baseDstu3")
                .withName("CQF Ruler Server (DSTU3 FHIR)")
                .addServer()
                .withId("home_dstu2")
                .withFhirVersion(FhirVersionEnum.DSTU2)
                //.withBaseUrl(System.getProperty("fhir.baseurl.dstu2"))
                //.withBaseUrl("cqf-ruler/baseDstu2")
                .withBaseUrl("baseDstu2")
                .withName("CQF Ruler Server (DSTU2 FHIR)");

        return config;
    }

    @Bean(autowire= Autowire.BY_TYPE)
    public SubscriptionController subscriptionPlaygroundController() {
        return new SubscriptionController();
    }
}
