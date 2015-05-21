package org.moussel.srtdownloader;

public class TvShowEpisodeInfoImpl implements TvShowEpisodeInfo {

	Integer episode;
	Integer season;
	TvShowInfo show;
	String team;
	String title;

	public TvShowEpisodeInfoImpl(String showName, int season, int episode) {
		super();
		this.show = new TvShowInfoImpl(showName);
		this.season = season;
		this.episode = episode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.moussel.srtdownloader.EpisodeInfo#getEpisode()
	 */
	@Override
	public Integer getEpisode() {
		return episode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.moussel.srtdownloader.EpisodeInfo#getSeason()
	 */
	@Override
	public Integer getSeason() {
		return season;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.moussel.srtdownloader.EpisodeInfo#getShow()
	 */
	@Override
	public TvShowInfo getShow() {
		return show;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.moussel.srtdownloader.TvShowEpisodeInfo#getTitle()
	 */
	@Override
	public String getTitle() {
		return title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.moussel.srtdownloader.EpisodeInfo#setEpisode(java.lang.Integer)
	 */
	@Override
	public void setEpisode(Integer episode) {
		this.episode = episode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.moussel.srtdownloader.EpisodeInfo#setSeason(java.lang.Integer)
	 */
	@Override
	public void setSeason(Integer season) {
		this.season = season;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.moussel.srtdownloader.EpisodeInfo#setShow(org.moussel.srtdownloader
	 * .ShowInfo)
	 */
	@Override
	public void setShow(TvShowInfo show) {
		this.show = show;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.moussel.srtdownloader.TvShowEpisodeInfo#setTitle(java.lang.String)
	 */
	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String toString() {
		return "Show: [" + show + "], Season " + season + ", Episode "
				+ episode + ((title != null) ? " (" + title + ")" : "");
	}
}
