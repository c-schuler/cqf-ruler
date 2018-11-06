package org.opencds.cqf.servlet;

import ca.uhn.fhir.context.FhirVersionEnum;
import com.google.gson.*;
import org.apache.http.entity.ContentType;
import org.hl7.fhir.dstu3.model.PlanDefinition;
import org.opencds.cqf.cdshooks.providers.Discovery;
import org.opencds.cqf.cdshooks.providers.DiscoveryItem;
import org.opencds.cqf.cdshooks.request.Request;
import org.opencds.cqf.exceptions.InvalidRequestException;
import org.opencds.cqf.providers.FHIRPlanDefinitionResourceProvider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CdsHooks extends BaseServlet {

    private FhirVersionEnum version;
    public FhirVersionEnum getVersion() {
        return version;
    }
    public void setVersion(FhirVersionEnum version) {
        this.version = version;
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        // validate that we are dealing with JSON
        if (!request.getContentType().startsWith("application/json")) {
            throw new ServletException(
                    String.format(
                            "Invalid content type %s. Please use application/json.",
                            request.getContentType()
                    )
            );
        }

        String service = request.getPathInfo().replace("/", "");

        JsonParser parser = new JsonParser();
        Request cdsHooksRequest =
                new Request(
                        parser.parse(request.getReader()).getAsJsonObject(),
                        getProvider(),
                        version,
                        service,
                        getService(service)
                );


    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        if (!request.getRequestURL().toString().endsWith("cds-services")) {
            throw new ServletException("This servlet is not configured to handle GET requests.");
        }

        response.setHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
        response.getWriter().println(new GsonBuilder().setPrettyPrinting().create().toJson(getServices()));
    }

    private JsonObject getService(String service) {
        for (JsonElement element : getServices().get("services").getAsJsonArray()) {
            if (element.isJsonObject() && element.getAsJsonObject().get("id").getAsString().equals(service)) {
                return element.getAsJsonObject();
            }
        }
        throw new InvalidRequestException("Cannot resolve service: " + service);
    }

    private JsonObject getServices() {
        JsonObject responseJson = new JsonObject();
        JsonArray services = new JsonArray();

        FHIRPlanDefinitionResourceProvider provider = (FHIRPlanDefinitionResourceProvider) getProvider("PlanDefinition");
        for (Discovery discovery : provider.getDiscoveries()) {
            PlanDefinition planDefinition = discovery.getPlanDefinition();
            JsonObject service = new JsonObject();
            if (planDefinition.hasAction()) {
                // TODO - this needs some work - too naive
                if (planDefinition.getActionFirstRep().hasTriggerDefinition()) {
                    if (planDefinition.getActionFirstRep().getTriggerDefinitionFirstRep().hasEventName()) {
                        service.addProperty("hook", planDefinition.getActionFirstRep().getTriggerDefinitionFirstRep().getEventName());
                    }
                }
            }
            if (planDefinition.hasName()) {
                service.addProperty("name", planDefinition.getName());
            }
            if (planDefinition.hasTitle()) {
                service.addProperty("title", planDefinition.getTitle());
            }
            if (planDefinition.hasDescription()) {
                service.addProperty("description", planDefinition.getDescription());
            }
            service.addProperty("id", planDefinition.getIdElement().getIdPart());

            if (!discovery.getItems().isEmpty()) {
                JsonObject prefetchContent = new JsonObject();
                for (DiscoveryItem item : discovery.getItems()) {
                    prefetchContent.addProperty(item.getItemNo(), item.getUrl());
                }
                service.add("prefetch", prefetchContent);
            }
            services.add(service);
        }

        responseJson.add("services", services);
        return responseJson;
    }
}
