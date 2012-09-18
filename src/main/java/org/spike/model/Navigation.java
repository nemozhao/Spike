/**
 *
 */
package org.spike.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.spike.model.navigation.PostArchive;
import org.spike.model.navigation.PostNav;

/**
 * @author mikomatic
 */
public class Navigation {

	private static final SimpleDateFormat monthFormat = new SimpleDateFormat("MMMMMMMM",
			Locale.FRENCH);
	private static final SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEEEEEE, d",
			Locale.FRENCH);
	private static Map<String, Map<String, Set<PostArchive>>> archiveMap = new LinkedHashMap<String, Map<String, Set<PostArchive>>>();

	private static Map<String, Set<PostNav>> categoriesMap = new LinkedHashMap<String, Set<PostNav>>();
	private static Map<String, Set<PostNav>> tagsMap = new LinkedHashMap<String, Set<PostNav>>();

	private Navigation() {
		// No instance
	}

	public static void clear() {
		archiveMap.clear();
		categoriesMap.clear();
		tagsMap.clear();
	}

	public static void add(final Post pPost) {
		Calendar publishedCal = pPost.getPublishedCal();
		String lYear = String.valueOf(publishedCal.get(Calendar.YEAR));
		String lMonth = monthFormat.format(pPost.getPublishedDate());
		String lDayOfMonth = dayOfWeekFormat.format(pPost.getPublishedDate());

		// Fill Archive Map
		Map<String, Set<PostArchive>> lYearMap = archiveMap.get(lYear);
		if (lYearMap == null) {
			lYearMap = new LinkedHashMap<String, Set<PostArchive>>();
			archiveMap.put(lYear, lYearMap);
		}

		Set<PostArchive> lMonthList = archiveMap.get(lYear).get(lMonth);
		if (lMonthList == null) {
			lMonthList = new LinkedHashSet<PostArchive>();
			archiveMap.get(lYear).put(lMonth, lMonthList);
		}

		PostArchive lPostArchive = new PostArchive(pPost.getTitle(), pPost.getUrl(), lYear, lMonth,
				lDayOfMonth);
		archiveMap.get(lYear).get(lMonth).add(lPostArchive);

		// Fill Category Map
		String lCategory = pPost.getCategory() != null ? pPost.getCategory().toLowerCase()
				: "no-category";
		Set<PostNav> lPostByCat = categoriesMap.get(lCategory);
		if (lPostByCat == null) {
			lPostByCat = new LinkedHashSet<PostNav>();
			categoriesMap.put(lCategory, lPostByCat);
		}
		categoriesMap.get(lCategory).add(
				new PostNav(pPost.getTitle(), pPost.getUrl(), pPost.getPublishedDate()));

		// Fill Category Map
		List<String> lTags = pPost.getTags();
		for (String lTag : lTags) {
			if (lTag != null && lTag.trim().length() != 0) {
				Set<PostNav> lPostByTag = tagsMap.get(lTag);
				if (lPostByTag == null) {
					lPostByTag = new LinkedHashSet<PostNav>();
					tagsMap.put(lTag, lPostByTag);
				}
				tagsMap.get(lTag).add(
						new PostNav(pPost.getTitle(), pPost.getUrl(), pPost.getPublishedDate()));
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

	public static Map<String, Map<String, Set<PostArchive>>> getArchiveMap() {
		return archiveMap;
	}

	public static Map<String, Set<PostNav>> getCategoriesMap() {
		return categoriesMap;
	}

	public static Map<String, Set<PostNav>> getTagsMap() {
		return tagsMap;
	};
}
