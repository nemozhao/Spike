package org.spike.utils;

import java.io.File;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * @author		mikomatic
 */
public class FileWatcher extends TimerTask {

    private static Logger log = Logger.getLogger( FileWatcher.class.getName() );

    private long timeStamp;
    private File file;

    public FileWatcher( File file ) {
        this.file = file;
        this.timeStamp = file.lastModified();
    }

    @Override
    public void run() {
        long timeStamp = file.lastModified();

        if ( this.timeStamp != timeStamp ) {
            this.timeStamp = timeStamp;
            onChange( file );
        }
    }

    private void onChange( File pFile ) {
        log.info( "File Changed: "+pFile.getName() );
    }
}
