package gl;

import static org.lwjgl.opengl.GL11.*;

public class Renderer {
	
	// TODO will take in an Object to draw, this could be an entity, or a square, really anything.
	// An Object will have a Texture (Shader + qualities + uniforms) and a Shape (VBO / IBO) which will eventually be loaded via file
	public static void draw() { 
		
	}
	
	public static void setClearColor(int r, int g, int b) {
		glClearColor(r / 255.0f, g / 255.0f, b / 255.0f, 0); // probably doesn't matter
	}
}
