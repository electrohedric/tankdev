package editor;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector4f;

import constants.Mode;
import gl.Shader;
import objects.Button;
import staindev.Game;
import util.ClickListener;
import util.Mouse;

/** Singleton which represents the only Editor Screen */
public class EditorScreen implements ClickListener {
	
	public static EditorScreen instance;
	
	private Button lineButton;
	private Button filletButton;
	
	private Wall ghostWall;
	private List<Wall> map;
	
	private boolean firstPointDown;
	private int firstClickX;
	private int firstClickY;
	
	public EditorScreen() {
		// TODO not Buttons, RadioButtons and make it extend Button
 		this.lineButton = new Button(Game.WIDTH * 0.8f, Game.HEIGHT * 0.2f, 1.0f, "editor/line_unpressed.png", "editor/line_pressed.png", Shader.texture, Mode.EDITOR, () -> {
			//TODO line button callback
		});
		this.filletButton = new Button(Game.WIDTH * 0.8f, Game.HEIGHT * 0.4f, 1.0f, "editor/fillet_unpressed.png", "editor/fillet_pressed.png", Shader.texture, Mode.EDITOR, () -> {
			//TODO fillet button callback
		});
		this.ghostWall = new Wall(0, 0, 0, 0, 3.0f, new Vector4f(0.5f), Shader.color); // instantiate a wall with essentially just a ghostly gray color
		this.map = new ArrayList<>();
		this.firstPointDown = false;
		this.firstClickX = 0;
		this.firstClickY = 0;
		ClickListener.addToCallback(this, Mode.EDITOR);
		instance = this; // allow more instantiations, but only one instance can exist
	}
	
	public void update() {
		lineButton.update();
		filletButton.update();
		if(firstPointDown) {
			ghostWall.setEndPoint(Mouse.x, Mouse.y);
		}
	}
	
	public void render() {
		for(Wall wall : map)
			wall.render();
		lineButton.render();
		filletButton.render();
		if(firstPointDown)
			ghostWall.render();
	}
	
	@Override
	public void handleClick(int button) {
		if(!lineButton.isMouseHovering() && !filletButton.isMouseHovering()) {
			if(button == Mouse.LEFT) {
				if(firstPointDown) {
					map.add(new Wall(firstClickX, firstClickY, Mouse.x, Mouse.y, 3.0f, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), Shader.color));
					firstPointDown = false;
				} else {
					firstClickX = Mouse.x;
					firstClickY = Mouse.y;
					ghostWall.setStartPoint(firstClickX, firstClickY);
					firstPointDown = true;
				}
			} else if(button == Mouse.RIGHT) {
				if(firstPointDown) {
					// TODO algorithm to determine closest color and remove it
				} else {
					// TODO cancel first point
				}
			}
		}
	}
	
	@Override
	public void handleRelease(int button) {
		
	}
	
}
