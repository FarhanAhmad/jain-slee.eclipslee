/**
 *   Copyright 2005 Open Cloud Ltd.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.mobicents.eclipslee.util.slee.xml.components;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.mobicents.eclipslee.util.slee.xml.DTDXML;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * @author cath
 */
public class DeployableUnitXML extends DTDXML {

	public static final String QUALIFIED_NAME = "deployable-unit";
	public static final String PUBLIC_ID_1_0 = "-//Sun Microsystems, Inc.//DTD JAIN SLEE Deployable Unit 1.0//EN";
	public static final String SYSTEM_ID_1_0 = "http://java.sun.com/dtd/slee-deployable-unit_1_0.dtd";
	
    public static final String PUBLIC_ID_1_1 = "-//Sun Microsystems, Inc.//DTD JAIN SLEE Deployable Unit 1.1//EN";
    public static final String SYSTEM_ID_1_1 = "http://java.sun.com/dtd/slee-deployable-unit_1_1.dtd";

    public static final String PUBLIC_ID = PUBLIC_ID_1_1;
    public static final String SYSTEM_ID = SYSTEM_ID_1_1;
    
    public DeployableUnitXML(EntityResolver resolver, InputSource dummyXML) throws ParserConfigurationException {
		super(QUALIFIED_NAME, PUBLIC_ID, SYSTEM_ID, resolver);
		readDTDVia(resolver, dummyXML);
	}
		
	/**
	 * Parse the provided InputStream as though it contains JAIN SLEE Event XML Data.
	 * @param stream
	 */
	
	public DeployableUnitXML(InputStream stream, EntityResolver resolver, InputSource dummyXML) throws SAXException, IOException, ParserConfigurationException {
		super(stream, resolver);
		
		// Verify that this is really a service XML file.
		if (!getRoot().getNodeName().equals(QUALIFIED_NAME))
			throw new SAXException("This was not a service XML file.");		
		
		readDTDVia(resolver, dummyXML);
	}

	public String[] getJars() {	
		Element nodes[] = getNodes("deployable-unit/jar");
		String jars[] = new String[nodes.length];
		
		for (int i = 0; i < nodes.length; i++)
			jars[i] = getTextData(nodes[i]);
		
		return jars;
	}
	
	public boolean containsJar(String jar) {
		String jars[] = getJars();
		for (int i = 0; i < jars.length; i++)
			if (jars[i].equals(jar))
				return true;
		
		return false;
	}
	
	public void addJar(String jar) {
		if (containsJar(jar)) return;
		
		Element child = addElement(root, "jar");
		setTextData(child, jar);
	}
	
	public void removeJar(String jar) {
		Element nodes[] = getNodes("deployable-unit/jar");
		for (int i = 0; i < nodes.length; i++) {
			if (getTextData(nodes[i]).equals(jar)) {
				nodes[i].getParentNode().removeChild(nodes[i]);
				return;
			}
		}
	}
	
	public String[] getServices() {	
		Element nodes[] = getNodes("deployable-unit/service-xml");
		String services[] = new String[nodes.length];
		
		for (int i = 0; i < nodes.length; i++)
			services[i] = getTextData(nodes[i]);
		
		return services;
	}
	
	public boolean containsService(String service) {
		String services[] = getServices();
		for (int i = 0; i < services.length; i++)
			if (services[i].equals(service))
				return true;
		
		return false;
	}
	
	public void addService(String service) {
		if (containsService(service)) return;
		
		Element child = addElement(root, "service-xml");
		setTextData(child, service);
	}
	
	public void removeService(String service) {
		Element nodes[] = getNodes("deployable-unit/service-xml");
		for (int i = 0; i < nodes.length; i++) {
			if (getTextData(nodes[i]).equals(service)) {
				nodes[i].getParentNode().removeChild(nodes[i]);
				return;
			}
		}
	}
	
}
