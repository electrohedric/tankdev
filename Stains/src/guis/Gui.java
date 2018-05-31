package guis;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

import gl.Texture;
import guis.elements.Button;
import staindev.Game;

public abstract class Gui {

	Screen background;
	List<Button> elements; // TODO should be an element list for elements, not just buttons
	
	public Gui(Texture background) {
		this.background = new Screen(background);
		this.elements = new ArrayList<>();
	}

	public void update() {
		for(Button b : elements)
			b.update();
	}
	
	public void render() {
		background.render();
		for(Button b : elements)
			b.render();
	}
	
	protected void setMousePointer(long cursor) {
		glfwSetCursor(Game.window, cursor);
	}
	
	public abstract void switchTo();
	
}
