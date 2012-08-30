/**
 *
 */
package org.spike.model.navigation;

/**
 * @author mikomatic
 *
 */
public abstract class AsbtractNavigation {

	public AsbtractNavigation(final String pTitle, final String pUrl) {
		super();
		this.title = pTitle;
		this.url = pUrl;
	}

	private final String title;
	private final String url;

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}
}
