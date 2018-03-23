package staindev;

import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.newdawn.slick.Color;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;


public class Main {
	
	// The window handle
	private long window;
	public Map<String, Texture> textures = new HashMap<>();
	public static final int WIDTH = 800, HEIGHT = 600;

	public void run() {
		init();
		loop();

		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	private void init() {
		GLFWErrorCallback.createPrint(System.err).set();
		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

		// Create the window
		window = glfwCreateWindow(WIDTH, HEIGHT, "Stain Game", NULL, NULL);
		if(window == NULL)
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if(key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
		});

		// Get the thread stack and push a new frame
		try(MemoryStack stack = stackPush()) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(
				window,
				(vidmode.width() - pWidth.get(0)) / 2,
				(vidmode.height() - pHeight.get(0)) / 2
			);
		} // the stack frame is popped automatically

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		// Enable v-sync
		glfwSwapInterval(1);
		
//		glEnable(GL_BLEND);
//    	glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
//    	
//    	glViewport(0, 0, WIDTH, HEIGHT);
//		glMatrixMode(GL_MODELVIEW);
//		
//		glMatrixMode(GL_PROJECTION);
//		glLoadIdentity();
//		glOrtho(0, WIDTH, HEIGHT, 0, 1, -1);
//		glMatrixMode(GL_MODELVIEW);

		// Make the window visible
		glfwShowWindow(window);
		
		System.out.println("Loading TEXTURES");
		loadTextures();
	}

	private void loop() {
		// This line is critical for LWJGL's interoperation with GLFW
		GL.createCapabilities();
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		
		// render
		while(!glfwWindowShouldClose(window)) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
			glfwSwapBuffers(window); // swap the color buffers
			glfwPollEvents();
			
			drawQuad(textures.get("staintest"), 1, 1, 64, 64);
		}
	}
	
	public void loadTextures() {
		loadTexture("HAAHAHA", "staintest");
	}
	
	public void loadTexture(String imageName, String key) {
		InputStream in = ResourceLoader.getResourceAsStream("res/" + imageName + ".png");
		Texture texture = null;
		try {
			texture = TextureLoader.getTexture("PNG", in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		textures.put(key, texture);
	}
	
	public void loadTexture(String imageName) {
		loadTexture(imageName, imageName);
	}
	
	public void drawQuad(Texture texture, int x, int y, int width, int height) {
		int w = texture.getTextureWidth();
		int h = texture.getTextureHeight();
		texture.bind();
		glTranslatef(x, y, 0);
		glBegin(GL_QUADS);
			glTexCoord2f(0, 0);
			glVertex2f(0, 0);
			glTexCoord2f(w, 0);
			glVertex2f(width, 0);
			glTexCoord2f(w, h);
			glVertex2f(width, height);
			glTexCoord2f(0, h);
			glVertex2f(0, height);
		glEnd();
		glLoadIdentity();
	}

	public static void main(String[] args) {
		new Main().run();
	}
}
