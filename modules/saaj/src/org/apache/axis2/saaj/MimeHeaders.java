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
package org.apache.axis2.saaj;

import javax.xml.soap.MimeHeader;
import java.util.Iterator;

public class MimeHeaders extends javax.xml.soap.MimeHeaders {
    public MimeHeaders() {
    }

    public MimeHeaders(javax.xml.soap.MimeHeaders h) {
        Iterator iterator = h.getAllHeaders();
        while (iterator.hasNext()) {
            MimeHeader hdr = (MimeHeader) iterator.next();
            addHeader(hdr.getName(), hdr.getValue());
        }
    }

    private int getHeaderSize() {
        int size = 0;
        Iterator iterator = getAllHeaders();
        while (iterator.hasNext()) {
            iterator.next();
            size++;
        }
        return size;
    }
}