package org.sonatype.tycho.versionbump;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tycho.model.Target;
import org.eclipse.tycho.model.Target.Location;
import org.eclipse.tycho.model.Target.Repository;
import org.eclipse.tycho.model.Target.Unit;
import org.eclipse.tycho.p2.resolver.P2ResolutionResult;
import org.eclipse.tycho.p2.resolver.P2Resolver;
import org.eclipse.tycho.p2.resolver.P2ResolverFactory;

/**
 * Quick&dirty way to update .target file to use latest versions of IUs available from specified metadata repositories.
 * 
 * @goal update-target
 */
public class UpdateTargetMojo
    extends AbstractUpdateMojo
{
    /**
     * @parameter expression="${target}"
     */
    private File targetFile;

    protected void doUpdate( P2ResolverFactory factory )
        throws IOException, URISyntaxException
    {
        P2Resolver p2 = newResolver( factory );

        Target target = Target.read( targetFile );

        for ( Location location : target.getLocations() )
        {
            for ( Repository repository : location.getRepositories() )
            {
                URI uri = new URI( repository.getLocation() );
                p2.addP2Repository( uri );
            }

            for ( Unit unit : location.getUnits() )
            {
                p2.addDependency( P2Resolver.TYPE_INSTALLABLE_UNIT, unit.getId(), "0.0.0" );
            }
        }

        P2ResolutionResult result = p2.resolveMetadata( getEnvironments().get( 0 ) );

        Map<String, String> ius = new HashMap<String, String>();
        for ( P2ResolutionResult.Entry entry : result.getArtifacts() )
        {
            ius.put( entry.getId(), entry.getVersion() );
        }

        for ( Location location : target.getLocations() )
        {
            for ( Unit unit : location.getUnits() )
            {
                String version = ius.get( unit.getId() );
                if ( version != null )
                {
                    unit.setVersion( version );
                }
                else
                {
                    getLog().error( "Resolution result does not contain root installable unit " + unit.getId() );
                }
            }
        }

        Target.write( target, targetFile );
    }

    @Override
    protected File getTargetFile()
    {
        return targetFile;
    }

}
