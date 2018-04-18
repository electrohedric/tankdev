package staindev;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import audio.Sounds;
import constants.Mode;
import editor.EditorScreen;
import entities.Entity;
import entities.Player;
import entities.Stain;
import gl.Shader;
import gl.Texture;
import objects.Button;
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
	
	/** list of all GUI elements for the title screen that wish to be updated */
	public static List<Button> titleGUIElements;
	
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
		titleGUIElements = new ArrayList<>();
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
		glClearColor(0.2f, 0.2f, 0.2f, 0.0f);

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

		titleGUIElements.add(new Button(WIDTH * 0.5f, HEIGHT * 0.45f, 1.0f, "titlescreen/play_unpressed.png", "titlescreen/play_pressed.png", Shader.texture, Mode.TITLE, () ->  {
			mode = Mode.PLAY;
			glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // probably doesn't matter
		}));
		titleGUIElements.add(new Button(WIDTH * 0.5f, HEIGHT * 0.55f, 1.0f, "titlescreen/editor_unpressed.png", "titlescreen/editor_pressed.png", Shader.texture, Mode.TITLE, () ->  {
			mode = Mode.EDITOR;
			glClearColor(0.03f, 0.07f, 0.23f, 0.0f);  // blueprint color
		}));
		
		Texture playerTexture = new Texture("player/alive.png", 98, 107, 1);
		Texture ketchupTexture = new Texture("stains/ketchup/alive.png", 33, 25, 1);
		Animation ketchupAnimation = new Animation("stains/ketchup/frame<4>.png", 24, 1, 33, 25, 1);
		
		new Player(500, 500, 0.4f, playerTexture, Shader.texture);
		new Stain(100, 100, 0.8f, ketchupTexture, ketchupAnimation, Shader.texture);
		
		new EditorScreen(); // create singleton
		
		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		double lastSystemTime = glfwGetTime();
		
		while(!glfwWindowShouldClose(window)) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
			
			// UPDATE & RENDER
			
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
				for(Button b : titleGUIElements) {
					b.update();
					b.render();
				}
				break;
			}
			
			glfwSwapBuffers(window); // swap the color buffers (tick) TODO implement fps system. Update every 1/120 seconds, render at vsync
			
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

		Shader.texture.delete();
		Shader.color.delete();
		Sounds.destroy();
	}
	
}