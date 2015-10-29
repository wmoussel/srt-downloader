package org.moussel.srtdownloader;

import java.util.LinkedHashMap;
import java.util.Map;

public class SubInfo {
	LinkedHashMap<String, String> headers;
	String url;
	Map<String, String> versionInfos;

	public LinkedHashMap<String, String> getHeaders() {
		return headers;
	}

	public String getUrl() {
		return url;
	}

	public Map<String, String> getVersionInfos() {
		return versionInfos;
	}

	public void setHeaders(LinkedHashMap<String, String> headers) {
		this.headers = headers;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setVersionInfos(Map<String, String> versionInfos) {
		this.versionInfos = versionInfos;
	}
}
