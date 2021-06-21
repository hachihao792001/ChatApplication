package server;

import java.awt.*;

public class GBCBuilder extends GridBagConstraints {
	// Tham khảo https://horstmann.com/articles/Taming_the_GridBagLayout.html
	private static final long serialVersionUID = 1L;

	public GBCBuilder(int gridx, int gridy) {
		this.gridx = gridx;
		this.gridy = gridy;
	}

	public GBCBuilder setGrid(int gridx, int gridy) {
		this.gridx = gridx;
		this.gridy = gridy;
		return this;
	}

	public GBCBuilder setSpan(int gridwidth, int gridheight) {
		this.gridwidth = gridwidth;
		this.gridheight = gridheight;
		return this;
	}

	public GBCBuilder setFill(int fill) {
		this.fill = fill;
		return this;
	}

	public GBCBuilder setWeight(double weightx, double weighty) {
		this.weightx = weightx;
		this.weighty = weighty;
		return this;
	}

	public GBCBuilder setInsets(int distance) {
		this.insets = new Insets(distance, distance, distance, distance);
		return this;
	}

	public GBCBuilder setInsets(int top, int left, int bottom, int right) {
		this.insets = new Insets(top, left, bottom, right);
		return this;
	}
	
	public GBCBuilder setAnchor(int anchor) {
		this.anchor = anchor;
		return this;
	}

}