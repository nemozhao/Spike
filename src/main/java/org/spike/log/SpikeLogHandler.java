/*
 * Copyright 1998-2012 by SLIB,
 * 70 rue Villette, 69003 Lyon, France
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SLIB. (&quot;Confidential Information&quot;).  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with SLIB.
 */

package org.spike.log;

import java.util.logging.ConsoleHandler;

/**
 * <code>SpikeLogHandler</code>
 *
 * @version $Revision: 1.2 $ $Date: 2007/02/13 16:12:29 1 août 2012 16:11:29 $
 * @author      ORTEGAMi, $Author: freyth $
 */
public class SpikeLogHandler extends ConsoleHandler {

    public SpikeLogHandler() {
        super();
        setFormatter( new SpikeLoggerFormatter() );
    }
}
