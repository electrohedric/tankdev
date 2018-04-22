package guis;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector4f;

import constants.Mode;
import constants.Textures;
import staindev.Game;
import util.ClickListener;
import util.Mouse;

/** Singleton which represents the only Editor Screen */
public class EditorScreen extends Gui implements ClickListener {
	
	public static EditorScreen instance;
	
	private Button lineButton;
	private Button filletButton;
	
	private Wall ghostWall;
	private List<Wall> map;
	
	private boolean firstPointDown;
	private int firstClickX;
	private int firstClickY;
	
	public EditorScreen() {
		super(Textures.Editor.BG);
		// TODO not Buttons, RadioButtons and make it extend Button
 		this.lineButton = new Button(Game.WIDTH * 0.8f, Game.HEIGHT * 0.2f, 1.0f, Textures.Editor.LINEOFF, Textures.Editor.LINEON, Mode.EDITOR, () -> {
			//TODO line button callback
		});
		this.filletButton = new Button(Game.WIDTH * 0.8f, Game.HEIGHT * 0.4f, 1.0f, Textures.Editor.FILLETOFF, Textures.Editor.FILLETON, Mode.EDITOR, () -> {
			//TODO fillet button callback
		});
		this.ghostWall = new Wall(0, 0, 0, 0, 3.0f, new Vector4f(0.5f)); // instantiate a wall with essentially just a ghostly gray color
		this.map = new ArrayList<>();
		this.firstPointDown = false;
		this.firstClickX = 0;
		this.firstClickY = 0;
		elements.add(lineButton);
		elements.add(filletButton);
		ClickListener.addToCallback(this, Mode.EDITOR);
		instance = this; // allow more instantiations, but only one instance can exist
	}
	
	@Override
	public void update() {
		super.update();
		if(firstPointDown) {
			ghostWall.setEndPoint(Mouse.x, Mouse.y); // TODO implement grid system
		}
	}
	
	@Override
	public void render() {
		super.render();
		for(Wall wall : map)
			wall.render();
		if(firstPointDown)
			ghostWall.render();
	}
	
	@Override
	public void handleClick(int button) { // handle painting
		if(isOnMap()) {
			if(button == Mouse.LEFT) {
				if(firstPointDown) {
					map.add(new Wall(firstClickX, firstClickY, Mouse.x, Mouse.y, 3.0f, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f)));
					firstPointDown = false;
				} else {
					firstClickX = Mouse.x;
					firstClickY = Mouse.y;
					ghostWall.setStartPoint(firstClickX, firstClickY);
					firstPointDown = true;
				}
			} else if(button == Mouse.RIGHT) {
				if(firstPointDown) {
					firstPointDown = false;
				} else {
					// TODO algorithm to determine closest corner and remove it
				}
			}
		}
	}
	
	private boolean isOnMap() {
		return !lineButton.isMouseHovering() && !filletButton.isMouseHovering();
	}
	
	@Override
	public void handleRelease(int button) {
		
	}
	
}
