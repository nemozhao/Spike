package org.spike.utils;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * @author mikomatic
 */
public abstract class DirectoryWatcher extends TimerTask {

	private static Logger LOG = Logger.getLogger(DirectoryWatcher.class
			.getName());

	protected String sourcePath;
	private File filesArray[];
	private Map<String, Long> dir = new HashMap<String, Long>();
	private DirFilterWatcher dfw;

	public DirectoryWatcher(String path) {
		this(path, "site");
	}

	public DirectoryWatcher(String path, String filter) {
		this.sourcePath = path;
		dfw = new DirFilterWatcher(filter, false);
		List<File> listsfiles = FileUtils.listFiles(path, dfw);
		File[] lFileArray = new File[listsfiles.size()];

		filesArray = listsfiles.toArray(lFileArray);

		// On considère qu'il n'y a pas deux fichiers avec le même path et le
		// même nom
		// lastModfied value
		for (File lFile : filesArray) {
			dir.put(lFile.getAbsolutePath(), new Long(lFile.lastModified()));
		}
	}

	@Override
	public final void run() {
		HashSet<String> checkedFiles = new HashSet<String>();

		List<File> listsfiles = FileUtils.listFiles(sourcePath, dfw);
		File[] lFileArray = new File[listsfiles.size()];

		filesArray = listsfiles.toArray(lFileArray);

		// scan the files and check for modification/addition
		for (File lFile : filesArray) {

			String lFilePathName = lFile.getAbsolutePath();
			Long current = dir.get(lFilePathName);
			if (current == null) {
				// new file
				dir.put(lFilePathName, new Long(lFile.lastModified()));
				onChange(lFilePathName, "add");
			} else if (current.longValue() != lFile.lastModified()) {
				// modified file
				dir.put(lFilePathName, new Long(lFile.lastModified()));
				onChange(lFilePathName, "modify");
			}
			checkedFiles.add(lFilePathName);
		}

		HashSet<String> lAllFiles = new HashSet<String>(dir.keySet());
		lAllFiles.removeAll(checkedFiles);
		// If All is not null, this means a files has been deleted
		Iterator<String> it = lAllFiles.iterator();
		while (it.hasNext()) {
			String lDeletedPathName = it.next();
			dir.remove(lDeletedPathName);
			onChange(lDeletedPathName, "delete");
		}

	}

	protected abstract void onChange(String pFilePathName, String action);

}
