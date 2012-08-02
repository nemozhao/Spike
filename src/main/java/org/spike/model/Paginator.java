/**
 *
 */
package org.spike.model;

/**
 * @author mortega
 * 
 */
public class Paginator {

	private String currentPage;

	private String nextPage;

	private String beforePage;

	public String getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(String currentPage) {
		this.currentPage = currentPage;
	}

	public String getNextPage() {
		return nextPage;
	}

	public void setNextPage(String nextPage) {
		this.nextPage = nextPage;
	}

	public String getBeforePage() {
		return beforePage;
	}

	public void setBeforePage(String beforePage) {
		this.beforePage = beforePage;
	}

}
