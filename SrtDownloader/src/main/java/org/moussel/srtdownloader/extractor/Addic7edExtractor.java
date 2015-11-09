package org.moussel.srtdownloader.extractor;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPather;
import org.htmlcleaner.XPatherException;
import org.moussel.srtdownloader.SubInfo;
import org.moussel.srtdownloader.SubInfoChoice;
import org.moussel.srtdownloader.SubtitleExtractor;
import org.moussel.srtdownloader.TvShowEpisodeInfo;
import org.moussel.srtdownloader.TvShowInfo;
import org.moussel.srtdownloader.VideoFileInfoImpl;
import org.moussel.srtdownloader.data.TvDbLocalDao;
import org.moussel.srtdownloader.data.tvdb.bean.TvDbSerieInfo;
import org.moussel.srtdownloader.utils.SrtDownloaderUtils;

public class Addic7edExtractor extends AbstractSubtitleExtractor implements SubtitleExtractor {

	static Map<String, String> languageMappingCache = null;

	public Addic7edExtractor() {
		super();
		// Default configuration value
		setConfigDefault(ConfigurationKeys.SERVICE_URL, "http://www.addic7ed.com");
	}

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
				return new SubInfoChoice("Team Match", choice.get());
			}

			choice = subInfos.stream().filter(new Predicate<SubInfo>() {
				@Override
				public boolean test(SubInfo si) {
					return si.getVersionInfos().get("comment").toLowerCase()
							.contains(videoFile.getTeam().toLowerCase());
				}
			}).findFirst();
			if (choice.isPresent()) {
				return new SubInfoChoice("Team Referenced in comment", choice.get());
			}

			choice = subInfos.stream().sorted(new Comparator<SubInfo>() {

				@Override
				public int compare(SubInfo o1, SubInfo o2) {
					return Integer.parseInt(o2.getVersionInfos().get("Downloads"))
							- Integer.parseInt(o1.getVersionInfos().get("Downloads"));
				}
			}).findFirst();
			if (choice.isPresent()) {
				return new SubInfoChoice("Most Downloaded Subtitle", choice.get());
			}

		}
		return new SubInfoChoice("First Available Subtitle", subInfos.get(0));
	}

	public Object[] extractElementsFromUrl(String path, String xPathExpression) {
		try {
			// Get Home page to have a valid show
			TagNode htmlNode = getHtmlUrl(path);
			XPather showList = new XPather(xPathExpression);
			Object[] x = showList.evaluateAgainstNode(htmlNode);
			if (x != null && x.length > 0) {
				return x;
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return null;
	}

	private Collection<String> getAlternativeShowNames(TvShowInfo show) {
		TvDbSerieInfo serieInfo = TvDbLocalDao.getInstance().getSerieByName(show.getName());
		return serieInfo != null ? serieInfo.getAlternativeNames() : new ArrayList<String>();
	}

	@Override
	public List<SubInfo> getAvailableSubtitles(TvShowEpisodeInfo episode, String langName) throws Exception {
		List<SubInfo> subInfos = new ArrayList<SubInfo>();

		LinkedHashMap<String, String> headers = new LinkedHashMap<>();
		TagNode htmlNode = getCorrectHtmlNode(episode, headers, langName);
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
						System.out.println((subInfos.size() + 1) + "): " + versionInfos + " : "
								+ getConfigString(ConfigurationKeys.SERVICE_URL) + href);
						si.setUrl(getConfigString(ConfigurationKeys.SERVICE_URL) + href);
						si.setVersionInfos(versionInfos);
						si.setHeaders(headers);
						subInfos.add(si);
					}
				}
			}
		}
		return subInfos;
	}

	TagNode getCorrectHtmlNode(TvShowEpisodeInfo episode, Map<String, String> headers, String langName) {
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
		System.out.println("Getting available " + langName + " subtitles for " + episode.toString() + "...");
		for (String showName : showNameList) {
			String path;
			try {
				path = "/serie/" + URLEncoder.encode(showName, "ISO-8859-1") + "/" + episode.getSeason() + "/"
						+ episode.getEpisode() + "/" + getLanguageCode(langName);
				TagNode htmlNode = getHtmlUrl(getConfigString(ConfigurationKeys.SERVICE_URL) + path);
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
						headers.put("Referer", getConfigString(ConfigurationKeys.SERVICE_URL) + path);
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
		// props.setOmitCdataOutsideScriptAndStyle(true);

		ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
		HashMap<String, String> headers = new LinkedHashMap<String, String>();
		SrtDownloaderUtils.getUrlContent(new URL(url), headers, responseStream);
		String responseString = responseStream.toString();
		// System.out.println("String: " + responseString);
		// do parsing
		// TagNode tagNode = new HtmlCleaner(props).clean(new URL(url));
		TagNode tagNode = new HtmlCleaner(props).clean(responseString);

		// serialize to xml file
		// System.out.println("Parsed: " + new
		// PrettyXmlSerializer(props).getAsString(tagNode, "utf-8"));
		return tagNode;
	}

	@Override
	public Map<String, String> getLanguageCodeMapping() throws UnsupportedEncodingException {
		if (languageMappingCache == null) {
			Map<String, String> langMapping = new LinkedHashMap<>();
			Object[] extracted = extractElementsFromUrl(getConfigString(ConfigurationKeys.SERVICE_URL),
					"//select[@id='qsShow']/option/text()");
			String showName = extracted[10].toString().trim();

			String path = "/serie/" + URLEncoder.encode(showName, "ISO-8859-1") + "/1/1/0";
			Object[] extractOptions = extractElementsFromUrl(getConfigString(ConfigurationKeys.SERVICE_URL) + path,
					"//select[@id='filterlang']/option");
			for (Object option : extractOptions) {
				if (option instanceof TagNode) {
					TagNode node = (TagNode) option;
					String text = node.getAllChildren().get(0).toString().toLowerCase();
					String value = node.getAttributeByName("value");
					langMapping.put(text, value);
				}
			}
			languageMappingCache = langMapping;
		}
		return languageMappingCache;
	}

	@Override
	public String getServiceName() {
		return "Addic7ed";
	}

}
