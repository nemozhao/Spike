package org.spike.model;

import java.util.List;
import java.util.Map;

import org.spike.SpikeCst;

/**
 * 
 * <code>Page</code>
 * 
 * @author mikomatic
 */
public class Page {

	private String title;

	private String url;

	private String content = "";

	private String layout;

	private List<Post> posts;

	public Page() {
	}

	public Page(final Map<String, Object> aYamlProps) {
		setTitle((String) aYamlProps.get(SpikeCst.TITLE));
		setLayout((String) aYamlProps.get(SpikeCst.LAYOUT));
	}

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}

	public String getContent() {
		return content;
	}

	public void setTitle(String pTitle) {
		title = pTitle;
	}

	public void setUrl(String pUrl) {
		url = pUrl;
	}

	public void setContent(String pContent) {
		content = pContent;
	}

	public List<Post> getPosts() {
		return posts;
	}

	public void setPosts(List<Post> pPosts) {
		posts = pPosts;
	}

	public String getLayout() {
		return layout;
	}

	public void setLayout(String layout) {
		this.layout = layout;
	}
}
