package guis.elements;

import constants.Mode;
import gl.Texture;
import objects.GameObject;
import util.ClickListener;
import util.Collision;
import util.Mouse;

public class Button extends GameObject implements ClickListener {

	int width;
	int height;
	boolean mouseHovered;
	boolean feedback;
	boolean clickedOn;
	Runnable callback;
	
	/**
	 * Creates a button that executes a task when pressed
	 * @param x Center X position
	 * @param y Center Y position
	 * @param scale Percentage of texture size to render at
	 * @param texture Path to button texture
	 * @param feedback Will increase brightness when hovered if <code>true</code>
	 * @param callback {@link Runnable} function to call when pressed
	 */
	public Button(float x, float y, float scale, Texture texture, Mode screen, boolean feedback, Runnable callback) {
		super(0, 0, 0, scale); // 0, 0 for x, y. Will set after we get width to center
		this.width = texture.getWidth();
		this.height = texture.getHeight();
		this.x = x - width / 2.0f;
		this.y = y - height / 2.0f;
		this.mouseHovered = false;
		this.clickedOn = false;
		this.feedback = feedback;
		this.callback = callback;
		setActiveTexture(texture);
		ClickListener.addToCallback(this, screen);
	}
	
	public void update() {
		if(Collision.pointCollidesAABB(Mouse.x, Mouse.y, x, y, width * scale, height * scale)) {
			if(!mouseHovered) {
				if(feedback)
					onEnter();
				mouseHovered = true;
			}
		}
		else {
			if(mouseHovered) {
				if(feedback)
					onExit();
				mouseHovered = false;
			}
		}
	}
	
	void onEnter() {
		brightScale = 0.2f; // slight brightness increase to show highlighted
	}
	
	void onExit() {
		brightScale = 0.0f; // brightness normal when not highlighted
	}
	
	/** sole purpose is glitter and custom functionality when selected on top of user-defined function */
	public void select() {
		callback.run();
	}
	
	
	@Override
	public void handleClick(int button) {
		if(mouseHovered)
			clickedOn = true;
		else
			clickedOn = false;
	}

	@Override
	public void handleRelease(int button) {
		if(mouseHovered && clickedOn) // only run if the click was pressed and released on this button
			select();
	}
	
	public boolean isMouseHovering() {
		return mouseHovered;
	}

}