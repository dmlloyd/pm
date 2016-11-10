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

package org.jboss.provisioning;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.jboss.provisioning.config.FeaturePackConfig;
import org.jboss.provisioning.config.ProvisioningConfig;
import org.jboss.provisioning.plugin.ProvisioningContext;
import org.jboss.provisioning.plugin.ProvisioningPlugin;
import org.jboss.provisioning.spec.FeaturePackDependencySpec;
import org.jboss.provisioning.spec.FeaturePackLayoutDescription;
import org.jboss.provisioning.spec.FeaturePackSpec;
import org.jboss.provisioning.state.ProvisionedState;
import org.jboss.provisioning.util.FeaturePackLayoutDescriber;
import org.jboss.provisioning.util.FeaturePackLayoutInstaller;
import org.jboss.provisioning.util.IoUtils;
import org.jboss.provisioning.util.LayoutUtils;
import org.jboss.provisioning.util.ProvisionedStateResolver;
import org.jboss.provisioning.util.ZipUtils;

/**
 *
 * @author Alexey Loubyansky
 */
class ProvisioningTask {

    private final ArtifactResolver artifactResolver;
    private final Path installationHome;
    private final String encoding;
    private final ProvisioningConfig provisioningConfig;

    private final Path workDir;
    private final Path layoutDir;
    private Collection<ArtifactCoords.Gav> provisioningPlugins = Collections.emptySet();

    ProvisioningTask(ArtifactResolver artifactResolver, Path installationHome, String encoding, ProvisioningConfig provisioningConfig) {
        this.artifactResolver = artifactResolver;
        this.installationHome = installationHome;
        this.encoding = encoding;
        this.provisioningConfig = provisioningConfig;

        workDir = IoUtils.createRandomTmpDir();
        layoutDir = workDir.resolve("layout");
    }

    void execute() throws ProvisioningException {
        try {
            // Here the layout description and the extended provisioning configuration are built first.
            // The extended provisioning config includes all the effective feature-packs configs
            // (including the ones from the transitive dependencies).
            final FeaturePackLayoutDescription.Builder layoutBuilder = FeaturePackLayoutDescription.builder();

            Map<ArtifactCoords.Gav, FeaturePackConfig.Builder> fpConfigBuilders = Collections.emptyMap();
            final Collection<FeaturePackConfig> fpConfigs = provisioningConfig.getFeaturePacks();
            for (FeaturePackConfig provisionedFp : fpConfigs) {
                final Map<ArtifactCoords.Gav, FeaturePackConfig.Builder> newBuilders = layoutFeaturePack(provisionedFp, layoutBuilder);
                fpConfigBuilders = merge(fpConfigBuilders, newBuilders);
            }
            for (FeaturePackConfig fpConfig : fpConfigs) {
                fpConfigBuilders = enforce(layoutBuilder.getFeaturePack(fpConfig.getGav().toGa()), fpConfig, fpConfigBuilders);
            }

            final FeaturePackLayoutDescription layoutDescr = layoutBuilder.build();
            final ProvisioningConfig.Builder extendedConfigBuilder = ProvisioningConfig.builder();
            for(Map.Entry<ArtifactCoords.Gav, FeaturePackConfig.Builder> entry : fpConfigBuilders.entrySet()) {
                extendedConfigBuilder.addFeaturePack(entry.getValue().build());
            }
            final ProvisioningConfig extendedConfig = extendedConfigBuilder.build();

            // Resolve the target provisioned state
            final ProvisionedState provisionedState = new ProvisionedStateResolver().resolve(extendedConfig, layoutDescr, layoutDir);

            if (Files.exists(installationHome)) {
                IoUtils.recursiveDelete(installationHome);
            }
            mkdirs(installationHome);

            // install the software
            FeaturePackLayoutInstaller.install(layoutDescr, layoutDir, provisioningConfig, provisionedState, installationHome);
            if(!provisioningPlugins.isEmpty()) {
                executePlugins(provisioningConfig, layoutDescr);
            }
        } finally {
            IoUtils.recursiveDelete(workDir);
        }
    }

