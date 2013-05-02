package org.spike.utils;

import java.io.File;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * @author		mikomatic
 */
public class FileWatcher extends TimerTask {

    private static final Logger log = Logger.getLogger( FileWatcher.class.getName() );

    private long timeStamp;
    private final File file;

    public FileWatcher( File file ) {
        this.file = file;
        this.timeStamp = file.lastModified();
    }

    @Override
    public void run() {
        long lTimeStamp = file.lastModified();

        if ( this.timeStamp != lTimeStamp ) {
            this.timeStamp = lTimeStamp;
            onChange( file );
        }
    }

    private void onChange( final File pFile ) {
        log.info( "File Changed: " + pFile.getName() );
    }
}
