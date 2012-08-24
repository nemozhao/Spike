package org.spike.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * <code>FileUtils</code>
 * 
 * @author mikomatic
 */
public class FileUtils {

	private static Logger log = Logger.getLogger(FileUtils.class.getName());

	private FileUtils() {
		// private constructor to preevent class instance
	}

	private static void listFiles(String pSrcPath, FileFilter pFileFilter,
			List<File> pFileList) {
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

	public static List<File> listFiles(String pSrcPath, FileFilter pFileFilter) {
		List<File> lFileList = new ArrayList<File>();
		listFiles(pSrcPath, pFileFilter, lFileList);
		return lFileList;
	}

	public static void copyFolder(String pSrcPath, String pDestPath,
			FilenameFilter pFilter) throws IOException {
		File lSource = new File(pSrcPath);
		if (!lSource.exists()) {
			log.info("Directory " + pSrcPath + "doesn't exists");
			return;
		}

		if (lSource.isDirectory()) {

			// if directory not exists, create it
			File lDestination = new File(pDestPath);
			if (!lDestination.exists()) {
				lDestination.mkdir();
				log.info("Directory copied from " + lSource + "  to "
						+ lDestination);
			}

			// list all the directory contents
			String files[] = lSource.list(pFilter);

			for (String file : files) {
				// construct the src and dest file structure
				File srcFile = new File(lSource, file);
				File destFile = new File(lDestination, file);
				// recursive copy
				copyFolder(srcFile.getAbsolutePath(),
						destFile.getAbsolutePath(), pFilter);
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
			log.info("File copied from " + lSource + " to " + lDestination);
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
