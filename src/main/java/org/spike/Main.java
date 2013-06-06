/**
 *
 */
package org.spike;

import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Timer;

import org.apache.log4j.Logger;

import freemarker.template.TemplateException;

/**
 * @author mikomatic
 *
 */
public class Main {
  private static final Logger LOG = Logger.getLogger(Main.class.getName());
  private static String sourceFolder;
  private static String outputFolder;
  private static Boolean keepAlive;
  private static Boolean server;
  private static boolean outputDelete;
  private static HashMap<String, String> arguments;

  public static final void decodeArgs(String[] pArgs) {
    arguments = new HashMap<String, String>();
    // Scan the arguments
    for (int i = 0, iMax = pArgs.length; i < iMax; i++) {
      if (pArgs[i].startsWith("-")) {
        String lKey = pArgs[i].substring(1);
        String lValue = "";
        if (i + 1 < iMax && !pArgs[i + 1].startsWith("-")) {
          lValue = pArgs[i + 1];
          i++;
        }
        arguments.put(lKey, lValue);
      }
    }
    sourceFolder = arguments.get("source");
    outputFolder = arguments.get("output");
    keepAlive = arguments.get("keepAlive") != null ? true : false;
    server = arguments.get("server") != null ? true : false;
    outputDelete = arguments.get("outputDelete") != null ? true : false;
    if (arguments.get("help") != null) {
      usage();
    }
  }

  /** Specify the correct parameters to use the class properly */
  public static final void usage() {
    System.out.println("Spike Parameters: -source -output -keepAlive -server -outputDelete");
    System.out.println("-source : Path to Source containing folders _layout and _posts (eg. C:/My/Path )");
    System.out.println("Default is set to currentPath/_raw \n");
    System.out
        .println("-output : Path to output source  (eg. C:/My/Path/Output ) \n Tous les fichiers contenus dans -source seront copiés également");
    System.out.println("Default is set to currentPath/_raw \n");
    System.out
        .println("-outputDelete : outputfolder is delete before running process. use with caution if already outputfolder exists");
    System.out.println("Default is set to false \n");
    System.out.println("-keepAlive : Keep processing alive. Will relaunch process if a file modification is detected");
    System.out.println("Default is false\n");
    System.out.println("-server : Launch local server for local testing. Port 1337 :) ");
    System.out.println("Test the result @ localhost:1337");
    System.out.println("Default is false\n");
    System.exit(0);
  }

  /**
   * @param args
   */
  public static void main(final String[] args) {
    System.out.println("Spike static blog generator  Copyright (C) 2012 by Miguel Ortega \n");
    try {
      long start = System.currentTimeMillis();
      decodeArgs(args);
      readDefaults();
      printParameters();
      Spike lSpike = new Spike(sourceFolder, outputFolder, outputDelete, true);
      System.out.println("Processing Posts and Layouts ...");
      lSpike.runProcess();
      lSpike.copySource();
      System.out.println("Spike - success in " + (System.currentTimeMillis() - start) + " ms");
      if (server) {
        lSpike.initServer();
      }
      if (keepAlive) {
        Timer t = new Timer();
        t.schedule(new SpikeDirectoryWatcher(lSpike), 0, 3 * 1000);
      }
      if (server || keepAlive) {
        System.out.println("Hit Enter to stop.");
        System.in.read();
      }
      System.exit(0);
    } catch (IOException e) {
      handleException(e);
    } catch (TemplateException e) {
      handleException(e);
    } catch (Throwable th) {
      LOG.error("Unexpected Error", th);
      System.exit(1);
    }
  }

  private static void handleException(Exception pEx) {
    LOG.error("Oups, something went wrong", pEx);
    System.exit(1);
  }

  private static void readDefaults() throws IOException {
    Properties prop = new Properties();
    // Load the property file
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    prop.load(cl.getResourceAsStream("config.properties"));
    // Get values
    if (isNulOrBlank(sourceFolder)) {
      sourceFolder = prop.getProperty("source");
    } else {
      if (isNulOrBlank(outputFolder)) {
        outputFolder = sourceFolder;
      }
    }
    if (isNulOrBlank(outputFolder)) {
      outputFolder = prop.getProperty("output");
    }
  }

  private static void printParameters() {
    System.out.println("Source folder --- " + sourceFolder);
    System.out.println("Output folder --- " + outputFolder);
    System.out.println("KeepAlive --- " + keepAlive);
    System.out.println("Server --- " + server);
    System.out.println("output Delete --- " + outputDelete);
  }

  private static boolean isNulOrBlank(String pString) {
    return pString == null || pString.trim().length() == 0;
  }
}
