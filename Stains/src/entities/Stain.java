package entities;

import java.util.ArrayList;
import java.util.List;

import constants.Mode;
import gl.Texture;
import util.Animation;
import util.ClickListener;
import util.Mouse;

public class Stain extends Entity implements ClickListener {
	
	/** list purely for convenience. Holds all Stains in the Game */
	public static List<Stain> list = new ArrayList<>();
	
	Texture aliveTexture;
	Animation deathAnimation;
	
	public Stain(float x, float y, float scale, Texture aliveTexture, Animation deathAnimation) {
		super(x, y, 0.0f, 0.0f, 0.0f, scale);
		this.moveSpeed = 20.0f; // pixels / second
		this.aliveTexture = aliveTexture;
		this.deathAnimation = deathAnimation;
		setActiveTexture(aliveTexture);
		ClickListener.addToCallback(this, Mode.PLAY);
		
	}

	@Override
	public void update() {
		if(alive) {
			rot = angleTo(Player.getInstance());
			move();
			if(deathAnimation.isFinished()) {
				kill();
			}
		}
	}
	
	@Override
	public void handleClick(int button) {
		if(alive) {
			if(button == Mouse.LEFT) {
				setActiveTexture(deathAnimation);
				deathAnimation.start();
			}
		}
	}

	@Override
	public void handleRelease(int button) {}

}
