/*
 * (C) Copyright 2018 Nuxeo SA (http://nuxeo.com/) and others.
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
 * Contributors:
 *     Damon Brown
 */

package org.nuxeo.sf86;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.Environment;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.platform.importer.xml.parser.XMLImporterService;
import org.nuxeo.runtime.api.Framework;

/**
 * Extract zipped SF86 project and load.
 *
 * @since 9.10
 */
public class SF86Parser {

    static final Log log = LogFactory.getLog(SF86Parser.class.getName());

    private Blob blob;
    private Path outDirPath;

    public SF86Parser(Blob inBlob) {
        super();
        this.blob = inBlob;

        String tmpDir = Environment.getDefault().getTemp().getPath();
        Path tmpDirPath = Paths.get(tmpDir);
        try {
            outDirPath = tmpDirPath != null ? Files.createTempDirectory(tmpDirPath, "sf86")
                    : Framework.createTempDirectory(null);
            log.info("Created SF86 temp dir: " + outDirPath);
        } catch (IOException ex) {
            log.error("Problem creating temp dir", ex);
        }
    }

    public Blob getXml(CoreSession session) {
        Blob sf86Blob;
        File sf86;

        // Extract the zip, get the sf86 file
        sf86 = unzip(this.blob);

        // Parse the contents
        log.info("Parsing SF86 XML: " + sf86);
        parseSF86(session, sf86);

        // Return that.
        sf86Blob = new FileBlob(sf86);

        return sf86Blob;
    }

    private void parseSF86(CoreSession session, File xml) {
        DocumentModel root = session.getRootDocument();

        XMLImporterService importer = Framework.getService(XMLImporterService.class);
        log.info("XML importer service: " + importer);
        try {
            List<DocumentModel> models = importer.importDocuments(root, xml);
            if (log.isDebugEnabled()) {
                for (DocumentModel mdl : models) {
                    log.debug("Imported: " + mdl);
                }
            }
        } catch (IOException e) {
            log.error("Problem importing documents", e);
        }

        session.save();
    }

    /**
     * Adapted from https://github.com/nuxeo-sandbox/nuxeo-unzip-file
     *
     * @param inBlob
     * @return
     */
    private File unzip(Blob inBlob) {
        File result = null;

        try {
            byte[] buffer = new byte[1024];
            int len = 0;

            // create output directory if it doesn't exist
            File folder = new File(outDirPath.toString());

            if (!folder.exists()) {
                folder.mkdir();
            }

            // get the zip file content
            ZipInputStream zis = new ZipInputStream(inBlob.getStream());

            // get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {

                String fileName = ze.getName();

                if (fileName.startsWith("__MACOSX/") || fileName.startsWith(".") || fileName.endsWith(".DS_Store")) {
                    ze = zis.getNextEntry();
                    continue;
                }

                String path = fileName.lastIndexOf("/") == -1 ? "" : fileName.substring(0, fileName.lastIndexOf("/"));

                if (ze.isDirectory()) {

                    path = path.indexOf("/") == -1 ? "" : path;

                    File newFile = new File(outDirPath.toString() + File.separator + fileName);
                    newFile.mkdirs();

                    ze = zis.getNextEntry();
                    continue;
                }

                File newFile = new File(outDirPath.toString() + File.separator + fileName);
                log.info("Extracted SF86 file: " + newFile);
                FileOutputStream fos = new FileOutputStream(newFile);

                // Need to return the sf86 XML file.
                if (fileName.endsWith("xml")) {
                    result = newFile;
                }

                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

        } catch (IOException ex) {
            log.error("Problem extracting documents", ex);
        }
        return result;
    }
}