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

import org.spike.model.navigation.PostArchive;
import org.spike.model.navigation.PostNav;

/**
 * @author mikomatic
 */
public class Navigation {

	private static SimpleDateFormat monthFormat = new SimpleDateFormat("MMMMMMMM", Locale.FRENCH);
	private static SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEEEEEE, d",
			Locale.FRENCH);
	private static Map<String, Map<String, List<PostArchive>>> archiveMap = new LinkedHashMap<String, Map<String, List<PostArchive>>>();

	private static Map<String, List<PostNav>> categoriesMap = new LinkedHashMap<String, List<PostNav>>();
	private static Map<String, List<PostNav>> tagsMap = new LinkedHashMap<String, List<PostNav>>();

	private Navigation() {
		// No instance
	}

	public static void add(final Post pPost) {
		Calendar publishedCal = pPost.getPublishedCal();
		String lYear = String.valueOf(publishedCal.get(Calendar.YEAR));
		String lMonth = monthFormat.format(pPost.getPublishedDate());
		String lDayOfMonth = dayOfWeekFormat.format(pPost.getPublishedDate());

		// Fill Archive Map
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

		PostArchive lPostArchive = new PostArchive(pPost.getTitle(), pPost.getUrl(), lYear, lMonth,
				lDayOfMonth);
		archiveMap.get(lYear).get(lMonth).add(lPostArchive);

		// Fill Category Map
		String lCategory = pPost.getCategory() != null ? pPost.getCategory().toLowerCase()
				: "no-category";
		List<PostNav> lPostByCat = categoriesMap.get(lCategory);
		if (lPostByCat == null) {
			lPostByCat = new LinkedList<PostNav>();
			categoriesMap.put(lCategory, lPostByCat);
		}
		categoriesMap.get(lCategory).add(new PostNav(pPost.getTitle(), pPost.getUrl(), lCategory));

		// Fill Category Map
		List<String> lTags = pPost.getTags();
		for (String lTag : lTags) {
			if (lTag != null && lTag.trim().length() != 0) {
				List<PostNav> lPostByTag = tagsMap.get(lTag);
				if (lPostByTag == null) {
					lPostByTag = new LinkedList<PostNav>();
					tagsMap.put(lTag, lPostByTag);
				}
				tagsMap.get(lTag).add(new PostNav(pPost.getTitle(), pPost.getUrl(), lTag));
			}
		}

	}

	@Deprecated
	public void buildArchiveFromList(List<Post> pPosts) {
		archiveMap.clear();

		for (Post lPost : pPosts) {
			add(lPost);
		}
	}

	public static Map<String, Map<String, List<PostArchive>>> getArchiveMap() {
		return archiveMap;
	}

	public static Map<String, List<PostNav>> getCategoriesMap() {
		return categoriesMap;
	}

	public static Map<String, List<PostNav>> getTagsMap() {
		return tagsMap;
	};
}
