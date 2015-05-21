package org.moussel.srtdownloader.data;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.moussel.srtdownloader.data.tvdb.bean.TvDbSerieInfo;

public class TvDbLocal {

	Long lastUpdate;

	// Stores series Informations in cache
	Map<Integer, TvDbSerieInfo> seriesInfoList = new LinkedHashMap<>();

	// Can be rebuilt from seriesInfoList
	Map<String, Integer> serieByAlternativeNameIndex = new HashMap<>();

	public TvDbLocal() {

	}

	public static TvDbLocal createNewDb() {
		TvDbLocal newDb = new TvDbLocal();
		newDb.lastUpdate = System.currentTimeMillis();
		return newDb;
	}

	void rebuildIndexes() {
		serieByAlternativeNameIndex.clear();
		for (Entry<Integer, TvDbSerieInfo> entry : seriesInfoList.entrySet()) {
			for (String name : entry.getValue().getAlternativeNames()) {
				serieByAlternativeNameIndex.put(name, entry.getKey());
			}
		}
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
}
