package org.spike;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.spike.log.SpikeLoggerFormatter;
import org.spike.model.Feed;
import org.spike.model.FeedMessage;
import org.spike.model.Page;
import org.spike.model.Paginator;
import org.spike.model.Post;
import org.spike.model.RSSFeedWriter;
import org.spike.model.Site;
import org.spike.model.SpikeTools;
import org.spike.utils.DirectoryWatcher;
import org.spike.utils.FileUtils;
import org.yaml.snakeyaml.Yaml;

import com.petebevin.markdown.MarkdownProcessor;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * @author mikomatic
 */
public class Spike extends DirectoryWatcher {

	private static final String HTML_EXT = "html";
	private static final String MD1_EXT = "md";
	private static final String MD2_EXT = "markdown";
	private static final int POST_PER_PAGE = 10;
	private static final String SRC_TEMPLATE_FOLDER = "_layouts";
	private static final String POSTS_FOLDER = "posts";
	private static final String SRC_POSTS_FOLDER = "_" + POSTS_FOLDER;

	private static String output;

	private static Logger log = Logger.getLogger(Spike.class.getName());

	private static List<Post> posts = new ArrayList<Post>();

	private static Map<String, String> arguments;

	private static Level logLevel;

	private static boolean deleteOldOutput = true;

	private static Pattern delimiterPtrn = Pattern.compile("\\s*[-]{3}\\s*\\n");

	private static MarkdownProcessor mdProcessor = new MarkdownProcessor();
	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd");;

	public Spike(String pSource, String pOutput) {
		super(pSource);
		output = pOutput;
	}

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

		Logger Logger = java.util.logging.Logger.global;

		if (arguments.get("info") != null) {
			logLevel = Level.INFO;
		} else if (arguments.get("trace") != null) {
			logLevel = Level.FINE;
		} else if (arguments.get("debug") != null) {
			// logLevel = Level.deLVL_DEBUGLOG;
		}

		String lBatchSize = arguments.get("batchSize");

