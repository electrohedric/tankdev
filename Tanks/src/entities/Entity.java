package entities;

import java.util.ArrayList;
import java.util.List;

import main.Game;
import objects.GameObject;

public abstract class Entity extends GameObject {
	
	/** list for all entities that wish to be updated and rendered */
	public static List<Entity> list = new ArrayList<>();
	
	//
	// I would rather keep these unused until we need to implement physics of some sort; until then just use move();
	//
//	public float vx;
//	public float vy;
	/** in pixels per second */
	public float moveSpeed;
	protected boolean alive = true;
	
	/**
	 * Creates an entity which can be updated and rendered to the screen. This object is automatically added the <code>list</code>
	 * @param x initial x position
	 * @param y initial y position
	 * @param vx initial velocity in the x direction
	 * @param vy initial velocity in the y direction
	 * @param rot initial rotation, in radians
	 * @param scale initial scale based on screen size
	 */
	public Entity(float x, float y, float vx, float vy, float rot, float scale) {
		super(x, y, rot, scale);
//		this.vx = vx;
//		this.vy = vy;
		list.add(this);
	}
	
	/** Let the subclass define how to update itself */
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
	
	public boolean isDead() {
		return !alive;
	}
	
	public void kill() {
		alive = false;
	}
	
}
