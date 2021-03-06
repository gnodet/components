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

package org.switchyard.component.http;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jboss.com.sun.net.httpserver.HttpContext;
import org.jboss.com.sun.net.httpserver.HttpExchange;
import org.jboss.com.sun.net.httpserver.HttpHandler;
import org.jboss.com.sun.net.httpserver.HttpServer;
import org.switchyard.Exchange;
import org.switchyard.Message;
import org.switchyard.ServiceDomain;
import org.switchyard.component.http.composer.HttpRequestBindingData;
import org.switchyard.component.http.composer.HttpComposition;
import org.switchyard.component.http.composer.HttpContextMapper;
import org.switchyard.component.http.config.model.HttpBindingModel;
import org.switchyard.component.test.mixins.http.HTTPMixIn;
import org.switchyard.config.model.ModelPuller;
import org.switchyard.config.model.composite.CompositeModel;
import org.switchyard.config.model.composite.CompositeReferenceModel;
import org.switchyard.config.model.composite.CompositeServiceModel;
import org.switchyard.metadata.BaseService;
import org.switchyard.metadata.InOutOperation;
import org.switchyard.metadata.InOutService;
import org.switchyard.metadata.ServiceOperation;
import org.switchyard.test.Invoker;
import org.switchyard.test.MockHandler;
import org.switchyard.test.SwitchYardRunner;
import org.switchyard.test.SwitchYardTestCaseConfig;

/**
 * Contains tests for HTTP Gateway.
 *
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2012 Red Hat Inc.
 */
@RunWith(SwitchYardRunner.class)
@SwitchYardTestCaseConfig(mixins = HTTPMixIn.class)
public class HttpGatewayTest {

    private static final QName STRING_QNAME = new QName("java:java.lang.String");
    private static ModelPuller<CompositeModel> _puller;
    private ServiceDomain _domain;
    private HTTPMixIn httpMixIn;

    @org.switchyard.test.ServiceOperation("{urn:http:test:1.0}SampleHttpConsumerService")
    private Invoker _consumerService;

    @org.switchyard.test.ServiceOperation("{urn:http:test:1.0}InvalidHttpConsumerService")
    private Invoker _consumerService2;

    @org.switchyard.test.ServiceOperation("{urn:http:test:1.0}AuthHttpConsumerService")
    private Invoker _consumerService3;

    @org.switchyard.test.ServiceOperation("{urn:http:test:1.0}NtlmHttpConsumerService")
    private Invoker _consumerService4;

    private HttpBindingModel _config;
    private HttpBindingModel _configRef;
    private HttpBindingModel _configRef2;
    private HttpBindingModel _configRef3;
    private HttpBindingModel _configRef4;
    private InboundHandler _httpInbound;
    private OutboundHandler _httpOutbound;
    private OutboundHandler _httpOutbound2;
    private OutboundHandler _httpOutbound3;
    private OutboundHandler _httpOutbound4;
    private final MockHandler mockService = new MockHandler().forwardInToOut();

    @Before
    public void setUp() throws Exception {
        _puller = new ModelPuller<CompositeModel>();
        CompositeModel composite = _puller.pull("/HelloSwitchYard.xml", getClass());
        composite.assertModelValid();

        CompositeServiceModel compositeService = composite.getServices().get(0);
        _config = (HttpBindingModel)compositeService.getBindings().get(0);

        // Massive hack for Test Runner. Register both a service and a reference binding.
        _domain.registerService(_config.getServiceName(), new InOutService(), mockService);
        _domain.registerServiceReference(_config.getServiceName(), new HelloInterface());
        _httpInbound = new InboundHandler(_config, _domain);
        _httpInbound.start();

        CompositeReferenceModel compositeReference = composite.getReferences().get(0);
        _configRef = (HttpBindingModel)compositeReference.getBindings().get(0);

        _httpOutbound = new OutboundHandler(_configRef);
        _domain.registerService(_configRef.getServiceName(), new HelloInterface(), _httpOutbound);
        _httpOutbound.start();

        compositeReference = composite.getReferences().get(1);
        _configRef2 = (HttpBindingModel)compositeReference.getBindings().get(0);
        _httpOutbound2 = new OutboundHandler(_configRef2);
        _domain.registerService(_configRef2.getServiceName(), new HelloInterface(), _httpOutbound2);
        _httpOutbound2.start();

        compositeReference = composite.getReferences().get(2);
        _configRef3 = (HttpBindingModel)compositeReference.getBindings().get(0);
        _httpOutbound3 = new OutboundHandler(_configRef3);
        _domain.registerService(_configRef3.getServiceName(), new HelloInterface(), _httpOutbound3);
        _httpOutbound3.start();

        compositeReference = composite.getReferences().get(3);
        _configRef4 = (HttpBindingModel)compositeReference.getBindings().get(0);
        _httpOutbound4 = new OutboundHandler(_configRef4);
        _domain.registerService(_configRef4.getServiceName(), new HelloInterface(), _httpOutbound4);
        _httpOutbound4.start();
    }

