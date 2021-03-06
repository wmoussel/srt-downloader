package org.moussel.srtdownloader.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSON.Feature;
import com.fasterxml.jackson.jr.ob.JSONObjectException;

public class SrtDownloaderUtils {

	private static Properties applicationProps;

	private static final String DEFAULT_APPLICATION_PROPERTIES = "srtDownloader.default.properties";

	static final Scanner inputScanner = new Scanner(System.in);

	private static final String PROPERTY_FILE_OVERRIDE = "props";

	static {
		System.out.println("Loading properties");
		applicationProps = new Properties();
		try {
			InputStream stream = null;
			if (System.getProperties().containsKey(PROPERTY_FILE_OVERRIDE)) {
				// Check regular File
				File file = new File(System.getProperty(PROPERTY_FILE_OVERRIDE));
				if (file.exists() && file.isFile() && file.canRead()) {
					stream = new FileInputStream(file);
				} else {
					stream = SrtDownloaderUtils.class.getClassLoader().getResourceAsStream(
							System.getProperty(PROPERTY_FILE_OVERRIDE));
				}
			}
			if (stream == null) {
				stream = SrtDownloaderUtils.class.getClassLoader().getResourceAsStream(DEFAULT_APPLICATION_PROPERTIES);
			}
			if (stream != null) {
				applicationProps.load(stream);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getStringProperty(String name, String defaultValue) {
		if (applicationProps.containsKey(name)) {
			return applicationProps.getProperty(name);
		}
		return defaultValue;
	}

	public static void getUrlContent(URL url, Map<String, String> headers, OutputStream out)
			throws MalformedURLException, IOException {
		BufferedInputStream in = null;
		try {
			System.out.print("URL: " + url);
			URLConnection connection = url.openConnection();
			connection.setUseCaches(false);
			if (headers == null) {
				headers = new LinkedHashMap<String, String>();
			}
			headers.put("Cache-Control", "max-age=0");
			headers.put("User-Agent",
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:40.0) Gecko/20100101 Firefox/40.0");
			for (Map.Entry<String, String> h : headers.entrySet()) {
				connection.setRequestProperty(h.getKey(), h.getValue());
			}

			Map<String, List<String>> respHeaders = connection.getHeaderFields();

			for (Entry<String, List<String>> hEntry : respHeaders.entrySet()) {
				headers.put(hEntry.getKey(), StringUtils.join(hEntry.getValue(), ";"));
			}
			System.out.println(" => [" + StringUtils.join(respHeaders.get(null), ";") + "]");

			in = new BufferedInputStream(connection.getInputStream());

			byte data[] = new byte[1024];
			int count;
			while ((count = in.read(data, 0, 1024)) != -1) {
				out.write(data, 0, count);
			}
		} finally {
			if (in != null)
				in.close();
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getUrlXmlContentAsBean(URL requestUrl, T bean) {
		T beanObject;
		try {

			ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
			SrtDownloaderUtils.getUrlContent(requestUrl, null, responseStream);
			String responseAsString = responseStream.toString();

			try {
				JAXBContext jc = JAXBContext.newInstance(bean.getClass());
				Unmarshaller unmarshaller = jc.createUnmarshaller();
				beanObject = (T) unmarshaller.unmarshal(new StringReader(responseAsString));
				return beanObject;
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Marshaller marshaller = jc.createMarshaller();
			// marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			// marshaller.marshal(api, System.out);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static String jsonString(Object bean) {
		try {
			return JSON.std.with(Feature.PRETTY_PRINT_OUTPUT).without(Feature.WRITE_NULL_PROPERTIES).asString(bean);
		} catch (JSONObjectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static int promtForInt(String invite, int maxRetries) {
		return promtForInt(invite, maxRetries, null);
	}

	public static int promtForInt(String invite, int maxRetries, Integer defaultValue) {

		for (int i = 0; i <= maxRetries; i++) {
			try {
				String choice = promtForString(invite);
				if (defaultValue != null && StringUtils.isBlank(choice)) {
					return defaultValue;
				}
				int choiceInt = Integer.parseInt(choice);
				return choiceInt;
			} catch (Exception e) {
				continue;
			}
		}
		return -1;
	}

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

	public static void zipExtract(Path zipFile, String toBeExtracted, Path extractedPath) throws IOException {
		FileSystem fileSystem = FileSystems.newFileSystem(zipFile, null);
		Path source = fileSystem.getPath(toBeExtracted);
		Files.copy(source, extractedPath, StandardCopyOption.REPLACE_EXISTING);
	}

	public static List<Path> zipList(Path zipFile, String globPattern) throws IOException {
		FileSystem zipfs = FileSystems.newFileSystem(zipFile, null);

		GlobSearch walkAndSearch = new GlobSearch(globPattern);

		Iterable<Path> dirs = zipfs.getRootDirectories();
		/* For every root folder inside the ZIP archive */
		for (Path root : dirs) {
			Files.walkFileTree(root, walkAndSearch);
		}
		return walkAndSearch.getResults();
	}
}
