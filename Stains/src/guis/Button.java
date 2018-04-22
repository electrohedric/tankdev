package guis;

import constants.Mode;
import gl.Shader;
import gl.Texture;
import objects.GameObject;
import util.ClickListener;
import util.Collision;
import util.Mouse;

public class Button extends GameObject implements ClickListener {

	private Texture offTexture;
	private Texture onTexture;
	private int width;
	private int height;
	private boolean mouseHovered;
	private Runnable callback;
	
	/**
	 * Creates a button that executes a task when pressed
	 * @param x Center X position
	 * @param y Center Y position
	 * @param scale Percentage of texture size to render at
	 * @param unpressedName Path to unpressed button texture
	 * @param pressedName Path to pressed button texture
	 * @param program {@link Shader} to render with
	 * @param callback {@link Runnable} function to call when pressed
	 */
	public Button(float x, float y, float scale, Texture off, Texture on, Mode screen, Runnable callback) {
		super(0, 0, 0, scale); // 0, 0 for x, y. Will set after we get width to center
		this.offTexture = off;
		this.onTexture = on;
		if(offTexture.getWidth() != onTexture.getWidth() || offTexture.getHeight() != onTexture.getHeight()) {
			throw new IllegalArgumentException("Both textures must be the same size");
		}
		this.width = offTexture.getWidth();
		this.height = onTexture.getHeight();
		this.x = x - width / 2.0f;
		this.y = y - height / 2.0f;
		this.mouseHovered = false;
		this.callback = callback;
		setActiveTexture(offTexture);
		ClickListener.addToCallback(this, screen);
	}
	
	public void update() {
		if(Collision.pointCollidesAABB(Mouse.x, Mouse.y, x, y, width * scale, height * scale)) {
			if(!mouseHovered) {
				setActiveTexture(onTexture);
				mouseHovered = true;
			}
		}
		else {
			if(mouseHovered) {
				setActiveTexture(offTexture);
				mouseHovered = false;
			}
		}
	}
	
	
	@Override
	public void handleClick(int button) {}

	@Override
	public void handleRelease(int button) {
		if(mouseHovered)
			callback.run();
	}
	
	public boolean isMouseHovering() {
		return mouseHovered;
	}

}
