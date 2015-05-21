package org.moussel.srtdownloader;

import java.io.IOException;
import java.util.Collection;

import org.moussel.srtdownloader.data.TvDbLocalDao;
import org.moussel.srtdownloader.data.TvDbServiceConnector;
import org.moussel.srtdownloader.data.tvdb.bean.TvDbSerieInfo;
import org.moussel.srtdownloader.data.tvdb.bean.TvDbSeriesList;
import org.moussel.srtdownloader.utils.SrtDownloaderUtils;

import asg.cliche.Command;
import asg.cliche.Param;
import asg.cliche.Shell;
import asg.cliche.ShellDependent;
import asg.cliche.ShellFactory;

public class SrtDownloaderShell implements ShellDependent {

	public static void main(String[] params) throws IOException {
		ShellFactory.createConsoleShell("srt-down", "Java Srt Downloader",
				new SrtDownloaderShell()).commandLoop();
	}

	Shell mainShell;

	@Override
	public void cliSetShell(Shell shell) {
		this.mainShell = shell;
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
					System.out.println(" " + (++i) + ") "
							+ tvDbSerieInfo.toString());
				}
				chosen = SrtDownloaderUtils.promtForInt(
						"Please select appropriate result", 2) - 1;
			}
			chosenSerieInfo = svc.getSerieInfo(series.get(chosen).id);
			TvDbLocalDao localDb = TvDbLocalDao.getInstance();
			localDb.addShow(chosenSerieInfo);
			return chosenSerieInfo.toString();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return "ERROR: " + e.getMessage();
		}

	}

	@Command(name = "showlist")
	public String showList() {
		TvDbLocalDao localDb = TvDbLocalDao.getInstance();
		try {
			Collection<TvDbSerieInfo> showList = localDb.getSeriesList();
			return new StringBuilder().append(showList.size())
					.append(" Series found:\n")
					.append(SrtDownloaderUtils.jsonString(showList)).toString();
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return "ERROR: " + e.getMessage();
		}

	}

	@Command(name = "subauto")
	public String subAutoDownload() {
		try {
			AutoDownload.main(null);
			return "Done.";
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return "ERROR: " + e.getMessage();
		}
	}

	@Command(name = "subget")
	public String subMaunalDownload(String serie, int season, String episodes) {
		try {
			CommandLineInput.getSubtitles(serie, season, episodes);
			return "Done.";
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return "ERROR: " + e.getMessage();
		}
	}
}
