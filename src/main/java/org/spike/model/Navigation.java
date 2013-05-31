/**
 *
 */
package org.spike.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
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

  public static Map<String, Set<PostNav>> getTagsmap() {
    return tagsMap;
  }

  private static final SimpleDateFormat monthFormat = new SimpleDateFormat("MMMMMMMM", Locale.FRENCH);

  private static final SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEEEEEE, d", Locale.FRENCH);

  private final static Set<Post> allPosts = new HashSet<Post>();

  private final static Map<String, Map<String, Set<PostArchive>>> archiveMap = new LinkedHashMap<String, Map<String, Set<PostArchive>>>();

  private final static Map<String, Set<PostNav>> categoriesMap = new LinkedHashMap<String, Set<PostNav>>();

  private final static Map<String, Set<PostNav>> tagsMap = new LinkedHashMap<String, Set<PostNav>>();

  private Navigation() {
    // No instance
  }

  public static void clear() {
    archiveMap.clear();
    categoriesMap.clear();
    tagsMap.clear();
  }

  public static void add(final Post pPost) {
    // Fill Archive Map
    Calendar publishedCal = pPost.getPublishedCal();
    String lYear = String.valueOf(publishedCal.get(Calendar.YEAR));
    String lMonth = monthFormat.format(pPost.getPublishedDate());
    String lDayOfMonth = dayOfWeekFormat.format(pPost.getPublishedDate());
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

    PostArchive lPostArchive = new PostArchive(pPost.getTitle(), pPost.getUrl(), lYear, lMonth, lDayOfMonth);
    archiveMap.get(lYear).get(lMonth).add(lPostArchive);

    // Fill Category Map
    String lCategory = pPost.getCategory() != null ? pPost.getCategory().toLowerCase() : "no-category";
    Set<PostNav> lPostByCat = categoriesMap.get(lCategory);
    if (lPostByCat == null) {
      lPostByCat = new LinkedHashSet<PostNav>();
      categoriesMap.put(lCategory, lPostByCat);
    }
    categoriesMap.get(lCategory).add(new PostNav(pPost.getTitle(), pPost.getUrl(), pPost.getPublishedDate()));

    // Fill tags Map
    List<String> lTags = pPost.getTags();
    for (String lTag : lTags) {
      if (lTag != null && lTag.trim().length() != 0) {
        String tag = lTag.toLowerCase();

        Set<PostNav> lPostByTag = tagsMap.get(tag);
        if (lPostByTag == null) {
          lPostByTag = new LinkedHashSet<PostNav>();
          tagsMap.put(tag, lPostByTag);
        }
        tagsMap.get(tag).add(new PostNav(pPost.getTitle(), pPost.getUrl(), pPost.getPublishedDate()));
      }
    }
  }

  public static Set<Post> getAllPosts() {
    return allPosts;
  }

  public static Map<String, Map<String, Set<PostArchive>>> getArchiveMap() {
    return archiveMap;
  }

  public static Map<String, Set<PostNav>> getCategoriesMap() {
    return categoriesMap;
  }

  public static Map<String, Set<PostNav>> getTagsMap() {
    return tagsMap;
  }
}
