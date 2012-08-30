/**
 *
 */
package org.spike.model.navigation;

/**
 * @author mikomatic
 *
 */
public class PostArchive extends AsbtractNavigation {

    private final String year;
    private final String month;
    private final String dayOfMonth;

    public String getYear() {
        return year;
    }

    public String getMonth() {
        return month;
    }

    public String getDayOfMonth() {
        return dayOfMonth;
    }

    public PostArchive( final String pTitle, final String pUrl, final String pYear, final String pMonth, final String pDayOfMonth ) {
        super( pTitle, pUrl );
        this.year = pYear;
        this.month = pMonth;
        this.dayOfMonth = pDayOfMonth;
    }

}
