package org.moussel.srtdownloader.data;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.moussel.srtdownloader.data.tvdb.bean.TvDbSerieInfo;

public class TvDbLocal {

	public static TvDbLocal createNewDb() {
		TvDbLocal newDb = new TvDbLocal();
		newDb.lastUpdate = System.currentTimeMillis();
		return newDb;
	}

	Long dbVersion;

	Long lastUpdate;
	// Can be rebuilt from seriesInfoList
	Map<String, String> serieIdByAlternativeName = new HashMap<>();

	// Stores series Informations in cache
	Map<String, TvDbSerieInfo> seriesInfoList = new LinkedHashMap<>();

	public TvDbLocal() {

	}

	public void createOrUpdateSerie(TvDbSerieInfo serie) {
		if (serie == null) {
			return;
		}
		if (seriesInfoList.containsKey(serie.id)) {
			// TODO: Merge objects
			seriesInfoList.put(serie.id, serie);
		} else {
			seriesInfoList.put(serie.id, serie);
		}
	}

	public Long getDbVersion() {
		return dbVersion;
	}

	public Long getLastUpdate() {
		return lastUpdate;
	}

	public Map<String, String> getSerieIdByAlternativeName() {
		return serieIdByAlternativeName;
	}

	public Map<String, TvDbSerieInfo> getSeriesInfoList() {
		return seriesInfoList;
	}

	void rebuildIndexes() {
		serieIdByAlternativeName.clear();
		for (Entry<String, TvDbSerieInfo> entry : seriesInfoList.entrySet()) {
			for (String name : entry.getValue().getAlternativeNames()) {
				serieIdByAlternativeName.put(name, entry.getKey());
			}
		}
	}

	public void setDbVersion(Long dbVersion) {
		this.dbVersion = dbVersion;
	}

	public void setLastUpdate(Long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public void setSerieIdByAlternativeName(Map<String, String> serieIdByAlternativeName) {
		this.serieIdByAlternativeName = serieIdByAlternativeName;
	}

	public void setSeriesInfoList(Map<String, TvDbSerieInfo> seriesInfoList) {
		this.seriesInfoList = seriesInfoList;
	}
}
