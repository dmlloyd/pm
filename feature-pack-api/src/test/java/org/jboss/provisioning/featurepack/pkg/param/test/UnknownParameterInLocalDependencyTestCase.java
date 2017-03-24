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

package org.jboss.provisioning.featurepack.pkg.param.test;



import org.jboss.provisioning.ArtifactCoords;
import org.jboss.provisioning.ArtifactCoords.Gav;
import org.jboss.provisioning.Errors;
import org.jboss.provisioning.ProvisioningDescriptionException;
import org.jboss.provisioning.spec.FeaturePackSpec;
import org.jboss.provisioning.spec.PackageDependencySpec;
import org.jboss.provisioning.spec.PackageSpec;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Alexey Loubyansky
 */
public class UnknownParameterInLocalDependencyTestCase  {

    @Test
    public void testMain() throws Exception {
        final Gav fp1Gav = ArtifactCoords.newGav("org.pm.test", "fp-install", "1.0.0.Beta1");
        try {
            FeaturePackSpec
                    .builder(fp1Gav)
                    .addPackage(PackageSpec.builder("a")
                            .addDependency(PackageDependencySpec.builder("b")
                                    .addParameter("param.b1", "b1")
                                    .build())
                            .build())
                    .addPackage(PackageSpec.builder("b")
                            .addParameter("param.b", "b")
                            .build())
                    .build();
            Assert.fail("Non-existing parameter overwrite");
        } catch(ProvisioningDescriptionException e) {
            Assert.assertEquals(Errors.unknownParameterInDependency(fp1Gav, "a", "b", "param.b1"), e.getMessage());
        }
    }
}
