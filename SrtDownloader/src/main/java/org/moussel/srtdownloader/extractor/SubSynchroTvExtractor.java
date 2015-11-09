/**
 * 
 */
package org.moussel.srtdownloader.extractor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.lang.StringUtils;
import org.moussel.srtdownloader.AutoDownload;
import org.moussel.srtdownloader.SubInfo;
import org.moussel.srtdownloader.SubInfoChoice;
import org.moussel.srtdownloader.SubtitleExtractor;
import org.moussel.srtdownloader.TvShowEpisodeInfo;
import org.moussel.srtdownloader.VideoFileInfoImpl;
import org.moussel.srtdownloader.utils.SrtDownloaderUtils;

import com.fasterxml.jackson.jr.ob.JSON;

/**
 * @author wandrillemoussel
 * 
 */
public class SubSynchroTvExtractor extends AbstractSubtitleExtractor implements SubtitleExtractor {

	/**
	 * 
	 */
	public SubSynchroTvExtractor() {
		super();
		// Default configuration value
		setConfigDefault(ConfigurationKeys.SERVICE_URL, "http://www.subsynchro.tv/include/ajax/subMarin.php");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.moussel.srtdownloader.extractor.AbstractSubtitleExtractor#chooseSub
	 * (java.util.List, org.moussel.srtdownloader.VideoFileInfoImpl)
	 */
	@Override
	protected SubInfoChoice chooseSub(List<SubInfo> subInfos, final VideoFileInfoImpl videoFile) {
		if (videoFile != null) {
			Optional<SubInfo> choice = subInfos.stream().filter(new Predicate<SubInfo>() {
				@Override
				public boolean test(SubInfo si) {
					String subVersion = si.getVersionInfos().get("version").toLowerCase();
					String videoVersion = videoFile.getTeam().toLowerCase();
					return subVersion.contains(videoVersion) || videoVersion.contains(subVersion);
				}
			}).findFirst();
			if (choice.isPresent()) {
				return new SubInfoChoice("Release Match", choice.get());
			}

			choice = subInfos.stream().sorted(new Comparator<SubInfo>() {

				@Override
				public int compare(SubInfo o1, SubInfo o2) {
					return Integer.parseInt(o2.getVersionInfos().get("pour"))
							- Integer.parseInt(o1.getVersionInfos().get("pour"));
				}
			}).filter(new Predicate<SubInfo>() {
				@Override
				public boolean test(SubInfo si) {
					return Integer.parseInt(si.getVersionInfos().get("pour")) > 0;
				}
			}).findFirst();
			if (choice.isPresent()) {
				return new SubInfoChoice("Most liked Subtitle", choice.get());
			}

		}
		return new SubInfoChoice("No good enough Sub to download.");
	}

	@Override
	public boolean downloadSubtitleToFile(SubInfo subInfoChosen, Path subFile) throws FileNotFoundException,
			MalformedURLException, IOException {
		FileOutputStream subZipOutputStream = null;
		try {
			Path zip = Files.createTempFile(null, "srtzip");
			subZipOutputStream = new FileOutputStream(zip.toFile());
			SrtDownloaderUtils.getUrlContent(new URL(subInfoChosen.getUrl()), subInfoChosen.getHeaders(),
					subZipOutputStream);
			// Search srt in zip
			List<Path> inZipSrtList = SrtDownloaderUtils.zipList(zip, "*.srt");
			if (inZipSrtList.size() == 1) {
				Files.copy(inZipSrtList.get(0), subFile, StandardCopyOption.REPLACE_EXISTING);
				if (subFile.toFile().exists()) {
					return true;
				}
			}
		} catch (Exception ex) {
			System.out.println("FAILED: " + ex.getMessage());
			ex.printStackTrace();
			return false;
		} finally {
			if (subZipOutputStream != null) {
				subZipOutputStream.close();
			}
		}
		return false;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.moussel.srtdownloader.SubtitleExtractor#getAvailableSubtitles(org
	 * .moussel.srtdownloader.TvShowEpisodeInfo, java.lang.String)
	 */
	@Override
	public List<SubInfo> getAvailableSubtitles(TvShowEpisodeInfo episode, String langName) throws Exception {
		List<SubInfo> subInfos = new ArrayList<SubInfo>();

		LinkedHashMap<String, String> headers = new LinkedHashMap<>();
		String urlCommand = "?title=" + URLEncoder.encode(episode.getShow().getName(), "ISO-8859-1") + "&season="
				+ episode.getSeason() + "&episode=" + episode.getEpisode();
		ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
		SrtDownloaderUtils.getUrlContent(new URL(getConfigString(ConfigurationKeys.SERVICE_URL) + urlCommand),
				new HashMap<String, String>(), responseStream);
		String responseString = responseStream.toString();
		Object jsonResponse = JSON.std.anyFrom(responseString);
		if (!((Map<String, Object>) jsonResponse).get("status").toString().equals("200")) {
			return subInfos;
		}
		List<Map<String, String>> versions = (List<Map<String, String>>) ((Map<String, Object>) jsonResponse)
				.get("data");

		for (Map<String, String> v : versions) {

			SubInfo si = new SubInfo();
			String href = v.get("telechargement").toString().trim();
			String release = v.get("release");
			String subVersion = release.replaceAll("^.*[sS][0123456789]{1,2}[eE][0123456789]{1,2}\\.(.*)$", "$1");
			LinkedHashMap<String, String> versionInfos = new LinkedHashMap<String, String>();
			versionInfos.putAll(v);
			versionInfos.remove("telechargement");
			versionInfos.remove("titre");
			versionInfos.remove("fichier");
			versionInfos.put("version", subVersion);
			System.out.println((subInfos.size() + 1) + "): " + versionInfos + " : " + href);
			si.setVersionInfos(versionInfos);
			si.setUrl(href);
			subInfos.add(si);
		}
		return subInfos;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.moussel.srtdownloader.extractor.AbstractSubtitleExtractor#
	 * getLanguageCodeMapping()
	 */
	@Override
	protected Map<String, String> getLanguageCodeMapping() throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.moussel.srtdownloader.SubtitleExtractor#getServiceName()
	 */
	@Override
	public String getServiceName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Path getSubtitlePath(TvShowEpisodeInfo episode, String langName, File destinationFolder,
			VideoFileInfoImpl videoFileInfo, SubInfo subInfoChosen) {
		Path subFile;
		if (videoFileInfo != null && videoFileInfo.getVideoFilePath() != null) {
			Path videoFile = videoFileInfo.getVideoFilePath();
			subFile = AutoDownload.getSubtitlePath(videoFile, langName);
		} else {
			String fileName = episode.getShow().getName() + " - "
					+ StringUtils.leftPad("" + episode.getSeason(), 2, "0") + "x"
					+ StringUtils.leftPad("" + episode.getEpisode(), 2, "0")
					+ ((episode.getTitle() == null) ? "" : " - " + episode.getTitle()) + "-"
					+ subInfoChosen.getVersionInfos().get("version") + "." + langName + ".srt";
			subFile = Paths.get(destinationFolder.getAbsolutePath(), fileName);
		}
		return subFile;
	}
}
