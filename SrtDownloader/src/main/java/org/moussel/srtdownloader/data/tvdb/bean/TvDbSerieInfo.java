package org.moussel.srtdownloader.data.tvdb.bean;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;
import org.moussel.srtdownloader.utils.SrtDownloaderUtils;

@XmlRootElement(name = "Series")
@XmlAccessorType(XmlAccessType.FIELD)
public class TvDbSerieInfo {
	/*
	 * XML Sample : <Series> <id>281662</id> <Actors>|Charlie Cox|Deborah Ann
	 * Woll|Vincent D'Onofrio|Rosario Dawson|Elden Henson|Wai Ching Ho|Vondie
	 * Curtis-Hall|Toby Leonard Moore|Scott Glenn|Ayelet Zurer|Peter
	 * Shinkoda|</Actors> <Airs_DayOfWeek></Airs_DayOfWeek>
	 * <Airs_Time></Airs_Time> <ContentRating>TV-MA</ContentRating>
	 * <FirstAired>2015-04-10</FirstAired>
	 * <Genre>|Action|Crime|Drama|Science-Fiction|</Genre>
	 * <IMDB_ID>tt3322312</IMDB_ID> <Language>fr</Language>
	 * <Network>Netflix</Network> <NetworkID></NetworkID> <Overview>Aveugle
	 * depuis l’enfance, mais doté de sens incroyablement développés, Matt
	 * Murdock combat l’injustice le jour en tant qu’avocat et la nuit en
	 * surveillant les rue de Hell’s Kitchen, à New York, dans le costume du
	 * super-héros Daredevil.</Overview> <Rating>8.5</Rating>
	 * <RatingCount>26</RatingCount> <Runtime>55</Runtime> <SeriesID></SeriesID>
	 * <SeriesName>Marvel's Daredevil</SeriesName> <Status>Continuing</Status>
	 * <added>2014-05-13 07:21:40</added> <addedBy>372816</addedBy>
	 * <banner>graphical/281662-g3.jpg</banner>
	 * <fanart>fanart/original/281662-7.jpg</fanart>
	 * <lastupdated>1430945994</lastupdated>
	 * <poster>posters/281662-1.jpg</poster> <tms_wanted_old>0</tms_wanted_old>
	 * <zap2it_id>SH02104223</zap2it_id> </Series>
	 */

	public int id;
	String language;

	@XmlElement(name = "IMDB_ID")
	String imdbId;

	@XmlElement(name = "zap2it_id")
	String zap2itId;

	@XmlElement(name = "SeriesName")
	String name;

	String banner;

	@XmlElement(name = "Network")
	String network;

	@XmlElement(name = "Overview")
	String overview;

	@XmlElement(name = "Rating")
	Double rating;

	@XmlElement(name = "Status")
	String status;

	@XmlElement(name = "FirstAired")
	String firstAirDate;

	@XmlElement(name = "lastupdated")
	Long lastUpdated;

	List<String> alternativeNames = new ArrayList<>();

	@Override
	public String toString() {
		return name + "[" + id + "]";
	}

	public String toJsonString() {
		return SrtDownloaderUtils.jsonString(this);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getImdbId() {
		return imdbId;
	}

	public void setImdbId(String imdbId) {
		this.imdbId = imdbId;
	}

	public String getZap2itId() {
		return zap2itId;
	}

	public void setZap2itId(String zap2itId) {
		this.zap2itId = zap2itId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBanner() {
		return banner;
	}

	public void setBanner(String banner) {
		this.banner = banner;
	}

	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}

	public String getOverview() {
		return overview;
	}

	public void setOverview(String overview) {
		this.overview = overview;
	}

	public Double getRating() {
		return rating;
	}

	public void setRating(Double rating) {
		this.rating = rating;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getFirstAirDate() {
		return firstAirDate;
	}

	public void setFirstAirDate(String firstAirDate) {
		this.firstAirDate = firstAirDate;
	}

	public Long getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Long lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public List<String> getAlternativeNames() {
		if (alternativeNames.size() == 0 && StringUtils.isNotBlank(name)) {
			alternativeNames.add(name);
		}
		return alternativeNames;
	}

	public void setAlternativeNames(List<String> alternativeNames) {
		this.alternativeNames = alternativeNames;
	}
}
