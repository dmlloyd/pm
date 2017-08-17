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

package org.jboss.provisioning.config.feature;

import org.jboss.provisioning.ArtifactCoords;
import org.jboss.provisioning.ArtifactCoords.Gav;
import org.jboss.provisioning.ProvisioningDescriptionException;
import org.jboss.provisioning.ProvisioningException;
import org.jboss.provisioning.config.FeaturePackConfig;
import org.jboss.provisioning.feature.Config;
import org.jboss.provisioning.feature.FeatureConfig;
import org.jboss.provisioning.feature.FeatureGroupConfig;
import org.jboss.provisioning.feature.FeatureGroupSpec;
import org.jboss.provisioning.feature.FeatureId;
import org.jboss.provisioning.feature.FeatureParameterSpec;
import org.jboss.provisioning.feature.FeatureReferenceSpec;
import org.jboss.provisioning.feature.FeatureSpec;
import org.jboss.provisioning.runtime.ResolvedFeatureId;
import org.jboss.provisioning.state.ProvisionedFeaturePack;
import org.jboss.provisioning.state.ProvisionedState;
import org.jboss.provisioning.test.PmInstallFeaturePackTestBase;
import org.jboss.provisioning.test.util.repomanager.FeaturePackRepoManager;
import org.jboss.provisioning.xml.ProvisionedConfigBuilder;
import org.jboss.provisioning.xml.ProvisionedFeatureBuilder;

/**
 *
 * @author Alexey Loubyansky
 */
public class NestedFeatureOverwriteTestCase extends PmInstallFeaturePackTestBase {

    private static final Gav FP_GAV = ArtifactCoords.newGav("org.jboss.pm.test", "fp1", "1.0.0.Final");

    @Override
    protected void setupRepo(FeaturePackRepoManager repoManager) throws ProvisioningDescriptionException {
        repoManager.installer()
        .newFeaturePack(FP_GAV)
            .addSpec(FeatureSpec.builder("specA")
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("p1", "def1"))
                    .addParam(FeatureParameterSpec.create("p2", "def2"))
                    .addParam(FeatureParameterSpec.create("p3", "def3"))
                    .addParam(FeatureParameterSpec.create("p4", "def4"))
                    .build())
            .addSpec(FeatureSpec.builder("specB")
                    .addRef(FeatureReferenceSpec.builder("specA").mapParam("specA", "name").build())
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("p1", "def1"))
                    .addParam(FeatureParameterSpec.create("p2", "def2"))
                    .addParam(FeatureParameterSpec.create("p3", "def3"))
                    .addParam(FeatureParameterSpec.create("specA"))
                    .build())
            .addSpec(FeatureSpec.builder("specC")
                    .addRef(FeatureReferenceSpec.builder("specB").mapParam("specB", "name").build())
                    .addRef(FeatureReferenceSpec.builder("specA").mapParam("specA", "name").build())
                    .addParam(FeatureParameterSpec.createId("name"))
                    .addParam(FeatureParameterSpec.create("p1", "def1"))
                    .addParam(FeatureParameterSpec.create("p2", "def2"))
                    .addParam(FeatureParameterSpec.create("p3", "def3"))
                    .addParam(FeatureParameterSpec.create("p4", "def4"))
                    .addParam(FeatureParameterSpec.create("p5", "def5"))
                    .addParam(FeatureParameterSpec.create("specB"))
                    .addParam(FeatureParameterSpec.create("specA"))
                    .build())
            .addFeatureGroup(FeatureGroupSpec.builder("group1")
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("name", "a1")
                            .setParam("p1", "group1")
                            .setParam("p2", "group2")
                            .setParam("p3", "group3")
                            .addFeature(
                                    new FeatureConfig("specC")
                                    .setParam("name", "c1")
                                    .setParam("p1", "group1a")
                                    .setParam("p2", "group1a")
                                    .setParam("p3", "group1a")))
                    .addFeature(
                            new FeatureConfig("specB")
                            .setParam("name", "b1")
                            .setParam("p1", "group1")
                            .setParam("p2", "group2")
                            .setParam("specA", "a1")
                            .addFeature(
                                    new FeatureConfig("specC")
                                    .setParam("name", "c1")
                                    .setParam("p1", "group1b")
                                    .setParam("p2", "group1b")))
                    .build())
            .addConfig(Config.builder()
                    .addFeatureGroup(FeatureGroupConfig.builder("group1")
                            .includeFeature(FeatureId.create("specA", "name", "a1"),
                                    new FeatureConfig("specA")
                                    .setParam("name", "a1")
                                    .setParam("p1", "groupConfig1")
                                    .setParam("p2", "groupConfig2")
                                    .addFeature(
                                            new FeatureConfig("specB")
                                            .setParam("name", "b1")
                                            .setParam("p2", "config1")))
                            .includeFeature(FeatureId.create("specC", "name", "c1"),
                                    new FeatureConfig("specC")
                                    .setParam("name", "c1")
                                    .setParam("p1", "config1"))
                            .build())
                    .addFeature(
                            new FeatureConfig("specA")
                            .setParam("name", "a1")
                            .setParam("p1", "config1"))
                    .addFeature(
                            new FeatureConfig("specC")
                            .setParam("name", "c1")
                            .setParam("p5", "config1"))
                    .build())
            .newPackage("p1", true)
                .getFeaturePack()
            .getInstaller()
        .install();
    }

    @Override
    protected FeaturePackConfig featurePackConfig() {
        return FeaturePackConfig.forGav(FP_GAV);
    }

    @Override
    protected ProvisionedState provisionedState() throws ProvisioningException {
        return ProvisionedState.builder()
                .addFeaturePack(ProvisionedFeaturePack.builder(FP_GAV)
                        .addPackage("p1")
                        .build())
                .addConfig(ProvisionedConfigBuilder.builder()
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specA", "name", "a1"))
                                .setParam("p1", "config1")
                                .setParam("p2", "groupConfig2")
                                .setParam("p3", "group3")
                                .setParam("p4", "def4")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specB", "name", "b1"))
                                .setParam("p1", "group1")
                                .setParam("p2", "config1")
                                .setParam("p3", "def3")
                                .setParam("specA", "a1")
                                .build())
                        .addFeature(ProvisionedFeatureBuilder.builder(ResolvedFeatureId.create(FP_GAV, "specC", "name", "c1"))
                                .setParam("p1", "config1")
                                .setParam("p2", "group1b")
                                .setParam("p3", "group1a")
                                .setParam("p4", "def4")
                                .setParam("p5", "config1")
                                .setParam("specA", "a1")
                                .setParam("specB", "b1")
                                .build())
                        .build())
                .build();
    }
}
