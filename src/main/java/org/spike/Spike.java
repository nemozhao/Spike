package org.spike;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.spike.model.Feed;
import org.spike.model.FeedItem;
import org.spike.model.Navigation;
import org.spike.model.Page;
import org.spike.model.Paginator;
import org.spike.model.Post;
import org.spike.model.RSSFeedWriter;
import org.spike.model.Site;
import org.spike.model.SpikeTools;
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
public class Spike {

	private static final String HTML_EXT = "html";
	private static final String MD1_EXT = "md";
	private static final String MD2_EXT = "markdown";
	private static final int POST_PER_PAGE = 10;
	private static final String SRC_TEMPLATE_FOLDER = "_layouts";
	private static final String POSTS_FOLDER = "posts";
	private static final String SRC_POSTS_FOLDER = "_" + POSTS_FOLDER;

	private static String output;
	private static String sourcePath;

	private static Logger log = Logger.getLogger(Spike.class.getName());

	private static boolean deleteOldOutput = true;

	private static Pattern delimiterPtrn = Pattern.compile("\\s*[-]{3}\\s*\\n");

	private static MarkdownProcessor mdProcessor = new MarkdownProcessor();
	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	private static Feed rssFeeder = initFeed();
	private Configuration templateConfig;
	private Map<String, Post> cachedPosts;;

	public Spike(String pSource, String pOutput) {
		output = pOutput;
		sourcePath = pSource;
	}

	public void runProcess() throws IOException, TemplateException {
		runProcess(true);
	}

	public void runProcess(boolean pDeleteOutput) throws IOException, TemplateException {
		// Si le repertoire source existe déja, on le supprime
		if (deleteOldOutput && pDeleteOutput) {
			FileUtils.deleteFolder(output);
		}

		initTemplateConfig();
		Template lTemplateBase = templateConfig.getTemplate("index.ftl");
		Template lTemplatePost = templateConfig.getTemplate("post.ftl");
		Template lTemplateArchive = null;
		try {
			lTemplateArchive = templateConfig.getTemplate("archive.ftl");
		} catch (IOException e) {
			// Do nothing
		}
		Template lTemplateCategory = null;
		try {
			lTemplateCategory = templateConfig.getTemplate("category.ftl");
		} catch (IOException e) {
			// Do nothing
		}

		File folder = new File(sourcePath + File.separator + SRC_POSTS_FOLDER);
		FilenameFilter lPostsFilter = new FilenameFilter() {

			public boolean accept(File dir, String name) {
				return name.matches("^.*((\\d\\d\\d\\d)-(\\d\\d)-(\\d\\d))-.+\\..+$");
			}
		};
		File[] listOfFiles = folder.listFiles(lPostsFilter);

		// Order by date descending
		Arrays.sort(listOfFiles, new Comparator<File>() {

			public int compare(File o1, File o2) {
				return -o1.getName().compareTo(o2.getName());
			}

		});

		cachedPosts = new LinkedHashMap<String, Post>(listOfFiles.length);
		for (int i = 0; i < listOfFiles.length; i++) {
			File lFile = listOfFiles[i];
			try {
				Post lPost = getFromCache(lFile);

				String lPrevious = null;
				String lNext = null;
				// Si premier index
				if (i == 0) {
					lPrevious = listOfFiles[i + 1].getName();
					cachedPosts.put(lPrevious, getFromCache(listOfFiles[i + 1]));
					// dernier index
				} else if (i == listOfFiles.length - 1) {
					lNext = listOfFiles[i - 1].getName();
					cachedPosts.put(lNext, getFromCache(listOfFiles[i - 1]));
				} else {
					lPrevious = listOfFiles[i + 1].getName();
					cachedPosts.put(lPrevious, getFromCache(listOfFiles[i + 1]));
					lNext = listOfFiles[i - 1].getName();
					cachedPosts.put(lNext, getFromCache(listOfFiles[i - 1]));
				}
				lPost.setPrevious(cachedPosts.get(lPrevious));
				lPost.setNext(cachedPosts.get(lNext));

				addFeedItem(lPost);
				Navigation.add(lPost);

				// Building post index.html
				buildPost(lPost, lTemplatePost);

			} catch (FileNotFoundException e) {
				System.out.println("Error reading File" + lFile.getAbsolutePath() + " :"
						+ e.getMessage());
				log.log(Level.WARNING, "Error reading File", e);
			}
		}

		// Building pagination
		processPagination(new ArrayList<Post>(cachedPosts.values()), lTemplateBase);

		// Create Rss Feed
		buildRssFeed(rssFeeder);

		// Building Index.html
		buildIndex(new ArrayList<Post>(cachedPosts.values()), lTemplateBase);

		// Building archive page
		if (lTemplateArchive != null) {
			processArchiveTemplate(lTemplateArchive);
		}
		if (lTemplateCategory != null) {
			processCategoryTemplate(lTemplateCategory);
		}

		log.info("Process OK. Handled " + listOfFiles.length + " Posts.");
	}

