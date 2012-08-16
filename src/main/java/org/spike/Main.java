/**
 * 
 */
package org.spike;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Properties;

import org.spike.utils.FileUtils;

import freemarker.template.TemplateException;

/**
 * @author mikomatic
 * 
 */
public class Main {

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

		String loutput = outputFolder + File.separator + output;
		Spike lSpike = new Spike(sourceFolder, loutput);

		try {
			lSpike.runProcess();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TemplateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		FilenameFilter lFilenameFilter = new FilenameFilter() {

			public boolean accept(File dir, String name) {
				return !name.startsWith("_") && !output.equals(name);
			}
		};
		try {
			FileUtils.copyFolder(sourceFolder, loutput, lFilenameFilter);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		lSpike.initServer();
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
