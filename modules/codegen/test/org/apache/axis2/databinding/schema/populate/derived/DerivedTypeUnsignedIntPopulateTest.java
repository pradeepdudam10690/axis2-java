package org.apache.axis2.databinding.schema.populate.derived;

import org.apache.axis2.databinding.schema.types.UnsignedByte;
import org.apache.axis2.databinding.schema.types.UnsignedInt;
import org.apache.axis2.databinding.schema.util.ConverterUtil;

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

public class DerivedTypeUnsignedIntPopulateTest extends AbstractDerivedPopulater{

    private String values[]= {
            "1",
            "0",
            "267582233" ,
            "-1" ,
            "-267582233"

    };

    private String xmlString[] = {
            "<DerivedUnsignedInt>"+values[0]+"</DerivedUnsignedInt>",
            "<DerivedUnsignedInt>"+values[1]+"</DerivedUnsignedInt>",
            "<DerivedUnsignedInt>"+values[2]+"</DerivedUnsignedInt>",
            "<DerivedUnsignedInt>"+values[3]+"</DerivedUnsignedInt>",
            "<DerivedUnsignedInt>"+values[4]+"</DerivedUnsignedInt>"
    };




    protected void setUp() throws Exception {
        className = "org.soapinterop.DerivedUnsignedInt";
        propertyClass = UnsignedInt.class;
    }

    // force others to implement this method
    public void testPopulate() throws Exception {

        for (int i = 0; i < 3; i++) {
            checkValue(xmlString[i],values[i]);
        }

        for (int i = 3; i < values.length; i++) {
            try {
                checkValue(xmlString[i],values[i]);
                fail();
            } catch (Exception e) {

            }
        }

    }

    protected String convertToString(Object o) {
        return ConverterUtil.convertToString((UnsignedInt)o);
    }
}
