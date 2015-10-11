package org.moussel.srtdownloader;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public interface SubtitleExtractor {

	Path extractTvSubtitle(TvShowEpisodeInfo episode, String langName, File destinationFolder,
			VideoFileInfoImpl videoFile) throws Exception;

	public abstract List<SubInfo> getAvailableSubtitles(TvShowEpisodeInfo episode, String langName) throws Exception;

	String getServiceName();
}
