/**
 *
 */
package org.spike;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Properties;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.spike.utils.FileUtils;

import freemarker.template.TemplateException;

/**
 * @author mikomatic
 * 
 */
public class Main {

	private static Logger log = Logger.getLogger(Main.class.getName());

	private static String sourceFolder;
	private static final String output = "site";
	private static String outputFolder;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args == null || args.length == 0) {
			readDefaults();
		}
		long start = System.currentTimeMillis();
		Spike lSpike = new Spike(sourceFolder, outputFolder, output);

		try {
			lSpike.runProcess();

			System.out.println("Processing Posts and Layouts ...");

			FilenameFilter lFilenameFilter = new FilenameFilter() {

				public boolean accept(File dir, String name) {
					return !name.startsWith("_") && !output.equals(name);
				}
			};

			System.out.println("Copying ressources files & directories...");
			FileUtils.copyFolder(sourceFolder, outputFolder + File.separator
					+ output, lFilenameFilter);
		} catch (IOException e) {
			log.log(Level.SEVERE, "IOException", e);
		} catch (TemplateException e) {
			log.log(Level.SEVERE, "TemplateException", e);
		}

		System.out.println("Spike - success in "
				+ (start - System.currentTimeMillis()) + " ms");
		lSpike.initServer();

		Timer t = new Timer();
		t.schedule(new SpikeDirecotryWatcher(lSpike, sourceFolder), 0, 1 * 1000);

		try {
			System.in.read();
		} catch (Throwable th) {
			System.exit(1);
		}
		;
	}

	private static void readDefaults() {
		Properties prop = new Properties();
		try {
			// Load the property file
			prop.load(new FileInputStream(
					"src/main/resources/config.properties"));
			// Get and print the values
			sourceFolder = prop.getProperty("source");
			outputFolder = prop.getProperty("output");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
