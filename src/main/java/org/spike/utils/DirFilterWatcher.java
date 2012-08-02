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

package org.spike.utils;

import java.io.File;
import java.io.FileFilter;


/**
 * <code>DirFilterWatcher</code>
 * @author mikomatic
 */
public class DirFilterWatcher implements FileFilter {

    private String filter;

    public DirFilterWatcher() {
      this.filter = "";
    }

    public DirFilterWatcher(String filter) {
      this.filter = filter;
    }

    public boolean accept(File file) {
      if ("".equals(filter)) {
        return true;
      }
      return (file.getName().startsWith(filter));
    }

}
