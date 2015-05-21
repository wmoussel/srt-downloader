package org.moussel.srtdownloader;

import java.util.List;

public interface TvShowInfo {

	/**
	 * @return the name
	 */
	public abstract String getName();

	/**
	 * @param name
	 *            the name to set
	 */
	public abstract void setName(String name);

	/**
	 * @return the alternateNames
	 */
	List<String> getAlternateNames();

	/**
	 * @param alternateNames
	 *            the alternateNames to set
	 */
	void setAlternateNames(List<String> alternateNames);

}