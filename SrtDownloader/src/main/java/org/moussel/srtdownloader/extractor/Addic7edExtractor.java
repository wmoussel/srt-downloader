package org.moussel.srtdownloader.extractor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPather;
import org.htmlcleaner.XPatherException;
import org.moussel.srtdownloader.AutoDownload;
import org.moussel.srtdownloader.SubtitleExtractor;
import org.moussel.srtdownloader.TvShowEpisodeInfo;
import org.moussel.srtdownloader.TvShowInfo;
import org.moussel.srtdownloader.VideoFileInfoImpl;
import org.moussel.srtdownloader.data.TvDbLocalDao;
import org.moussel.srtdownloader.data.tvdb.bean.TvDbSerieInfo;
import org.moussel.srtdownloader.utils.SrtDownloaderUtils;

public class Addic7edExtractor implements SubtitleExtractor {

	class SubInfo {
		public LinkedHashMap<String, String> headers;
		String url;
		Map<String, String> versionInfos;
	}

	private static final String SERVICE_NAME = "Addic7ed";
	private static final String SERVICE_URL = "http://www.addic7ed.com";

	SubInfo chooseSub(List<SubInfo> subInfos, final VideoFileInfoImpl videoFile) {
		if (videoFile != null) {
			Optional<SubInfo> choice = subInfos.stream().filter(new Predicate<SubInfo>() {
				@Override
				public boolean test(SubInfo si) {
					String subVersion = si.versionInfos.get("version").toLowerCase();
					String videoVersion = videoFile.getTeam().toLowerCase();
					return subVersion.contains(videoVersion) || videoVersion.contains(subVersion);
				}
			}).findFirst();
			if (choice.isPresent()) {
				System.out.println("Team Match");
				return choice.get();
			}

			choice = subInfos.stream().filter(new Predicate<SubInfo>() {
				@Override
				public boolean test(SubInfo si) {
					return si.versionInfos.get("comment").toLowerCase().contains(videoFile.getTeam().toLowerCase());
				}
			}).findFirst();
			if (choice.isPresent()) {
				System.out.println("Team Referenced in comment");
				return choice.get();
			}

			choice = subInfos.stream().sorted(new Comparator<SubInfo>() {

				@Override
				public int compare(SubInfo o1, SubInfo o2) {
					return Integer.parseInt(o2.versionInfos.get("Downloads"))
							- Integer.parseInt(o1.versionInfos.get("Downloads"));
				}
			}).findFirst();
			if (choice.isPresent()) {
				System.out.println("Most Downloaded Subtitle");
				return choice.get();
			}

		}
		return subInfos.get(0);
	}

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

	private Collection<String> getAlternativeShowNames(TvShowInfo show) {
		TvDbSerieInfo serieInfo = TvDbLocalDao.getInstance().getSerieByName(show.getName());
		return serieInfo != null ? serieInfo.getAlternativeNames() : new ArrayList<String>();
	}

	List<SubInfo> getAvailableSubtitles(TvShowEpisodeInfo episode) throws Exception {
		List<SubInfo> subInfos = new ArrayList<SubInfo>();

		LinkedHashMap<String, String> headers = new LinkedHashMap<>();
		TagNode htmlNode = getCorrectHtmlNode(episode, headers);
		if (htmlNode == null) {
			return subInfos;
		}
		XPather srtVersionXPather = new XPather("//table[@class='tabel95']//table[@class='tabel95']");
		Object[] versions = srtVersionXPather.evaluateAgainstNode(htmlNode);

		for (Object v : versions) {
			// System.out.println("node{"+v.getClass()+"} = ["+v.toString()+"]");
			XPather completedXPather = new XPather("//b/text()");
			XPather hrefXPather = new XPather("//a[@class='buttonDownload'][last()]/@href");
			XPather versionNameXPather = new XPather("//td[@class='NewsTitle']/text()");
			XPather versionDownloadedXPather = new XPather("//td[@class='newsDate']/text()");
			if (v instanceof TagNode) {
				Object[] completed = completedXPather.evaluateAgainstNode((TagNode) v);
				for (Object com : completed) {
					if (com.toString().trim().equalsIgnoreCase("Completed")) {
						SubInfo si = new SubInfo();
						String href = hrefXPather.evaluateAgainstNode((TagNode) v)[0].toString().trim();
						String versionName = versionNameXPather.evaluateAgainstNode((TagNode) v)[0].toString().trim();
						String versionDl = versionDownloadedXPather.evaluateAgainstNode((TagNode) v)[1].toString()
								.trim();

						LinkedHashMap<String, String> versionInfos = new LinkedHashMap<String, String>();
						versionInfos.put("version", versionName.split(",", -1)[0].replaceAll("Version", "").trim());
						Pattern dlPattern = Pattern.compile("([0-9]+) ([ a-zA-Z]+) ");
						Matcher dlMatcher = dlPattern.matcher(versionDl);
						while (dlMatcher.find()) {
							versionInfos.put(dlMatcher.group(2), dlMatcher.group(1));
						}
						String comment = versionDownloadedXPather.evaluateAgainstNode((TagNode) v)[0].toString().trim();
						versionInfos.put("comment", comment);
						System.out.println((subInfos.size() + 1) + "): " + versionInfos + " : " + SERVICE_URL + href);
						si.url = SERVICE_URL + href;
						si.versionInfos = versionInfos;
						si.headers = headers;
						subInfos.add(si);
					}
				}
			}
		}
		return subInfos;
	}

