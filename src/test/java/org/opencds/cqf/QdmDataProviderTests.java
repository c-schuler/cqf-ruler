package org.opencds.cqf;

import org.cqframework.cql.cql2elm.LibraryManager;
import org.cqframework.cql.cql2elm.ModelManager;
import org.opencds.cqf.cql.execution.Context;
import org.opencds.cqf.helpers.LibraryHelper;
import org.opencds.cqf.qdm.providers.QdmDataProvider;

public class QdmDataProviderTests {

    private TestServer server;
    private final String qdmDataLocation = "qdm-data-provider/";
    private ModelManager modelManager;
    private LibraryManager libraryManager;

    public QdmDataProviderTests(TestServer server) {
        this.server = server;
        this.server.dataProvider = new QdmDataProvider(this.server.dataProvider.getCollectionProviders());
        modelManager = new ModelManager();
        libraryManager = new LibraryManager(modelManager);
    }

    void SimpleEncounterPerformedTest() {
        String cql = "library EncounterPerformedTest version '1.0'\n" +
                "using QDM version '5.3'\n" +
                "context Patient\n" +
                "define \"Qualifying Encounters\":\n" +
                "\t[\"Encounter, Performed\"] E where E.relevantPeriod during day of Interval[@2012-01-01, @2012-12-31]";

        Context context = new Context(LibraryHelper.translateLibrary(cql, libraryManager, modelManager));
        context.registerDataProvider("urn:healthit-gov:qdm:v5_3", server.dataProvider);
        Object result = context.resolveExpressionRef("Qualifying Encounters").getExpression().evaluate(context);
        String s = "s";
    }
}
