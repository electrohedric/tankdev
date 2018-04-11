package objects;

import static org.lwjgl.opengl.GL11.*;

import org.joml.Matrix4f;

import gl.Shader;
import gl.Texture;
import staindev.Game;

// TODO this should probably be abstract
public class GameObject {
	// assuming to be rectangle so we can use the Rect class for our buffers
	
	public float x;
	public float y;
	
	/** In radians **/
	public float rot;
	public float scale;
	
	private Texture texture;
	private int slot;
	private Shader program;
	
	/**
	 * Initializes a Game Object which is specifically only a textured quad
	 * @param x Initial X
	 * @param y Initial Y
	 * @param rot Initial rotation, in radians
	 * @param scale Initial scale from actual size
	 * @param texture Texture to render with
	 * @param program Shader program to render with. Must have the following uniforms: uTexture, u_MVP
	 */
	public GameObject(float x, float y, float rot, float scale, Texture texture, Shader program) {
		this.x = x;
		this.y = y;
		this.rot = rot;
		this.scale = scale;
		this.texture = texture;
		this.slot = 0;
		this.program = program;
	}
	
	public void render() {
		program.bind();
		texture.bind(slot);
		Rect.bind(); // binds the VAO
		program.set("u_Texture", slot);
		Matrix4f proj = new Matrix4f().ortho(0, Game.WIDTH, 0, Game.HEIGHT, -1.0f, 1.0f);
		Matrix4f model = new Matrix4f().translate(x, y, 0).mul(new Matrix4f().scale(texture.getWidth() * scale, texture.getHeight() * scale, 1.0f));
		Matrix4f mvp = proj.mul(model); // M x V x P
		program.set("u_MVP", mvp);
		glDrawElements(GL_TRIANGLES, Rect.ibo.length, GL_UNSIGNED_INT, 0);
	}

	public Texture getTexture() {
		return texture;
	}

	public void setTexture(Texture texture) {
		this.texture = texture;
	}

	public Shader getProgram() {
		return program;
	}
	
}
