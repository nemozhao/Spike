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
public class SpikeDirecotryWatcher extends DirectoryWatcher {

	private static Logger log = Logger.getLogger(SpikeDirecotryWatcher.class
			.getName());

	private static Spike spike;

	public SpikeDirecotryWatcher(Spike pSpike, String path) {
		super(path);
		spike = pSpike;
	}

	@Override
	protected void onChange(String pFilePathName, String pAction) {
		log.info("A File change has been detected \n " + "File Path : "
				+ pFilePathName + " Action: " + pAction);
		try {
			spike.runProcess();

			FilenameFilter lFilenameFilter = new FilenameFilter() {

				public boolean accept(File dir, String name) {
					return !name.startsWith("_")
							&& !spike.getOutputName().equals(name);
				}
			};

			System.out.println("Copying ressources files & directories...");
			FileUtils.copyFolder(spike.getSourcePath(), spike.getOutput(),
					lFilenameFilter);

		} catch (IOException e) {
			log.severe(e.getMessage());
		} catch (TemplateException e) {
			log.severe(e.getMessage());
		}
	}

}
