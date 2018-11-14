package org.opencds.cqf.servlet;

import ca.uhn.fhir.context.FhirVersionEnum;
import com.google.gson.*;
import org.apache.http.entity.ContentType;
import org.hl7.fhir.dstu3.model.PlanDefinition;
import org.opencds.cqf.cdshooks.evaluation.EvaluationContext;
import org.opencds.cqf.cdshooks.hooks.*;
import org.opencds.cqf.cdshooks.providers.Discovery;
import org.opencds.cqf.cdshooks.providers.DiscoveryItem;
import org.opencds.cqf.cdshooks.request.JsonHelper;
import org.opencds.cqf.cdshooks.request.Request;
import org.opencds.cqf.cdshooks.response.CdsCard;
import org.opencds.cqf.exceptions.InvalidRequestException;
import org.opencds.cqf.providers.FHIRPlanDefinitionResourceProvider;
import org.opencds.cqf.providers.JpaDataProvider;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@WebServlet(name = "cds-services")
public class CdsHooksServlet extends BaseServlet {

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
        try {
            // validate that we are dealing with JSON
            if (!request.getContentType().startsWith("application/json")) {
                throw new ServletException(
                        String.format(
                                "Invalid content type %s. Please use application/json.",
                                request.getContentType()
                        )
                );
            }

            String baseUrl =
                    request.getRequestURL().toString()
                            .replace(request.getPathInfo(), "").replace(request.getServletPath(), "") + "/baseDstu3";
            String service = request.getPathInfo().replace("/", "");

            JsonParser parser = new JsonParser();
            Request cdsHooksRequest =
                    new Request(
                            service,
                            parser.parse(request.getReader()).getAsJsonObject(),
                            JsonHelper.getObjectRequired(getService(service), "prefetch")
                    );
            Hook hook = HookFactory.createHook(cdsHooksRequest);
            EvaluationContext evaluationContext = new EvaluationContext(hook, version, (JpaDataProvider) getProvider().setEndpoint(baseUrl));

            response.setHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
            response.getWriter().println(toJsonResponse(HookEvaluator.evaluate(evaluationContext)));
        } catch (Exception e) {
            e.printStackTrace();
            response.setHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
            response.getWriter().println(toJsonResponse(Collections.singletonList(CdsCard.errorCard(e))));
        }
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

    private String toJsonResponse(List<CdsCard> cards) {
        JsonObject ret = new JsonObject();
        JsonArray cardArray = new JsonArray();

        for (CdsCard card : cards) {
            cardArray.add(card.toJson());
        }

        ret.add("cards", cardArray);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return  gson.toJson(ret);
    }
}
