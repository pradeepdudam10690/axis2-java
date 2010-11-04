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

package org.apache.axis2.om;

import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.soap.impl.llom.factory.SOAPLinkedListImplFactory;

public class OMAbstractFactory {
    /**
     * Eran Chinthaka (chinthaka@apache.org)
     */
    /**
     * Constructor OMFactory
     */
    protected OMAbstractFactory() {
    }

    /**
     * This will pick up the default factory implementation from the classpath
     *
     * @return
     */
    public static OMFactory getOMFactory() {
        return FactoryFinder.findOMFactory(null);
    }

    /**
     * If user needs to provide his own factory implementation, here provide the
     * Class Loader for that.
     *
     * @param classLoader
     * @return
     */
    public static OMFactory getOMFactory(ClassLoader classLoader) {
        return FactoryFinder.findOMFactory(classLoader);
    }

    /**
     * This will pick up the default factory implementation from the classpath
     *
     * @return
     */
    public static SOAPFactory getSOAP11Factory() {
        return FactoryFinder.findSOAP11Factory(null);
    }

    /**
     * If user needs to provide his own factory implementation, here provide the
     * Class Loader for that.
     *
     * @param classLoader
     * @return
     */
    public static SOAPFactory getSOAP11Factory(ClassLoader classLoader) {
        return FactoryFinder.findSOAP11Factory(classLoader);
    }

    /**
     * This will pick up the default factory implementation from the classpath
     *
     * @return
     */
    public static SOAPFactory getSOAP12Factory() {
        return FactoryFinder.findSOAP12Factory(null);
    }

    /**
     * If user needs to provide his own factory implementation, here provide the
     * Class Loader for that.
     *
     * @param classLoader
     * @return
     */
    public static SOAPFactory getSOAP12Factory(ClassLoader classLoader) {
        return FactoryFinder.findSOAP12Factory(classLoader);
    }

    /**
     * WARNING - DO NOT USE THIS METHOD !!!!!.
     * This method is used in the case where we do not know the correct SOAP version to be used.
     * We can do some operation using the factory returned from this, without knowing the SOAP version.
     * But most of the methods have not been implemented.
     * We use this in the builder, where we want to first create the SOAP envelope to get the SOAP version.
     * So this method is to solve the chicken and egg problem, we have. If you do not know the SOAP version to be used
     * to process a particluar SOAP message you have recd, use this method to buid the SOAP envelope, and then extract the SOAP
     * version from that envlope and switch to the proper factory using that.
     */
    public static SOAPFactory getDefaultSOAPFactory() {
        return new SOAPLinkedListImplFactory();
    }
}