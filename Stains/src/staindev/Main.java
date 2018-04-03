package staindev;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import constants.Sizeof;
import helpers.Loader;
import helpers.ShaderPair;

import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

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
		if ( !glfwInit() )
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

		// Create the window
		window = glfwCreateWindow(300, 300, "Here come dat boi!", NULL, NULL);
		if ( window == NULL )
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
		});

		// Get the thread stack and push a new frame
		try ( MemoryStack stack = stackPush() ) {
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
		
		float positions[] = {
			-0.5f, -0.5f,
			 0.5f, -0.5f,
			 0.5f,  0.5f,
			-0.5f,  0.5f,
		};
		
		int indices[] = {
			0, 1, 2,
			2, 3, 0
		};
		
		int vertexBuffer = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
		glBufferData(GL_ARRAY_BUFFER, positions, GL_STATIC_DRAW);
		
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 2, GL_FLOAT, false, Sizeof.FLOAT * 2, NULL);
		
		int indexBuffer = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
		
		ShaderPair triangleShader = Loader.loadShader("triangle");
		int program = createShader(triangleShader);
		glUseProgram(program);
		
		int colorLocation = glGetUniformLocation(program, "u_color");
		assert(colorLocation != -1);
		
		float r = 0.0f;
		
		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		while ( !glfwWindowShouldClose(window) ) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
			
			// RENDER
			glUniform4f(colorLocation, r, 0.4f, 0.3f, 1.0f);
			glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, NULL);
			
			r += 0.01f;
			if(r > 1.0f) r = 0.0f;
			
			glfwSwapBuffers(window); // swap the color buffers
			
			// Poll for window events. The key callback above will only be
			// invoked during this call.
			glfwPollEvents();
		}
		
		glDeleteProgram(program);
	}
	
	private int compileShader(String source, int type) {
		int shader = glCreateShader(type);
		glShaderSource(shader, source);
		glCompileShader(shader);
		int[] result = new int[1];
		glGetShaderiv(shader, GL_COMPILE_STATUS, result);
		if(result[0] == GL_FALSE) { // bad compile
			String log = glGetShaderInfoLog(shader);
			System.err.println("Failed to compile " + (type == GL_VERTEX_SHADER ? "VERTEX" : "FRAGMENT") + " shader.\n");
			System.err.flush();
			System.out.println(log + "\n");
		}
		return shader;
	}
	
	private int createShader(String vertex, String fragment) {
		int program = glCreateProgram();
		
		int vs = compileShader(vertex, GL_VERTEX_SHADER);
		int fs = compileShader(fragment, GL_FRAGMENT_SHADER);
		
		glAttachShader(program, vs);
		glAttachShader(program, fs);
		glLinkProgram(program);
		glValidateProgram(program);
		
		glDeleteShader(vs);
		glDeleteShader(fs);
		
		return program;
	}
	
	private int createShader(ShaderPair shader) {
		return createShader(shader.getVertex(), shader.getFragment());
	}

	public static void main(String[] args) {
		new Main().run();
	}

}