/**
 *
 */
package org.spike;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.logging.Logger;

import org.spike.utils.DirectoryWatcher;
import org.spike.utils.FileUtils;

import freemarker.template.TemplateException;

/**
 * @author mikomatic
 *
 */
public class SpikeDirectoryWatcher extends DirectoryWatcher {

	private static Logger log = Logger.getLogger(SpikeDirectoryWatcher.class.getName());

	private static Spike spike;

	public SpikeDirectoryWatcher(Spike pSpike) {
		super(pSpike.getSourcePath());
		spike = pSpike;
	}

	@Override
	protected void onChange(String pFilePathName, String pAction) {
		log.info("A File change has been detected\n " + "File Path : " + pFilePathName
				+ " Action: " + pAction);

		try {
			File lFile = new File(spike.getSourcePath());
			// C'est un fichier li� au process. Il faut le relance
			if (pFilePathName.replace(lFile.getAbsolutePath() + File.separator, "").startsWith("_")) {
				System.out.println("Relaunching spike process...please wait");
				spike.runProcess();
			}
			// Sinon (eg. fichier d'affichage: css, js)
			else {
				FilenameFilter lFilenameFilter = new FilenameFilter() {

					public boolean accept(File dir, String name) {
						return !name.startsWith("_") && !name.startsWith(".");
					}
				};

				System.out.println("Copying ressources files & directories...please wait");
				FileUtils.copyFolder(spike.getSourcePath(), spike.getOutput(), lFilenameFilter);
			}
		} catch (IOException e) {
			log.severe(e.getMessage());
		} catch (TemplateException e) {
			log.severe(e.getMessage());
		}
		System.out.println("Hit any key yo stop...");
	}

}
