package org.moussel.srtdownloader.data;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import org.moussel.srtdownloader.data.tvdb.bean.TvDbSerieInfo;
import org.moussel.srtdownloader.utils.SrtDownloaderUtils;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONObjectException;

public class TvDbLocalDao {

	private static TvDbLocalDao singleton;

	private static final String DB_FILE = "tv-db.json";

	private TvDbLocal jsonDb;

	public static TvDbLocalDao getInstance() {
		if (singleton == null) {
			singleton = new TvDbLocalDao();
		}
		return singleton;
	}

	public static void main(String[] args) {

	}

	public TvDbLocalDao() {
		Path dbPath = Paths.get(
				SrtDownloaderUtils.getStringProperty("persistance.folder", System.getProperty("user.home")), DB_FILE);
		if (dbPath.toFile().exists()) {
			try {

				FileInputStream sourceStream = new FileInputStream(dbPath.toFile());
				jsonDb = JSON.std.beanFrom(TvDbLocal.class, sourceStream);
				System.out.println("Database found at: " + dbPath.toAbsolutePath().toString());
				sourceStream.close();
			} catch (JSONObjectException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (jsonDb == null) {
			jsonDb = TvDbLocal.createNewDb();
			try {
				JSON.std.write(jsonDb, dbPath.toFile());
				System.out.println("Database created at: " + dbPath.toAbsolutePath().toString());
			} catch (JSONObjectException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void addShow(TvDbSerieInfo serieInfo) {
		jsonDb.createOrUpdateSerie(serieInfo);
	}

	public Collection<TvDbSerieInfo> getSeriesList() {
		return jsonDb.seriesInfoList.values();
	}
}
