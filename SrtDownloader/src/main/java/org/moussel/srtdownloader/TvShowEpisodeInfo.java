package org.moussel.srtdownloader;

public interface TvShowEpisodeInfo {

	/**
	 * @return the episode
	 */
	public abstract Integer getEpisode();

	/**
	 * @return the season
	 */
	public abstract Integer getSeason();

	/**
	 * @return the show
	 */
	public abstract TvShowInfo getShow();

	/**
	 * 
	 * @return the episode title
	 */
	public abstract String getTitle();

	/**
	 * @param episode
	 *            the episode to set
	 */
	public abstract void setEpisode(Integer episode);

	/**
	 * @param season
	 *            the season to set
	 */
	public abstract void setSeason(Integer season);

	/**
	 * @param show
	 *            the show to set
	 */
	public abstract void setShow(TvShowInfo show);

	/**
	 * 
	 * @param title
	 */
	public abstract void setTitle(String title);
}