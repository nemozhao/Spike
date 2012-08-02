package org.spike.model;

import java.util.List;

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

    private List<Post> posts;

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getContent() {
        return content;
    }

    public void setTitle( String pTitle ) {
        title = pTitle;
    }

    public void setUrl( String pUrl ) {
        url = pUrl;
    }

    public void setContent( String pContent ) {
        content = pContent;
    }


    public List<Post> getPosts() {
        return posts;
    }


    public void setPosts( List<Post> pPosts ) {
        posts = pPosts;
    }
}