    private Map<ArtifactCoords.Gav, FeaturePackConfig.Builder> layoutFeaturePack(
            FeaturePackConfig fpConfig,
            FeaturePackLayoutDescription.Builder layoutBuilder) throws ProvisioningException {

        final ArtifactCoords.Gav fpGav = fpConfig.getGav();
        final FeaturePackSpec fpSpec;
        final Path fpWorkDir = LayoutUtils.getFeaturePackDir(layoutDir, fpGav, false);
        if(!layoutBuilder.hasFeaturePack(fpGav.toGa())) {
            final Path artifactPath = artifactResolver.resolve(fpGav.toArtifactCoords());
            mkdirs(fpWorkDir);
            try {
                System.out.println("Adding " + fpGav + " to the layout at " + fpWorkDir);
                ZipUtils.unzip(artifactPath, fpWorkDir);
            } catch (IOException e) {
                throw new ProvisioningException("Failed to unzip " + artifactPath + " to " + layoutDir, e);
            }

            try {
                fpSpec = FeaturePackLayoutDescriber.describeFeaturePack(fpWorkDir, encoding);
            } catch (ProvisioningDescriptionException e) {
                throw new ProvisioningException("Failed to describe feature-pack " + fpGav, e);
            }

            if(fpSpec.hasProvisioningPlugins()) {
                for(ArtifactCoords.Gav gav : fpSpec.getProvisioningPlugins()) {
                    addProvisioningPlugin(gav);
                }
            }

            try {
                layoutBuilder.addFeaturePack(fpSpec);
            } catch (ProvisioningDescriptionException e) {
                throw new ProvisioningException("Failed to layout feature packs", e);
            }
        } else {
            fpSpec = layoutBuilder.getFeaturePack(fpGav.toGa());
            if(!fpSpec.getGav().equals(fpGav)) {
                throw new ProvisioningException(Errors.featurePackVersionConflict(fpSpec.getGav(), fpGav));
            }
        }

        Map<ArtifactCoords.Gav, FeaturePackConfig.Builder> fpBuilders = Collections.emptyMap();
        if(fpSpec.hasDependencies()) {
            for(FeaturePackDependencySpec dep : fpSpec.getDependencies()) {
                fpBuilders = merge(fpBuilders, layoutFeaturePack(dep.getTarget(), layoutBuilder));
            }
            for (FeaturePackDependencySpec dep : fpSpec.getDependencies()) {
                fpBuilders = enforce(layoutBuilder.getFeaturePack(dep.getTarget().getGav().toGa()), dep.getTarget(), fpBuilders);
            }
        }

        // resources should be copied last overriding the dependency resources
        final Path fpResources = fpWorkDir.resolve(Constants.RESOURCES);
        if(Files.exists(fpResources)) {
            try {
                IoUtils.copy(fpResources, workDir.resolve(Constants.RESOURCES));
            } catch (IOException e) {
                throw new ProvisioningException(Errors.copyFile(fpResources, workDir.resolve(Constants.RESOURCES)), e);
            }
        }
        return fpBuilders;
    }

    private Map<ArtifactCoords.Gav, FeaturePackConfig.Builder> enforce(
            FeaturePackSpec fpSpec,
            FeaturePackConfig fpConfig,
            Map<ArtifactCoords.Gav, FeaturePackConfig.Builder> fpBuilders) throws ProvisioningDescriptionException {
        final ArtifactCoords.Gav fpGav = fpConfig.getGav();
        switch(fpBuilders.size()) {
            case 0:
                fpBuilders = Collections.singletonMap(fpGav, FeaturePackConfig.builder(fpSpec, fpConfig));
                break;
            case 1:
                if(fpBuilders.containsKey(fpGav)) {
                    fpBuilders.get(fpGav).enforce(fpConfig);
                    break;
                }
                fpBuilders = new LinkedHashMap<>(fpBuilders);
            default:
                if(fpBuilders.containsKey(fpGav)) {
                    fpBuilders.get(fpGav).enforce(fpConfig);
                } else {
                    fpBuilders.put(fpGav, FeaturePackConfig.builder(fpSpec, fpConfig));
                }
        }
        return fpBuilders;
    }

