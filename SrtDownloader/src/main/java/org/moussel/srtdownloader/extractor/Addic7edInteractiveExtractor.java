package org.moussel.srtdownloader.extractor;

import java.util.List;

import org.moussel.srtdownloader.SubInfo;
import org.moussel.srtdownloader.SubInfoChoice;
import org.moussel.srtdownloader.SubtitleExtractor;
import org.moussel.srtdownloader.VideoFileInfoImpl;
import org.moussel.srtdownloader.utils.SrtDownloaderUtils;

public class Addic7edInteractiveExtractor extends Addic7edExtractor implements SubtitleExtractor {

	public Addic7edInteractiveExtractor() {
		super();
	}

	@Override
	protected SubInfoChoice chooseSub(List<SubInfo> subInfos, VideoFileInfoImpl videoFile) {
		SubInfoChoice defaultChoice = super.chooseSub(subInfos, videoFile);
		int defaultChoiceIndex = subInfos.indexOf(defaultChoice.getSub()) + 1;
		int choice = 0;
		choice = SrtDownloaderUtils.promtForInt("Please choose File to download (1-" + subInfos.size() + ") [default: "
				+ defaultChoiceIndex + ": " + defaultChoice.getReason() + "]", 3, defaultChoiceIndex);
		if (choice >= 1 && choice <= subInfos.size()) {
			// Valid choice
			if (choice == defaultChoiceIndex) {
				return defaultChoice;
			} else {
				return new SubInfoChoice("User choice", subInfos.get(choice - 1));
			}
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
