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

package org.jboss.provisioning.spec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.provisioning.util.PmCollections;

/**
 *
 * @author Alexey Loubyansky
 */
public abstract class PackageDepsSpec {

    protected final List<PackageDependencySpec> localPkgDeps;
    protected final Map<String, List<PackageDependencySpec>> externalPkgDeps;

    protected PackageDepsSpec() {
        localPkgDeps = Collections.emptyList();
        externalPkgDeps = Collections.emptyMap();
    }

    protected PackageDepsSpec(PackageDepsSpec src) {
        localPkgDeps = src.localPkgDeps;
        externalPkgDeps = src.externalPkgDeps;
    }

    protected PackageDepsSpec(PackageDepsSpecBuilder<?> builder) {
        this.localPkgDeps = getValueList(builder.localPkgDeps);
        if(builder.externalPkgDeps.isEmpty()) {
            externalPkgDeps = Collections.emptyMap();
        } else if(builder.externalPkgDeps.size() == 1) {
            final Map.Entry<String, Map<String, PackageDependencySpec>> first = builder.externalPkgDeps.entrySet().iterator().next();
            externalPkgDeps = Collections.singletonMap(first.getKey(), getValueList(first.getValue()));
        } else {
            final Map<String, List<PackageDependencySpec>> tmp = new HashMap<>(builder.externalPkgDeps.size());
            for(Map.Entry<String, Map<String, PackageDependencySpec>> externalEntry : builder.externalPkgDeps.entrySet()) {
                tmp.put(externalEntry.getKey(), getValueList(externalEntry.getValue()));
            }
            externalPkgDeps = PmCollections.unmodifiable(tmp);
        }
    }

    private static List<PackageDependencySpec> getValueList(Map<String, PackageDependencySpec> localPkgDeps) {
        final List<PackageDependencySpec> list;
        if(localPkgDeps.isEmpty()) {
            list = Collections.emptyList();
        } else if(localPkgDeps.size() == 1) {
            list = Collections.singletonList(localPkgDeps.entrySet().iterator().next().getValue());
        } else {
            final List<PackageDependencySpec> tmp = new ArrayList<>(localPkgDeps.size());
            for(Map.Entry<String, PackageDependencySpec> entry : localPkgDeps.entrySet()) {
                tmp.add(entry.getValue());
            }
            list = Collections.unmodifiableList(tmp);
        }
        return list;
    }

    public boolean hasPackageDeps() {
        return !(localPkgDeps.isEmpty() && externalPkgDeps.isEmpty());
    }

    public boolean hasLocalPackageDeps() {
        return !localPkgDeps.isEmpty();
    }

    public Collection<PackageDependencySpec> getLocalPackageDeps() {
        return localPkgDeps;
    }

    public boolean hasExternalPackageDeps() {
        return !externalPkgDeps.isEmpty();
    }

    public Collection<String> getExternalPackageSources() {
        return externalPkgDeps.keySet();
    }

    public Collection<PackageDependencySpec> getExternalPackageDeps(String fpDep) {
        final List<PackageDependencySpec> fpDeps = externalPkgDeps.get(fpDep);
        return fpDeps == null ? Collections.emptyList() : fpDeps;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((externalPkgDeps == null) ? 0 : externalPkgDeps.hashCode());
        result = prime * result + ((localPkgDeps == null) ? 0 : localPkgDeps.hashCode());
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
        PackageDepsSpec other = (PackageDepsSpec) obj;
        if (externalPkgDeps == null) {
            if (other.externalPkgDeps != null)
                return false;
        } else if (!externalPkgDeps.equals(other.externalPkgDeps))
            return false;
        if (localPkgDeps == null) {
            if (other.localPkgDeps != null)
                return false;
        } else if (!localPkgDeps.equals(other.localPkgDeps))
            return false;
        return true;
    }
}
