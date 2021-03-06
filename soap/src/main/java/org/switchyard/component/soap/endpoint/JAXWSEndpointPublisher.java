/* 
 * JBoss, Home of Professional Open Source 
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved. 
 * See the copyright.txt in the distribution for a 
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use, 
 * modify, copy, or redistribute it subject to the terms and conditions 
 * of the GNU Lesser General Public License, v. 2.1. 
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details. 
 * You should have received a copy of the GNU Lesser General Public License, 
 * v.2.1 along with this distribution; if not, write to the Free Software 
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 */
 
package org.switchyard.component.soap.endpoint;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.WSDLException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Endpoint;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.MessageContext;

import org.switchyard.component.soap.InboundHandler;
import org.switchyard.component.soap.WebServicePublishException;
import org.switchyard.component.soap.config.model.SOAPBindingModel;
import org.switchyard.component.soap.util.WSDLUtil;

/**
 * Handles publishing of Webservice Endpoints on JAX-WS implementations.
 *
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2012 Red Hat Inc.
 */
public class JAXWSEndpointPublisher extends AbstractEndpointPublisher {

    private static final String HTTP_SCHEME = "http";

    /**
     * {@inheritDoc}
     */
    public synchronized WSEndpoint publish(final SOAPBindingModel config, final String bindingId, final InboundHandler handler, WebServiceFeature... features) {
        JAXWSEndpoint wsEndpoint = null;
        try {
            initialize(config);
            List<Source> metadata = new ArrayList<Source>();
            StreamSource source = WSDLUtil.getStream(config.getWsdl());
            metadata.add(source);
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put(Endpoint.WSDL_SERVICE, config.getPort().getServiceQName());
            properties.put(Endpoint.WSDL_PORT, config.getPort().getPortQName());
            properties.put(MessageContext.WSDL_DESCRIPTION, getWsdlLocation());

            String publishUrl = HTTP_SCHEME + "://" + config.getSocketAddr().getHost() + ":" + config.getSocketAddr().getPort() + "/" + getContextPath();

            wsEndpoint = new JAXWSEndpoint(bindingId, handler, features);
            wsEndpoint.getEndpoint().setMetadata(metadata);
            wsEndpoint.getEndpoint().setProperties(properties);
            wsEndpoint.publish(publishUrl);
        } catch (MalformedURLException e) {
            throw new WebServicePublishException(e);
        } catch (WSDLException e) {
            throw new WebServicePublishException(e);
        }
        return wsEndpoint;
    }
}
