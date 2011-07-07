/*******************************************************************************
 * Copyright (c) 2010, 2011 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.tycho.plugins.p2.extras;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.it.util.IOUtil;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.eclipse.tycho.testing.AbstractTychoMojoTestCase;

public class MirrorMojoTest extends AbstractTychoMojoTestCase {

    public void testMirror() throws Exception {
        File basedir = getBasedir("mirroring/testProject");
        List<MavenProject> projects = getSortedProjects(basedir, null);
        MavenProject project = projects.get(0);

        initLegacySupport(projects, project);

        File publishedContentDir = new File(project.getFile().getParent(), "target/repository").getCanonicalFile();
        File sourceRepository = new File("src/test/resources/mirroring/sourceUpdatesite").getCanonicalFile();

        // call mirror mojo
        Mojo mirrorMojo = lookupMojo("mirror", project.getFile());
        setVariableValueToObject(mirrorMojo, "project", project);
        setVariableValueToObject(mirrorMojo, "source",
                Collections.singletonList(new Repository(sourceRepository.toURI())));
        setVariableValueToObject(mirrorMojo, "destination", publishedContentDir);

        mirrorMojo.execute();

        assertTrue(publishedContentDir.exists());
        assertMirroredBundle(publishedContentDir, "testbundle", "1.0.0");
        assertMirroredFeature(publishedContentDir, "testfeature", "1.0.0");
    }

    private static void assertMirroredBundle(File publishedContentDir, String bundleID, String version) {
        assertMirroredArtifact(publishedContentDir, bundleID, version, "plugins");
    }

    private static void assertMirroredFeature(File publishedContentDir, String featureID, String version) {
        assertMirroredArtifact(publishedContentDir, featureID, version, "features");
    }

    private static void assertMirroredArtifact(File publishedContentDir, String id, String version, String folder) {
        String pluginArtifactNamePrefix = id + "_" + version; // without qualifier
        for (File bundle : new File(publishedContentDir, folder).listFiles()) {
            if (bundle.getName().startsWith(pluginArtifactNamePrefix))
                return;
        }

        Assert.fail("Published artifact not found: " + pluginArtifactNamePrefix);
    }

    private void initLegacySupport(List<MavenProject> projects, MavenProject currentProject) throws Exception {
        MavenSession session = newMavenSession(currentProject, projects);
        LegacySupport buildContext = lookup(LegacySupport.class);
        buildContext.setSession(session);
    }

    // use the normal local Maven repository (called by newMavenSession)
    @Override
    protected ArtifactRepository getLocalRepository() throws Exception {
        RepositorySystem repoSystem = lookup(RepositorySystem.class);
        File path = getLocalMavenRepository().getCanonicalFile();
        ArtifactRepository r = repoSystem.createLocalRepository(path);
        return r;
    }

    private File getLocalMavenRepository() {
        /*
         * The build (more specifically, the maven-properties-plugin) writes the local Maven
         * repository location to a file. Here, we read this file. (Approach copied from tycho-its.)
         */
        Properties buildProperties = new Properties();
        InputStream is = this.getClassLoader().getResourceAsStream("baseTest.properties");
        try {
            buildProperties.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtil.close(is);
        }
        return new File(buildProperties.getProperty("local-repo"));
    }
}