	private void processCategoryTemplate(Template pTemplateCategory) throws TemplateException,
			IOException {
		File categoriesFolder = new File(output + File.separator + "categories");
		categoriesFolder.mkdirs();
		SimpleHash categoriesHash = new SimpleHash();
		categoriesHash.put("allCategories", Navigation.getCategoriesMap().keySet());
		categoriesHash.put("allTags", Navigation.getTagsMap().keySet());
		categoriesHash.put("categories", Navigation.getCategoriesMap());
		OutputStreamWriter lArchiveWriter = new OutputStreamWriter(new FileOutputStream(
				categoriesFolder.getAbsolutePath() + File.separator + "index.html"), "UTF-8");
		pTemplateCategory.process(categoriesHash, lArchiveWriter);
	}

	/**
	 * Retourne le post à partir du nom du fichier. On le met en cache si
	 * nécéssaire afin de pas recréer un post existant
	 * 
	 * @param pFile
	 * @return
	 * @throws FileNotFoundException
	 */
	private Post getFromCache(File pFile) throws FileNotFoundException {
		String lFileName = pFile.getName();
		if (cachedPosts.containsKey(lFileName)) {
			return cachedPosts.get(lFileName);
		} else {
			String lSplit = lFileName.split("^.*((\\d\\d\\d\\d)-(\\d\\d)-(\\d\\d))-")[1];
			String[] lFileInfo = lSplit.split("\\.");
			String lFilePath = lFileInfo[0];
			String lExtension = lFileInfo[1];

			String lDate = lFileName.substring(0, 10);
			Calendar lCalendar = (Calendar) simpleDateFormat.getCalendar().clone();
			try {
				lCalendar.setTime(simpleDateFormat.parse(lDate));
			} catch (ParseException e) {
				// Should Never Happen
			}

			Post lPost = readFile(pFile, lExtension);
			String lPostUrl = buildPostUrl(lFilePath, lCalendar);
			lPost.setUrl(lPostUrl.replace(" ", "_"));
			lPost.setPublishedDate(lCalendar.getTime());

			cachedPosts.put(lFileName, lPost);
			return lPost;
		}

	}

	private void buildPost(Post pPost, Template lTemplatePost) throws TemplateException {
		String lPostDirectoryUrl = output + pPost.getUrl() + "index." + HTML_EXT;
		File lPostFile = new File(lPostDirectoryUrl);
		try {
			SimpleHash postHash = new SimpleHash();
			postHash.put(POSTS_FOLDER, pPost);
			postHash.put("allCategories", Navigation.getCategoriesMap().keySet());
			postHash.put("allTags", Navigation.getTagsMap().keySet());
			log.fine("Creating file " + lPostDirectoryUrl);
			if (!lPostFile.getParentFile().exists()) {
				lPostFile.getParentFile().mkdirs();
			}
			lPostFile.createNewFile();
			OutputStreamWriter lPostFileW = new OutputStreamWriter(new FileOutputStream(lPostFile),
					"UTF-8");
			lTemplatePost.process(postHash, lPostFileW);
		} catch (IOException e) {
			System.out.println("Error creating File" + lPostFile + " :" + e.getMessage());
			log.log(Level.WARNING, "Error creating File", e);
		}
	}

	private Site buildIndex(List<Post> lPosts, Template pTemplate) throws TemplateException,
			IOException {
		Site lSite = new Site();
		lSite.getPosts().addAll(lPosts);
		SimpleHash root = new SimpleHash();
		root.put("paginator", SpikeTools.getPaginator(null, File.separator + "Page1"));
		root.put("allCategories", Navigation.getCategoriesMap().keySet());
		root.put("allTags", Navigation.getTagsMap().keySet());
		root.put("site", lSite);
		OutputStreamWriter lSiteWriter = new OutputStreamWriter(new FileOutputStream(output
				+ File.separator + "index.html"), "UTF-8");
		pTemplate.process(root, lSiteWriter);
		return lSite;
	}

	private void buildRssFeed(Feed lRssFeeder) {
		File XmlFolder = new File(output + File.separator + "feed");
		XmlFolder.mkdirs();
		// Now write the file
		RSSFeedWriter writer = new RSSFeedWriter(lRssFeeder, output + File.separator + "feed"
				+ File.separator + "rss2.xml");
		try {
			writer.write();
		} catch (Exception e) {
			// should not happen
			System.out.println("Error writing RSS Feed : " + e.getMessage());
		}
	}

