package guis;

import java.util.ArrayList;
import java.util.List;

import gl.Texture;

public class Gui {

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
	
}
