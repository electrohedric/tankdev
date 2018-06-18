package entities;

import static org.lwjgl.glfw.GLFW.*;

import constants.Mode;
import constants.Sounds;
import constants.Textures;
import gl.Texture;
import staindev.Game;
import util.Camera;
import util.ClickListener;
import util.Key;
import util.Mouse;

/** Singleton which represents the only Player */
public class Player extends Entity implements ClickListener {
	
	/** The player. For now, there is only one player as multiplayer support not implemented */
	private static Player instance;
	public static Camera camera;
	
	public Player(float x, float y, float scale, Texture texture, Camera camera) {
		super(x, y, 0, 0, 0, scale, camera);
		this.moveSpeed = 200;
		this.setActiveTexture(texture);
		ClickListener.addToCallback(this, Mode.PLAY);
		instance = this;
	}
	
	public static Player getInstance() {
		if(instance != null)
			return instance;
		else {
			camera = new Camera(0, 0);
			return new Player(500, 500, 0.4f, Textures.PLAYER, Player.camera);
		}
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
		rot = (float) (Math.atan2(Mouse.y - Game.HEIGHT / 2, Mouse.x - Game.WIDTH / 2));
	}

	@Override
	public void handleClick(int button) {
		Sounds.SPRAY.forcePlay();
	}

	@Override
	public void handleRelease(int button) {
		
	}
	
}
