/**
 * 
 */
package org.spike.model;

/**
 * @author mikomatic
 * 
 */
public class PostArchive {

	private String title;
	private String url;
	private String year;
	private String month;
	private String dayOfMonth;

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}

	public String getYear() {
		return year;
	}

	public String getMonth() {
		return month;
	}

	public String getDayOfMonth() {
		return dayOfMonth;
	}

	public PostArchive(String title, String url, String year, String month,
			String dayOfMonth) {
		super();
		this.title = title;
		this.url = url;
		this.year = year;
		this.month = month;
		this.dayOfMonth = dayOfMonth;
	}

}
