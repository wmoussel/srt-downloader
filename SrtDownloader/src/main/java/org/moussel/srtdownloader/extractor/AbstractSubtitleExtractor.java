package org.moussel.srtdownloader.extractor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.moussel.srtdownloader.AutoDownload;
import org.moussel.srtdownloader.SubtitleExtractor;
import org.moussel.srtdownloader.TvShowEpisodeInfo;
import org.moussel.srtdownloader.TvShowInfo;
import org.moussel.srtdownloader.VideoFileInfoImpl;
import org.moussel.srtdownloader.data.TvDbLocalDao;
import org.moussel.srtdownloader.data.tvdb.bean.TvDbSerieInfo;
import org.moussel.srtdownloader.utils.SrtDownloaderUtils;

public abstract class AbstractSubtitleExtractor implements SubtitleExtractor {

	protected enum ConfigurationKeys {
		SERVICE_URL, SUB_LANG_CODE, SUB_LANG_NAME
	}

	class SubInfo {
		public LinkedHashMap<String, String> headers;
		String url;
		Map<String, String> versionInfos;
	}

	private static Map<String, String> configuration = new HashMap<String, String>();

	public AbstractSubtitleExtractor() {
		TvDbLocalDao jsonDb = TvDbLocalDao.getInstance();
		configuration = jsonDb.getConfiguration(getServiceName());
		if (configuration == null) {
			configuration = new HashMap<String, String>();
		}
	}

	protected abstract SubInfo chooseSub(List<SubInfo> subInfos, VideoFileInfoImpl videoFileInfo);

	@Override
	public Path extractTvSubtitle(TvShowEpisodeInfo episode, File destinationFolder, VideoFileInfoImpl videoFileInfo)
			throws Exception {
		List<SubInfo> subInfos = null;
		subInfos = getAvailableSubtitles(episode);
		if (subInfos.isEmpty()) {
			System.out.println("No Subtitle found.");
			return null;
		} else {
			SubInfo subInfoChosen = chooseSub(subInfos, videoFileInfo);
			if (subInfoChosen != null) {
				FileOutputStream subFileOutputStream = null;
				try {
					Path subFile;
					if (videoFileInfo != null && videoFileInfo.getVideoFilePath() != null) {
						Path videoFile = videoFileInfo.getVideoFilePath();
						subFile = AutoDownload.getSubtitlePath(videoFile);
					} else {
						String fileName = episode.getShow().getName() + " - "
								+ StringUtils.leftPad("" + episode.getSeason(), 2, "0") + "x"
								+ StringUtils.leftPad("" + episode.getEpisode(), 2, "0")
								+ ((episode.getTitle() == null) ? "" : " - " + episode.getTitle()) + "."
								+ subInfoChosen.versionInfos.get("version") + ".fr.srt";
						subFile = Paths.get(destinationFolder.getAbsolutePath(), fileName);
					}
					System.out
							.print("Downloading file [" + subInfoChosen.url + "] to [" + subFile.toString() + "]... ");
					subFileOutputStream = new FileOutputStream(subFile.toFile());
					SrtDownloaderUtils.getUrlContent(new URL(subInfoChosen.url), subInfoChosen.headers,
							subFileOutputStream);
					System.out.println("OK.");
					return subFile;
				} catch (Exception ex) {
					System.out.println("FAILED: " + ex.getMessage());
				} finally {
					if (subFileOutputStream != null) {
						subFileOutputStream.close();
					}
				}
			}
			return null;
		}
	}

	abstract protected List<SubInfo> getAvailableSubtitles(TvShowEpisodeInfo episode) throws Exception;

	protected String getConfig(ConfigurationKeys key) {
		return configuration.get(key.toString());
	}

	protected String getLanguageCode() {
		if (!hasConfig(ConfigurationKeys.SUB_LANG_CODE)) {
			String langName = getConfig(ConfigurationKeys.SUB_LANG_NAME);
			if (langName != null) {
				try {
					setSubtitleLanguage(langName);
				} catch (Throwable e) {
					e.printStackTrace();
					throw new RuntimeException("Couldn't get Language code for " + langName, e);
				}
			}
		}
		return getConfig(ConfigurationKeys.SUB_LANG_CODE);
	}

	abstract protected Map<String, String> getLanguageCodeMapping() throws UnsupportedEncodingException;

	public String getPreferredShowName(TvShowInfo show) {
		TvDbSerieInfo serieInfo = TvDbLocalDao.getInstance().getSerieByName(show.getName());
		if (serieInfo != null && serieInfo.getOtherIds().containsKey(getServiceName())) {
			return serieInfo.getOtherIds().get(getServiceName());
		}
		return null;
	}

	protected boolean hasConfig(ConfigurationKeys key) {
		return configuration.containsKey(key.toString());
	}

	protected void saveConfiguration() {
		TvDbLocalDao localDb = TvDbLocalDao.getInstance();
		localDb.setConfiguration(getServiceName(), configuration);
	}

	protected void setConfig(ConfigurationKeys key, String value) {
		configuration.put(key.toString(), value);
	}

	protected void setConfigDefault(ConfigurationKeys key, String value) {
		if (!configuration.containsKey(key)) {
			configuration.put(key.toString(), value);
		}
	}

	protected void setPreferredShowName(TvShowInfo showInfo, String showName) {
		TvDbSerieInfo serieInfo = TvDbLocalDao.getInstance().getSerieByName(showInfo.getName());
		if (serieInfo != null) {
			System.out.println("Saving [" + showName + "] as preferred name for " + getServiceName());
			serieInfo.getOtherIds().put(getServiceName(), showName);
			TvDbLocalDao.getInstance().updateShow(serieInfo);
		}
	}

	public void setSubtitleLanguage(String lang) throws UnsupportedEncodingException {

		Map<String, String> langMapping = null;
		langMapping = getLanguageCodeMapping();
		Locale loc = new Locale(lang);
		String langName = loc.getDisplayLanguage(Locale.ENGLISH).toLowerCase();
		if (langMapping.containsKey(langName)) {
			System.out.println("Found language code meaning [" + loc.toString() + "] for " + getServiceName());
			setConfig(ConfigurationKeys.SUB_LANG_NAME, loc.getLanguage());
			setConfig(ConfigurationKeys.SUB_LANG_CODE, langMapping.get(langName));
			saveConfiguration();
		}
	}

}