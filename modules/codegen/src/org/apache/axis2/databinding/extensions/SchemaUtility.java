package org.apache.axis2.databinding.extensions;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;

import javax.wsdl.Definition;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
 *
 * @author : Eran Chinthaka (chinthaka@apache.org)
 */

public interface SchemaUtility {
    /**
     * This interface should be implemented by any of the data binding frameworks that are plugged in
     * to Axis2. This will help to get the schema out of the generated code, when WSDL is not available.
     */


    /**
     * Before asking for schema, one needs to find out the data binding framework that was used
     * to code gen the skeletons. This method will check, any how, to see whether the generated
     * code was generated by him and reurns appropriate boolean depending on the result.
     *
     * @param axisService
     * @return boolean 
     */

    public boolean isRelevant(AxisService axisService) throws AxisFault;

    /**
     * isRelevant() must be called before calling this method.
     * This will fill the given definition element with the information that can be extracted from
     * the deployed service archive
     * @param axisService
     * @throws AxisFault
     */
    public void fillInformationFromAxisService(AxisService axisService, Definition definition) throws AxisFault;

    /**
     * isRelevant() must be called before calling this method.
     * This will fill the given definition element with the information that can be extracted from
     * the deployed service archive
     * @param axisService
     * @return Definition
     * @throws AxisFault
     */
    public Definition fillInformationFromAxisService(AxisService axisService) throws AxisFault;

}