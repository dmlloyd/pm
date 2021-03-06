/*
 * Copyright 2016-2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.provisioning.util.formatparser.formats;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.jboss.provisioning.util.PmCollections;
import org.jboss.provisioning.util.formatparser.FormatErrors;
import org.jboss.provisioning.util.formatparser.FormatParsingException;
import org.jboss.provisioning.util.formatparser.ParsingContext;
import org.jboss.provisioning.util.formatparser.ParsingFormat;

/**
 *
 * @author Alexey Loubyansky
 */
public class CompositeParsingFormat extends ObjectParsingFormat {

    public static CompositeParsingFormat newInstance() {
        return newInstance(null);
    }

    public static CompositeParsingFormat newInstance(String name) {
        return new CompositeParsingFormat(name == null ? ObjectParsingFormat.NAME : name);
    }

    private Character nameValueSeparator;
    private boolean acceptAll = false;
    private ParsingFormat defaultValueFormat;
    private Map<String, ParsingFormat> elems = Collections.emptyMap();

    protected CompositeParsingFormat(String name) {
        super(name);
    }

    public String getContentType() {
        return ObjectParsingFormat.NAME;
    }

    public CompositeParsingFormat setAcceptAll(boolean acceptAll) {
        this.acceptAll = acceptAll;
        return this;
    }

    public CompositeParsingFormat setDefaultValueFormat(ParsingFormat format) {
        if(nameValueSeparator == null) {
            this.defaultValueFormat = NameValueParsingFormat.getInstance(format);
        } else {
            this.defaultValueFormat = NameValueParsingFormat.getInstance(nameValueSeparator, format);
        }
        return this;
    }

    public CompositeParsingFormat addElement(String name) {
        return addElement(name, defaultValueFormat);
    }

    public CompositeParsingFormat addElement(String name, ParsingFormat valueFormat) {
        NameValueParsingFormat nameValue = null;
        if(valueFormat == null) {
            if(defaultValueFormat != null) {
                nameValue = nameValueSeparator == null ? NameValueParsingFormat.getInstance(defaultValueFormat) : NameValueParsingFormat.getInstance(nameValueSeparator, defaultValueFormat);
            }
        } else {
            nameValue = nameValueSeparator == null ? NameValueParsingFormat.getInstance(valueFormat) : NameValueParsingFormat.getInstance(nameValueSeparator, valueFormat);
        }
        elems = PmCollections.put(elems, name, nameValue);
        return this;
    }

    public CompositeParsingFormat setNameValueSeparator(char ch) {
        this.nameValueSeparator = ch;
        return this;
    }

    @Override
    public boolean isAcceptsElement(String name) {
        return acceptAll || elems.containsKey(name);
    }

    @Override
    public void deal(ParsingContext ctx) throws FormatParsingException {
        if(Character.isWhitespace(ctx.charNow())) {
            return;
        }

        Map.Entry<String, ParsingFormat> matchedElem = null;
        for(Map.Entry<String, ParsingFormat> elem : elems.entrySet()) {
            if(ctx.startsNow(elem.getKey())) {
                if(matchedElem == null) {
                    matchedElem = elem;
                } else if(matchedElem.getKey().length() < elem.getKey().length()) {
                    matchedElem = elem;
                }
            }
        }

        ParsingFormat valueFormat = null;
        if(matchedElem == null) {
            if(!acceptAll) {
                throw new FormatParsingException(FormatErrors.unexpectedCompositeFormatElement(this, null));
            }
        } else {
            valueFormat = matchedElem.getValue();
        }

        if(valueFormat == null) {
            valueFormat = defaultValueFormat;
            if(valueFormat == null) {
                throw new FormatParsingException("Format " + this + " attribute " + matchedElem.getKey() + " is missing value format");
            }
        }
        ctx.pushFormat(valueFormat);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (acceptAll ? 1231 : 1237);
        result = prime * result + ((defaultValueFormat == null) ? 0 : defaultValueFormat.hashCode());
        result = prime * result + ((elems == null) ? 0 : elems.hashCode());
        result = prime * result + ((nameValueSeparator == null) ? 0 : nameValueSeparator.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        CompositeParsingFormat other = (CompositeParsingFormat) obj;
        if (acceptAll != other.acceptAll)
            return false;
        if (defaultValueFormat == null) {
            if (other.defaultValueFormat != null)
                return false;
        } else if (!defaultValueFormat.equals(other.defaultValueFormat))
            return false;
        if (elems == null) {
            if (other.elems != null)
                return false;
        } else if (!elems.equals(other.elems))
            return false;
        if (nameValueSeparator == null) {
            if (other.nameValueSeparator != null)
                return false;
        } else if (!nameValueSeparator.equals(other.nameValueSeparator))
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        if(name != null) {
            buf.append(name);
        }
        buf.append('{');
        if (!elems.isEmpty()) {
            final Iterator<Map.Entry<String, ParsingFormat>> i = elems.entrySet().iterator();
            Map.Entry<String, ParsingFormat> elem = i.next();
            buf.append(elem.getKey()).append(':').append(((NameValueParsingFormat)elem.getValue()).getValueFormat());
            while (i.hasNext()) {
                elem = i.next();
                buf.append(',').append(elem.getKey()).append(':').append(((NameValueParsingFormat)elem.getValue()).getValueFormat());

            }
        }
        return buf.append('}').toString();
    }
}
