package org.moussel.srtdownloader;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPather;

public class Addic7edExtractor implements SubtitleExtractor {

	@Override
	public void extractSubtitle(EpisodeInfo episode) throws Exception {
		String url = "http://www.addic7ed.com";
		String path = "/serie/"+episode.getShow().getName()+"/"+episode.getSeason()+"/"+episode.getEpisode()+"/8";

		System.out.println("Getting "+url+path);
		TagNode htmlNode = getHtmlUrl(url + path);

		System.out.println("Cleaned Response...");

		XPather episodeInfoXPather = new XPather("//span[@class='titulo']");
		TagNode episodeInfo = (TagNode) episodeInfoXPather.evaluateAgainstNode(htmlNode)[0];
		String episodeName = episodeInfo.getAllChildren().get(0).toString().trim();
		System.out.println("Found Episode: "+episodeName);

		XPather srtVersionXPather = new XPather("//table[@class='tabel95']//table[@class='tabel95']");
		Object[] versions = srtVersionXPather.evaluateAgainstNode(htmlNode);

		LinkedHashMap<String, String> headers = new LinkedHashMap<String, String>();
		headers.put("Referer",url+path);
		
		LinkedHashMap<String, Map<String, String>> subInfos = new LinkedHashMap<String, Map<String,String>>();
		for(Object v : versions) {
			//	System.out.println("node{"+v.getClass()+"} = ["+v.toString()+"]");
			XPather completedXPather = new XPather("//b/text()");
			XPather hrefXPather = new XPather("//a[@class='buttonDownload'][last()]/@href");
			XPather versionNameXPather = new XPather("//td[@class='NewsTitle']/text()");
			XPather versionDownloadedXPather = new XPather("//td[@class='newsDate']/text()");
			if(v instanceof TagNode) {
				Object[] completed = completedXPather.evaluateAgainstNode((TagNode)v);
				for(Object com : completed) {
					if(com.toString().trim().equalsIgnoreCase("Completed")) {
						String href = hrefXPather.evaluateAgainstNode((TagNode)v)[0].toString().trim();
						String versionName = versionNameXPather.evaluateAgainstNode((TagNode)v)[0].toString().trim();
						String versionDl = versionDownloadedXPather.evaluateAgainstNode((TagNode)v)[1].toString().trim();
						
						LinkedHashMap<String, String> versionInfos = new LinkedHashMap<String, String>();
						versionInfos.put("version",versionName.split(",",-1)[0].replaceAll("Version","").trim());
						Pattern dlPattern = Pattern.compile("([0-9]+) ([ a-zA-Z]+) ");
						Matcher dlMatcher = dlPattern.matcher(versionDl);
						while(dlMatcher.find()) {
							versionInfos.put(dlMatcher.group(2),dlMatcher.group(1));
						}
						String comment = versionDownloadedXPather.evaluateAgainstNode((TagNode)v)[0].toString().trim();
						versionInfos.put("comment",comment);
						System.out.println((subInfos.size()+1)+"): "+versionInfos+" : "+url+href);
						subInfos.put(url+href,versionInfos);
					}
				}
			}
		}
		if(subInfos.isEmpty()) {
			System.out.println("No Subtitle found.");
		} else {
			int choice = 1;
			if(subInfos.size() > 1) {
				choice = interactiveChoice("Please choose File to download (1-"+subInfos.size()+")",3);
			}
			if(choice >= 1 && choice <= subInfos.size()) {
				Iterator<Map.Entry<String, Map<String,String>>> it = subInfos.entrySet().iterator();
				Map.Entry<String, Map<String,String>> e = it.next();
				if(choice >= 2) {
					for(int i=1 ; i < choice ; i++) {
						e = it.next();
					}
				}
				String fileName = episodeName+"."+e.getValue().get("version")+".fr.srt";
				System.out.println("Downloading file "+e.getKey()+" to "+fileName);
				saveUrl(fileName,e.getKey(),headers);
			} else {
				System.out.println("Invalid choice. Aborting.");
			}
		}
	}

	public TagNode getHtmlUrl(String url) throws Exception {
		CleanerProperties props = new CleanerProperties();
 
		// set some properties to non-default values
		props.setTranslateSpecialEntities(true);
		props.setTransResCharsToNCR(true);
		props.setOmitComments(true);
 
		// do parsing
		TagNode tagNode = new HtmlCleaner(props).clean(
			new URL(url)
		);

		// serialize to xml file
		//System.out.println(new PrettyXmlSerializer(props).getAsString(tagNode, "utf-8"));
		return tagNode;
	}

	public void saveUrl(String localFilename, String urlString, Map<String,String> headers)
			throws MalformedURLException, IOException {
		System.out.println("saveUrl(\""+localFilename+"\", \""+urlString+"\");");
		BufferedInputStream in = null;
		FileOutputStream fout = null;
		try {
			URLConnection connection = new URL(urlString).openConnection();
			for(Map.Entry<String, String> h : headers.entrySet()) {
				connection.setRequestProperty(h.getKey(), h.getValue());
			}
			in = new BufferedInputStream(connection.getInputStream());
			fout = new FileOutputStream(localFilename);

			byte data[] = new byte[1024];
			int count;
			while ((count = in.read(data, 0, 1024)) != -1) {
				fout.write(data, 0, count);
			}
		}
		finally {
			if (in != null) in.close();
			if (fout != null) fout.close();
		}
	}
	
	public int interactiveChoice(String invite, int maxRetries) {

		Scanner scanIn = new Scanner(System.in);
		for(int i=0; i <= maxRetries; i++) {
			System.out.print("\n"+invite+": ");
			String choice = scanIn.nextLine();
			try {
				int choiceInt = Integer.parseInt(choice);
				scanIn.close();
				return choiceInt;
			} catch (Exception e) {
				continue;
			}
		}
	 	scanIn.close();
		return -1;
	}


}
