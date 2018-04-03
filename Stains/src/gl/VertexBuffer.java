package gl;

import static org.lwjgl.opengl.GL15.*;

public class VertexBuffer {

	private int id;
	
	public VertexBuffer(float[] data) {
		id = glGenBuffers();
		bind();
		glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
	}
	
	public void bind() {
		glBindBuffer(GL_ARRAY_BUFFER, id);
	}
	
	public void unbind() {
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	public void delete() {
		glDeleteBuffers(id);
	}
}
