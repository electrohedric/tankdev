package entities;

import gl.Shader;
import gl.Texture;
import objects.Renderable;
import staindev.Game;
import util.Animation;
import util.ClickListener;
import util.Mouse;

public class Stain extends Entity implements ClickListener {
	
	public Stain(float x, float y, float scale, Renderable texture, Shader program) {
		super(x, y, 0.0f, 0.0f, 0.0f, scale, texture, program);
		moveSpeed = 20.0f; // pixels / second
		Game.mouseClickCallback.add(this);
	}

	@Override
	public void update() {
		rot = angleTo(Game.player);
		move();
	}

	@Override
	public void handleClick(int button) {
		if(button == Mouse.LEFT) {
			((Animation) getTexture()).start();
		}
	}

	@Override
	public void handleRelease(int button) {}

}
