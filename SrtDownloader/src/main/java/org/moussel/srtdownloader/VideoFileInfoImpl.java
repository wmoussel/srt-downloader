package org.moussel.srtdownloader;

import java.io.File;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.SystemUtils;

public class VideoFileInfoImpl {

	public static void main(String[] args) {
		new VideoFileInfoImpl("Californication.S07E02.HDTV.x264-2HD.mp4")
				.printDetails();
		new VideoFileInfoImpl(
				"Greys Anatomy - 10x17 - Do You Know .HDTV.LOL.mp4")
				.printDetails();
	}

	String audioDetails;
	String fileExtension;
	String fileName;
	String quality;
	String source;
	Path sourceFile;
	String team;

	TvShowEpisodeInfo tvEpisodeInfo;

	String videoDetails;

	public VideoFileInfoImpl(File sourceFile) {
		this.sourceFile = sourceFile.toPath();
		initFromFileName(sourceFile.getName());
	}

	public VideoFileInfoImpl(String fileName) {
		initFromFileName(fileName);
	}

	public String getQuality() {
		return quality;
	}

	public String getSource() {
		return source;
	}

	public String getTeam() {
		return team;
	}

	public Path getVideoFilePath() {
		return sourceFile;
	}

	private void initFromFileName(String fileName) {
		this.fileName = fileName;
		boolean isTv = resolveTvEpisodeInfo(fileName);
		if (!isTv) {
			resolveMovieInfo(fileName);
		}
	}

	public boolean isMovie() {
		return false;
	}

	public boolean isTvEpisode() {
		return tvEpisodeInfo != null;
	}

	private void parseRelease(String dirtyRelease) {
		if (dirtyRelease.contains("-")) {
			team = dirtyRelease.replaceAll("^.*-", "");
		} else {
			team = dirtyRelease.replaceAll("^.*\\.", "");
		}
	}

	/**
	 * @param fileName
	 * @param seEpisodePattern
	 */
	private boolean parseTvShowFileName(String fileName,
			final Pattern seEpisodePattern) {
		Matcher seMatch = seEpisodePattern.matcher(fileName);
		if (seMatch.find()) {
			String dirtyShow = seMatch.group(1);
			int season = Integer.parseInt(seMatch.group(2));
			int epNum = Integer.parseInt(seMatch.group(3));
			String dirtyRelease = seMatch.group(4);
			fileExtension = seMatch.group(5);

			String cleanShow = dirtyShow.replaceAll("[-. ]+", " ").trim();
			tvEpisodeInfo = new TvShowEpisodeInfoImpl(cleanShow, season, epNum);
			parseRelease(dirtyRelease);
			return true;
		}
		return false;
	}

	public void printDetails() {
		StringBuilder sb = new StringBuilder("Video file details :")
				.append(SystemUtils.LINE_SEPARATOR);
		sb.append("File name: ").append(fileName)
				.append(SystemUtils.LINE_SEPARATOR);
		sb.append("Video Type: ")
				.append(isTvEpisode() ? "TV Show Episode" : isMovie() ? "Movie"
						: "<unknown>").append(SystemUtils.LINE_SEPARATOR);
		sb.append("Video infos: ").append(tvEpisodeInfo)
				.append(SystemUtils.LINE_SEPARATOR);
		sb.append("File extension: ").append(fileExtension)
				.append(SystemUtils.LINE_SEPARATOR);
		sb.append("Video Source: ").append(source)
				.append(SystemUtils.LINE_SEPARATOR);
		sb.append("Video Quality: ").append(quality)
				.append(SystemUtils.LINE_SEPARATOR);
		sb.append("Video tecnical infos: ").append(videoDetails)
				.append(SystemUtils.LINE_SEPARATOR);
		sb.append("Audio tecnical infos: ").append(audioDetails)
				.append(SystemUtils.LINE_SEPARATOR);
		sb.append("Release Team: ").append(team)
				.append(SystemUtils.LINE_SEPARATOR);
		System.out.println(sb.toString());
	}

	private boolean resolveMovieInfo(String fileName) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean resolveTvEpisodeInfo(String fileName) {
		final Pattern xEpisodePattern = Pattern.compile(
				"^(.*)[s .-]+([0-9]+)[xe]([0-9]+)[ .-]+(.*)\\.([^.]{1,4})$",
				Pattern.CASE_INSENSITIVE);
		for (Pattern currentPattern : new Pattern[] { xEpisodePattern }) {
			if (parseTvShowFileName(fileName, currentPattern)) {
				return true;
			}
		}

		return false;
	}
}
