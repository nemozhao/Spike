/**
 *
 */
package org.spike.model.navigation;

/**
 * @author mikomatic
 *
 */
public class PostNav extends AsbtractNavigation {

	private final String selector;

	public PostNav(final String pTitle,final  String pUrl,final String pSelector) {
		super(pTitle, pUrl);
		selector = pSelector;
	}

	public String getSelector() {
		return selector;
	}

}
