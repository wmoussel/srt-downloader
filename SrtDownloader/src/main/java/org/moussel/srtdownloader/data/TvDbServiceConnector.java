package org.moussel.srtdownloader.data;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.moussel.srtdownloader.data.tvdb.bean.TvDbSerieInfo;
import org.moussel.srtdownloader.data.tvdb.bean.TvDbSeriesList;
import org.moussel.srtdownloader.utils.SrtDownloaderUtils;

public class TvDbServiceConnector {

	static final String apiKey = "3399055529ED13E7";
	static final String mirrorPath = "http://thetvdb.com";

	String mirrorUrlWithApiKey;
	private final String userLanguage = "fr";

	public TvDbServiceConnector() {
		mirrorUrlWithApiKey = mirrorPath + "/api/" + apiKey;
	}

	public static void main(String[] args) {
		TvDbServiceConnector svc = new TvDbServiceConnector();
		svc.getMirrorsList();
		TvDbSeriesList series = svc.getSeriesList("Daredevil");
		if (series != null && series.size() == 1) {
			svc.getSerieInfo(series.get(0).id);
		}
	}

	void getMirrorsList() {
		try {
			URL requestUrl = new URL("http://thetvdb.com/api/" + apiKey + "/mirrors.xml");
			System.out.println(requestUrl.toString());
			SrtDownloaderUtils.getUrlContent(requestUrl, null, System.out);
			System.out.println();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void getCurrentServerTime() {
		try {
			URL requestUrl = new URL("http://thetvdb.com/api/Updates.php?type=none");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	void getLanguageList() {
		try {
			URL requestUrl = new URL(mirrorUrlWithApiKey + "/languages.xml");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public TvDbSeriesList getSeriesList(String serieName) {
		try {
			URL requestUrl = new URL("http://thetvdb.com/api/GetSeries.php?seriesname="
					+ URLEncoder.encode(serieName, "ISO-8859-1"));
			System.out.println(requestUrl.toString());
			TvDbSeriesList seriesList = new TvDbSeriesList();
			seriesList = SrtDownloaderUtils.getUrlXmlContentAsBean(requestUrl, seriesList);
			System.out.println(seriesList.toString());
			return seriesList;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public TvDbSerieInfo getSerieInfo(int serieId) {
		try {
			URL requestUrl = new URL(mirrorUrlWithApiKey + "/series/" + serieId + "/" + userLanguage + ".xml");
			System.out.println(requestUrl.toString());
			TvDbSeriesList serieList = new TvDbSeriesList();
			serieList = SrtDownloaderUtils.getUrlXmlContentAsBean(requestUrl, serieList);
			System.out.println(SrtDownloaderUtils.jsonString(serieList.get(0)));
			return serieList.get(0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
