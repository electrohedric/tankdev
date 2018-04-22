package guis;

import constants.Mode;
import constants.Textures;
import staindev.Game;

public class TitleScreen extends Gui {
	
	public static TitleScreen instance;
	
	public TitleScreen() {
		super(Textures.Title.BG);
		elements.add(new Button(Game.WIDTH * 0.5f, Game.HEIGHT * 0.45f, 1.0f, Textures.Title.NEWGAME , Textures.Title.NEWGAME, Mode.TITLE, () ->  {
			Game.mode = Mode.PLAY;
		}));
		elements.add(new Button(Game.WIDTH * 0.5f, Game.HEIGHT * 0.55f, 1.0f, Textures.Title.EDITOR, Textures.Title.EDITOR, Mode.TITLE, () ->  {
			Game.mode = Mode.EDITOR;
		}));
		instance = this;
	}
	
	@Override
	public void update() {
		super.update(); // TODO make moving hand
	}

}
