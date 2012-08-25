/**
 * 
 */
package org.spike.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author mikomatic
 */
public class Archive {

	private static SimpleDateFormat monthFormat = new SimpleDateFormat("MMMMMMMM",
			Locale.FRENCH);
	private static SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEEEEEE, d",
			Locale.FRENCH);
	private Map<String, Map<String, List<PostArchive>>> archiveMap = new LinkedHashMap<String, Map<String, List<PostArchive>>>();

	public Archive() {
		super();
	}

	public void add(Post pPost) {
		Calendar publishedCal = pPost.getPublishedCal();
		String lYear = String.valueOf(publishedCal.get(Calendar.YEAR));
		String lMonth = monthFormat.format(pPost.getPublishedDate());
		String lDayOfMonth = dayOfWeekFormat.format(pPost.getPublishedDate());

		Map<String, List<PostArchive>> lYearMap = archiveMap.get(lYear);
		if (lYearMap == null) {
			lYearMap = new LinkedHashMap<String, List<PostArchive>>();
			archiveMap.put(lYear, lYearMap);
		}

		List<PostArchive> lMonthList = archiveMap.get(lYear).get(lMonth);
		if (lMonthList == null) {
			lMonthList = new LinkedList<PostArchive>();
			archiveMap.get(lYear).put(lMonth, lMonthList);
		}

		PostArchive lPostArchive = new PostArchive(pPost.getTitle(), pPost.getUrl(),
				lYear, lMonth, lDayOfMonth);
		archiveMap.get(lYear).get(lMonth).add(lPostArchive);
	}

	public void buildArchiveFromList(List<Post> pPosts) {
		archiveMap.clear();

		for (Post lPost : pPosts) {
			add(lPost);
		}
	}

	public Map<String, Map<String, List<PostArchive>>> getArchiveMap() {
		return archiveMap;
	};
}
