package org.moussel.srtdownloader;

import java.util.List;

public class TvShowInfoImpl implements TvShowInfo {

	List<String> alternateNames;
	String name;
	Integer tvDbId;

	public TvShowInfoImpl() {
	}

	public TvShowInfoImpl(String name) {
		this.name = name;
		getRealShowInfo();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.moussel.srtdownloader.TvShowInfo#getAlternateNames()
	 */
	@Override
	public List<String> getAlternateNames() {
		return alternateNames;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.moussel.srtdownloader.ShowInfo#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	private void getRealShowInfo() {
		// getRealShowFromCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.moussel.srtdownloader.TvShowInfo#setAlternateNames(java.util.List)
	 */
	@Override
	public void setAlternateNames(List<String> alternateNames) {
		this.alternateNames = alternateNames;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.moussel.srtdownloader.ShowInfo#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
