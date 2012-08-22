/*
 * Copyright 1998-2012 by SLIB,
 * 70 rue Villette, 69003 Lyon, France
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SLIB. (&quot;Confidential Information&quot;).  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with SLIB.
 */

package org.spike.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <code>Post</code>
 *
 * @author mikomatic
 */
public class Post implements Comparable<Post>{

    private String title;

    private String category;

    private List<String> tags = new ArrayList<String>();

    private String content = "";

    private Date publishedDate;

    private String url;

    private String source;

    private Post previous;

    private Post next;

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getContent() {
        return content;
    }

    public Date getPublishedDate() {
        return publishedDate;
    }

    public void setTitle(String pTitle) {
        title = pTitle;
    }

    public void setCategory(String pCategory) {
        category = pCategory;
    }

    public void setTags(List<String> pTags) {
        tags = pTags;
    }

    public void setContent(String pContent) {
        content = pContent;
    }

    public void setPublishedDate(Date pPublishedDate) {
        publishedDate = pPublishedDate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String pUrl) {
        url = pUrl;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Post getPrevious() {
        return previous;
    }

    public void setPrevious(Post previousPost) {
        this.previous = previousPost;
    }

    public Post getNext() {
        return next;
    }

    public void setNext(Post nextPost) {
        this.next = nextPost;
    }

    @Override
    public int compareTo( Post pPost ) {
        return pPost.getPublishedDate().compareTo(this.getPublishedDate());
    }
}
