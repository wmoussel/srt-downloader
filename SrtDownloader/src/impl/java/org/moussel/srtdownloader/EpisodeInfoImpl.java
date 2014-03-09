package org.moussel.srtdownloader;

public class EpisodeInfoImpl implements EpisodeInfo  {

	ShowInfo show;
	Integer season;
	Integer episode;
	
	public EpisodeInfoImpl(String showName, int season, int episode) {
		super();
		this.show = new ShowInfoImpl(showName);
		this.season = season;
		this.episode = episode;
	}
	
	/* (non-Javadoc)
	 * @see org.moussel.srtdownloader.EpisodeInfo#getShow()
	 */
	@Override
	public ShowInfo getShow() {
		return show;
	}
	/* (non-Javadoc)
	 * @see org.moussel.srtdownloader.EpisodeInfo#setShow(org.moussel.srtdownloader.ShowInfo)
	 */
	@Override
	public void setShow(ShowInfo show) {
		this.show = show;
	}
	/* (non-Javadoc)
	 * @see org.moussel.srtdownloader.EpisodeInfo#getSeason()
	 */
	@Override
	public Integer getSeason() {
		return season;
	}
	/* (non-Javadoc)
	 * @see org.moussel.srtdownloader.EpisodeInfo#setSeason(java.lang.Integer)
	 */
	@Override
	public void setSeason(Integer season) {
		this.season = season;
	}
	/* (non-Javadoc)
	 * @see org.moussel.srtdownloader.EpisodeInfo#getEpisode()
	 */
	@Override
	public Integer getEpisode() {
		return episode;
	}
	/* (non-Javadoc)
	 * @see org.moussel.srtdownloader.EpisodeInfo#setEpisode(java.lang.Integer)
	 */
	@Override
	public void setEpisode(Integer episode) {
		this.episode = episode;
	}
	

}
