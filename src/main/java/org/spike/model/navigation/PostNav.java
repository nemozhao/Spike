/**
 *
 */
package org.spike.model.navigation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author mikomatic
 *
 */
public class PostNav extends AsbtractNavigation {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat( "MMMMMMMM, yyyy", Locale.FRENCH );

    private final String date;

    public PostNav( final String pTitle, final String pUrl, final Date pDate ) {
        super( pTitle, pUrl );
        date = dateFormat.format( pDate );
    }

    public String getDate() {
        return date;
    }

}
