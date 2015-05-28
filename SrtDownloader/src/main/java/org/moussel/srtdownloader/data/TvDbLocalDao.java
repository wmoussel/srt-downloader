package org.moussel.srtdownloader.data;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import org.moussel.srtdownloader.data.tvdb.bean.TvDbSerieInfo;
import org.moussel.srtdownloader.utils.SrtDownloaderUtils;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSON.Feature;
import com.fasterxml.jackson.jr.ob.JSONObjectException;

public class TvDbLocalDao {

	private static final Long CURRENT_DB_MODEL_VERSION = 1L;

	private static final String DB_FILE = "tv-db.json";

	private static TvDbLocalDao singleton;

	private static Path getDefaultDbPath() {
		Path dbPath = Paths.get(
				SrtDownloaderUtils.getStringProperty("persistance.folder", System.getProperty("user.home")), DB_FILE);
		return dbPath;
	}

	public static TvDbLocalDao getInstance() {
		if (singleton == null) {
			singleton = new TvDbLocalDao();
		}
		return singleton;
	}

	private final Path dbPath;

	private TvDbLocal jsonDb;

	public TvDbLocalDao() {
		dbPath = getDefaultDbPath();
		boolean loaded = loadDbFromFile();
		if (!loaded) {
			jsonDb = TvDbLocal.createNewDb();
			jsonDb.setDbVersion(CURRENT_DB_MODEL_VERSION);
			if (writeDbToFile()) {
				System.out.println("Database created at: " + dbPath.toAbsolutePath().toString());
			}
		} else {
			System.out.println("Database found at: " + dbPath.toAbsolutePath().toString());
		}
		migrateDbVersionIfNeeded();
	}

	public void addShow(TvDbSerieInfo serieInfo) {
		updateShow(serieInfo);
	}

	public TvDbSerieInfo getSerieByName(String show) {
		if (jsonDb.getSerieIdByAlternativeName().containsKey(show)) {
			String serieId = jsonDb.getSerieIdByAlternativeName().get(show);
			return jsonDb.getSeriesInfoList().get(serieId);
		}
		return null;
	}

	public Collection<TvDbSerieInfo> getSeriesList() {
		return jsonDb.seriesInfoList.values();
	}

	private boolean loadDbFromFile() {
		if (dbPath.toFile().exists()) {
			try {
				FileInputStream sourceStream = new FileInputStream(dbPath.toFile());
				jsonDb = JSON.std.beanFrom(TvDbLocal.class, sourceStream);
				sourceStream.close();
			} catch (JSONObjectException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}

	private void migrateDbVersionIfNeeded() {
		if (jsonDb.dbVersion == null || CURRENT_DB_MODEL_VERSION.compareTo(jsonDb.dbVersion) > 0) {
			jsonDb.setDbVersion(CURRENT_DB_MODEL_VERSION);
			persist();
		}
	}

	private boolean persist() {
		jsonDb.rebuildIndexes();
		return writeDbToFile();
	}

	public void updateShow(TvDbSerieInfo serieInfo) {
		jsonDb.createOrUpdateSerie(serieInfo);
		persist();
	}

	private boolean writeDbToFile() {
		try {
			JSON.std.with(Feature.PRETTY_PRINT_OUTPUT).write(jsonDb, dbPath.toFile());
			return true;
		} catch (JSONObjectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
