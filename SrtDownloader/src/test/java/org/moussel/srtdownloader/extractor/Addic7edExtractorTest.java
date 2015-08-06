/**
 * 
 */
package org.moussel.srtdownloader.extractor;

import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.moussel.srtdownloader.utils.SrtDownloaderUtils;

/**
 * @author wandrillemoussel
 * 
 */
public class Addic7edExtractorTest extends Addic7edExtractor {

	@BeforeClass
	public static void setUpBeforeClass() {
		System.setProperty("props", "srtDownloader.test.properties");
		SrtDownloaderUtils.promtForString("n/a");
	}

	/**
	 * Test method for
	 * {@link org.moussel.srtdownloader.extractor.Addic7edExtractor#chooseSub(java.util.List, org.moussel.srtdownloader.VideoFileInfoImpl)}
	 * .
	 */
	@Test
	public void testChooseSub() {
	}

	/**
	 * Test method for
	 * {@link org.moussel.srtdownloader.extractor.Addic7edExtractor#getAvailableSubtitles(org.moussel.srtdownloader.TvShowEpisodeInfo)}
	 * .
	 */
	@Test
	public void testGetAvailableSubtitles() {
	}

	/**
	 * Test method for
	 * {@link org.moussel.srtdownloader.extractor.Addic7edExtractor#getHtmlUrl(java.lang.String)}
	 * .
	 */
	@Test
	public void testGetHtmlUrl() {
	}

	/**
	 * Test method for
	 * {@link org.moussel.srtdownloader.extractor.Addic7edExtractor#getPreferredShowName(org.moussel.srtdownloader.TvShowInfo)}
	 * .
	 */
	@Test
	public void testGetPreferredShowName() {
	}

	/**
	 * Test method for
	 * {@link org.moussel.srtdownloader.extractor.Addic7edExtractor#getAndSaveLanguageCodeFor(java.lang.String)}
	 * .
	 * 
	 * @throws UnsupportedEncodingException
	 */
	@Test
	public void testSetDefaultSubtitleLanguage() throws UnsupportedEncodingException {
		String[] langToTest = new String[] { "fr", "en", "de", "es", "it", "pt" };
		String[] expectedTest = new String[] { "8", "1", "11", "4", "7", "9" };

		for (int i = 0; i < langToTest.length; i++) {
			String lang = langToTest[i];
			String exp = expectedTest[i];
			String langCode = getLanguageCode(lang);
			assertTrue("Couldn't get correct language code for " + lang, exp.equals(langCode));
		}
	}

}