    private Map<ArtifactCoords.Gav, FeaturePackConfig.Builder> merge(
            Map<ArtifactCoords.Gav, FeaturePackConfig.Builder> allBuilders,
            final Map<ArtifactCoords.Gav, FeaturePackConfig.Builder> depBuilders)
            throws ProvisioningDescriptionException {
        switch(allBuilders.size()) {
            case 0:
                allBuilders = depBuilders;
                break;
            case 1:
                final ArtifactCoords.Gav provisionedGav = allBuilders.keySet().iterator().next();
                if(depBuilders.size() == 1 && depBuilders.containsKey(provisionedGav)) {
                    allBuilders.get(provisionedGav).merge(depBuilders.get(provisionedGav).build());
                    break;
                }
                allBuilders = new LinkedHashMap<>(allBuilders);
            default:
                for(Map.Entry<ArtifactCoords.Gav, FeaturePackConfig.Builder> depEntry : depBuilders.entrySet()) {
                    final FeaturePackConfig.Builder fpBuilder = allBuilders.get(depEntry.getKey());
                    if(fpBuilder == null) {
                        allBuilders.put(depEntry.getKey(), depEntry.getValue());
                    } else {
                        fpBuilder.merge(depEntry.getValue().build());
                    }
                }
        }
        return allBuilders;
    }

    private void executePlugins(final ProvisioningConfig provisioningConfig,
            final FeaturePackLayoutDescription layoutDescr) throws ProvisioningException {
        final List<java.net.URL> urls = new ArrayList<java.net.URL>(provisioningPlugins.size());
        for(ArtifactCoords.Gav gavPart : provisioningPlugins) {
            try {
                urls.add(artifactResolver.resolve(gavPart.toArtifactCoords()).toUri().toURL());
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if (!urls.isEmpty()) {
            final ProvisioningContext ctx = new ProvisioningContext() {
                @Override
                public Path getLayoutDir() {
                    return layoutDir;
                }
                @Override
                public Path getInstallDir() {
                    return installationHome;
                }
                @Override
                public Path getResourcesDir() {
                    return workDir.resolve("resources");
                }
                @Override
                public ProvisioningConfig getProvisioningConfig() {
                    return provisioningConfig;
                }
                @Override
                public FeaturePackLayoutDescription getLayoutDescription() {
                    return layoutDescr;
                }
                @Override
                public Path resolveArtifact(ArtifactCoords coords) throws ArtifactResolutionException {
                    return artifactResolver.resolve(coords);
                }
                @Override
                public String getEncoding() {
                    return encoding;
                }
            };
            final java.net.URLClassLoader ucl = new java.net.URLClassLoader(
                    urls.toArray(new java.net.URL[urls.size()]),
                    Thread.currentThread().getContextClassLoader());
            final ServiceLoader<ProvisioningPlugin> plugins = ServiceLoader.load(ProvisioningPlugin.class, ucl);
            for (ProvisioningPlugin plugin : plugins) {
                try {
                    plugin.postInstall(ctx);
                } catch (ProvisioningException e) {
                    throw new ProvisioningException("Provisioning plugin failed", e);
                }
            }
        }
    }

    private void addProvisioningPlugin(ArtifactCoords.Gav gav) {
        switch(provisioningPlugins.size()) {
            case 0:
                provisioningPlugins = Collections.singleton(gav);
                break;
            case 1:
                if(provisioningPlugins.contains(gav)) {
                    return;
                }
                provisioningPlugins = new LinkedHashSet<>(provisioningPlugins);
            default:
                provisioningPlugins.add(gav);
        }
    }

    private void mkdirs(final Path path) throws ProvisioningException {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new ProvisioningException(Errors.mkdirs(path));
        }
    }
}
