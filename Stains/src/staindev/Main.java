package staindev;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;

import java.nio.IntBuffer;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import gl.IndexBuffer;
import gl.Shader;
import gl.VertexArray;
import gl.VertexBuffer;
import gl.VertexBufferFormat;

public class Main {

	// The window handle
	private long window;

	public void run() {
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

	private void init() {
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
		window = glfwCreateWindow(300, 300, "Here come dat boi!", 0, 0);
		if (window == 0)
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed, repeated
		// or released.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
		});

		// Get the thread stack and push a new frame
		try (MemoryStack stack = stackPush()) {
			IntBuffer pWidth = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			// Get the window size passed to glfwCreateWindow
			glfwGetWindowSize(window, pWidth, pHeight);

			// Get the resolution of the primary monitor
			GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

			// Center the window
			glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
		} // the stack frame is popped automatically

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);
	}

	private void loop() {
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();
		System.out.println("OpenGL version " + glGetString(GL_VERSION));

		// Set the clear color
		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		/*
		 * +---+
		 * |   |
		 * +---+
		 */
		float positions[] = { 
			   -0.5f, -0.5f,
				0.5f, -0.5f, 
				0.5f,  0.5f, 
			   -0.5f,  0.5f
		};

		int indices[] = { 
				0, 1, 2,
				2, 3, 0
		};

		VertexArray vao = new VertexArray();
		
		VertexBufferFormat format = new VertexBufferFormat();
		format.pushFloat(2);
		VertexBuffer vbo = new VertexBuffer(positions, format);
		vao.addBuffer(vbo);

		IndexBuffer ibo = new IndexBuffer(indices);

		Shader shader = new Shader("triangle");
		shader.linkUniform("u_color");

		float r = 0.0f;

		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		while (!glfwWindowShouldClose(window)) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

			// RENDER
			shader.bind();
			shader.set("u_color", (float) Math.sin(r), (float) Math.cos(r), 1 - (float) Math.cos(r), 1.0f);

			vao.bind();
			ibo.bind();
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

			r += 0.03f;

			glfwSwapBuffers(window); // swap the color buffers (tick)

			// Poll for window events. The key callback above will only be
			// invoked during this call.
			glfwPollEvents();
		}

		shader.delete();
	}

	

	public static void main(String[] args) {
		new Main().run();
	}

}