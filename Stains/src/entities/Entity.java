package entities;

import gl.Shader;
import gl.Texture;
import objects.GameObject;
import objects.Renderable;
import staindev.Game;

public abstract class Entity extends GameObject {
	
	//
	// I would rather keep these unused until we need to implement physics of some sort; until then just use move();
	//
//	public float vx;
//	public float vy;
	/** in pixels per second */
	public float moveSpeed;
	
	/**
	 * Creates an entity which can be updated and rendered to the screen. This object is automatically added the <code>Game.entities</code> list
	 * @param x initial x position
	 * @param y initial y position
	 * @param vx initial velocity in the x direction
	 * @param vy initial velocity in the y direction
	 * @param rot initial rotation, in radians
	 * @param scale initial scale based on actual texture size (i.e. <code>1.0f</code> is 1:1 scale with texture)
	 * @param texture <code>Texture</code> to be rendered
	 * @param program <code>Shader</code> program to use when rendering. Must have the following uniforms: uTexture, u_MVP
	 */
	public Entity(float x, float y, float vx, float vy, float rot, float scale, Renderable texture, Shader program) {
		super(x, y, rot, scale, texture, program);
//		this.vx = vx;
//		this.vy = vy;
		Game.entities.add(this);
	}

	public abstract void update();
	
	public float angleTo(Entity other) {
		return (float) Math.atan2(other.y - y, other.x - x);
	}
	
	/** Move by <strong>moveSpeed</strong> in direction <strong>dir</strong>*/
	public void move(float dir) {
		x += (Math.cos(dir) * moveSpeed) * Game.delta;
		y += (Math.sin(dir) * moveSpeed) * Game.delta;
	}
	
	/** Move by <strong>moveSpeed</strong> in direction <strong>rot</strong>*/
	public void move() {
		move(rot);
	}
	
}
