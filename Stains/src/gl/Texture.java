package gl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import constants.Resources;

public class Texture {
	
	private int id;
	private int width;
	private int height;
	private float offsetX;
	private float offsetY;
	//private ByteBuffer data; // NOTE: we may want to retain this bytebuffer so we can sample pixels if we wish
	
	public Texture(String name, int centerX, int centerY) {
		loadImageToGL(name);
		this.offsetX = centerX / 2.0f - width / 4.0f;
		this.offsetY = centerY / 2.0f - height / 4.0f;
	}
	
	public Texture(String name) {
		loadImageToGL(name);
		offsetX = 0;
		offsetY = 0;
	}
	
	public void loadImageToGL(String name) {
		BufferedImage image = null;
		try {
			image = ImageIO.read(new File(Resources.IMAGES_PATH + name));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		width = image.getWidth();
		height = image.getHeight();
		int size = width * height * 4; // width * height * channels
		int[] store = new int[size];
		image.getData().getPixels(0, 0, image.getWidth(), image.getHeight(), store);
		
		// cast array to bytes for the ByteBuffer
		byte[] storeBytes = new byte[size];
		for(int i = 0; i < size; i++)
			storeBytes[i] = (byte) store[i];
		
		ByteBuffer data = ByteBuffer.allocateDirect(size);
		data.put(storeBytes);
		data.flip(); // openGL wants data starting from bottom left corner
		
		id = glGenTextures();
		bind();
		
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
		unbind();
	}
	
	public void bind(int slot) {
		glActiveTexture(GL_TEXTURE0 + slot);
		glBindTexture(GL_TEXTURE_2D, id);
	}
	
	public void bind() {
		bind(0);
	}
	
	public void unbind() {
		glBindTexture(GL_TEXTURE_2D, 0);
	}
	
	public void delete() {
		glDeleteTextures(id);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public float getOffsetX() {
		return offsetX;
	}

	public float getOffsetY() {
		return offsetY;
	}
	
}
