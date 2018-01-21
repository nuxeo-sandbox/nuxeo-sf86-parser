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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;

/**
 * Extract zipped SF86 XML and attachments.
 *
 * Assumes there is only one XML file in the zip (uses the first one found).
 *
 * @since 9.10
 */
@Operation(id = SF86ParserOp.ID, category = Constants.CAT_BLOB, label = "Parse SF86 Bundle", description = "Extract zipped SF86 Bundle and process enclosed XML.")
public class SF86ParserOp {

    public static final String ID = "SF86.Parser";

    static final Log log = LogFactory.getLog(SF86ParserOp.class.getName());

    @Context
    protected CoreSession session;

    @OperationMethod
    public Blob run(Blob inBlob) {
        SF86Parser parser = new SF86Parser(inBlob);
        return parser.getXml(session);
    }
}