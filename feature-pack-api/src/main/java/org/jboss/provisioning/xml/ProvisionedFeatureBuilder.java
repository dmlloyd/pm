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

package org.jboss.provisioning.xml;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.provisioning.ProvisioningDescriptionException;
import org.jboss.provisioning.ProvisioningException;
import org.jboss.provisioning.runtime.ResolvedFeatureId;
import org.jboss.provisioning.runtime.ResolvedSpecId;
import org.jboss.provisioning.state.ProvisionedFeature;
import org.jboss.provisioning.util.PmCollections;
import org.jboss.provisioning.util.StringUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class ProvisionedFeatureBuilder implements ProvisionedFeature {

    public static ProvisionedFeatureBuilder builder(ResolvedFeatureId id) {
        return new ProvisionedFeatureBuilder(id, id.getSpecId());
    }

    public static ProvisionedFeatureBuilder builder(ResolvedSpecId id) {
        return new ProvisionedFeatureBuilder(null, id);
    }

    private final ResolvedSpecId specId;

    private ResolvedFeatureId id;
    private ResolvedFeatureId.Builder idBuilder;

    private Map<String, String> configParams = Collections.emptyMap();
    private Map<String, Object> resolvedParams = Collections.emptyMap();

    private ProvisionedFeatureBuilder(ResolvedFeatureId id, ResolvedSpecId specId) {
        this.id = id;
        this.specId = specId;
        if(id != null) {
            resolvedParams = id.getParams();
            if(resolvedParams.size() > 1) {
                resolvedParams = new HashMap<>(resolvedParams);
                configParams = new HashMap<>(resolvedParams.size());
                for(Map.Entry<String, Object> entry : resolvedParams.entrySet()) {
                    configParams.put(entry.getKey(), entry.getValue().toString());
                }
            } else {
                final Map.Entry<String, Object> entry = resolvedParams.entrySet().iterator().next();
                configParams = Collections.singletonMap(entry.getKey(), entry.getValue().toString());
            }
            idBuilder = null;
        } else {
            idBuilder = ResolvedFeatureId.builder(specId);
        }
    }

    /**
     * Sets the parameter's configuration value
     */
    public ProvisionedFeatureBuilder setConfigParam(String name, String value) {
        configParams = PmCollections.put(configParams, name, value);
        return this;
    }

    /**
     * Sets the parameter's resolved value.
     */
    public ProvisionedFeatureBuilder setResolvedParam(String name, Object value) {
        resolvedParams = PmCollections.put(resolvedParams, name, value);
        return this;
    }

    /**
     * Sets the parameter's configuration and resolved values.
     */
    public ProvisionedFeatureBuilder setParam(String name, String config, Object resolved) {
        setConfigParam(name, config);
        setResolvedParam(name, resolved);
        return this;
    }

    /**
     * Sets the parameter's configuration and resolved values to the value passed in.
     */
    public ProvisionedFeatureBuilder setParam(String name, String value) {
        return setParam(name, value, value);
    }

    /**
     * Sets the ID parameter's resolved value.
     */
    public ProvisionedFeatureBuilder setIdParam(String name, Object value) {
        if(idBuilder == null) {
            throw new IllegalStateException("The ID builder has not been initialized");
        }
        idBuilder.setParam(name, value);
        resolvedParams = PmCollections.put(resolvedParams, name, value);
        return this;
    }

    /**
     * Sets the ID parameter's configuration and resolved values.
     */
    public ProvisionedFeatureBuilder setIdParam(String name, String config, Object resolved) {
        setIdParam(name, resolved);
        setConfigParam(name, config);
        return this;
    }

    /**
     * Sets ID parameter's configuration and resolved values to the value passed in.
     */
    public ProvisionedFeatureBuilder setIdParam(String name, String value) {
        return setIdParam(name, value, value);
    }

    public ProvisionedFeature build() throws ProvisioningDescriptionException {
        if(idBuilder != null) {
            id = idBuilder.build();
            idBuilder = null;
        }
        if(resolvedParams.size() > 1) {
            resolvedParams = Collections.unmodifiableMap(resolvedParams);
        }
        if(configParams.size() > 1) {
            configParams = Collections.unmodifiableMap(configParams);
        }
        return this;
    }

    @Override
    public boolean hasId() {
        return id != null;
    }

    @Override
    public ResolvedFeatureId getId() {
        return id;
    }

    @Override
    public ResolvedSpecId getSpecId() {
        return specId;
    }

    @Override
    public boolean hasParams() {
        return !resolvedParams.isEmpty();
    }

    @Override
    public Collection<String> getParamNames() {
        return resolvedParams.keySet();
    }

    @Override
    public Object getResolvedParam(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getConfigParam(String name) throws ProvisioningException {
        return configParams.get(name);
    }

    @Override
    public Map<String, Object> getResolvedParams() {
        return resolvedParams;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((configParams == null) ? 0 : configParams.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((resolvedParams == null) ? 0 : resolvedParams.hashCode());
        result = prime * result + ((specId == null) ? 0 : specId.hashCode());
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
        ProvisionedFeatureBuilder other = (ProvisionedFeatureBuilder) obj;
        if (configParams == null) {
            if (other.configParams != null)
                return false;
        } else if (!configParams.equals(other.configParams))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (resolvedParams == null) {
            if (other.resolvedParams != null)
                return false;
        } else if (!resolvedParams.equals(other.resolvedParams))
            return false;
        if (specId == null) {
            if (other.specId != null)
                return false;
        } else if (!specId.equals(other.specId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append('[');
        if(id != null) {
            buf.append(id);
        } else {
            buf.append(specId);
        }
        if(!resolvedParams.isEmpty()) {
            buf.append(' ');
            StringUtils.append(buf, resolvedParams.entrySet());
        }
        return buf.append(']').toString();
    }
}
