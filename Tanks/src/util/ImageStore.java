package util;

import java.awt.image.BufferedImage;

public class ImageStore {

	private int width;
	private int height;
	public int[] pixels;
	
	public ImageStore(BufferedImage image) {
		this.width = image.getWidth();
		this.height = image.getHeight();
		this.pixels = new int[width * height * 4];  // width * height * channels
		image.getData().getPixels(0, 0, width, height, pixels);  // store data from pixels into int array
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
	/**
	 * @return red of a pixel at x, y
	 */
	public int getR(int x, int y) {
		return pixels[(y * width + x) * 4 + 0];
	}
	
	/**
	 * @return green of a pixel at x, y
	 */
	public int getG(int x, int y) {
		return pixels[(y * width + x) * 4 + 1];
	}

	/**
	 * @return blue of a pixel at x, y
	 */
	public int getB(int x, int y) {
		return pixels[(y * width + x) * 4 + 2];
	}
	
	/**
	 * @return alpha of a pixel at x, y
	 */
	public int getA(int x, int y) {
		return pixels[(y * width + x) * 4 + 3];
	}
	
	/**
	 * @return lightness of a pixel at x, y (average of the color bands)
	 */
	public int getLight(int x, int y) {
		int r = getR(x, y);
		int g = getG(x, y);
		int b = getB(x, y);
		return (r + g + b) / 3;
	}

}
