package objects;

import static org.lwjgl.opengl.GL11.*;

import org.joml.Matrix4f;

import gl.Shader;
import staindev.Game;

// TODO this should probably be abstract eventually
public class GameObject {
	// assuming to be rectangle so we can use the Rect class for our buffers
	
	public float x;
	public float y;
	
	/** In radians **/
	public float rot;
	public float scale;
	
	private int slot;
	private Shader program;
	private Surface activeTexture;
	
	/**
	 * Initializes a Game Object which is specifically only a textured quad
	 * @param x Initial X
	 * @param y Initial Y
	 * @param rot Initial rotation, in radians
	 * @param scale Initial scale from actual size
	 * @param program {@link Shader} program to use when rendering. Must have the following uniforms: uTexture, u_MVP
	 */
	public GameObject(float x, float y, float rot, float scale, Shader program) {
		this.x = x;
		this.y = y;
		this.rot = rot;
		this.scale = scale;
		this.slot = 0;
		this.program = program;
		this.activeTexture = null;
	}
	
	/** 
	 * Renders the <code>activeTexture</code> to the screen using its properties and this <code>GameObject</code>'s position.
	 * <code>null</code> is a valid <code>activeTexture</code> which renders nothing.
	 * */
	public void render() { // TODO optimize this so we aren't creating a crap load of matrices every frame. hint: look at Wall
		if(activeTexture != null) {
			program.bind();
			activeTexture.bind(slot);
			Rect.bind(); // binds the VAO
			program.set("u_Texture", slot);
			Matrix4f proj = new Matrix4f().ortho(0, Game.WIDTH, 0, Game.HEIGHT, -1.0f, 1.0f);
			Matrix4f model = new Matrix4f().translate(x, y, 0).mul( // translate to position
							 new Matrix4f().rotate(rot - activeTexture.getOffsetRot(), 0.0f, 0.0f, 1.0f)).mul( // rotate about new center
							 new Matrix4f().scale(scale, scale, 1.0f)).mul( // rescale to desired size
							 new Matrix4f().translate(-activeTexture.getOffsetX(), activeTexture.getOffsetY(), 0)).mul( // translate to offset
							 new Matrix4f().scale(activeTexture.getWidth(), activeTexture.getHeight(), 1.0f)); // scale to actual size
			Matrix4f mvp = proj.mul(model); // M x V x P
			program.set("u_MVP", mvp);
			glDrawElements(GL_TRIANGLES, Rect.ibo.length, GL_UNSIGNED_INT, 0);
		}
	}

	public void setActiveTexture(Surface activeTexture) {
		this.activeTexture = activeTexture;
	}
	
}
