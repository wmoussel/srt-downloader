package org.moussel.srtdownloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.moussel.srtdownloader.data.TvDbLocalDao;
import org.moussel.srtdownloader.extractor.Addic7edExtractor;
import org.moussel.srtdownloader.utils.SrtDownloaderUtils;

public class AutoDownload {

	private class counter {
		int foundSub = 0;
		int withoutSubBefore = 0;
		int withSub = 0;

		@Override
		public String toString() {
			return String.format(
					"Total video files found: %d\nFiles with subtitles already: %d\nNewly found subtitles: %d/%d",
					(withoutSubBefore + withSub), withSub, foundSub, withoutSubBefore);
		}
	}

	public static String getDefaultLanguage() {
		return Locale.getDefault().getLanguage();
	}

	private static List<String> getScanFolders() {
		String folderInConf = "SCAN_FOLDERS";
		TvDbLocalDao jsonDb = TvDbLocalDao.getInstance();
		Map<String, Object> conf = jsonDb.getConfiguration("AutoDownload");
		if (conf == null) {
			conf = new HashMap<>();
		}
		if (conf == null || !conf.containsKey(folderInConf)) {
			String path = SrtDownloaderUtils.promtForString("Please setup folders to scan by giving path");
			conf.put(folderInConf, Arrays.asList(new String[] { path }));
			jsonDb.setConfiguration("AutoDownload", conf);
		}
		return (List<String>) conf.get(folderInConf);
	}

	public static Path getSubtitlePath(Path moviePath, String langName) {
		String fileName = moviePath.getFileName().toString();
		String fileBaseName = fileName.replaceFirst("\\.[^.]{2,4}$", "");
		String subtitleFileName = fileBaseName + "." + langName + ".srt";
		return moviePath.getParent().resolve(subtitleFileName);
	}

	public static void lauchDownload(String[] foldersToScan, String langName) throws IOException {
		AutoDownload downloader = new AutoDownload(langName);
		List<String> foldersToScanList = new ArrayList<String>();
		if (foldersToScan == null || foldersToScan.length == 0) {
			foldersToScanList = getScanFolders();
		} else {
			foldersToScanList.addAll(Arrays.asList(foldersToScan));
		}
		for (String folder : foldersToScanList) {
			downloader.autoDownload(folder, "**.{avi,mp4,mkv}");
		}
	}

	public static void main(String[] args) throws IOException {
		String langName = System.getProperty("lang", getDefaultLanguage());
		lauchDownload(args, langName);
	}

	String currentLanguage = null;

	public AutoDownload(String langName) {
		currentLanguage = langName;
	}

	public void autoDownload(String folder, String glob) throws IOException {
		final SubtitleExtractor extractor = new Addic7edExtractor();
		final counter count = new counter();
		System.out.println("\nAutoDownload for folder: " + folder);
		try (Stream<Path> stream = Files.walk(Paths.get(folder))) {
			stream.filter(new Predicate<Path>() {
				@Override
				public boolean test(Path p) {
					String fn = p.getFileName().toString();
					return Files.isRegularFile(p)
							&& (fn.endsWith(".mkv") || fn.endsWith(".mp4") || fn.endsWith(".avi"));
				}
			}).forEach(new Consumer<Path>() {
				@Override
				public void accept(Path p) {
					Path srtPath = getSubtitlePath(p, AutoDownload.this.currentLanguage);
					if (!srtPath.toFile().exists()) {
						count.withoutSubBefore++;
						// Download it
						System.out.println("Subtitle missing for movie " + p.getFileName());
						VideoFileInfoImpl fileInfo = new VideoFileInfoImpl(p.toFile());
						try {
							Path subPath = extractor.extractTvSubtitle(fileInfo.tvEpisodeInfo, getCurrentLanguage(), p
									.getParent().toFile(), fileInfo);
							if (subPath != null) {
								count.foundSub++;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						count.withSub++;
						// Debug
						// System.out.println("Subtitle already there for movie "
						// + p.getFileName());
					}
				}

			});

			System.out.println(count);
		}
	}

	public String getCurrentLanguage() {
		if (currentLanguage == null) {
			currentLanguage = getDefaultLanguage();
		}
		return currentLanguage;
	}

	public void setCurrentLanguage(String currentLanguage) {
		this.currentLanguage = currentLanguage;
	}
}
