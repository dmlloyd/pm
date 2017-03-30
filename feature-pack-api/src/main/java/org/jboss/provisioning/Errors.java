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
package org.jboss.provisioning;

import java.nio.file.Path;
import java.util.Collection;

import org.jboss.provisioning.ArtifactCoords.Gav;

/**
 *
 * @author Alexey Loubyansky
 */
public interface Errors {

    // GENERAL MESSAGES

    static String pathDoesNotExist(Path p) {
        return "Failed to locate " + p.toAbsolutePath();
    }

    static String pathAlreadyExists(Path p) {
        return "Path already exists " + p.toAbsolutePath();
    }

    static String mkdirs(Path p) {
        return "Failed to make directories " + p.toAbsolutePath();
    }

    static String readDirectory(Path p) {
        return "Failed to read directory " + p.toAbsolutePath();
    }

    static String notADir(Path p) {
        return p.toAbsolutePath() + " is not a directory";
    }

    static String copyFile(Path src, Path target) {
        return "Failed to copy " + src + " to " + target;
    }

    static String moveFile(Path src, Path target) {
        return "Failed to move " + src.toAbsolutePath() + " to " + target.toAbsolutePath();
    }

    static String openFile(Path p) {
        return "Failed to open " + p.toAbsolutePath();
    }

    static String readFile(Path p) {
        return "Failed to read " + p.toAbsolutePath();
    }

    static String parseXml() {
        return "Failed to parse XML";
    }

    static String parseXml(Path p) {
        return "Failed to parse " + p.toAbsolutePath();
    }

    static String writeFile(Path p) {
        return "Failed to write to " + p.toAbsolutePath();
    }

    static String hashCalculation(Path path) {
        return "Hash calculation failed for " + path;
    }

    // FEATURE PACK INSTALL MESSAGES

    static String packageContentCopyFailed(String packageName) {
        return "Failed to copy package " + packageName + " content";
    }

    static String packageNotFound(ArtifactCoords.Gav fp, String packageName) {
        return "Failed to resolve package " + packageName + " in " + fp;
    }

    static String missingParameter(String string) {
        return "Missing " + string;
    }

    static String unknownPackage(ArtifactCoords.Gav gav, String pkgName) {
        return "Package " + pkgName + " is not found in " + gav;
    }

    static String unknownFeaturePack(ArtifactCoords.Gav gav) {
        return "Feature-pack " + gav + " is not found";
    }

    static String featurePackVersionConflict(ArtifactCoords.Gav gav, ArtifactCoords.Gav gav2) {
        return "Feature-pack " + gav.toGa() + " was specified with version " + gav.getVersion() + " and " + gav2.getVersion();
    }

    static String unsatisfiedPackageDependencies(ArtifactCoords.Gav fpGav, String packageName, Collection<String> unsatisfiedDeps) {
        return "Feature-pack " + fpGav + " package " + packageName + " has unsatisfied dependencies on packages " + unsatisfiedDeps;
    }

    static String unsatisfiedPackageDependency(ArtifactCoords.Gav fpGav, String srcPackage, String targetPackage) {
        return "Feature-pack " + fpGav + " package " + srcPackage + " has unsatisfied dependency on package " + targetPackage;
    }

    static String unsatisfiedExternalPackageDependency(ArtifactCoords.Gav srcGav, String srcPackage, ArtifactCoords.Gav targetGav, String targetPackage) {
        return "Feature-pack " + srcGav + " package " + srcPackage + " has unsatisfied dependency on feature-pack " + targetGav + " package " + targetPackage;
    }

    static String packageExcludeInclude(String packageName) {
        return "Package " + packageName + " is explicitly excluded and included";
    }

    static String duplicateDependencyName(String name) {
        return "Dependency with name " + name + " already exists";
    }

    static String unknownDependencyName(Gav gav, String depName) {
        return "Dependency " + depName + " not found in " + gav + " feature-pack description";
    }

    static String featurePackAlreadyInstalled(Gav gav) {
        return "Feature-pack " + gav + " is already installed";
    }

    static String unknownFeaturePackDependencyName(ArtifactCoords.Gav fpGav, String pkgName, String depName) {
        return fpGav + " package " + pkgName + " references unknown feature-pack dependency " + depName;
    }

    static String packageAlreadyExists(Gav gav, String name) {
        return "Package " + name + " already exists in feature-pack " + gav;
    }

    static String packageParameterResolverNotProvided(Gav gav, String pkgName) {
        return "Package parameter resolver not provided for package " + pkgName + " in feature-pack " + gav;
    }

    static String unknownParameterInDependency(Gav gav, String srcPkg, String trgPkg, String param) {
        return "Package " + srcPkg + " from feature-pack " + gav + " overwrites a non-existing parameter " + param
                + " in its dependency on package " + trgPkg;
    }
}
