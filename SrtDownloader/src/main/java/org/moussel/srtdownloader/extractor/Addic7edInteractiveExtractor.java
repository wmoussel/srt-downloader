package org.moussel.srtdownloader.extractor;

import java.util.List;

import org.moussel.srtdownloader.SubtitleExtractor;
import org.moussel.srtdownloader.VideoFileInfoImpl;
import org.moussel.srtdownloader.utils.SrtDownloaderUtils;

public class Addic7edInteractiveExtractor extends Addic7edExtractor implements
		SubtitleExtractor {

	@Override
	SubInfo chooseSub(List<SubInfo> subInfos, VideoFileInfoImpl videoFile) {
		int choice = 1;
		if (subInfos.size() > 1) {
			choice = SrtDownloaderUtils.promtForInt(
					"Please choose File to download (1-" + subInfos.size()
							+ ")", 3);
		}
		if (choice >= 1 && choice <= subInfos.size()) {
			return subInfos.get(choice - 1);
		} else {
			System.out.println("Invalid choice. Aborting.");
		}
		return null;
	}
}
