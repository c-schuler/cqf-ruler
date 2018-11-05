package org.opencds.cqf.servlet;

import ca.uhn.fhir.context.FhirVersionEnum;
import com.google.gson.JsonParser;
import org.opencds.cqf.cdshooks.request.Request;

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

        JsonParser parser = new JsonParser();
        Request cdsHooksRequest =
                new Request(
                        parser.parse(request.getReader()).getAsJsonObject(),
                        getProvider(),
                        version
                );

        cdsHooksRequest.validate();
        String s = "s";
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {

    }
}
