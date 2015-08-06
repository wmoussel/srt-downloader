package org.moussel.srtdownloader;

import java.io.File;
import java.nio.file.Path;

public interface SubtitleExtractor {

	Path extractTvSubtitle(TvShowEpisodeInfo episode, String langName, File destinationFolder,
			VideoFileInfoImpl videoFile) throws Exception;

	String getServiceName();
}
