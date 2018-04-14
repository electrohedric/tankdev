package entities;

import gl.Shader;
import gl.Texture;
import staindev.Game;
import util.ClickListener;
import util.Key;
import util.Mouse;

import static org.lwjgl.glfw.GLFW.*;

public class Player extends Entity implements ClickListener {
	
	public Player(float x, float y, float scale, Texture texture, Shader program) {
		super(x, y, 0.0f, 0.0f, 0.0f, scale, texture, program);
		moveSpeed = 200;
		Game.mouseClickCallback.add(this); // add this Player as a mouse click listener
	}

	@Override
	public void update() {
		checkKeys();
		checkMouse();
	}
	
	private void checkKeys() {
		double moveX = 0, moveY = 0;
		
		if(Key.down(GLFW_KEY_W)) moveY++;
		if(Key.down(GLFW_KEY_S)) moveY--; // no ELSE so that movement stops if W and S are pressed together
			
		if(Key.down(GLFW_KEY_A)) moveX--;
		if(Key.down(GLFW_KEY_D)) moveX++; // ditto
			
		if(moveX != 0 || moveY != 0) // atan2 returns 0 if x and y are 0, which moves right, so lets not do that
			move((float) Math.atan2(moveY, moveX)); // move in direction of key presses
	}
	
	private void checkMouse() {
		rot = (float) (Math.atan2(Mouse.y - y, Mouse.x - x));
	}

	@Override
	public void handleClick(int button) {
		
	}

	@Override
	public void handleRelease(int button) {
		
	}
}
