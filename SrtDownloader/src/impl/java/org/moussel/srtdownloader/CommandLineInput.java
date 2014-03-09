package org.moussel.srtdownloader;
import java.util.ArrayList;

import org.moussel.srtdownloader.extractor.Addic7edExtractor;

public class CommandLineInput {

	public static void main(String[] args) throws Exception {

		String show = "The_Mentalist";
		String seasonNumber = "6";
		String episodeNumber = "6-12";
	
		for (int n = 0; n < args.length; ++n)
		{
			switch(n) {
				case 0: show = args[n]; break;
				case 1: seasonNumber = args[n]; break;
				case 2: episodeNumber = args[n]; break;
			}
		}
		System.out.println("Starting...");
		int season = Integer.parseInt(seasonNumber);

		ArrayList<EpisodeInfoImpl> episodeList = new ArrayList<EpisodeInfoImpl>();
		if(episodeNumber.contains("-")) {
			String[] eps = episodeNumber.split("-");
			if(eps.length == 2) {
				int epMin = Integer.parseInt(eps[0]);
				int epMax = Integer.parseInt(eps[1]);
				for(int i=epMin; i <= epMax ; i++) {
					episodeList.add(new EpisodeInfoImpl(show,season,i));
				}
			}
		}
		else {
			episodeList.add(new EpisodeInfoImpl(show,season,Integer.parseInt(episodeNumber)));
		}

		SubtitleExtractor extractor = new Addic7edExtractor();
		
		for(EpisodeInfo episode : episodeList) {
			
			extractor.extractSubtitle(episode);
		}
		System.out.println("Done.");

	}

}