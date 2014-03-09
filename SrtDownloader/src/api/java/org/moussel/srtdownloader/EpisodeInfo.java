package org.moussel.srtdownloader;

public interface EpisodeInfo {

	/**
	 * @return the show
	 */
	public abstract ShowInfo getShow();

	/**
	 * @param show the show to set
	 */
	public abstract void setShow(ShowInfo show);

	/**
	 * @return the season
	 */
	public abstract Integer getSeason();

	/**
	 * @param season the season to set
	 */
	public abstract void setSeason(Integer season);

	/**
	 * @return the episode
	 */
	public abstract Integer getEpisode();

	/**
	 * @param episode the episode to set
	 */
	public abstract void setEpisode(Integer episode);

}