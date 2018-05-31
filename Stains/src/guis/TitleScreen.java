package guis;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

import constants.Mode;
import constants.Sounds;
import constants.Textures;
import guis.elements.Button;
import staindev.Game;
import util.Music;

public class TitleScreen extends Gui {
	
	public static TitleScreen instance;
	
	private TitleScreen() {
		super(Textures.Title.BG);
		elements.add(new Button(Game.WIDTH * 0.5f, Game.HEIGHT * 0.5f, 1.0f, Textures.Title.CONTINUE, Mode.TITLE, false, () ->  {
			Sounds.LEMON.forcePlay();
		}));
		elements.add(new Button(Game.WIDTH * 0.5f, Game.HEIGHT * 0.4f, 1.0f, Textures.Title.NEWGAME, Mode.TITLE, false, () ->  {
			Game.mode = Mode.PLAY;
		}));
		elements.add(new Button(Game.WIDTH * 0.5f, Game.HEIGHT * 0.3f, 1.0f, Textures.Title.SETTINGS, Mode.TITLE, true, () ->  {
			Sounds.SCARY.forcePlay();
		}));
		elements.add(new Button(Game.WIDTH * 0.5f, Game.HEIGHT * 0.2f, 1.0f, Textures.Title.EDITOR, Mode.TITLE, true, () ->  {
			EditorScreen.getInstance().switchTo(); //TODO wash out music with a swiping sound and change to editor track
		}));
		elements.add(new Button(Game.WIDTH * 0.5f, Game.HEIGHT * 0.1f, 1.0f, Textures.Title.QUIT, Mode.TITLE, true, () ->  {
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
	}

	@Override
	public void switchTo() {
		Game.mode = Mode.TITLE;
		Music.queue(Sounds.TITLE_INTRO);
		Music.queueLoop(Sounds.TITLE_LOOP);
		Music.play();
	}

}
