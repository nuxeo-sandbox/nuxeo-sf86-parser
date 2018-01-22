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
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.importer.xml.parser.XMLImporterService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;
import org.nuxeo.runtime.test.runner.PartialDeploy;
import org.nuxeo.runtime.test.runner.TargetExtensions;

/**
 * Verifying Service mapping with multiple file attachments
 *
 * @author <a href="mailto:anechaev@nuxeo.com">Mika</a>
 */

@RunWith(FeaturesRunner.class)
@Features(CoreFeature.class)
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy("nuxeo-importer-xml-parser")
@PartialDeploy(bundle = "studio.extensions.unisys-nbis", extensions = { TargetExtensions.ContentModel.class })
@LocalDeploy({ "nuxeo-sf86-parser-core:test-sf86-mapping.xml" })
public class TestSF86ParserHC {

    @Inject
    private CoreSession session;

    private static final String SSN = "123108888";

    @Test
    public void testSF86Parser() throws Exception {
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

        File xml = FileUtils.getResourceFileFromContext("sf86/HubCap.xml");
        assertNotNull(xml);

        DocumentModel root = session.getRootDocument();

        XMLImporterService importer = Framework.getService(XMLImporterService.class);
        assertNotNull(importer);
        importer.importDocuments(root, xml);

        session.save();

        List<DocumentModel> docs = session.query("SELECT * FROM Document WHERE ecm:primaryType='Application'");
        assertEquals("we should have only one Application", 1, docs.size());

        DocumentModel doc = docs.get(0);

        String result = (String) doc.getPropertyValue("application:SSN");
        assertEquals(SSN, result);
        Blob blob = getBlob(doc);
        assertNotNull("we should have a blob attached to the application", blob);

        List<DocumentModel> debug = session.query("SELECT * FROM Document");
        for (DocumentModel docx : debug) {
            System.out.print("doc: " + docx);
            Arrays.asList(docx.getSchemas()).stream().forEach(s -> System.out.print("; " + docx.getProperties(s)));
            System.out.println();
        }

        List<DocumentModel> interviews = session.query("SELECT * FROM Document WHERE ecm:primaryType='Interview'");
        assertEquals("we should have four Interviews", 4, interviews.size());
    }

    protected static Blob getBlob(DocumentModel doc) throws NuxeoException {
        BlobHolder blobHolder = doc.getAdapter(BlobHolder.class);
        if (blobHolder == null) {
            return null;
        }
        return blobHolder.getBlob();

    }

}