package entities;

import gl.Shader;
import gl.Texture;
import objects.GameObject;

public abstract class Entity extends GameObject {
	
	public float vx;
	public float vy;
	
	public Entity(float x, float y, float vx, float vy, float rot, float scale, Texture texture, Shader program) {
		super(x, y, rot, scale, texture, program);
		this.vx = vx;
		this.vy = vy;
	}

	public abstract void update();
	
}
