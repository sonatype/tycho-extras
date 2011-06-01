package org.eclipse.tycho.versionbump;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.eclipse.tycho.core.utils.ExecutionEnvironmentUtils;
import org.eclipse.tycho.core.utils.PlatformPropertiesUtils;
import org.eclipse.tycho.equinox.EquinoxServiceFactory;
import org.eclipse.tycho.p2.facade.internal.P2RepositoryCacheImpl;
import org.eclipse.tycho.p2.resolver.P2Logger;
import org.eclipse.tycho.p2.resolver.P2Resolver;
import org.eclipse.tycho.p2.resolver.P2ResolverFactory;

public abstract class AbstractUpdateMojo
    extends AbstractMojo
{
    /**
     * @parameter expression="${session}"
     */
    protected MavenSession session;

    /** @component */
    protected EquinoxServiceFactory equinox;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        try
        {
            P2ResolverFactory factory = equinox.getService( P2ResolverFactory.class );
            doUpdate( factory );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Could not update " + getTargetFile().getAbsolutePath(), e );
        }
    }

    protected abstract File getTargetFile();

    protected abstract void doUpdate( P2ResolverFactory factory ) throws IOException, URISyntaxException;

    protected P2Resolver newResolver( P2ResolverFactory factory )
    {
        P2Resolver p2 = factory.createResolver();
        p2.setRepositoryCache( new P2RepositoryCacheImpl() );
        p2.setLocalRepositoryLocation( new File( session.getLocalRepository().getBasedir() ) );
        p2.setLogger( new P2Logger()
        {
            public void debug( String message )
            {
                if ( message != null && message.length() > 0 )
                {
                    getLog().info( message ); // TODO
                }
            }

            public void info( String message )
            {
                if ( message != null && message.length() > 0 )
                {
                    getLog().info( message );
                }
            }

            public boolean isDebugEnabled()
            {
                return getLog().isDebugEnabled();
            }
        } );
        return p2;
    }

    protected List<Map<String, String>> getEnvironments()
    {
        Properties properties = new Properties();
        properties.put( PlatformPropertiesUtils.OSGI_OS, PlatformPropertiesUtils.getOS( properties ) );
        properties.put( PlatformPropertiesUtils.OSGI_WS, PlatformPropertiesUtils.getWS( properties ) );
        properties.put( PlatformPropertiesUtils.OSGI_ARCH, PlatformPropertiesUtils.getArch( properties ) );
        ExecutionEnvironmentUtils.loadVMProfile( properties );

        // TODO does not belong here
        properties.put( "org.eclipse.update.install.features", "true" );

        Map<String, String> map = new LinkedHashMap<String, String>();
        for ( Object key : properties.keySet() )
        {
            map.put( key.toString(), properties.getProperty( key.toString() ) );
        }

        ArrayList<Map<String, String>> result = new ArrayList<Map<String, String>>();
        result.add( map );
        return result;
    }

}
