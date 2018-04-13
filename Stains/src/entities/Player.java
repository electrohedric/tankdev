package entities;

import gl.Shader;
import gl.Texture;
import staindev.Game;
import util.ClickListener;
import util.Key;
import util.Mouse;

import static org.lwjgl.glfw.GLFW.*;

public class Player extends Entity implements ClickListener {
	
	/** in pixels per second **/
	private int moveSpeed;
	
	public Player(float x, float y, float vx, float vy, float rot, float scale, Texture texture, Shader program) {
		super(x, y, vx, vy, rot, scale, texture, program);
		moveSpeed = 200;
		Game.mouseClickCallback.add(this); // add this Player as a mouse click listener
	}

	@Override
	public void update() {
		checkKeys();
		checkMouse();
		x += vx * Game.delta;
		y += vy * Game.delta;
	}
	
	private void checkKeys() {
		double moveX = 0, moveY = 0;
		if(Key.down(GLFW_KEY_W)) {
			moveY++;
		}
		if(Key.down(GLFW_KEY_S)) {
			moveY--;
		}
		
		if(Key.down(GLFW_KEY_A)) {
			moveX--;
		}
		if(Key.down(GLFW_KEY_D)) {
			moveX++;
		}
		double angle = Math.atan2(moveY, moveX);
		vx = (float) (moveX * Math.abs(Math.cos(angle)) * moveSpeed);
		vy = (float) (moveY * Math.abs(Math.sin(angle)) * moveSpeed);
	}
	
	private void checkMouse() {
		rot = (float) (Math.atan2(Mouse.y - y, Mouse.x - x) - Math.PI / 2);
	}

	@Override
	public void handleClick(int button) {
		scale *= 1.1;
	}

	@Override
	public void handleRelease(int button) {
		
	}
}
