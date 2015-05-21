package org.moussel.srtdownloader.data.tvdb.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;

@XmlRootElement(name = "Data")
@XmlAccessorType(XmlAccessType.FIELD)
public class TvDbSeriesList implements Iterable<TvDbSerieInfo> {

	@XmlElement(name = "Series")
	private List<TvDbSerieInfo> seriesList = new ArrayList<TvDbSerieInfo>();

	public TvDbSerieInfo get(int i) {
		if (size() > 0) {
			return seriesList.get(i);
		} else {
			return null;
		}
	}

	public List<TvDbSerieInfo> getSeriesList() {
		return seriesList;
	}

	@Override
	public Iterator<TvDbSerieInfo> iterator() {
		return seriesList.iterator();
	}

	public void setSeriesList(List<TvDbSerieInfo> seriesList) {
		this.seriesList = seriesList;
	}

	public int size() {
		return (seriesList != null) ? seriesList.size() : 0;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("GetSeriesListResponse(");
		sb.append((seriesList != null) ? seriesList.size() : "null");
		sb.append("): ");
		sb.append(StringUtils.join(seriesList, ","));

		return sb.toString();
	}

}