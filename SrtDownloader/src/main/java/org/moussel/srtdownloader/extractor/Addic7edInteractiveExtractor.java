package org.moussel.srtdownloader.extractor;

import java.util.List;

import org.moussel.srtdownloader.SubtitleExtractor;
import org.moussel.srtdownloader.VideoFileInfoImpl;
import org.moussel.srtdownloader.utils.SrtDownloaderUtils;

public class Addic7edInteractiveExtractor extends Addic7edExtractor implements SubtitleExtractor {

	public Addic7edInteractiveExtractor() {
		super();
	}

	@Override
	protected SubInfo chooseSub(List<SubInfo> subInfos, VideoFileInfoImpl videoFile) {
		SubInfo defaultChoice = super.chooseSub(subInfos, videoFile);
		int defaultChoiceIndex = subInfos.indexOf(defaultChoice) + 1;
		int choice = 0;
		choice = SrtDownloaderUtils.promtForInt("Please choose File to download (1-" + subInfos.size() + ") [default: "
				+ defaultChoiceIndex + "]", 3, defaultChoiceIndex);
		if (choice >= 1 && choice <= subInfos.size()) {
			return subInfos.get(choice - 1);
		} else {
			System.out.println("Invalid choice. Aborting.");
		}
		return null;
	}

	@Override
	public String getServiceName() {
		return "Addic7edInteractive";
	}
}
