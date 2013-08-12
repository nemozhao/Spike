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
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
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
	private static final String PAGE = "Page";
	private static final String HTML_EXT = "html";
	private static final String MD1_EXT = "md";
	private static final String MD2_EXT = "markdown";
	private static final String FTL_EXT = "ftl";
	private static final int POST_PER_PAGE = 10;
	private static final String archiveTemplateName = "archive" + "." + FTL_EXT;
	private static final String categoriesTemplateName = "category" + "." + FTL_EXT;
	private static final String SRC_TEMPLATE_FOLDER = "_layouts";
	private static final String POSTS = "posts";
	private static final String SRC_POSTS_FOLDER = "_" + POSTS;
	private static final String SRC_PAGES_FOLDER = "_pages";
	private static final String URL_SEPARATOR = "/";
	private static String output;
	private static String sourcePath;
	private static boolean canCopySoure;
	private static FilenameFilter filenameFilter = new FilenameFilter() {
		public boolean accept(File dir, String name) {
			return !name.startsWith("_") && !name.startsWith(".");
		}
	};
	private static Logger LOG = Logger.getLogger(Spike.class.getName());
	private static boolean deleteOldOutput;
	// get the line separator for the current platform
	private static Pattern delimiterPtrn = Pattern.compile("\\s*[-]{3}\\s*\\n");
	private static MarkdownProcessor mdProcessor = new MarkdownProcessor();
	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static Feed rssFeeder = initFeed();
	private Configuration templateConfig;
	private Map<String, Post> cachedPosts;

	public Spike(String pSource, String pOutput, boolean pDeleteOutput, boolean pCanCopySource) {
		output = pOutput;
		sourcePath = pSource;
		deleteOldOutput = pDeleteOutput;
		canCopySoure = pCanCopySource;
		if (isSameInputOutPut()) {
			deleteOldOutput = false;
			canCopySoure = false;
		} else {
			deleteOldOutput = pDeleteOutput;
			canCopySoure = pCanCopySource;
		}
	}

	public void runProcess() throws IOException, TemplateException {
		Navigation.clear();
		// Si le repertoire source existe déja, on le supprime
		if (deleteOldOutput) {
			FileUtils.deleteFolder(output);
		}
		initTemplateConfig();
		Template lTemplateBase = templateConfig.getTemplate("index.ftl");
		File folder = new File(sourcePath + File.separator + SRC_POSTS_FOLDER);
		FilenameFilter postNameFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.matches("^((\\d\\d\\d\\d)-(\\d\\d)-(\\d\\d))-.+\\..+$")
						&& !name.startsWith(".");
			}
		};
		File[] filteredFiles = folder.listFiles(postNameFilter);
		// Order by date descending
		Arrays.sort(filteredFiles, new Comparator<File>() {
			public int compare(File o1, File o2) {
				return -o1.getName().compareTo(o2.getName());
			}
		});
		cachedPosts = new LinkedHashMap<String, Post>(filteredFiles.length);
		for (int i = 0; i < filteredFiles.length; i++) {
			File lFile = filteredFiles[i];
			try {
				Post lPost = getFromCache(lFile);
				String lPrevious = null;
				String lNext = null;
				// Si premier index
				if (i == 0) {
					lPrevious = filteredFiles[i + 1].getName();
					cachedPosts.put(lPrevious, getFromCache(filteredFiles[i + 1]));
					// dernier index
				} else if (i == filteredFiles.length - 1) {
					lNext = filteredFiles[i - 1].getName();
					cachedPosts.put(lNext, getFromCache(filteredFiles[i - 1]));
				} else {
					lPrevious = filteredFiles[i + 1].getName();
					cachedPosts.put(lPrevious, getFromCache(filteredFiles[i + 1]));
					lNext = filteredFiles[i - 1].getName();
					cachedPosts.put(lNext, getFromCache(filteredFiles[i - 1]));
				}
				lPost.setPrevious(cachedPosts.get(lPrevious));
				lPost.setNext(cachedPosts.get(lNext));
				addFeedItem(lPost);
				Navigation.add(lPost);
			} catch (FileNotFoundException e) {
				System.out.println("Error reading File" + lFile.getAbsolutePath() + " :"
						+ e.getMessage());
				LOG.warn("Error reading File", e);
			} catch (InterruptedException e) {
				System.out.println("Error process file " + lFile.getAbsolutePath() + " :"
						+ e.getMessage());
				LOG.warn("Error reading File", e);
			} catch (Throwable e) {
				System.out.println("Error creating File " + lFile.getAbsolutePath() + " :"
						+ e.getMessage());
			}
		}
		// Build post once all posts, tags, categories have been cached
		for (Post post : cachedPosts.values()) {
			buildPost(post);
		}
		// Building pagination
		processPagination(new ArrayList<Post>(cachedPosts.values()), lTemplateBase);
		// Create Rss Feed
		buildRssFeed(rssFeeder);
		// Building Index.html
		buildIndex(new ArrayList<Post>(cachedPosts.values()), lTemplateBase);
		// Building custom pages
		File pagesFolder = new File(sourcePath + File.separator + SRC_PAGES_FOLDER);
		File[] pages = pagesFolder.listFiles();
		for (File page : pages) {
			try {
				buildPage(page);
			} catch (InterruptedException e) {
				System.out.println("Error process file " + page.getAbsolutePath() + " :"
						+ e.getMessage());
				LOG.warn("Error reading File", e);
			}
		}
		System.out.println("Process OK. Handled " + filteredFiles.length + " posts. ");
	}

	/**
	 * Builds page
	 * 
	 * @param aPage
	 * @throws FileNotFoundException
	 * @throws InterruptedException
	 * @throws TemplateException
	 */
	private void buildPage(final File aPage) throws FileNotFoundException, InterruptedException,
			TemplateException {
		String fileName = aPage.getName();
		String[] fileInfo = fileName.split("\\.");
		String filePath = fileInfo[0];
		String extension = fileInfo[1];

		Page page = readPage(aPage, extension);
		String templateName = page.getLayout() + "." + FTL_EXT;
		if (templateName == null || templateName.trim().length() == 0) {
			return;
		}

		try {
			Template template = templateConfig.getTemplate(templateName);

			File pageFolder = new File(output + File.separator + filePath);
			pageFolder.mkdirs();
			SimpleHash pageHash = new SimpleHash();
			pageHash.put("allCategories", Navigation.getCategoriesMap().keySet());
			pageHash.put("allTags", Navigation.getTagsMap().keySet());
			if (templateName.equals(archiveTemplateName)) {
				pageHash.put("archive", Navigation.getArchiveMap());
			} else if (templateName.equals(categoriesTemplateName)) {
				pageHash.put("categories", Navigation.getCategoriesMap());
			}
			pageHash.put("page", page);
			OutputStreamWriter pageWriter = new OutputStreamWriter(new FileOutputStream(
					pageFolder.getAbsolutePath() + File.separator + "index.html"), "UTF-8");
			template.process(pageHash, pageWriter);
		} catch (IOException e) {
			LOG.error("Template " + templateName + " was not found. Page was not built");
		}
	}

	/**
	 * Retourne le post à partir du nom du fichier. On le met en cache si
	 * nécéssaire afin de pas recréer un post existant
	 * 
	 * @param pFile
	 * @return
	 * @throws FileNotFoundException
	 * @throws InterruptedException
	 */
	private Post getFromCache(final File pFile) throws FileNotFoundException, InterruptedException {
		String lFileName = pFile.getName();
		if (cachedPosts.containsKey(lFileName)) {
			return cachedPosts.get(lFileName);
		}
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
		Post lPost = readPost(pFile, lExtension);
		String lPostUrl = buildPostUrl(lFilePath, lCalendar);
		lPost.setUrl(lPostUrl.replace(" ", "_"));
		lPost.setPublishedDate(lCalendar.getTime());
		cachedPosts.put(lFileName, lPost);
		return lPost;
	}

	private void buildPost(final Post pPost) throws TemplateException {
		String lPostDirectoryUrl = output + pPost.getUrl() + "index." + HTML_EXT;
		File lPostFile = new File(lPostDirectoryUrl);
		try {

			String templateName = pPost.getLayout() + "." + FTL_EXT;
			Template template = templateConfig.getTemplate(templateName);

			SimpleHash postHash = new SimpleHash();
			postHash.put(POSTS, pPost);
			postHash.put("allCategories", Navigation.getCategoriesMap().keySet());
			postHash.put("allTags", Navigation.getTagsMap().keySet());
			LOG.debug("Creating file " + lPostDirectoryUrl);
			if (lPostFile.getParentFile().exists()) {
				FileUtils.deleteFolder(lPostFile.getParentFile().getAbsolutePath());
			}
			lPostFile.getParentFile().mkdirs();
			lPostFile.createNewFile();
			OutputStreamWriter lPostFileW = new OutputStreamWriter(new FileOutputStream(lPostFile,
					false), "UTF-8");
			template.process(postHash, lPostFileW);
		} catch (IOException e) {
			LOG.warn("Error creating File", e);
		}
	}

	private Site buildIndex(final List<Post> lPosts, final Template pTemplate)
			throws TemplateException, IOException {
		Site lSite = new Site();
		lSite.getPosts().addAll(lPosts);
		SimpleHash root = new SimpleHash();
		root.put("paginator",
				SpikeTools.getPaginator(null, URL_SEPARATOR + PAGE + URL_SEPARATOR + 1));
		root.put("allCategories", Navigation.getCategoriesMap().keySet());
		root.put("allTags", Navigation.getTagsMap().keySet());
		root.put("site", lSite);
		OutputStreamWriter lSiteWriter = new OutputStreamWriter(new FileOutputStream(output
				+ File.separator + "index.html", false), "UTF-8");
		pTemplate.process(root, lSiteWriter);
		return lSite;
	}

	private void buildRssFeed(final Feed lRssFeeder) {
		final File XmlFolder = new File(output + File.separator + "feed");
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

	private void processPagination(final List<Post> lPosts, final Template lTemplateBase)
			throws TemplateException, IOException {
		int lNumberofPage = lPosts.size() / POST_PER_PAGE;
		for (int i = 0; i < lNumberofPage; i++) {
			if (i == 0) {
				continue;
			}
			List<Post> subList = lPosts.subList(i * POST_PER_PAGE, i * POST_PER_PAGE
					+ POST_PER_PAGE);
			Page lPage = new Page();
			lPage.setUrl(URL_SEPARATOR + PAGE + URL_SEPARATOR + i);
			lPage.setPosts(subList);
			// premier index
			Paginator lPaginator = null;
			if (i == 0) {
				lPaginator = SpikeTools.getPaginator(null, URL_SEPARATOR + PAGE + URL_SEPARATOR
						+ (i + 1));
			} else {
				if (i == lNumberofPage - 1) {
					lPaginator = SpikeTools.getPaginator(URL_SEPARATOR + PAGE + URL_SEPARATOR
							+ (i - 1), null);
				} else {
					if (i == 1) {
						lPaginator = SpikeTools.getPaginator(URL_SEPARATOR + "index.html",
								URL_SEPARATOR + PAGE + URL_SEPARATOR + (i + 1));
					} else {
						lPaginator = SpikeTools.getPaginator(URL_SEPARATOR + PAGE + URL_SEPARATOR
								+ (i - 1), URL_SEPARATOR + PAGE + URL_SEPARATOR + (i + 1));
					}
				}
				String lPageDirectory = output + File.separator + PAGE + File.separator + (i)
						+ File.separator;
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
						lPageDirectory + "index.html", false), "UTF-8");
				lTemplateBase.process(root, lPageW);
			}
		}
	}

	private void addFeedItem(final Post post) {
		FeedItem feed = new FeedItem();
		feed.setTitle(post.getTitle());
		feed.setDescription(post.getContent() + post.getTags());
		feed.setGuid(rssFeeder.getLink() + post.getUrl());
		feed.setLink(rssFeeder.getLink() + post.getUrl());
		feed.setPubDate(post.getPublishedDate());
		rssFeeder.getMessages().add(feed);
	}

	private String buildPostUrl(final String lFilePath, final Calendar lCalendar) {
		StringBuilder lStringBuilder = new StringBuilder();
		lStringBuilder.append(URL_SEPARATOR);
		lStringBuilder.append(lCalendar.get(Calendar.YEAR));
		lStringBuilder.append(URL_SEPARATOR);
		lStringBuilder.append(lCalendar.get(Calendar.MONTH) + 1);
		lStringBuilder.append(URL_SEPARATOR);
		lStringBuilder.append(lCalendar.get(Calendar.DAY_OF_MONTH));
		lStringBuilder.append(URL_SEPARATOR);
		lStringBuilder.append(lFilePath);
		lStringBuilder.append(URL_SEPARATOR);
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
		String description = "Blogging about software, dance, movies, life, and every thing else in between. Mexican curious in french disguise";
		String language = "fr";
		String link = "http://elmike.net";
		Date creationDate = new Date();
		SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		String pubdate = date_format.format(creationDate);
		return new Feed(title, link, description, language, copyright, pubdate);
	}

	/**
	 * Reads Yaml header
	 * 
	 * @param pFile
	 * @param pHeader
	 * @return
	 * @throws IllegalArgumentException
	 * @throws InterruptedException
	 */
	@SuppressWarnings("unchecked")
	private final static Map<String, Object> readYamlHeader(File pFile, final String pHeader)
			throws IllegalArgumentException, InterruptedException {
		try {
			return (Map<String, Object>) new Yaml().load(pHeader);
		} catch (Throwable th) {
			LOG.error("Error reading File", th);
			throw new InterruptedException("Header error");
		}
	}

	private static Page readPage(final File pFile, final String pExtension)
			throws FileNotFoundException, InterruptedException {
		Page page = null;
		Scanner lScanner = new Scanner(pFile, "UTF-8");
		lScanner.useDelimiter(delimiterPtrn);
		int i = 1;
		String lContent = "";
		while (lScanner.hasNext()) {
			String lNext = lScanner.next().trim();
			if (i == 1) {
				LOG.debug("reading yaml header of file: " + pFile.getName());
				page = new Page(readYamlHeader(pFile, lNext));
				i += 1;
				continue;
			}
			lContent += lNext;
		}
		lScanner.close();
		if (MD1_EXT.equals(pExtension) || MD2_EXT.equals(pExtension)) {
			page.setContent(mdProcessor.markdown(lContent));
		} else {
			page.setContent(lContent);
		}
		return page;
	}

	private static Post readPost(final File pFile, final String pExtension)
			throws FileNotFoundException, InterruptedException {
		Post lPost = null;
		Scanner lScanner = new Scanner(pFile, "UTF-8");
		lScanner.useDelimiter(delimiterPtrn);
		int i = 1;
		String lContent = "";
		while (lScanner.hasNext()) {
			String lNext = lScanner.next().trim();
			if (i == 1) {
				LOG.debug("reading yaml header of file: " + pFile.getName());
				Map<String, Object> yamlHeader = readYamlHeader(pFile, lNext);
				String lDescription = (String) yamlHeader.get(SpikeCst.DESCRIPTION);
				yamlHeader.put(SpikeCst.DESCRIPTION, convertDescription(lDescription));
				lPost = new Post(yamlHeader);
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

	private static String convertDescription(final String aDescription) {
		if (aDescription != null && aDescription.trim().length() != 0) {
			return mdProcessor.markdown(aDescription);
		}
		return null;
	}

	public final void initServer() {
		try {
			new NanoHTTPD(1337, new File(output));
		} catch (IOException ioe) {
			System.err.println("Couldn't start server:\n" + ioe);
			System.exit(-1);
		}
		LOG.info("Listening on port " + 1337 + ".\n");
	}

	public String getOutput() {
		return output;
	}

	public void copySource() throws IOException {
		if (canCopySoure) {
			System.out.println("Copying ressources files & directories...");
			FileUtils.copyFolder(sourcePath, output, filenameFilter);
		}
	}

	public boolean isSameInputOutPut() {
		return output.equals(sourcePath);
	}

	public String getSourcePath() {
		return sourcePath;
	}
}
