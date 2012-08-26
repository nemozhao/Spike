/**
 * 
 */
package org.spike.model.navigation;

/**
 * @author mikomatic
 * 
 */
public class PostArchive extends AsbtractNavigation {

	private String year;
	private String month;
	private String dayOfMonth;

	public String getYear() {
		return year;
	}

	public String getMonth() {
		return month;
	}

	public String getDayOfMonth() {
		return dayOfMonth;
	}

	public PostArchive(String pTitle, String pUrl, String pYear, String pMonth, String pDayOfMonth) {
		super(pTitle, pUrl);
		this.year = pYear;
		this.month = pMonth;
		this.dayOfMonth = pDayOfMonth;
	}

}
