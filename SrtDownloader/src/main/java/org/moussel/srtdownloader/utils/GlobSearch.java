package org.moussel.srtdownloader.utils;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

class GlobSearch implements FileVisitor<Path> {

	private final PathMatcher matcher;
	List<Path> searchResult;

	public GlobSearch(String searchPattern) {
		matcher = FileSystems.getDefault().getPathMatcher("glob:" + searchPattern);
		searchResult = new ArrayList<Path>();
	}

	public List<Path> getResults() {
		return new ArrayList<>(searchResult);
	}

	/* We don't use these, so just override them */
	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	public void resetResult() {
		searchResult = new ArrayList<Path>();

	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		Path my_file = file;
		Path name = my_file.getFileName();
		if (name != null && matcher.matches(name)) {
			// System.out.println("Searched file was found: " + name + " in " +
			// my_file.toRealPath().toString());
			searchResult.add(my_file);
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
		return FileVisitResult.CONTINUE;
	}
}