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
package org.jboss.provisioning.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.provisioning.ProvisioningDescriptionException;
import org.jboss.provisioning.feature.FeatureParameterSpec;
import org.jboss.provisioning.feature.FeatureReferenceSpec;

/**
 *
 * @author Alexey Loubyansky
 */
class ResolvedFeature {

    final ResolvedFeatureId id;
    final ResolvedFeatureSpec spec;
    Map<String, String> params;
    Set<ResolvedFeatureId> dependencies = Collections.emptySet();
    boolean liningUp;

    ResolvedFeature(ResolvedFeatureId id, ResolvedFeatureSpec spec, Map<String, String> params) {
        this.id = id;
        this.spec = spec;
        this.params = params;
    }

//    List<ResolvedFeatureId> resolveDependencies() throws ProvisioningDescriptionException {
//
//    }

    List<ResolvedFeatureId> resolveRefs() throws ProvisioningDescriptionException {
        if(spec.resolvedRefTargets.isEmpty()) {
            return Collections.emptyList();
        }
        if(spec.resolvedRefTargets.size() == 1) {
            final Entry<String, ResolvedSpecId> refEntry = spec.resolvedRefTargets.entrySet().iterator().next();
            final ResolvedFeatureId refId = getRefTarget(refEntry.getValue(), spec.xmlSpec.getRef(refEntry.getKey()));
            return refId == null ? Collections.emptyList() : Collections.singletonList(refId);
        }
        final List<ResolvedFeatureId> refIds = new ArrayList<>(spec.resolvedRefTargets.size());
        for(Map.Entry<String, ResolvedSpecId> refEntry : spec.resolvedRefTargets.entrySet()) {
            final ResolvedFeatureId refId = getRefTarget(refEntry.getValue(), spec.xmlSpec.getRef(refEntry.getKey()));
            if(refId != null) {
                refIds.add(refId);
            }
        }
        return refIds;
    }

    String resolveParam(String name) throws ProvisioningDescriptionException {
        final FeatureParameterSpec param = spec.xmlSpec.getParam(name);
        String value = params.get(param.getName());
        if(value == null) {
            value = param.getDefaultValue();
        }
        if(value == null && (param.isFeatureId() || !param.isNillable())) {
            final StringBuilder buf = new StringBuilder();
            if(id == null) {
                buf.append(spec.id).append(" configuration");
            } else {
                buf.append(id);
            }
            throw new ProvisioningDescriptionException(buf.append(" is missing required parameter ").append(param.getName()).toString());
        }
        return value;
    }

    private ResolvedFeatureId getRefTarget(final ResolvedSpecId specId, final FeatureReferenceSpec refSpec)
            throws ProvisioningDescriptionException {
        if(refSpec.getParamsMapped() == 1) {
            final String paramValue = resolveParam(refSpec.getLocalParam(0));
            if(paramValue == null) {
                if (!refSpec.isNillable()) {
                    final StringBuilder buf = new StringBuilder();
                    buf.append("Reference ").append(refSpec).append(" of ");
                    if (id != null) {
                        buf.append(id);
                    } else {
                        buf.append(spec.id).append(" configuration ");
                    }
                    buf.append(" cannot be null");
                    throw new ProvisioningDescriptionException(buf.toString());
                }
                return null;
            }
            return new ResolvedFeatureId(specId, Collections.singletonMap(refSpec.getTargetParam(0), paramValue));
        }
        Map<String, String> params = new HashMap<>(refSpec.getParamsMapped());
        for(int i = 0; i < refSpec.getParamsMapped(); ++i) {
            final String paramValue = resolveParam(refSpec.getLocalParam(i));
            if(paramValue == null) {
                if (!refSpec.isNillable()) {
                    final StringBuilder buf = new StringBuilder();
                    buf.append("Reference ").append(refSpec).append(" of ");
                    if (id != null) {
                        buf.append(id);
                    } else {
                        buf.append(spec.id).append(" configuration ");
                    }
                    buf.append(" cannot be null");
                    throw new ProvisioningDescriptionException(buf.toString());
                }
                return null;
            }
            params.put(refSpec.getTargetParam(i), paramValue);
        }
        return new ResolvedFeatureId(specId, params);
    }
}