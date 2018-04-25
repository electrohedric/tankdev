package guis;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

import constants.Mode;
import constants.Sounds;
import constants.Textures;
import guis.elements.Button;
import staindev.Game;
import util.Sound;

public class TitleScreen extends Gui {
	
	public static TitleScreen instance;
	
	private TitleScreen() {
		super(Textures.Title.BG);
		elements.add(new Button(Game.WIDTH * 0.5f, Game.HEIGHT * 0.50f, 1.0f, Textures.Title.CONTINUE, Mode.TITLE, false, () ->  {
			// TODO figure it out
		}));
		elements.add(new Button(Game.WIDTH * 0.5f, Game.HEIGHT * 0.40f, 1.0f, Textures.Title.NEWGAME, Mode.TITLE, false, () ->  {
			Game.mode = Mode.PLAY;
		}));
		elements.add(new Button(Game.WIDTH * 0.5f, Game.HEIGHT * 0.30f, 1.0f, Textures.Title.SETTINGS, Mode.TITLE, false, () ->  {
			// TODO figure it out
		}));
		elements.add(new Button(Game.WIDTH * 0.5f, Game.HEIGHT * 0.20f, 1.0f, Textures.Title.EDITOR, Mode.TITLE, false, () ->  {
			EditorScreen.getInstance().switchTo(); //TODO wash out music with a swiping sound and change to editor track
		}));
		elements.add(new Button(Game.WIDTH * 0.5f, Game.HEIGHT * 0.10f, 1.0f, Textures.Title.QUIT, Mode.TITLE, false, () ->  {
			glfwSetWindowShouldClose(Game.window, true);
		}));
		
		instance = this;
	}
	
	public static TitleScreen getInstance() {
		if(instance != null)
			return instance;
		else
			return new TitleScreen();
	}
	
	@Override
	public void update() {
		super.update(); // TODO make moving hand
		Sound.continueLooping(Sounds.TITLE_LOOP);
	}

	@Override
	public void switchTo() {
		Game.mode = Mode.TITLE;
		Sounds.TITLE_INTRO.addToQueue();
		Sounds.TITLE_LOOP.addToQueue();
		Sound.startMusic();
	}

}
