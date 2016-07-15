/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.pm.fp.xml;

import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jboss.pm.descr.PackageDescription;
import org.jboss.pm.descr.PackageDescription.Builder;
import org.jboss.staxmapper.XMLMapper;


/**
 *
 * @author Alexey Loubyansky
 */
public class PackageXMLParser {

    private static final QName ROOT_1_0 = new QName(PackageXMLParser10.NAMESPACE_1_0, PackageXMLParser10.Element.PACKAGE.getLocalName());

    private static final XMLInputFactory inputFactory;
    static {
        final XMLInputFactory tmpIF = XMLInputFactory.newInstance();
        setIfSupported(tmpIF, XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
        setIfSupported(tmpIF, XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        inputFactory = tmpIF;
    }

    private static void setIfSupported(final XMLInputFactory inputFactory, final String property, final Object value) {
        if (inputFactory.isPropertySupported(property)) {
            inputFactory.setProperty(property, value);
        }
    }

    private final XMLMapper mapper;

    public PackageXMLParser() {
        mapper = XMLMapper.Factory.create();
        mapper.registerRootElement(ROOT_1_0, new PackageXMLParser10());
    }

    public PackageDescription parse(final InputStream input) throws XMLStreamException {
        final XMLStreamReader streamReader = inputFactory.createXMLStreamReader(input);
        final Builder pkgBuilder = PackageDescription.packageBuilder();
        mapper.parseDocument(pkgBuilder, streamReader);
        return pkgBuilder.build();
    }
}
