package org.moussel.srtdownloader;

public class SubInfoChoice {

	private final String reason;
	private final SubInfo sub;

	public SubInfoChoice(String reason) {
		this.reason = reason;
		this.sub = null;
	}

	public SubInfoChoice(String reason, SubInfo sub) {
		this.reason = reason;
		this.sub = sub;
	}

	public String getReason() {
		return reason;
	}

	public SubInfo getSub() {
		return sub;
	}

	public boolean hasSub() {
		return sub != null;
	}
}
