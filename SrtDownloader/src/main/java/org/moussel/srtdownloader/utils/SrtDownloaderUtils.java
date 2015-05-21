package org.moussel.srtdownloader.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSON.Feature;
import com.fasterxml.jackson.jr.ob.JSONObjectException;

public class SrtDownloaderUtils {

	private static final String APPLICATION_PROPERTIES = "srtDownloader.default.properties";
	private static Properties applicationProps;
	static final Scanner inputScanner = new Scanner(System.in);

	static {

		applicationProps = new Properties();
		try {
			InputStream stream = SrtDownloaderUtils.class.getClassLoader()
					.getResourceAsStream(APPLICATION_PROPERTIES);
			if (stream != null) {
				applicationProps.load(stream);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Connection getDbConnection() throws Exception {
		Connection c = null;
		try {
			c = DriverManager.getConnection("jdbc:sqlite:test.db");
			return c;
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			throw e;
		}
	}

	public static String getStringProperty(String name, String defaultValue) {
		if (applicationProps.containsKey(name)) {
			return applicationProps.getProperty(name);
		}
		return defaultValue;
	}

	public static void getUrlContent(URL url, Map<String, String> headers,
			OutputStream out) throws MalformedURLException, IOException {
		BufferedInputStream in = null;
		try {
			URLConnection connection = url.openConnection();
			if (headers != null) {
				for (Map.Entry<String, String> h : headers.entrySet()) {
					connection.setRequestProperty(h.getKey(), h.getValue());
				}
			}
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
				beanObject = (T) unmarshaller.unmarshal(new StringReader(
						responseAsString));
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
			return JSON.std.with(Feature.PRETTY_PRINT_OUTPUT)
					.without(Feature.WRITE_NULL_PROPERTIES).asString(bean);
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
}
