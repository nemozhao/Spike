/**
 *
 */
package org.spike;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.spike.utils.FileUtils;

import freemarker.template.TemplateException;

/**
 * @author mikomatic
 * 
 */
public class SpikeDirectoryWatcher extends TimerTask implements FilenameFilter {

	private static Logger log = Logger.getLogger(SpikeDirectoryWatcher.class.getName());

	private static Spike spike;
	private File filesArray[];
	private final Map<String, Long> dir = new HashMap<String, Long>();

	private enum Action {
		ADD, DELETE, MODIFY;
	}

	private class FileAction {

		private final Action action;

		private final String fileName;

		public FileAction(Action pAction, String pFileName) {
			super();
			action = pAction;
			fileName = pFileName;
		}

		public Action getAction() {
			return action;
		}

		public String getFileName() {
			return fileName;
		}
	}

	public SpikeDirectoryWatcher(Spike pSpike) {
		spike = pSpike;
		List<File> listsfiles = FileUtils.listFiles(spike.getSourcePath(), this);
		filesArray = listsfiles.toArray(new File[listsfiles.size()]);

		for (File lFile : filesArray) {
			dir.put(lFile.getAbsolutePath(), new Long(lFile.lastModified()));
		}
	}

	@Override
	public final void run() {
		HashSet<String> checkedFiles = new HashSet<String>();

		Map<Action, List<String>> pFileModMap = new HashMap<SpikeDirectoryWatcher.Action, List<String>>();
		pFileModMap.put(Action.ADD, new ArrayList<String>());
		pFileModMap.put(Action.DELETE, new ArrayList<String>());
		pFileModMap.put(Action.MODIFY, new ArrayList<String>());

		List<FileAction> fileActionList = new ArrayList<SpikeDirectoryWatcher.FileAction>();
		// Scan the files and check for modification/addition
		for (File lFile : filesArray) {

			String lFilePathName = lFile.getAbsolutePath();
			Long current = dir.get(lFilePathName);
			if (current == null) {
				// new file
				dir.put(lFilePathName, new Long(lFile.lastModified()));
				fileActionList.add(new FileAction(Action.ADD, lFilePathName));
				break;
			} else if (current.longValue() != lFile.lastModified()) {
				// modified file
				dir.put(lFilePathName, new Long(lFile.lastModified()));
				fileActionList.add(new FileAction(Action.MODIFY, lFilePathName));
				break;
			}
			checkedFiles.add(lFilePathName);
		}

		if (!fileActionList.isEmpty()) {
			if (fileActionList.size() == 1) {
				onChange(fileActionList.get(0).getFileName(), fileActionList.get(0).getAction()
						.name());
			} else {
				onChanges();
			}
		}

		// TODO handle file delete
		// HashSet<String> lAllFiles = new HashSet<String>(dir.keySet());
		// lAllFiles.removeAll(checkedFiles);
		// // If All is not null, this means a files has been deleted
		// Iterator<String> it = lAllFiles.iterator();
		// while (it.hasNext()) {
		// String lDeletedPathName = it.next();
		// dir.remove(lDeletedPathName);
		// onChange(lDeletedPathName, "delete");
		// break;
		// }

	}

	protected void onChanges() {
		System.out.println("I don't know how to handle several changes at once...yet");
	}

	protected void onChange(String pFilePathName, String pAction) {
		log.info("A File change has been detected\n " + "File Path : " + pFilePathName
				+ " Action: " + pAction);

		try {
			// C'est un fichier li� au process. Il faut le relance
			if (pFilePathName.replace(spike.getSourcePath() + File.separator, "").startsWith("_")) {
				System.out.println("Relaunching spike process...please wait");
				spike.runProcess();
			}
			// Sinon (eg. fichier d'affichage: css, js)
			else {
				spike.copySource();
			}
		} catch (IOException e) {
			log.severe(e.getMessage());
		} catch (TemplateException e) {
			log.severe(e.getMessage());
		}
		System.out.println("Hit any key yo stop...");
	}

	public boolean accept(File pDir, String pName) {
		// Si le dossier source et output sont les m�mes: on ne scanne que les
		// dossiers source
		if (spike.isSameInputOutPut()) {
			if (pName.startsWith("_")) {
				return true;
			}
			if (pDir.getName().startsWith("_")) {
				return true;
			}
			return false;
		} else {
			// On ne scanne jamais le fichier output qui risque d'�tre modifi�
			// souvent
			if (pDir.getAbsolutePath().equals(spike.getOutput())) {
				return false;
			}
			return !pName.startsWith(".") ? true : false;
		}
	}

}
