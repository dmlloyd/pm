/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.jboss.provisioning.descr;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.provisioning.Gav;

/**
 * Provisioned feature-pack with dependencies on other provisioned feature-packs.
 *
 * TODO intended to be used in ProvisionedInstallationDescription
 *
 * @author Alexey Loubyansky
 */
public class ProvisionedFeaturePackWithDependenciesDescription extends ProvisionedFeaturePackDescription {

    public static class Builder extends ProvisionedFeaturePackDescription.Builder {

        protected Map<Gav, ProvisionedFeaturePackDescription> dependencies = Collections.emptyMap();

        protected Builder() {
            super();
        }

        public Builder addDependency(ProvisionedFeaturePackDescription dependency) {
            switch(dependencies.size()) {
                case 0:
                    dependencies = Collections.singletonMap(dependency.getGav(), dependency);
                    break;
                case 1:
                    dependencies = new LinkedHashMap<>(dependencies);
                default:
                    dependencies.put(dependency.getGav(), dependency);
            }
            return this;
        }

        public ProvisionedFeaturePackWithDependenciesDescription build() {
            return new ProvisionedFeaturePackWithDependenciesDescription(gav,
                    Collections.unmodifiableSet(excludedPackages), Collections.unmodifiableSet(includedPackages),
                    Collections.unmodifiableMap(dependencies));
        }
    }

    public static Builder builderWithDependencies() {
        return new Builder();
    }

    private final Map<Gav, ProvisionedFeaturePackDescription> dependencies;

    protected ProvisionedFeaturePackWithDependenciesDescription(Gav gav, Set<String> excludedPackages, Set<String> includedPackages,
            Map<Gav, ProvisionedFeaturePackDescription> dependencies) {
        super(gav, excludedPackages, includedPackages);
        this.dependencies = dependencies;
    }

    public boolean hasDependencies() {
        return !dependencies.isEmpty();
    }

    public Collection<ProvisionedFeaturePackDescription> getDependencies() {
        return dependencies.values();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((dependencies == null) ? 0 : dependencies.hashCode());
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
        ProvisionedFeaturePackWithDependenciesDescription other = (ProvisionedFeaturePackWithDependenciesDescription) obj;
        if (dependencies == null) {
            if (other.dependencies != null)
                return false;
        } else if (!dependencies.equals(other.dependencies))
            return false;
        return true;
    }
}