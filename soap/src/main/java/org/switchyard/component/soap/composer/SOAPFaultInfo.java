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
package org.switchyard.component.soap.composer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

import org.switchyard.component.soap.util.SOAPUtil;

/**
 * Wrapper for SOAPFault details.
 * 
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2012 Red Hat Inc. 
 */
public class SOAPFaultInfo implements Serializable {

    private static final long serialVersionUID = -2522751288771130086L;

    private String _actor;
    private String _code;
    private String _prefix;
    private QName _codeAsQName;
    private String _node;
    private List<Locale> _reasonLocales = new ArrayList<Locale>();
    private Map<Locale, String> _reasonTexts = new HashMap<Locale, String>();
    private String _role;
    private String _string;
    private Locale _stringLocale;
    private List<QName> _subcodes = new ArrayList<QName>();

    /**
     * @return the Fault actor
     */
    public String getActor() {
        return _actor;
    }

    /**
     * @param actor the Fault actor to set
     */
    public void setActor(String actor) {
        _actor = actor;
    }

    /**
     * @return the Fault code
     */
    public String getCode() {
        return _code;
    }

    /**
     * @param code the Fault code to set
     */
    public void setCode(String code) {
        _code = code;
    }

    /**
     * @return the Fault code prefix
     */
    public String getPrefix() {
        return _prefix;
    }

    /**
     * @param prefix the Fault code prefix to set
     */
    public void setPrefix(String prefix) {
        _prefix = prefix;
    }

    /**
     * @return the Fault code as a QName
     */
    public QName getCodeAsQName() {
        return _codeAsQName;
    }

    /**
     * @param codeAsQName the Fault code QName to set
     */
    public void setCodeAsQName(QName codeAsQName) {
        _codeAsQName = codeAsQName;
    }

    /**
     * @return the Fault node
     */
    public String getNode() {
        return _node;
    }

    /**
     * @param node the Fault node to set
     */
    public void setNode(String node) {
        _node = node;
    }

    /**
     * @return the Fault reasonLocales
     */
    public List<Locale> getReasonLocales() {
        return _reasonLocales;
    }

    /**
     * @param reasonLocales the Fault reasonLocales to set
     */
    public void setReasonLocales(List<Locale> reasonLocales) {
        _reasonLocales = reasonLocales;
    }

    /**
     * @param reasonLocales the Fault reasonLocales to set
     */
    public void setReasonLocales(Iterator<Locale> reasonLocales) {
        _reasonLocales = new ArrayList<Locale>();
        while (reasonLocales.hasNext()) {
            _reasonLocales.add(reasonLocales.next());
        }
    }

    /**
     * Return the Fault reason text for a given Locale if it exists.
     * 
     * @param locale the reasonLocale
     * @return the Fault reasonText
     */
    public String getReasonText(Locale locale) {
        return _reasonTexts.get(locale);
    }

    /**
     * Return all the Fault reason texts.
     * 
     * @return the Fault reasonTexts
     */
    public Iterator<String> getReasonTexts() {
        return _reasonTexts.values().iterator();
    }

    /**
     * @param reasonTexts the Fault reasonTexts to set
     */
    public void setReasonTexts(Map<Locale, String> reasonTexts) {
        _reasonTexts = reasonTexts;
    }

    /**
     * @param locale the reasonLocale
     * @param reasonText the Fault reasonText to add
     */
    public void addReasonText(Locale locale, String reasonText) {
        _reasonTexts.put(locale, reasonText);
    }

    /**
     * @return the Fault role
     */
    public String geRole() {
        return _role;
    }

    /**
     * @param role the Fault role to set
     */
    public void setRole(String role) {
        _role = role;
    }

    /**
     * @return the Fault string
     */
    public String getString() {
        return _string;
    }

    /**
     * @param string the Fault string to set
     */
    public void setString(String string) {
        _string = string;
    }

    /**
     * @return the Fault string's Locale
     */
    public Locale getStringLocale() {
        return _stringLocale;
    }

    /**
     * @param stringLocale the Fault string Locale to set
     */
    public void setStringLocale(Locale stringLocale) {
        _stringLocale = stringLocale;
    }

    /**
     * @return the Fault subcodes
     */
    public List<QName> getSubcodes() {
        return _subcodes;
    }

    /**
     * @param subcodes the Fault subcodes to set
     */
    public void setSubcodes(List<QName> subcodes) {
        _subcodes = subcodes;
    }

    /**
     * @param subcodes the Fault subcodes to set
     */
    public void setSubcodes(Iterator<QName> subcodes) {
        _subcodes = new ArrayList<QName>();
        while (subcodes.hasNext()) {
            _subcodes.add(subcodes.next());
        }
    }

    @Override
    public String toString() {
        return "SOAPFaultInfo [_actor=" + _actor + ", _codeAsQName="
                + _codeAsQName + ", _reasonTexts=" + _reasonTexts + ", _role="
                + _role + ", _string=" + _string + ", _stringLocale="
                + _stringLocale + ", _subcodes=" + _subcodes + "]";
    }

    /**
     * @param soapMessage the soap message to copy fault information from
     * @throws SOAPException if any
     */
    public void copyFaultInfo(SOAPMessage soapMessage) throws SOAPException {
        SOAPFault fault = soapMessage.getSOAPBody().getFault();
        setActor(fault.getFaultActor());
        setCode(fault.getFaultCode());
        setPrefix(fault.getFaultCodeAsName().getPrefix());
        setCodeAsQName(fault.getFaultCodeAsQName());
        setString(fault.getFaultString());
        setStringLocale(fault.getFaultStringLocale());

        // SOAP 1.2 specifics
        if (SOAPUtil.isSOAP12(soapMessage)) {
            setReasonLocales(fault.getFaultReasonLocales());
            Iterator<Locale> locales = fault.getFaultReasonLocales();
            while (locales.hasNext()) {
                Locale locale = locales.next();
                addReasonText(locale, fault.getFaultReasonText(locale));
            }
            setRole(fault.getFaultRole());
            setSubcodes(fault.getFaultSubcodes());
        }
    }
}
