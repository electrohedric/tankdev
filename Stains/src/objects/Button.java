package objects;

import constants.Mode;
import gl.Shader;
import gl.Texture;
import gl.Texture.Anchor;
import util.ClickListener;
import util.Collision;
import util.Mouse;

public class Button extends GameObject implements ClickListener {

	private Texture unpressedTexture;
	private Texture pressedTexture;
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
	public Button(float x, float y, float scale, String unpressedName, String pressedName, Shader program, Mode screen, Runnable callback) {
		super(0, 0, 0.0f, scale, program); // 0, 0 for x, y. Will set after we get width to center
		this.unpressedTexture = new Texture(unpressedName, Anchor.BOTTOM_LEFT);
		this.pressedTexture = new Texture(pressedName, Anchor.BOTTOM_LEFT);
		if(unpressedTexture.getWidth() != pressedTexture.getWidth() || unpressedTexture.getHeight() != pressedTexture.getHeight()) {
			throw new IllegalArgumentException("Both textures must be the same size");
		}
		this.width = unpressedTexture.getWidth();
		this.height = pressedTexture.getHeight();
		this.x = x - width / 2.0f;
		this.y = y - height / 2.0f;
		this.mouseHovered = false;
		this.callback = callback;
		setActiveTexture(unpressedTexture);
		ClickListener.addToCallback(this, screen);
	}
	
	public void update() {
		if(Collision.pointCollidesAABB(Mouse.x, Mouse.y, x, y, width * scale, height * scale)) {
			if(!mouseHovered) {
				setActiveTexture(pressedTexture);
				mouseHovered = true;
			}
		}
		else {
			if(mouseHovered) {
				setActiveTexture(unpressedTexture);
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

}
