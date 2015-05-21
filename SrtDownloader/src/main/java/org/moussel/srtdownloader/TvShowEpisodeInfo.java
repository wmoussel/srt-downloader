package org.moussel.srtdownloader;

public interface TvShowEpisodeInfo {

	/**
	 * @return the show
	 */
	public abstract TvShowInfo getShow();

	/**
	 * @param show
	 *            the show to set
	 */
	public abstract void setShow(TvShowInfo show);

	/**
	 * @return the season
	 */
	public abstract Integer getSeason();

	/**
	 * @param season
	 *            the season to set
	 */
	public abstract void setSeason(Integer season);

	/**
	 * @return the episode
	 */
	public abstract Integer getEpisode();

	/**
	 * @param episode
	 *            the episode to set
	 */
	public abstract void setEpisode(Integer episode);

	/**
	 * 
	 * @return the episode title
	 */
	public abstract String getTitle();

	/**
	 * 
	 * @param title
	 */
	public abstract void setTitle(String title);
}