/*
 * (C) Copyright 2006-2017 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Contributors:
 *     anechaev
 */
package org.nuxeo.sf86;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

/**
 * Verifying Service mapping with multiple file attachments
 *
 * @author <a href="mailto:anechaev@nuxeo.com">Mika</a>
 */

@RunWith(FeaturesRunner.class)
@Features(PlatformFeature.class)
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({ "nuxeo-sf86-parser", "nuxeo-importer-xml-parser", "org.nuxeo.ecm.automation.core",
        "org.nuxeo.ecm.automation.features" })
@LocalDeploy({ "nuxeo-sf86-parser-core:test-sf86-mapping.xml", "nuxeo-sf86-parser-core:test-doctypes.xml" })
public class TestSF86ParserOp {

    @Inject
    private CoreSession session;

    @Inject
    private AutomationService service;

    private static final String SSN = "123106667";

    @Test
    public void testSF86Zip() throws Exception {

        DocumentModel domain = session.createDocumentModel("Domain");
        domain.setPathInfo("/", "default-domain");
        session.createDocument(domain);

        DocumentModel icontainer = session.createDocumentModel("CaseContainer");
        icontainer.setPathInfo("/default-domain", "Investigations");
        session.createDocument(icontainer);

        DocumentModel investigation = session.createDocumentModel("Case");
        investigation.setPathInfo("/default-domain/Investigations", "INV-00001");
        investigation.setPropertyValue("case:SSN", SSN);
        session.createDocument(investigation);
        session.save();

        DocumentModelList dml = session
                .query(String.format("select * from Document where ecm:primaryType = 'Case' and case:SSN = '%s'", SSN));
        assertEquals(1, dml.size());
        for (DocumentModel dm : dml) {
            assertEquals(SSN, dm.getPropertyValue("case:SSN"));
        }

        File zipFile = FileUtils.getResourceFileFromContext("sf86/sf86.zip");
        Blob zipFileBlob = new FileBlob(zipFile);

        OperationContext ctx = new OperationContext(session);
        ctx.setInput(zipFileBlob);
        // Setting parameters of the chain
        Map<String, Object> params = new HashMap<String, Object>();
        // Run Automation service
        Object xml = service.run(ctx, "SF86.Parser", params);

        System.out.println("XML: " + xml);

        List<DocumentModel> docs = session.query("SELECT * FROM Document WHERE ecm:primaryType='Application'");
        assertEquals("we should have only one Application", 1, docs.size());

        DocumentModel doc = docs.get(0);

        String result = (String) doc.getPropertyValue("application:SSN");
        assertEquals(SSN, result);

        List<DocumentModel> debug = session.query("SELECT * FROM Document");
        for (DocumentModel docx : debug) {
            System.out.print("doc: " + docx);
            Arrays.asList(docx.getSchemas()).stream().forEach(s -> System.out.print("; " + docx.getProperties(s)));
            System.out.println();
        }

        List<DocumentModel> interviews = session.query("SELECT * FROM Document WHERE ecm:primaryType='Interview'");
        assertEquals("we should have four Interviews", 4, interviews.size());
    }

}