/**
 *
 */
package org.spike.model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author mikomatic
 *
 */
public class XmlFeed {

    private StringBuilder content;

    private String domain;

    private SimpleDateFormat dateFormat = new SimpleDateFormat( "ddd, dd mmm yyyy HH:mm:ss" );

    public XmlFeed( String pDomain ) {
        super();
        domain = pDomain;
        content = new StringBuilder();
        content.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " );
        content.append( "<rss version=\"2.0\" >" );
        // content.append("xmlns:content=\"http://purl.org/rss/1.0/modules/content/\" ");
        // content.append("xmlns:wfw=\"http://wellformedweb.org/CommentAPI/\" ");
        // content.append("xmlns:dc=\"http://purl.org/dc/elements/1.1/\" ");
        // content.append("xmlns:atom=\"http://www.w3.org/2005/Atom\"> ");

        content.append( "<channel>" );
        content.append( "<title>Mikomatic Blog</title>" );
        content.append( "<link>" + domain + "</link>" );
        // content.append("<atom:link href=\"http://"
        // + domain
        // + "/feed/rss2.xml\" rel=\"self\" type=\"application/rss+xml\" />");
        content.append( "<description>Mikomatic Blog Rss Feed</description>" );
        content.append( "<lastBuildDate>" + dateFormat.format( new Date() ) + "</lastBuildDate>" );
        content.append( "<language>en</language>" );

    }

    public void addItem( final Post post ) {
        content.append( "<item>" );
        content.append( "<title>" + post.getTitle() + "</title>" );
        content.append( "<guid isPermaLink=\"true\">http:/" + domain + post.getUrl() + "</guid>" );
        content.append( "<link>http:/" + domain + post.getUrl() + "</link>" );
        content.append( "<pubDate>" + dateFormat.format( post.getPublishedDate() ) + "</pubDate>" );
        content.append( "<description>" + post.getContent() + " | Tags: " + post.getTags().toString() + "</description>" );
        if ( post.getCategory() != null && post.getCategory().trim().length() != 0 ) {
            content.append( "<category>" + post.getCategory() + "</category>" );
        }
        content.append( "</item>" );
    }

    @Override
    public String toString() {
        content.append( "</channel>" );
        content.append( "</rss>" );
        return content.toString();
    }

}
