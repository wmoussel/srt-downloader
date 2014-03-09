package org.moussel.srtdownloader;

public class ShowInfoImpl implements ShowInfo {

	String name;
	
	public ShowInfoImpl() {
	}
	
	public ShowInfoImpl(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see org.moussel.srtdownloader.ShowInfo#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.moussel.srtdownloader.ShowInfo#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}
}
