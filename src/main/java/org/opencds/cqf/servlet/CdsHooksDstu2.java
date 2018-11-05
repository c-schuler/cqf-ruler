package org.opencds.cqf.servlet;

import ca.uhn.fhir.context.FhirVersionEnum;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CdsHooksDstu2 extends CdsHooks {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setVersion(FhirVersionEnum.DSTU2);
        super.doPost(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setVersion(FhirVersionEnum.DSTU2);
        super.doGet(request, response);
    }
}
