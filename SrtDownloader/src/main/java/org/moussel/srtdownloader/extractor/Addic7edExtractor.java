package org.moussel.srtdownloader.extractor;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
import org.moussel.srtdownloader.AutoDownload;
import org.moussel.srtdownloader.SubtitleExtractor;
import org.moussel.srtdownloader.TvShowEpisodeInfo;
import org.moussel.srtdownloader.VideoFileInfoImpl;
import org.moussel.srtdownloader.utils.SrtDownloaderUtils;

public class Addic7edExtractor implements SubtitleExtractor {

	class SubInfo {
		String url;
		Map<String, String> versionInfos;
		public LinkedHashMap<String, String> headers;
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

	SubInfo chooseSub(List<SubInfo> subInfos, final VideoFileInfoImpl videoFile) {
		if (videoFile != null) {
			Optional<SubInfo> choice = subInfos.stream().filter(new Predicate<SubInfo>() {
				@Override
				public boolean test(SubInfo si) {
					return si.versionInfos.get("version").contains(videoFile.getTeam());
				}
			}).findFirst();
			if (choice.isPresent()) {
				System.out.println("Team Match");
				return choice.get();
			} else {
				choice = subInfos.stream().filter(new Predicate<SubInfo>() {
					@Override
					public boolean test(SubInfo si) {
						return si.versionInfos.get("comment").contains(videoFile.getTeam());
					}
				}).findFirst();
				if (choice.isPresent()) {
					System.out.println("Team Referenced in comment");
					return choice.get();
				}
			}
		}
		return subInfos.get(0);
	}

	List<SubInfo> getAvailableSubtitles(TvShowEpisodeInfo episode) throws Exception {
		List<SubInfo> subInfos = new ArrayList<SubInfo>();

		String url = "http://www.addic7ed.com";
		String path = "/serie/" + URLEncoder.encode(episode.getShow().getName(), "ISO-8859-1") + "/"
				+ episode.getSeason() + "/" + episode.getEpisode() + "/8";

		System.out.println("Getting " + url + path);
		TagNode htmlNode = getHtmlUrl(url + path);

		System.out.println("Cleaned Response...");

		XPather episodeInfoXPather = new XPather("//span[@class='titulo']");
		// Object[] x = episodeInfoXPather.evaluateAgainstNode(htmlNode);
		TagNode episodeInfo = (TagNode) episodeInfoXPather.evaluateAgainstNode(htmlNode)[0];
		String episodeName = episodeInfo.getAllChildren().get(0).toString().trim();
		System.out.println("Found Episode: " + episodeName);
		if (episode.getTitle() == null) {
			try {
				episode.setTitle(episodeName.split("-", 3)[2].trim());
			} catch (Exception e) {

			}
		}

		XPather srtVersionXPather = new XPather("//table[@class='tabel95']//table[@class='tabel95']");
		Object[] versions = srtVersionXPather.evaluateAgainstNode(htmlNode);

		LinkedHashMap<String, String> headers = new LinkedHashMap<String, String>();
		headers.put("Referer", url + path);

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
						System.out.println((subInfos.size() + 1) + "): " + versionInfos + " : " + url + href);
						si.url = url + href;
						si.versionInfos = versionInfos;
						si.headers = headers;
						subInfos.add(si);
					}
				}
			}
		}
		return subInfos;
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

}
