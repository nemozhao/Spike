package org.spike.utils;

import java.io.File;
import java.util.TimerTask;

import org.apache.log4j.Logger;

/**
 * @author		mikomatic
 */
public class FileWatcher extends TimerTask {
  private static final Logger LOG = Logger.getLogger(FileWatcher.class.getName());
  private long timeStamp;
  private final File file;

  public FileWatcher(File file) {
    this.file = file;
    this.timeStamp = file.lastModified();
  }

  @Override
  public void run() {
    long lTimeStamp = file.lastModified();
    if (this.timeStamp != lTimeStamp) {
      this.timeStamp = lTimeStamp;
      onChange(file);
    }
  }

  private void onChange(final File pFile) {
    LOG.info("File Changed: " + pFile.getName());
  }
}
