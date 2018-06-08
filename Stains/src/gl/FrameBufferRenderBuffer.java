package gl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;

import util.Log;

public class FrameBufferRenderBuffer {

	private int id;
	private int renderBufferId;
	private int width;
	private int height;
	
	public FrameBufferRenderBuffer(int width, int height) {
		this.width = width;
		this.height = height;
		
		renderBufferId = glGenRenderbuffers();
		glBindRenderbuffer(GL_RENDERBUFFER, renderBufferId);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_RGB8, width, height);

		id = glGenFramebuffers();
		bind();
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, renderBufferId);
		if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
			Log.err("FBO-RenderBuffer creation failed");
		
		glBindRenderbuffer(GL_RENDERBUFFER, 0);
		unbind();
	}
	
	public void bind() {
		glBindFramebuffer(GL_FRAMEBUFFER, id);
	}
	
	public void unbind() {
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	public void delete() {
		glDeleteRenderbuffers(renderBufferId);
		glDeleteFramebuffers(id);
	}
	
	public BufferedImage readPixels() {
		int imgsize = width * height;
		int size = imgsize * 3; // width * height * channels;
		ByteBuffer buf = BufferUtils.createByteBuffer(size);
		// read pixels from the fbo
		bind();
		glReadPixels(0, 0, width, height, GL_RGB, GL_UNSIGNED_BYTE, buf);
		
		// copy data stored directly to buffered image data array
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int[] data = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
		for(int x = 0; x < width; x++) {
		    for(int y = 0; y < height; y++) {
		        int i = (x + (width * y)) * 3;
		        int r = buf.get(i) & 0xFF;
		        int g = buf.get(i + 1) & 0xFF;
		        int b = buf.get(i + 2) & 0xFF;
		        data[(height - (y + 1)) * width + x] = (0xFF << 24) | (r << 16) | (g << 8) | b; // directly set image data
		    }
		}
		return img;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
}
