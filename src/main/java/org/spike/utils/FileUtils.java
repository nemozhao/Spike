package org.spike.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * <code>FileUtils</code>
 *
 * @author mikomatic
 */
public class FileUtils {
  private static final Logger LOG = Logger.getLogger(FileUtils.class.getName());

  private FileUtils() {
    // private constructor to preevent class instance
  }

  private static void listFiles(final String pSrcPath, final FilenameFilter pFileFilter, final List<File> pFileList) {
    File[] listFiles = new File(pSrcPath).listFiles(pFileFilter);
    if (listFiles != null) {
      for (File file : listFiles) {
        if (file.isDirectory()) {
          pFileList.add(file);
          listFiles(file.getAbsolutePath(), pFileFilter, pFileList);
        } else {
          pFileList.add(file);
        }
      }
    }
  }

  public static List<File> listFiles(final String pSrcPath, final FilenameFilter pFileFilter) {
    List<File> lFileList = new ArrayList<File>();
    listFiles(pSrcPath, pFileFilter, lFileList);
    return lFileList;
  }

  public static void copyFolder(final String pSrcPath, final String pDestPath, final FilenameFilter pFilter)
      throws IOException {
    File lSource = new File(pSrcPath);
    if (!lSource.exists()) {
      LOG.warn("Directory " + pSrcPath + "doesn't exists");
      return;
    }
    if (lSource.isDirectory()) {
      // if directory not exists, create it
      File lDestination = new File(pDestPath);
      if (!lDestination.exists()) {
        lDestination.mkdir();
        LOG.warn("Directory copied from " + lSource + "  to " + lDestination);
      }
      // list all the directory contents
      String files[] = lSource.list(pFilter);
      for (String file : files) {
        // construct the src and dest file structure
        File srcFile = new File(lSource, file);
        File destFile = new File(lDestination, file);
        // recursive copy
        copyFolder(srcFile.getAbsolutePath(), destFile.getAbsolutePath(), pFilter);
      }
    } else {
      File lDestination = new File(pDestPath);
      // if file, then copy it
      // Use bytes stream to support all file types
      InputStream in = new FileInputStream(lSource);
      OutputStream out = new FileOutputStream(lDestination);
      byte[] buffer = new byte[1024];
      int length;
      // copy the file content in bytes
      while ((length = in.read(buffer)) > 0) {
        out.write(buffer, 0, length);
      }
      in.close();
      out.close();
      LOG.warn("File copied from " + lSource + " to " + lDestination);
    }
  }

  public static void deleteFolder(String pPathName) {
    File lFolderToDelete = new File(pPathName);
    if (lFolderToDelete.exists() && lFolderToDelete.isDirectory()) {
      for (File lFile : lFolderToDelete.listFiles()) {
        if (lFile.isDirectory()) {
          deleteFolder(lFile.getAbsolutePath());
        } else {
          lFile.delete();
        }
      }
      // Should be empty now
      lFolderToDelete.delete();
    }
  }
}