		// sourcePath = arguments.get( "source" );
		// if ( sourcePath == null ) {
		// usage();
		// }

	}

	public void runProcess() throws IOException, TemplateException {
		// Si le repertoire source existe déjà, on le supprime TODO: à mettre en
		// option
		if (deleteOldOutput) {
			FileUtils.deleteFolder(output);
		}

		long start = System.currentTimeMillis();

		Configuration cfg = new Configuration();
		// Specify the data source where the template files come from.
		// Here I set a file directory for it:
		cfg.setDirectoryForTemplateLoading(new File(sourcePath + File.separator
				+ SRC_TEMPLATE_FOLDER));
		// Specify how templates will see the data-model. This is an advanced
		// topic...
		// but just use this:
		cfg.setObjectWrapper(new BeansWrapper());
		Template lTemplateBase = null;
		Template lTemplatePost = null;
		try {
			lTemplateBase = cfg.getTemplate("index.ftl");
			lTemplatePost = cfg.getTemplate("post.ftl");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		File folder = new File(sourcePath + File.separator + SRC_POSTS_FOLDER);
		File[] listOfFiles = folder.listFiles();
		File lPostsFolder = new File(sourcePath + File.separator + POSTS_FOLDER);
		if (!lPostsFolder.exists()) {
			lPostsFolder.mkdir();
		}
		Site lSite = new Site();

		for (File lFile : listOfFiles) {
			try {
				String lFileName = lFile.getName();
				if (!lFileName
						.matches("^.*((\\d\\d\\d\\d)-(\\d\\d)-(\\d\\d))-.+\\..+$")) {
					continue;
				}
				String lSplit = lFileName
						.split("^.*((\\d\\d\\d\\d)-(\\d\\d)-(\\d\\d))-")[1];
				String[] lFileInfo = lSplit.split("\\.");
				String lFilePath = lFileInfo[0];
				String lExtension = lFileInfo[1];
				//
				String lDate = lFileName.substring(0, 10);
				Calendar lCalendar = (Calendar) simpleDateFormat.getCalendar()
						.clone();
				lCalendar.setTime(simpleDateFormat.parse(lDate));

				Post lPost = readFile(lFile, lExtension);
				String lPostUrl = File.separator + lCalendar.get(Calendar.YEAR)
						+ File.separator + lCalendar.get(Calendar.MONTH)
						+ File.separator + lCalendar.get(Calendar.DAY_OF_MONTH)
						+ File.separator + lFilePath + File.separator;

				File lPostDirectory = new File(output + lPostUrl);
				lPostDirectory.mkdirs();
				lPost.setUrl(lPostUrl);
				lPost.setPublishedDate(lCalendar.getTime());

				// SimpleHash postHash = new SimpleHash();
				// postHash.put(POSTS_FOLDER, lPost);
				// File lPostFile = new File(lPostDirectoryUrl);
				// log.info("Creating file " + lPostDirectoryUrl);
				// lPostFile.createNewFile();
				// FileWriter lPostFileW = new FileWriter(lPostFile);
				// lTemplatePost.process(postHash, lPostFileW);
				posts.add(lPost);

			} catch (FileNotFoundException e) {
				log.info("Creating file " + lFile.getAbsolutePath() + " :"
						+ e.getMessage());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Collections.sort(posts);

		// Build also xml feed
		// Create the rss feed
		String copyright = "Copyright hold by Mikomatic";
		String title = "Mike's blog";
		String description = "Blogging about software, movies, life, and every thing else in between";
		String language = "fr";
		String link = "http://www.mikomatic.com";
		Date creationDate = new Date();
		SimpleDateFormat date_format = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss");
		String pubdate = date_format.format(creationDate);
		Feed rssFeeder = new Feed(title, link, description, language,
				copyright, pubdate);

		int lSize = posts.size();
		for (int i = 0; i < lSize; i++) {
			Post post = posts.get(i);

			// Now add one example entry
			FeedMessage feed = new FeedMessage();
			feed.setTitle(posts.get(i).getTitle());
			feed.setDescription(posts.get(i).getContent());
			feed.setGuid(posts.get(i).getUrl());
			feed.setLink(posts.get(i).getUrl());
			feed.setPubDate(post.getPublishedDate());
			rssFeeder.getMessages().add(feed);

			// Si premier index
			if (i == 0) {
				post.setNext(posts.get(i + 1));
				// dernier index
			} else if (i == lSize - 1) {
				post.setPrevious(posts.get(i - 1));
			} else {
				post.setNext(posts.get(i + 1));
				post.setPrevious(posts.get(i - 1));
			}

			SimpleHash postHash = new SimpleHash();
			postHash.put(POSTS_FOLDER, post);
			String lPostDirectoryUrl = output + post.getUrl() + "index."
					+ HTML_EXT;
			File lPostFile = new File(lPostDirectoryUrl);
			log.info("Creating file " + lPostDirectoryUrl);
			lPostFile.createNewFile();
			FileWriter lPostFileW = new FileWriter(lPostFile);
			lTemplatePost.process(postHash, lPostFileW);
		}

		int lNumberofPage = lSize / POST_PER_PAGE;
		for (int i = 0; i < lNumberofPage; i++) {
			if (i == 0) {
				continue;
			}
			List<Post> subList = posts.subList(i * POST_PER_PAGE, i
					* POST_PER_PAGE + POST_PER_PAGE);

			Page lPage = new Page();
			lPage.setUrl(File.separator + "Page" + i);
			lPage.setPosts(subList);
			// premier index
			Paginator lPaginator = null;
			if (i == 0) {
				lPaginator = SpikeTools.getPaginator(null, File.separator
						+ "Page" + (i + 1));
			} else {
				if (i == lNumberofPage - 1) {
					lPaginator = SpikeTools.getPaginator(File.separator
							+ "Page" + (i - 1), null);
				} else {
					if (i == 1) {
						lPaginator = SpikeTools.getPaginator(File.separator
								+ "index.html", File.separator + "Page"
								+ (i + 1));
					} else {
						lPaginator = SpikeTools.getPaginator(File.separator
								+ "Page" + (i - 1), File.separator + "Page"
								+ (i + 1));
					}
				}

				String lPageDirectory = output + File.separator + "Page" + (i)
						+ File.separator;
				File lPostDirectory = new File(lPageDirectory);
				lPostDirectory.mkdirs();

				Site lSubPosts = new Site();
				lSubPosts.getPosts().addAll(subList);
				SimpleHash root = new SimpleHash();
				root.put("site", lSubPosts);
				root.put("paginator", lPaginator);
				FileWriter ltest = new FileWriter(lPageDirectory + "index.html");
				lTemplateBase.process(root, ltest);
			}
		}

		File XmlFolder = new File(output + File.separator + "feed");
		XmlFolder.mkdirs();
		// Now write the file
		RSSFeedWriter writer = new RSSFeedWriter(rssFeeder, output
				+ File.separator + "feed" + File.separator + "rss2.xml");
		try {
			writer.write();
		} catch (Exception e) {
			e.printStackTrace();
		}

		lSite.getPosts().addAll(posts);
		SimpleHash root = new SimpleHash();
		root.put("paginator",
				SpikeTools.getPaginator(null, File.separator + "Page1"));
		root.put("site", lSite);
		FileWriter ltest = new FileWriter(output + File.separator
				+ "index.html");
		lTemplateBase.process(root, ltest);
		long end = System.currentTimeMillis();
		log.info("Spike process - Processed site in was " + (end - start)
				+ " ms. " + lSite.getPosts().size() + " Posts.");

	}

	private static void readConfigFile() {
		File file = new File("./_config.yml");
		if (!file.exists()) {
			System.out.println("My config file not exists!");
			log.severe("Config file NOT OK");
		} else {
			System.out.println("My config file exists!");
			log.info("Config file OK");
		}
	}

	private static void initLogger() {

		log.setLevel(Level.INFO);
		try {
			FileHandler logFile = new FileHandler("spike.log");
			logFile.setFormatter(new SpikeLoggerFormatter());
			log.addHandler(logFile);
			// log.addHandler()
		} catch (Exception e) {
			System.out
					.println("Error setting up logger file " + e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private final static void readYamlHeader(Post pPost, final String pHeader) {
		Map<String, Object> lLoad = (Map<String, Object>) new Yaml()
				.load(pHeader);

		pPost.setTitle((String) lLoad.get(SpikeCst.TITLE));
		pPost.setCategory((String) lLoad.get(SpikeCst.CATEGORY));
		Object lTags = lLoad.get(SpikeCst.TAGS);
		if (lTags != null) {
			pPost.getTags().addAll((List<String>) lTags);
		}
		pPost.setSource((String) lLoad.get(SpikeCst.SRC));
	}

	private static Post readFile(final File pFile, final String pExtension)
			throws FileNotFoundException {
		Post lPost = new Post();
		Scanner lScanner = new Scanner(pFile);
		lScanner.useDelimiter(delimiterPtrn);
		int i = 1;
		String lContent = "";
		while (lScanner.hasNext()) {
			String lNext = lScanner.next().trim();
			if (i == 1) {
				System.out.println("reading yaml header of file: "
						+ pFile.getName());
				readYamlHeader(lPost, lNext);
				i += 1;
				continue;
			}
			lContent += lNext;
		}
		lScanner.close();

		if (MD1_EXT.equals(pExtension) || MD2_EXT.equals(pExtension)) {
			lPost.setContent(mdProcessor.markdown(lContent));
		} else {
			lPost.setContent(lContent);
		}

		return lPost;
	}

	/** Specify the correct parameters to use the class properly */
	public static final void usage() {
		System.out
				.println("usage: Batch -name BatchName -config ContextPath -csvPath ExportedCsvPath [-verbose] [-debug] [-batchSize 500]");
		System.out
				.println("(ContextPath should be a repositorty which contains econtext.xml, erabledata.xml ...)");
		System.out
				.println("Batch -name PAR01PEEE -config /home/dataeee/etc/ -csvPath /home/dataEEE/home");
		System.exit(1);
	}

	public final void initServer() {
		try {
			new NanoHTTPD(1666, new File(output));
		} catch (IOException ioe) {
			System.err.println("Couldn't start server:\n" + ioe);
			System.exit(-1);
		}
		log.info("Listening on port " + 1666 + ". Hit Enter to stop.\n");
		try {
			System.in.read();
		} catch (Throwable t) {
			System.exit(1);
		}
		;
	}

	@Override
	protected void onChange(String pFilePathName, String pAction) {
		log.info("A File change has been detected \n " + "File Path : "
				+ pFilePathName + " Action: " + pAction);
		try {
			runProcess();
		} catch (IOException e) {
			log.severe(e.getMessage());
		} catch (TemplateException e) {
			log.severe(e.getMessage());
		}
	}

}
