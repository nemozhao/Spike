/**
 *
 */
package org.spike.model;

/**
 * @author mikomatic
 */
public class SpikeTools {

    private SpikeTools() {
        // private constructor to prevent class instance
    }

    public static Paginator getPaginator(final String pNextPage,
            final String pBeforePage) {
        Paginator lPaginator = new Paginator();
        lPaginator.setBeforePage(pBeforePage);
        lPaginator.setNextPage(pNextPage);
        return lPaginator;
    }

}
