package org.spike.log;

import java.util.logging.Logger;


/**
 * <code>SpikeLogger</code>
 *
 * @version $Revision: 1.2 $ $Date: 2007/02/13 16:12:29 1 août 2012 16:16:36 $
 * @author      ORTEGAMi, $Author: freyth $
 */
public class SpikeLogger extends Logger {

    protected SpikeLogger( String pName ) {
        super( pName,null );
        addHandler( new SpikeLogHandler() );
    }




}
