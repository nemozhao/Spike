/**
 * 
 */
package org.spike.model.navigation;

/**
 * @author mikomatic
 * 
 */
public abstract class AsbtractNavigation {

	public AsbtractNavigation(String pTitle, String pUrl) {
		super();
		this.title = pTitle;
		this.url = pUrl;
	}

	private String title;
	private String url;

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}
}