	private void processPagination(List<Post> lPosts, Template lTemplateBase)
			throws TemplateException, IOException {
		int lNumberofPage = lPosts.size() / POST_PER_PAGE;
		for (int i = 0; i < lNumberofPage; i++) {
			if (i == 0) {
				continue;
			}
			List<Post> subList = lPosts.subList(i * POST_PER_PAGE, i * POST_PER_PAGE
					+ POST_PER_PAGE);

			Page lPage = new Page();
			lPage.setUrl(File.separator + "Page" + i);
			lPage.setPosts(subList);
			// premier index
			Paginator lPaginator = null;
			if (i == 0) {
				lPaginator = SpikeTools.getPaginator(null, File.separator + "Page" + (i + 1));
			} else {
				if (i == lNumberofPage - 1) {
					lPaginator = SpikeTools.getPaginator(File.separator + "Page" + (i - 1), null);
				} else {
					if (i == 1) {
						lPaginator = SpikeTools.getPaginator(File.separator + "index.html",
								File.separator + "Page" + (i + 1));
					} else {
						lPaginator = SpikeTools.getPaginator(File.separator + "Page" + (i - 1),
								File.separator + "Page" + (i + 1));
					}
				}

				String lPageDirectory = output + File.separator + "Page" + (i) + File.separator;
				File lPostDirectory = new File(lPageDirectory);
				lPostDirectory.mkdirs();

				Site lSubPosts = new Site();
				lSubPosts.getPosts().addAll(subList);
				SimpleHash root = new SimpleHash();
				root.put("site", lSubPosts);
				root.put("paginator", lPaginator);
				root.put("allCategories", Navigation.getCategoriesMap().keySet());
				root.put("allTags", Navigation.getTagsMap().keySet());
				OutputStreamWriter lPageW = new OutputStreamWriter(new FileOutputStream(
						lPageDirectory + "index.html"), "UTF-8");
				lTemplateBase.process(root, lPageW);
			}
		}
	}

	private void processArchiveTemplate(Template lTemplateArchive) throws TemplateException,
			IOException {
		File archiveFolder = new File(output + File.separator + "archive");
		archiveFolder.mkdirs();
		SimpleHash archiveHash = new SimpleHash();
		archiveHash.put("allCategories", Navigation.getCategoriesMap().keySet());
		archiveHash.put("allTags", Navigation.getTagsMap().keySet());
		archiveHash.put("archive", Navigation.getArchiveMap());
		OutputStreamWriter lArchiveWriter = new OutputStreamWriter(new FileOutputStream(
				archiveFolder.getAbsolutePath() + File.separator + "index.html"), "UTF-8");
		lTemplateArchive.process(archiveHash, lArchiveWriter);
	}

	private void addFeedItem(Post post) {
		FeedItem feed = new FeedItem();
		feed.setTitle(post.getTitle());
		feed.setDescription(post.getContent() + post.getTags());
		feed.setGuid(post.getUrl());
		feed.setLink(post.getUrl());
		feed.setPubDate(post.getPublishedDate());
		rssFeeder.getMessages().add(feed);
	}

	private String buildPostUrl(String lFilePath, Calendar lCalendar) {
		StringBuilder lStringBuilder = new StringBuilder();
		lStringBuilder.append(File.separator);
		lStringBuilder.append(lCalendar.get(Calendar.YEAR));
		lStringBuilder.append(File.separator);
		lStringBuilder.append(lCalendar.get(Calendar.MONTH) + 1);
		lStringBuilder.append(File.separator);
		lStringBuilder.append(lCalendar.get(Calendar.DAY_OF_MONTH));
		lStringBuilder.append(File.separator);
		lStringBuilder.append(lFilePath);
		lStringBuilder.append(File.separator);
		return lStringBuilder.toString();
	}

	private void initTemplateConfig() throws IOException {
		templateConfig = new Configuration();
		templateConfig.setDirectoryForTemplateLoading(new File(sourcePath + File.separator
				+ SRC_TEMPLATE_FOLDER));
		// Specify how templates will see the data-model.
		templateConfig.setObjectWrapper(new BeansWrapper());
	}

	private static Feed initFeed() {
		String copyright = "Copyright hold by Mikomatic";
		String title = "Mike's blog";
		String description = "Blogging about software, movies, life, and every thing else in between";
		String language = "fr";
		String link = "http://www.mikomatic.com";
		Date creationDate = new Date();
		SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		String pubdate = date_format.format(creationDate);
		return new Feed(title, link, description, language, copyright, pubdate);
	}

	@SuppressWarnings("unchecked")
	private final static void readYamlHeader(Post pPost, final String pHeader) {
		Map<String, Object> lLoad = (Map<String, Object>) new Yaml().load(pHeader);

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
		Scanner lScanner = new Scanner(pFile, "UTF-8");
		lScanner.useDelimiter(delimiterPtrn);
		int i = 1;
		String lContent = "";
		while (lScanner.hasNext()) {
			String lNext = lScanner.next().trim();
			if (i == 1) {
				log.fine("reading yaml header of file: " + pFile.getName());
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

	public final void initServer() {
		try {
			new NanoHTTPD(1337, new File(output));
		} catch (IOException ioe) {
			System.err.println("Couldn't start server:\n" + ioe);
			System.exit(-1);
		}
		log.info("Listening on port " + 1337 + ".\n");
	}

	public String getOutput() {
		return output;
	}

	public String getSourcePath() {
		return sourcePath;
	}

}
