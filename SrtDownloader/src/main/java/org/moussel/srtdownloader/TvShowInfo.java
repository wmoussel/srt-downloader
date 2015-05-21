package org.moussel.srtdownloader;

import java.util.List;

public interface TvShowInfo {

	/**
	 * @return the alternateNames
	 */
	List<String> getAlternateNames();

	/**
	 * @return the name
	 */
	public abstract String getName();

	/**
	 * @param alternateNames
	 *            the alternateNames to set
	 */
	void setAlternateNames(List<String> alternateNames);

	/**
	 * @param name
	 *            the name to set
	 */
	public abstract void setName(String name);

}