	private TagNode getCorrectHtmlNode(TvShowEpisodeInfo episode, Map<String, String> headers) {
		List<String> showNameList = new ArrayList<>();
		TvShowInfo showInfo = episode.getShow();
		String preferredName = getPreferredShowName(showInfo);
		if (preferredName != null) {
			showNameList.add(preferredName);
		}
		showNameList.addAll(getAlternativeShowNames(showInfo));
		if (showNameList.isEmpty()) {
			showNameList.add(showInfo.getName());
		}
		System.out.println("Getting available subtitles for " + episode.toString() + "...");
		for (String showName : showNameList) {
			String path;
			try {
				path = "/serie/" + URLEncoder.encode(showName, "ISO-8859-1") + "/" + episode.getSeason() + "/"
						+ episode.getEpisode() + "/8";
				TagNode htmlNode = getHtmlUrl(SERVICE_URL + path);
				XPather episodeInfoXPather = new XPather("//span[@class='titulo']");
				Object[] x = episodeInfoXPather.evaluateAgainstNode(htmlNode);
				if (x != null && x.length >= 1) {
					TagNode episodeInfo = (TagNode) episodeInfoXPather.evaluateAgainstNode(htmlNode)[0];
					String episodeName = episodeInfo.getAllChildren().get(0).toString().trim();
					if (episode.getTitle() == null) {
						System.out.println("Found Episode: " + episodeName);
						try {
							episode.setTitle(episodeName.split("-", 3)[2].trim());
						} catch (Exception e) {

						}
						headers.put("Referer", SERVICE_URL + path);
						if (!showName.equals(preferredName)) {
							setPreferredShowName(showInfo, showName);
						}
						return htmlNode;
					}
				}
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			} catch (XPatherException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}
		return null;
	}

	public TagNode getHtmlUrl(String url) throws Exception {
		CleanerProperties props = new CleanerProperties();

		// set some properties to non-default values
		props.setTranslateSpecialEntities(true);
		props.setTransResCharsToNCR(true);
		props.setOmitComments(true);

		// do parsing
		TagNode tagNode = new HtmlCleaner(props).clean(new URL(url));

		// serialize to xml file
		// System.out.println(new
		// PrettyXmlSerializer(props).getAsString(tagNode, "utf-8"));
		return tagNode;
	}

	public String getPreferredShowName(TvShowInfo show) {
		TvDbSerieInfo serieInfo = TvDbLocalDao.getInstance().getSerieByName(show.getName());
		if (serieInfo != null && serieInfo.getOtherIds().containsKey(getServiceName())) {
			return serieInfo.getOtherIds().get(getServiceName());
		}
		return null;
	}

	@Override
	public String getServiceName() {
		return SERVICE_NAME;
	}

	private void setPreferredShowName(TvShowInfo showInfo, String showName) {
		TvDbSerieInfo serieInfo = TvDbLocalDao.getInstance().getSerieByName(showInfo.getName());
		if (serieInfo != null) {
			System.out.println("Saving [" + showName + "] as preferred name for " + getServiceName());
			serieInfo.getOtherIds().put(SERVICE_NAME, showName);
			TvDbLocalDao.getInstance().updateShow(serieInfo);
		}
	}
}
