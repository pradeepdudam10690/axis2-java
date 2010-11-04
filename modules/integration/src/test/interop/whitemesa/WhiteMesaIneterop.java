/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test.interop.whitemesa;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import test.interop.util.XMLComparatorInterop;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.soap.SOAPBody;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.impl.llom.builder.StAXSOAPModelBuilder;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
public class WhiteMesaIneterop extends TestCase {

    protected boolean compare(SOAPEnvelope retEnv, String filePath) throws AxisFault {
        boolean ok;
        try {
            if (retEnv != null) {
                SOAPBody body = retEnv.getBody();
                if (!body.hasFault()) {
                    //OMElement firstChild = (OMElement) body.getFirstElement();

                    InputStream stream = Thread.currentThread().getContextClassLoader()
                            .getResourceAsStream(filePath);

                    XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(stream);
                    OMXMLParserWrapper builder = new StAXSOAPModelBuilder(parser, null);
                    SOAPEnvelope refEnv = (SOAPEnvelope) builder.getDocumentElement();
                    //OMElement refNode = (OMElement) resEnv.getBody().getFirstElement();
                    XMLComparatorInterop comparator = new XMLComparatorInterop();
                    ok = comparator.compare(retEnv, refEnv);
                } else
                    return false;
            } else
                return false;

        } catch (Exception e) {
            throw new AxisFault(e);
        }
        return ok;
    }
}