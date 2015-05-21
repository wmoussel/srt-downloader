package org.moussel.srtdownloader;

import java.io.File;
import java.util.ArrayList;

import org.moussel.srtdownloader.extractor.Addic7edInteractiveExtractor;
import org.moussel.srtdownloader.utils.SrtDownloaderUtils;

public class CommandLineInput {
	static File downloadToFolder = new File("/Users/wandrillemoussel/Movies");

	public static void main(String[] args) throws Exception {

		String show = null;
		Integer season = null;
		String episodeNumber = null;

		for (int n = 0; n < args.length; ++n) {
			switch (n) {
			case 0:
				show = args[n];
				break;
			case 1:
				try {
					season = Integer.parseInt(args[n]);
				} catch (Exception e) {
				}
				break;
			case 2:
				episodeNumber = args[n];
				break;
			}
		}
		if (show == null) {
			show = SrtDownloaderUtils.promtForString("TV Show Name");
		}
		if (season == null) {
			season = SrtDownloaderUtils.promtForInt("TV Show Season", 3);
		}
		if (episodeNumber == null) {
			episodeNumber = SrtDownloaderUtils.promtForString("Episode(s) [accepts \"4-6\" for getting several]");
		}
		System.out.println("Starting...");

		getSubtitles(show, season, episodeNumber);
		System.out.println("Done.");

	}

	public static void getSubtitles(String show, Integer season, String episodeNumber) throws Exception {
		getSubtitles(show, season, episodeNumber, downloadToFolder);
	}

	public static void getSubtitles(String show, Integer season, String episodeNumber, File downloadToFolder)
			throws Exception {
		ArrayList<TvShowEpisodeInfoImpl> episodeList = new ArrayList<TvShowEpisodeInfoImpl>();
		if (episodeNumber.contains("-")) {
			String[] eps = episodeNumber.split("-");
			if (eps.length == 2) {
				int epMin = Integer.parseInt(eps[0]);
				int epMax = Integer.parseInt(eps[1]);
				for (int i = epMin; i <= epMax; i++) {
					episodeList.add(new TvShowEpisodeInfoImpl(show, season, i));
				}
			}
		} else {
			episodeList.add(new TvShowEpisodeInfoImpl(show, season, Integer.parseInt(episodeNumber)));
		}

		SubtitleExtractor extractor = new Addic7edInteractiveExtractor();

		for (TvShowEpisodeInfo episode : episodeList) {
			extractor.extractTvSubtitle(episode, downloadToFolder, null);
		}
	}

}