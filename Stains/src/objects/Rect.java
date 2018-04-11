package objects;

import gl.IndexBuffer;
import gl.VertexBuffer;
import gl.VertexBufferFormat;
import staindev.Game;

public class Rect {

	private static VertexBufferFormat vboFormat;
	private static float[] positions = {
		    -0.5f, -0.5f, 0.0f, 0.0f,
		     0.5f, -0.5f, 1.0f, 0.0f,
			 0.5f,  0.5f, 1.0f, 1.0f,
		    -0.5f,  0.5f, 0.0f, 1.0f
	};
	private static int indices[] = { 
			0, 1, 2,
			2, 3, 0
	};
	public static IndexBuffer ibo;
	public static VertexBuffer vbo;
	
	public static void init() {
		vboFormat = new VertexBufferFormat();
		vboFormat.pushFloat(2); // x, y
		vboFormat.pushFloat(2); // u, v
		vbo  = new VertexBuffer(positions, vboFormat);
		ibo = new IndexBuffer(indices);
		Game.vao.addBuffer(vbo);
	}
	
	public static void bind() {
		Game.vao.bind();
		ibo.bind();
	}
	
	public static void unbind() {
		Game.vao.unbind();
		ibo.unbind();
	}
}