    @After
    public void tearDown() throws Exception {
        _httpInbound.stop();
        _httpOutbound.stop();
        _httpOutbound2.stop();
        _httpOutbound3.stop();
    }

    @Test
    public void httpGatewayServiceTest() throws Exception {
        String response = httpMixIn.sendString("http://localhost:8080/http", "magesh", HTTPMixIn.HTTP_POST);
        Assert.assertEquals(1, mockService.getMessages().size());
        Assert.assertEquals("magesh", response);
    }

    @Test
    public void httpGatewayReferenceTest() throws Exception {
        Message responseMsg = _consumerService.operation("sayHello").sendInOut("magesh");
        Assert.assertEquals("magesh", responseMsg.getContent(String.class));
    }

    @Test
    public void httpStatus() throws Exception {
        MockHandler handler = new MockHandler();
        Exchange ex = _consumerService.operation("sayHello").createExchange(handler);
        Message requestMsg = ex.createMessage().setContent("magesh");
        requestMsg.getContext().setProperty("SomeRequestHeader", "BAR");
        ex.send(requestMsg);
        handler.waitForOKMessage();
        Assert.assertEquals(200, ex.getContext().getProperty(HttpContextMapper.HTTP_RESPONSE_STATUS).getValue());
    }

    @Test
    public void httpFault() throws Exception {
        MockHandler handler = new MockHandler();
        Exchange ex = _consumerService2.operation("sayHello").createExchange(handler);
        Message requestMsg = ex.createMessage().setContent("magesh");
        ex.send(requestMsg);
        handler.waitForFaultMessage();
        Assert.assertEquals(404, ex.getContext().getProperty(HttpContextMapper.HTTP_RESPONSE_STATUS).getValue());
    }

    @Test
    public void authentication() throws Exception {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8100), 10);
        httpServer.setExecutor(null); // creates a default executor
        httpServer.start();
        HttpContext context = httpServer.createContext("/basic-secured-endpoint", new StandaloneHandler());
        context.setAuthenticator(new HttpBasicAuthenticator());
        Message responseMsg = _consumerService3.operation("sayHello").sendInOut("magesh");
        Assert.assertEquals("response to magesh", responseMsg.getContent(String.class));
        httpServer.stop(0);
    }

    @Ignore // Exclusively for Magesh ;)
    @Test
    public void ntlmAuthentication() throws Exception {
        Message responseMsg = _consumerService4.operation("sayHello").sendInOut("magesh");
        Assert.assertEquals("response to magesh", responseMsg.getContent(String.class));
    }

    private static class HelloInterface extends BaseService {
        private static Set<ServiceOperation> _operations = new HashSet<ServiceOperation>(1);
        static {
            _operations.add(new InOutOperation("sayHello", STRING_QNAME, STRING_QNAME));
        }
        public HelloInterface() {
            super(_operations);
        }
    }

    private static class StandaloneHandler implements HttpHandler {

        public void handle(HttpExchange exchange) {
            try {
                HttpRequestBindingData httpRequest = new HttpRequestBindingData();
                httpRequest.setBodyFromStream(exchange.getRequestBody());
                Assert.assertEquals("magesh", httpRequest.getBodyAsString());
                httpRequest.setBody("response to magesh");
                exchange.sendResponseHeaders(200, httpRequest.getBodyBytes().available());
                httpRequest.writeBodyToStream(exchange.getResponseBody());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

