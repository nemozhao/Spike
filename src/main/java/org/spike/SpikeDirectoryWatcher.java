/**
 *
 */
package org.spike;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.logging.Logger;

import org.spike.utils.DirectoryWatcher;
import org.spike.utils.FileUtils;

import freemarker.template.TemplateException;

/**
 * @author mikomatic
 *
 */
public class SpikeDirectoryWatcher extends DirectoryWatcher {

    private static Logger log = Logger.getLogger( SpikeDirectoryWatcher.class.getName() );

    private static Spike spike;

    public SpikeDirectoryWatcher( Spike pSpike, String path ) {
        super( path );
        spike = pSpike;
    }

    @Override
    protected void onChange( String pFilePathName, String pAction ) {
        log.info( "A File change has been detected \n " + "File Path : " + pFilePathName + " Action: " + pAction );

        try {
            File lFile = new File(  spike.getSourcePath() );
            //C'est un fichier lié au process. Il faut le relance
            if ( pFilePathName.replace( lFile.getAbsolutePath()+File.separator,"" ).startsWith( "_" ) ) {
                System.out.println( "Relaunching spike process" );
                spike.runProcess(false);
            }
            //Sinon (eg. fichier d'affichage: css, js)
            else {
                FilenameFilter lFilenameFilter = new FilenameFilter() {

                    public boolean accept( File dir, String name ) {
                        return !name.startsWith( "_" ) && !spike.getOutputName().equals( name );
                    }
                };

                System.out.println( "Copying ressources files & directories..." );
                FileUtils.copyFolder( spike.getSourcePath(), spike.getOutput(), lFilenameFilter );
            }
        }
        catch ( IOException e ) {
            log.severe( e.getMessage() );
        }
        catch ( TemplateException e ) {
            log.severe( e.getMessage() );
        }
    }

}
