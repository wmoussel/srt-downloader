package org.moussel.srtdownloader;

import java.io.File;
import java.nio.file.Path;

public interface SubtitleExtractor {

	Path extractTvSubtitle(TvShowEpisodeInfo episode, File destinationFolder, VideoFileInfoImpl videoFile)
			throws Exception;

}
