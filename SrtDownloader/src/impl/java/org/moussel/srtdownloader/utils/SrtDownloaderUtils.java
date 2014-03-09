package org.moussel.srtdownloader.utils;

import java.util.Scanner;

public class SrtDownloaderUtils {

	static final Scanner inputScanner = new Scanner(System.in);

	public static String promtForString(String invite) {

		System.out.print("\n" + invite + ": ");
		try {
			String choice = inputScanner.nextLine();
			return choice;
		} catch (Exception e) {
		} finally {
		}
		return null;
	}

	public static int promtForInt(String invite, int maxRetries) {

		for (int i = 0; i <= maxRetries; i++) {
			try {
				String choice = promtForString(invite);
				int choiceInt = Integer.parseInt(choice);
				return choiceInt;
			} catch (Exception e) {
				continue;
			}
		}
		return -1;
	}

}
