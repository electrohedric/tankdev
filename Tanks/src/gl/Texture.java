package gl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.*;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;

import constants.Resources;
import objects.Surface;
import util.FileUtil;
import util.ImageStore;

public class Texture extends Surface {
	
	private int id;
	private int width;
	private int height;
	private float offsetX;
	private float offsetY;
	private float offsetRot;
	private boolean isLoaded;
	private String filename;
	
	private static String localPath = "";
	
	//private ByteBuffer data; // NOTE: we may want to retain this bytebuffer so we can sample pixels if we wish
	
	/**
	 * Creates a Texture which can render to the screen. Handles all behind-the-scenes OpenGL code.
	 * @param name Path relative to <strong>res/textures</strong> to the file containing the texture
	 * @param centerX The center X pixel which the texture will rotate about
	 * @param centerY The center Y pixel which the texture will rotate about
	 * @param quarterTurns The number of <code>PI/2</code> turns in the positive direction (i.e. counter-clockwise) 
	 * 					   which the texture is rotated intially from facing to the right
	 */
	public Texture(String name, float centerX, float centerY, float quarterTurns) {
		super();
		basicInit(name);
		loadImageToGL();
		this.offsetX = width * (centerX -  0.5f);
		this.offsetY = height * (centerY -  0.5f);
		this.offsetRot = (float) (quarterTurns * Math.PI / 2);
	}
	
	public Texture(String name, Anchor anchor) {
		this(name, anchor.getX(), anchor.getY(), 0);
	}

	/**
	 * Creates, but does not load the texture if load is set to <code>false</code>. 
	 * This is recommended when the texture is very large and needs to be loaded and deleted as needed
	 * @param name
	 * @param load
	 */
	public Texture(String name, boolean load) {
		super();
		basicInit(name);
		if(load)
			loadImageToGL();
		this.offsetX = 0; // default offset none = middle
		this.offsetY = 0;
		this.offsetRot = 0;
	}
	
	/**
	 * Creates and loads a texture with default anchor as the center
	 * @param name File name relative to <strong>res/textures</strong>
	 */
	public Texture(String name) {
		this(name, true);
	}
	
	private void basicInit(String name) {
		this.filename = name;
		this.isLoaded = false;
		this.width = 0;
		this.height = 0;
	}
	
	public static void setLocalPath(String path) {
		Texture.localPath = path;
	}
	
	public void loadImageToGL() {
		String fullLocalPath = Resources.TEXTURES_PATH + Texture.localPath;
		ImageStore store = FileUtil.loadImage(fullLocalPath + filename);
		this.width = store.getWidth();
		this.height = store.getHeight();
		
		// cast array to bytes for the ByteBuffer
		int size = store.pixels.length;
		byte[] storeBytes = new byte[size];
		for(int i = 0; i < size; i++)
			storeBytes[i] = (byte) store.pixels[i];
		
		ByteBuffer data = BufferUtils.createByteBuffer(size);
		data.put(storeBytes);
		data.flip(); // openGL wants data starting from bottom left corner
		
		isLoaded = true;
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
		if(isLoaded) {
			glActiveTexture(GL_TEXTURE0 + slot);
			glBindTexture(GL_TEXTURE_2D, id);
		} else
			throw new IllegalStateException("Tried to bind " + filename + " but can't because texture not loaded"); // XXX: for debugging, instead bind a dev null texture
	}
	
	public void bind() {
		bind(0);
	}
	
	public void unbind() {
		glBindTexture(GL_TEXTURE_2D, 0);
	}
	
	public void delete() {
		glDeleteTextures(id);
		isLoaded = false;
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
	
	public float getOffsetRot() {
		return offsetRot;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public static String getLocalPath() {
		return localPath;
	}
	
	public enum Anchor {
		TOP_LEFT, TOP_CENTER, TOP_RIGHT, MIDDLE_LEFT, CENTER, MIDDLE_RIGHT, BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT;
		
		float getX() {
			switch(this) {
			case BOTTOM_CENTER: return 0.5f;
			case BOTTOM_LEFT: return 0;
			case BOTTOM_RIGHT: return 1.0f;
			case CENTER: return 0.5f;
			case MIDDLE_LEFT: return 0;
			case MIDDLE_RIGHT: return 1.0f;
			case TOP_CENTER: return 0.5f;
			case TOP_LEFT: return 0;
			case TOP_RIGHT: return 1.0f;
			default: return 0;
			}
		}
		
		float getY() {
			switch(this) {
			case BOTTOM_CENTER: return 1.0f;
			case BOTTOM_LEFT: return 1.0f;
			case BOTTOM_RIGHT: return 1.0f;
			case CENTER: return 0.5f;
			case MIDDLE_LEFT: return 0.5f;
			case MIDDLE_RIGHT: return 0.5f;
			case TOP_CENTER: return 0;
			case TOP_LEFT: return 0;
			case TOP_RIGHT: return 0;
			default: return 0;
			}
		}
	}
	
}
