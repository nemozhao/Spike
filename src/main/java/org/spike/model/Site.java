package org.spike.model;

import java.util.ArrayList;
import java.util.List;

public class Site {

    private List<Post> posts = new ArrayList<Post>();

    private List<Page> pages;

    private Paginator paginator;

    public List<Post> getPosts() {
        return posts;
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pPages) {
        pages = pPages;
    }

    public Paginator getPaginator() {
        return paginator;
    }

    public void setPaginator(Paginator paginator) {
        this.paginator = paginator;
    }

}
