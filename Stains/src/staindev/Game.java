package staindev;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import org.joml.Matrix4f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import constants.Mode;
import constants.Shaders;
import constants.Sounds;
import constants.Textures;
import entities.Entity;
import entities.Player;
import entities.Stain;
import gl.Renderer;
import guis.EditorScreen;
import guis.TitleScreen;
import objects.Line;
import objects.Rect;
import util.Animation;
import util.ClickListener;
import util.Mouse;

public class Game {

	// The window handle
	public static long window;
	private static String TITLE = "Stain Game";
	public static int WIDTH;
	public static int HEIGHT;
	public static float delta = 0.0f;
	public static Mode mode = Mode.TITLE;
	public static Matrix4f proj = new Matrix4f(); // can't instantiate until WIDTH and HEIGHT are set
	
	public static void main(String[] args) {
		System.out.println("LWJGL version " + Version.getVersion());

		init();
		loop();

		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	private static void init() {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

		// Create the window
		GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		WIDTH = vidMode.width();
		HEIGHT = vidMode.height();
		window = glfwCreateWindow(WIDTH, HEIGHT, TITLE, glfwGetPrimaryMonitor(), 0);
		if (window == 0)
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed, repeated
		// or released. This will be for events such as things that happen once, other
		// keys will be recognized with glfwGetKey
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if(key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
				glfwSetWindowShouldClose(window, true);
			}
		});
		glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
			if(action == GLFW_PRESS)
				for(ClickListener listener : ClickListener.getCallbackList(mode))
					listener.handleClick(button);
			else if(action == GLFW_RELEASE)
				for(ClickListener listener : ClickListener.getCallbackList(mode))
					listener.handleRelease(button);
		});

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		// Disable v-sync. Run as fast as possible NO. Enable VSync Plz. This is how you destroy your GPU and CPU in one fell swoop
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);
		
		// init buffers and things
		
		// This color is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();
		System.out.println("OpenGL version " + glGetString(GL_VERSION));
		
		Sounds.init();
		Rect.init(); // using rectangle, so let's initialize it
		Line.init();
		proj = new Matrix4f().ortho(0, Game.WIDTH, 0, Game.HEIGHT, -1.0f, 1.0f);
	}
	
	public static void checkError() {
		int error = glGetError();
		boolean errorOccured = false;
		while(error != 0) {
			System.out.println(error);
			error = glGetError();
			errorOccured = true;
		}
		if(errorOccured) {
			System.out.println("-----");
		}
	}
	
	public static void clearError() {
		while(glGetError() != 0) {} // just loop until error is 0
	}

	private static void loop() {
		// Set the clear color
		Renderer.setClearColor(0, 0, 0);

		/*
		 * +---+
		 * |   |
		 * +---+
		 * x, y, u, v
		 */
		
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glEnable(GL_LINE_SMOOTH);
		glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
		
		// TODO buttons size should be relative to the screen width and height, not the button so buttons can be displayed the same on all screens
		
		
		new Player(500, 500, 0.4f, Textures.PLAYER);
		new Stain(100, 100, 0.8f, Textures.KETCHUP_ALIVE, Textures.KETCHUP_DEATH);
		
		 // create singletons
		new TitleScreen();
		new EditorScreen();
		
		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		double lastSystemTime = glfwGetTime();
		
		while(!glfwWindowShouldClose(window)) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
			
			// UPDATE & RENDER TODO [Dependency: split up updating and rendering] implement fps system. Update every 1/120 seconds, render at vsync
			
			switch(mode) {
			case EDITOR:
				EditorScreen.instance.update();
				EditorScreen.instance.render();
				break;
			case JANITOR:
				
				break;
			case PAUSED:
				
				break;
			case PLAY:
				for(int i = Animation.queue.size() - 1; i >= 0; i--) // have to use regular old loop to avoid ConcurrentModificationException
					Animation.queue.get(i).update();
				
				for(Entity e : Entity.list) {
					e.update();
					e.render();
				}
				
				for(int i = Entity.list.size() - 1; i >= 0; i--)
					if(Entity.list.get(i).isDead())
						Entity.list.remove(i);
				break;
			case TITLE:
				TitleScreen.instance.update();
				TitleScreen.instance.render();
				break;
			}
			
			glfwSwapBuffers(window); // swap the color buffers (tick)
			
			Mouse.getUpdate();
			// Poll for window events. The key callback above will only be
			// invoked during this call.
			glfwPollEvents();
			checkError();
			
			double currentSystemTime = glfwGetTime();
			delta = (float) (currentSystemTime - lastSystemTime);
			if(delta > 1.0f) delta = 0; // if delta is way too large (e.g. Game was paused) don't process a million frames, please
			lastSystemTime = currentSystemTime;
		}
		
		Textures.destroy();
		Shaders.destroy();
		Sounds.destroy();
	}
	
}