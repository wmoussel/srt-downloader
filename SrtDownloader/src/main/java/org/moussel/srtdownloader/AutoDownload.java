package org.moussel.srtdownloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.moussel.srtdownloader.extractor.Addic7edExtractor;

public class AutoDownload {

	public static String getDefaultLanguage() {
		return Locale.getDefault().getLanguage();
	}

	public static Path getSubtitlePath(Path moviePath) {
		String fileName = moviePath.getFileName().toString();
		String fileBaseName = fileName.replaceFirst("\\.[^.]{2,4}$", "");
		String subtitleFileName = fileBaseName + "." + getDefaultLanguage()
				+ ".srt";
		return moviePath.getParent().resolve(subtitleFileName);
	}

	public static void main(String[] args) throws IOException {
		new AutoDownload().autoDownload(
				"/Users/wandrillemoussel/Movies/TV Shows/", "**.{mp4,mkv}");
		new AutoDownload().autoDownload(
				"/Volumes/Public/Shared Videos/TV_Shows/", "**.{mp4,mkv}");
	}

	public void autoDownload(String folder, String glob) throws IOException {
		final SubtitleExtractor extractor = new Addic7edExtractor();
		try (Stream<Path> stream = Files.walk(Paths.get(folder))) {
			stream.filter(new Predicate<Path>() {
				@Override
				public boolean test(Path p) {
					String fn = p.getFileName().toString();
					return Files.isRegularFile(p)
							&& (fn.endsWith(".mkv") || fn.endsWith(".mp4"));
				}
			}).forEach(new Consumer<Path>() {
				@Override
				public void accept(Path p) {
					Path srtPath = getSubtitlePath(p);
					if (!srtPath.toFile().exists()) {
						// Download it
						System.out.println("Subtitle missing for movie "
								+ p.getFileName());
						VideoFileInfoImpl fileInfo = new VideoFileInfoImpl(p
								.toFile());
						try {
							extractor.extractTvSubtitle(fileInfo.tvEpisodeInfo,
									p.getParent().toFile(), fileInfo);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						System.out.println("Subtitle already there for movie "
								+ p.getFileName());
					}
				}

			});
		}
	}
}
