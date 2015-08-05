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
		ENABLED, SERVICE_URL, SUB_LANG_MAPPING
	}

	class SubInfo {
		public LinkedHashMap<String, String> headers;
		String url;
		Map<String, String> versionInfos;
	}

	private static Map<String, Object> configuration = new HashMap<>();

	// static public List<SubtitleExtractor> getEnabledExtractorList() {
	// // Annotation
	// }

	public AbstractSubtitleExtractor() {
		TvDbLocalDao jsonDb = TvDbLocalDao.getInstance();
		configuration = jsonDb.getConfiguration(getServiceName());
		if (configuration == null) {
			configuration = new HashMap<>();
		}
	}

	protected abstract SubInfo chooseSub(List<SubInfo> subInfos, VideoFileInfoImpl videoFileInfo);

	@Override
	public Path extractTvSubtitle(TvShowEpisodeInfo episode, String langName, File destinationFolder,
			VideoFileInfoImpl videoFileInfo) throws Exception {
		List<SubInfo> subInfos = null;
		subInfos = getAvailableSubtitles(episode, langName);
		if (subInfos.isEmpty()) {
			System.out.println("No " + langName + " Subtitle found.");
			return null;
		} else {
			SubInfo subInfoChosen = chooseSub(subInfos, videoFileInfo);
			if (subInfoChosen != null) {
				FileOutputStream subFileOutputStream = null;
				try {
					Path subFile;
					if (videoFileInfo != null && videoFileInfo.getVideoFilePath() != null) {
						Path videoFile = videoFileInfo.getVideoFilePath();
						subFile = AutoDownload.getSubtitlePath(videoFile, langName);
					} else {
						String fileName = episode.getShow().getName() + " - "
								+ StringUtils.leftPad("" + episode.getSeason(), 2, "0") + "x"
								+ StringUtils.leftPad("" + episode.getEpisode(), 2, "0")
								+ ((episode.getTitle() == null) ? "" : " - " + episode.getTitle()) + "."
								+ subInfoChosen.versionInfos.get("version") + "." + langName + ".srt";
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

	abstract protected List<SubInfo> getAvailableSubtitles(TvShowEpisodeInfo episode, String langName) throws Exception;

	@SuppressWarnings("unchecked")
	protected Map<String, String> getConfigMap(ConfigurationKeys key) {
		if (!configuration.containsKey(key.toString())) {
			return new LinkedHashMap<String, String>();
		} else {
			return (Map<String, String>) configuration.get(key.toString());
		}
	}

	protected String getConfigString(ConfigurationKeys key) {
		return (String) configuration.get(key.toString());
	}

	protected String getLanguageCode(String langName) {
		Map<String, String> mapping = getConfigMap(ConfigurationKeys.SUB_LANG_MAPPING);
		if (!mapping.containsKey(langName)) {
			try {
				setSubtitleLanguage(langName);
			} catch (Throwable e) {
				e.printStackTrace();
				throw new RuntimeException("Couldn't get Language code for " + langName, e);
			}
		}
		return getConfigMap(ConfigurationKeys.SUB_LANG_MAPPING).get(langName);
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

	protected void setConfigMap(ConfigurationKeys key, Map<String, String> value) {
		configuration.put(key.toString(), value);
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
			Map<String, String> langMappingConfig = getConfigMap(ConfigurationKeys.SUB_LANG_MAPPING);
			langMappingConfig.put(loc.getLanguage(), langMapping.get(langName));
			setConfigMap(ConfigurationKeys.SUB_LANG_MAPPING, langMappingConfig);
			saveConfiguration();
		}
	}

}