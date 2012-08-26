/**
 * 
 */
package org.spike.model.navigation;

/**
 * @author mikomatic
 * 
 */
public class PostNav extends AsbtractNavigation {

	private String selector;

	public PostNav(String pTitle, String pUrl, String pSelector) {
		super(pTitle, pUrl);
		selector = pSelector;
	}

	public String getSelector() {
		return selector;
	}

}
