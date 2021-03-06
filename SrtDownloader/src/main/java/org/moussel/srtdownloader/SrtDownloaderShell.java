package org.moussel.srtdownloader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.moussel.srtdownloader.data.TvDbLocalDao;
import org.moussel.srtdownloader.data.TvDbServiceConnector;
import org.moussel.srtdownloader.data.tvdb.bean.TvDbSerieInfo;
import org.moussel.srtdownloader.data.tvdb.bean.TvDbSeriesList;
import org.moussel.srtdownloader.extractor.Addic7edInteractiveExtractor;
import org.moussel.srtdownloader.extractor.SubSynchroTvExtractor;
import org.moussel.srtdownloader.utils.SrtDownloaderUtils;

import asg.cliche.Command;
import asg.cliche.Param;
import asg.cliche.Shell;
import asg.cliche.ShellDependent;
import asg.cliche.ShellFactory;

public class SrtDownloaderShell implements ShellDependent {

	public static void main(String[] params) throws IOException {
		ShellFactory.createConsoleShell("srt-down", "Java Srt Downloader", new SrtDownloaderShell()).commandLoop();
	}

	private String currentLangName = null;
	Shell mainShell;

	@Override
	public void cliSetShell(Shell shell) {
		this.mainShell = shell;
	}

	@Command(name = "lang")
	public String getCurrentLangName() {
		if (currentLangName == null) {
			return AutoDownload.getDefaultLanguage();
		} else {
			return currentLangName;
		}
	}

	@Command(name = "lang")
	public void setLangName(@Param(name = "langName") String langName) {
		currentLangName = langName;
	}

	@Command(name = "showadd")
	public String showAdd(@Param(name = "showName") String showName) {
		TvDbServiceConnector svc = new TvDbServiceConnector();
		try {
			TvDbSeriesList series = svc.getSeriesList(showName);
			int chosen = 0;
			TvDbSerieInfo chosenSerieInfo;
			if (series != null && series.size() > 1) {
				System.out.println("Multiple Series found: ");
				int i = 0;
				for (TvDbSerieInfo tvDbSerieInfo : series) {
					System.out.println(" " + (++i) + ") " + tvDbSerieInfo.toString());
				}
				chosen = SrtDownloaderUtils.promtForInt("Please select appropriate result", 2) - 1;
			}
			chosenSerieInfo = svc.getSerieInfo(series.get(chosen).id);
			chosenSerieInfo.getAlternativeNames().add(showName);
			String cleanedName = showName.replaceAll("[-. ]+", " ");
			if (!cleanedName.equals(showName)) {
				chosenSerieInfo.getAlternativeNames().add(cleanedName);
			}
			TvDbLocalDao localDb = TvDbLocalDao.getInstance();
			localDb.addShow(chosenSerieInfo);
			return chosenSerieInfo.toString();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return "ERROR: " + e.getMessage();
		}

	}

	@Command(name = "showalias")
	public String showAddAlias(@Param(name = "fullName") String show, @Param(name = "alias") String alias) {
		TvDbLocalDao localDb = TvDbLocalDao.getInstance();
		try {
			TvDbSerieInfo serieInfo = localDb.getSerieByName(show);
			if (alias.startsWith("-")) {
				serieInfo.getAlternativeNames().remove(alias);
			} else {
				serieInfo.getAlternativeNames().add(alias);
			}
			localDb.updateShow(serieInfo);
			return "Alternative names are now: " + StringUtils.join(serieInfo.getAlternativeNames(), ",");
		} catch (Exception e) {
			return "An error occured: " + e.getMessage();
		}

	}

	@Command(name = "showlist")
	public String showList() {
		TvDbLocalDao localDb = TvDbLocalDao.getInstance();
		try {
			Collection<TvDbSerieInfo> showList = localDb.getSeriesList();
			return new StringBuilder().append(showList.size()).append(" Series found:\n")
					.append(SrtDownloaderUtils.jsonString(showList)).toString();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return "ERROR: " + e.getMessage();
		}

	}

	@Command(name = "showrefresh")
	public String showRefreshList() {
		TvDbServiceConnector svc = new TvDbServiceConnector();
		TvDbLocalDao localDb = TvDbLocalDao.getInstance();
		List<TvDbSerieInfo> updatedShows = new ArrayList<>();
		try {
			Collection<TvDbSerieInfo> showList = localDb.getSeriesList();
			for (TvDbSerieInfo show : showList) {
				TvDbSerieInfo refreshedInfo = svc.getSerieInfo(show.id);
				if (show.updateInfos(refreshedInfo)) {
					updatedShows.add(show);
				}
			}
			if (!updatedShows.isEmpty()) {
				localDb.persist();
			}
			return new StringBuilder().append(updatedShows.size()).append("/").append(showList.size())
					.append(" Series updated:\n").append(SrtDownloaderUtils.jsonString(updatedShows)).toString();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return "ERROR: " + e.getMessage();
		}

	}

	@Command(name = "subauto")
	public String subAutoDownload() {
		try {
			AutoDownload.lauchDownload(null, getCurrentLangName());
			return "Done.";
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return "ERROR: " + e.getMessage();
		}
	}

	@Command(name = "subauto")
	public String subAutoDownload(@Param(name = "folder") String folder) {
		try {
			AutoDownload.lauchDownload(new String[] { folder }, getCurrentLangName());
			return "Done.";
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return "ERROR: " + e.getMessage();
		}
	}

	@Command(name = "subget")
	public String subDownloadForFile(@Param(name = "videoFilePath") String videoFile) {
		try {
			File file = new File(videoFile);
			VideoFileInfoImpl fileInfo = new VideoFileInfoImpl(file);
			try {
				Path subPath = new Addic7edInteractiveExtractor().extractTvSubtitle(fileInfo.tvEpisodeInfo,
						getCurrentLangName(), file.getParentFile(), fileInfo);
				System.out.println("File downloaded at: " + subPath.toString());

			} catch (Exception e) {
				e.printStackTrace();
			}
			return "Done.";
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return "ERROR: " + e.getMessage();
		}
	}

	@Command(name = "ssget")
	public String subMaunalDownload(@Param(name = "showName") String serie, @Param(name = "season") int season,
			@Param(name = "episode") int episode) {
		try {
			SubSynchroTvExtractor extractor = new SubSynchroTvExtractor();
			TvShowEpisodeInfo ep = new TvShowEpisodeInfoImpl(serie, season, episode);
			extractor.extractTvSubtitle(ep, getCurrentLangName(), new File("/tmp/"), null);
			return "Done.";
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return "ERROR: " + e.getMessage();
		}
	}

	@Command(name = "subget")
	public String subMaunalDownload(@Param(name = "showName") String serie, @Param(name = "season") int season,
			@Param(name = "episode(s)") String episodes) {
		try {
			CommandLineInput.setCurrentLangName(getCurrentLangName());
			CommandLineInput.getSubtitles(serie, season, episodes);
			return "Done.";
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return "ERROR: " + e.getMessage();
		}
	}
}
