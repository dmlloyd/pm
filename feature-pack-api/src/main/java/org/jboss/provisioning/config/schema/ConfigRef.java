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

package org.jboss.provisioning.config.schema;

import java.util.Arrays;

/**
 *
 * @author Alexey Loubyansky
 */
public class ConfigRef {

    public static ConfigRef create(SchemaPath path, String... featureId) {
        return new ConfigRef(path, featureId);
    }

    final SchemaPath path;
    final String[] featureId;

    private ConfigRef(SchemaPath path, String[] featureId) {
        if(path.length() != featureId.length) {
            throw new IllegalArgumentException("SchemaPath length does not match featureId length");
        }
        this.path = path;
        this.featureId = featureId;
    }

    public SchemaPath getPath() {
        return path;
    }

    public String[] getFeatureId() {
        return featureId;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append('[');
        if(featureId.length > 0) {
            buf.append(path.getName(0)).append('=').append(featureId[0]);
            if(featureId.length > 1) {
                for(int i = 1; i < featureId.length; ++i) {
                    buf.append(',').append(path.getName(i)).append('=').append(featureId[i]);
                }
            }
        }
        return buf.append(']').toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(featureId);
        result = prime * result + ((path == null) ? 0 : path.hashCode());
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
        ConfigRef other = (ConfigRef) obj;
        if (!Arrays.equals(featureId, other.featureId))
            return false;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        return true;
    }
}
