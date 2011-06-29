/*******************************************************************************
 * Copyright (c) 2011 SAP AG and others.
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
import java.net.URI;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.tycho.equinox.EquinoxServiceFactory;
import org.eclipse.tycho.osgi.adapters.MavenLoggerAdapter;
import org.eclipse.tycho.p2.tools.DestinationRepositoryDescriptor;
import org.eclipse.tycho.p2.tools.FacadeException;
import org.eclipse.tycho.p2.tools.RepositoryReferences;
import org.eclipse.tycho.p2.tools.mirroring.facade.MirrorApplicationService;

/**
 * @goal mirror
 */
public class MirrorMojo extends AbstractMojo {

    /** @parameter expression="${project}" */
    private MavenProject project;

    /** @component */
    private EquinoxServiceFactory p2;

    /** @component */
    private Logger logger;

    /** @parameter */
    private URI[] sourceRepositories;

    /** @parameter default-value="${project.build.directory}/repository" */
    private File destination;

    /** @parameter default-value="true" */
    private boolean compress = true;

    public void execute() throws MojoExecutionException, MojoFailureException {
        final MirrorApplicationService mirrorService = p2.getService(MirrorApplicationService.class);

        final RepositoryReferences sources = new RepositoryReferences();
        for (final URI sourceRepository : sourceRepositories) {
            sources.addMetadataRepository(sourceRepository);
            sources.addArtifactRepository(sourceRepository);
        }

        int flags = MirrorApplicationService.MIRROR_ARTIFACTS;
        flags |= (compress ? MirrorApplicationService.REPOSITORY_COMPRESS : 0);

        final DestinationRepositoryDescriptor destinationDescriptor = new DestinationRepositoryDescriptor(destination,
                "");

        try {
            mirrorService.mirrorStandalone(sources, destinationDescriptor, flags, new File(project.getBuild()
                    .getDirectory()), new MavenLoggerAdapter(logger, false));
        } catch (final FacadeException e) {
            throw new MojoExecutionException("Error during mirroring", e);
        }
    }

}
