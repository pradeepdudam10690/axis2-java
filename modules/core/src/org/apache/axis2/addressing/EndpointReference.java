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
package org.apache.axis2.addressing;

import org.apache.axis2.om.OMElement;

import javax.xml.namespace.QName;
import java.io.Serializable;

/**
 * Class EndpointReference
 * TODO : Policy has not been integrated to this
 */
public class EndpointReference implements Serializable {
    /**
     * Required property. may be a logical address or identifier for the service endpoint
     */
    private String address;

    /**
     * Field interfaceName
     */
    private QName interfaceName;

    /**
     * Field referenceProperties
     */
    private AnyContentType referenceProperties;

    /**
     * Field referenceParameters
     */
    private AnyContentType referenceParameters;

    /**
     * Field serviceName
     */
    private ServiceName serviceName;

    private OMElement policies;

    private AnyContentType metadata;

    /**
     * @param address
     */
    public EndpointReference(String address) {
        this.address = address;
    }

    /**
     * Method getAddress
     */
    public String getAddress() {
        return address;
    }

    /**
     * Method setAddress
     *
     * @param address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Method getInterfaceName
     */
    public QName getInterfaceName() {
        return interfaceName;
    }

    /**
     * Method setInterfaceName
     *
     * @param interfaceName
     */
    public void setInterfaceName(QName interfaceName) {
        this.interfaceName = interfaceName;
    }

    /**
     * Method getReferenceProperties
     */
    public AnyContentType getReferenceProperties() {
        return referenceProperties;
    }

    /**
     * Method setReferenceProperties
     *
     * @param referenceProperties
     */
    public void setReferenceProperties(AnyContentType referenceProperties) {
        this.referenceProperties = referenceProperties;
    }

    /**
     * Method getReferenceParameters
     */
    public AnyContentType getReferenceParameters() {
        return referenceParameters;
    }

    /**
     * Method setReferenceParameters
     *
     * @param referenceParameters
     */
    public void setReferenceParameters(AnyContentType referenceParameters) {
        this.referenceParameters = referenceParameters;
    }

    /**
     * Method getServiceName
     */
    public ServiceName getServiceName() {
        return serviceName;
    }

    /**
     * Method setServiceName
     *
     * @param serviceName
     */
    public void setServiceName(ServiceName serviceName) {
        this.serviceName = serviceName;
    }

    public OMElement getPolicies() {
        return policies;
    }

    public void setPolicies(OMElement policies) {
        this.policies = policies;
    }

    public AnyContentType getMetadata() {
        return metadata;
    }

    public void setMetadata(AnyContentType metadata) {
        this.metadata = metadata;
    }

    public void addReferenceParameter(QName qname, String value) {
        if (getReferenceParameters() == null) {
            setReferenceParameters(new AnyContentType());
        }
        referenceParameters.addReferenceValue(qname, value);
    }


}