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

package org.jboss.provisioning.util.formatparser;

/**
 *
 * @author Alexey Loubyansky
 */
public class ParsingFormatBase implements ParsingFormat {

    protected final String name;
    protected final boolean wrapper;

    protected ParsingFormatBase(String name) {
        this(name, false);
    }

    protected ParsingFormatBase(String name, boolean wrapper) {
        this.name = name;
        this.wrapper = wrapper;
    }

    /* (non-Javadoc)
     * @see org.jboss.provisioning.spec.type.ParsingFormat#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getContentType() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.jboss.provisioning.spec.type.ParsingFormat#isWrapper()
     */
    @Override
    public boolean isWrapper() {
        return wrapper;
    }

    /* (non-Javadoc)
     * @see org.jboss.provisioning.spec.type.ParsingFormat#pushed(org.jboss.provisioning.spec.type.ParsingContext)
     */
    @Override
    public void pushed(ParsingContext ctx) throws FormatParsingException {
    }

    /* (non-Javadoc)
     * @see org.jboss.provisioning.spec.type.ParsingFormat#react(org.jboss.provisioning.spec.type.ParsingContext)
     */
    @Override
    public void react(ParsingContext ctx) throws FormatParsingException {
    }

    /* (non-Javadoc)
     * @see org.jboss.provisioning.spec.type.ParsingFormat#deal(org.jboss.provisioning.spec.type.ParsingContext)
     */
    @Override
    public void deal(ParsingContext ctx) throws FormatParsingException {
    }

    /* (non-Javadoc)
     * @see org.jboss.provisioning.spec.type.ParsingFormat#eol(org.jboss.provisioning.spec.type.ParsingContext)
     */
    @Override
    public void eol(ParsingContext ctx) throws FormatParsingException {
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (wrapper ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ParsingFormatBase other = (ParsingFormatBase) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (wrapper != other.wrapper)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return name;
    }
}
