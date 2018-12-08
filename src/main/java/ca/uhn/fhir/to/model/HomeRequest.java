package ca.uhn.fhir.to.model;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.BufferedWriter;
import java.io.FileWriter;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ModelAttribute;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.impl.GenericClient;
import ca.uhn.fhir.rest.server.IncomingRequestAddressStrategy;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.util.ITestingUiClientFactory;
import ca.uhn.fhir.to.Controller;
import ca.uhn.fhir.to.TesterConfig;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class HomeRequest {

	private String myEncoding;
	private String myPretty;
	private String myResource;
	private String myServerId;
	private String mySummary;


	@ModelAttribute("encoding")
	public String getEncoding() {
		return myEncoding;
	}

	@ModelAttribute("encoding")
	public String getPretty() {
		return myPretty;
	}

	@ModelAttribute("_summary")
	public String get_summary() {
		return mySummary;
	}

	@ModelAttribute("resource")
	public String getResource() {
		return myResource;
	}

	public String getServerBase(HttpServletRequest theRequest, TesterConfig theConfig) {
		String retVal;
		if (isBlank(myServerId) && !theConfig.getIdToServerBase().containsKey(myServerId)) {
			retVal = theConfig.getIdToServerBase().entrySet().iterator().next().getValue();
		} else {
			retVal = theConfig.getIdToServerBase().get(myServerId);
		}

		if (retVal.contains("${serverBase}")) {
			IncomingRequestAddressStrategy strategy = new IncomingRequestAddressStrategy();
			strategy.setServletPath("");
			String base = strategy.determineServerBase(theRequest.getServletContext(), theRequest);
			if (base.endsWith("/")) {
				base = base.substring(0, base.length() - 1);
			}
			if (base.endsWith("/resource")) {
				base = base.substring(0, base.length() - "/resource".length());
			}
			retVal = retVal.replace("${serverBase}", base);
		}

		return retVal;
	}

	@ModelAttribute("serverId")
	public String getServerId() {
		return myServerId;
	}

	public String getServerIdWithDefault(TesterConfig theConfig) {
		String retVal = myServerId;
		if (isBlank(retVal)) {
			retVal = theConfig.getIdToServerBase().keySet().iterator().next();
		}
		return retVal;
	}

	public FhirVersionEnum getFhirVersion(TesterConfig theConfig) {
		if (isBlank(myServerId) && !theConfig.getIdToFhirVersion().containsKey(myServerId)) {
			return theConfig.getIdToFhirVersion().entrySet().iterator().next().getValue();
		} else {
			return theConfig.getIdToFhirVersion().get(myServerId);
		}
	}

	public String getServerName(TesterConfig theConfig) {
		if (isBlank(myServerId) && !theConfig.getIdToServerName().containsKey(myServerId)) {
			return theConfig.getIdToServerName().entrySet().iterator().next().getValue();
		} else {
			return theConfig.getIdToServerName().get(myServerId);
		}
	}

	public void setEncoding(String theEncoding) {
		myEncoding = theEncoding;
	}

	public void setPretty(String thePretty) {
		myPretty = thePretty;
	}

	public void set_summary(String theSummary) {
		mySummary = theSummary;
	}

	public void setResource(String theResource) {
		myResource = theResource;
	}

	public void setServerId(String theServerId) {
		myServerId = theServerId;
	}

	public GenericClient newClient(HttpServletRequest theRequest, FhirContext theContext, TesterConfig theConfig,
			Controller.CaptureInterceptor theInterceptor) {
		theContext.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);

		GenericClient retVal;
		ITestingUiClientFactory clientFactory = theConfig.getClientFactory();

		if (clientFactory != null) {
			retVal = (GenericClient) clientFactory.newClient(theContext, theRequest,
					getServerBase(theRequest, theConfig));
		} else {
			retVal = (GenericClient) theContext.newRestfulGenericClient(getServerBase(theRequest, theConfig));
		}
		if (theRequest.getSession() != null) {

			/*
			 * IClientInterceptor authInterceptor = new BasicAuthInterceptor(username,
			 * password); retVal.registerInterceptor(authInterceptor);
			 */
		}
		retVal.registerInterceptor(new BufferResponseInterceptor());

		retVal.setKeepResponses(true);

		if ("true".equals(getPretty())) {
			retVal.setPrettyPrint(true);
		} else if ("false".equals(getPretty())) {
			retVal.setPrettyPrint(false);
		}

		if ("xml".equals(getEncoding())) {
			retVal.setEncoding(EncodingEnum.XML);
		} else if ("json".equals(getEncoding())) {
			retVal.setEncoding(EncodingEnum.JSON);
		}

		if (isNotBlank(get_summary())) {
			SummaryEnum summary = SummaryEnum.fromCode(get_summary());
			if (summary != null) {
				retVal.setSummary(summary);
			}
		}

		retVal.registerInterceptor(theInterceptor);

		final String remoteAddr = org.slf4j.MDC.get("req.remoteAddr");
		retVal.registerInterceptor(new IClientInterceptor() {

			@Override
			public void interceptResponse(IHttpResponse theRequest) {
				// nothing
			}

			@Override
			public void interceptRequest(IHttpRequest theRequest) {
				// String username = "jpaserverui@mihin.org";
				// String password = "BossRwMIConnect18";
				OkHttpClient client = new OkHttpClient();

				MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
				RequestBody body = RequestBody.create(mediaType,
						"grant_type=password&username=jchen1%40mihin.org&password=Sandbox2%40");
				Request request = new Request.Builder().url("https://fire-pit.mihin.org/authservice/oauth/token")
						.post(body).addHeader("Accept", "application/json")
						.addHeader("Content-Type", "application/x-www-form-urlencoded")
						.addHeader("Authorization", "Basic YWNyc2RldjE6c2VjcmV0").addHeader("Cache-Control", "no-cache")
						.build();
				/*
				 * String authorizationUnescaped = StringUtils.defaultString(username) + ":" +
				 * StringUtils.defaultString(password); String encoded;
				 */
				try {
					Response response = client.newCall(request).execute();
					String jsonData = response.body().string();
					ObjectMapper objectMapper = new ObjectMapper();
					JsonNode jsonNode = objectMapper.readTree(jsonData);
					String token = jsonNode.get("access_token").asText();
					// encoded =
					// Base64.encodeBase64String(authorizationUnescaped.getBytes("ISO-8859-1"));
					theRequest.addHeader("Authorization", ("Bearer " + token));
					BufferedWriter writer = new BufferedWriter(new FileWriter(System.getProperty("catalina.base")+"/target/jwt"));
					writer.write(token);
					writer.close();
				} catch (Exception e) {
					e.printStackTrace();
					throw new InternalErrorException("Could not find US-ASCII encoding. This shouldn't happen!");
				}

				if (isNotBlank(remoteAddr)) {
					theRequest.addHeader("x-forwarded-for", remoteAddr);
				}
			}
		});

		return retVal;
	}

	public IParser newParser(FhirContext theCtx) {
		if ("json".equals(getEncoding())) {
			return theCtx.newJsonParser();
		}
		return theCtx.newXmlParser();
	}

	public String getApiKey(HttpServletRequest theServletRequest, TesterConfig theConfig) {
		Boolean allowsApiKey;
		if (isBlank(myServerId) && !theConfig.getIdToFhirVersion().containsKey(myServerId)) {
			allowsApiKey = theConfig.getIdToAllowsApiKey().entrySet().iterator().next().getValue();
		} else {
			allowsApiKey = theConfig.getIdToAllowsApiKey().get(myServerId);
		}
		if (!Boolean.TRUE.equals(allowsApiKey)) {
			return null;
		}

		return defaultString(theServletRequest.getParameter("apiKey"));
	}

}
