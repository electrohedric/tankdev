package objects;

import static org.lwjgl.opengl.GL11.*;

import org.joml.Matrix4f;

import constants.Shaders;
import gl.Shader;
import staindev.Game;
import util.Camera;

// TODO this should probably be abstract eventually
public class GameObject {
	// assuming to be rectangle so we can use the Rect class for our buffers
	
	public float x;
	public float y;
	
	/** In radians **/
	public float rot;
	public float scale;
	/** changes brightness of texture. 0 is normal. 1 is fully white. -1 is fully black */
	public float brightScale;
	
	private int slot;
	private Shader program;
	private Surface activeTexture;
	private Camera camera;
	
	// Preallocations
	private Matrix4f proj = new Matrix4f();
	private Matrix4f model = new Matrix4f();
	private Matrix4f mvp = new Matrix4f();
	
	/**
	 * Initializes a Game Object which is specifically only a textured quad
	 * @param x Initial X
	 * @param y Initial Y
	 * @param rot Initial rotation, in radians
	 * @param scale Initial scale from actual size
	 * @param camera {@link Camera} to which the object is rendered with respect to
	 */
	public GameObject(float x, float y, float rot, float scale, Camera camera) {
		this.x = x;
		this.y = y;
		this.rot = rot;
		this.scale = scale;
		this.brightScale = 0.0f; // normal color
		this.slot = 0;
		this.program = Shaders.TEXTURE;
		this.activeTexture = null;
		if(camera == null)
			this.camera = Game.nullCamera;
		else
			this.camera = camera;
	}
	
	/**
	 * Initializes a GameObject with no camera. (i.e. absolute positioning)
	 */
	public GameObject(float x, float y, float rot, float scale) {
		this(x, y, rot, scale, null);
	}
	
	/** 
	 * Renders the <code>activeTexture</code> to the screen using its properties and this <code>GameObject</code>'s position.
	 * <code>null</code> is a valid <code>activeTexture</code> which renders nothing.
	 * */
	public void render() {
		if(activeTexture != null) {
			program.bind();
			activeTexture.bind(slot);
			Rect.bind(); // binds the VAO
			proj.set(Game.proj);
			model = model.
					translation(x - camera.x, y - camera.y, 0).
					rotate(rot - activeTexture.getOffsetRot(), 0.0f, 0.0f, 1.0f).
					scale(scale, scale, 1.0f).
					translate(-activeTexture.getOffsetX(), activeTexture.getOffsetY(), 0).
					scale(activeTexture.getWidth(), activeTexture.getHeight(), 1.0f);
			mvp = proj.mul(model); // M x V x P
			
			//uniforms
			program.set("u_Texture", slot);
			program.set("u_MVP", mvp);
			program.set("u_BrightScale", brightScale);
			
			glDrawElements(GL_TRIANGLES, Rect.ibo.length, GL_UNSIGNED_INT, 0);
		}
	}

	public void setActiveTexture(Surface activeTexture) {
		this.activeTexture = activeTexture;
	}
	
}
