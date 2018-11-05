package guis;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

import constants.Mode;
import constants.Sounds;
import constants.Textures;
import guis.elements.Button;
import main.Game;
import util.Music;

public class TitleScreen extends Gui {
	
	private static TitleScreen instance;
	
	private TitleScreen() {
		super(Textures.Title.BG);
		elements.add(new Button(Game.WIDTH * 0.5f, Game.HEIGHT * 0.5f, 0.15f, Textures.Title.CONTINUE, Mode.TITLE, false, () ->  {
			Sounds.LEMON.forcePlay();
		}));
		elements.add(new Button(Game.WIDTH * 0.5f, Game.HEIGHT * 0.4f, 0.15f, Textures.Title.NEWGAME, Mode.TITLE, true, () ->  {
			Game.mode = Mode.PLAY;
		}));
		elements.add(new Button(Game.WIDTH * 0.5f, Game.HEIGHT * 0.3f, 0.15f, Textures.Title.SETTINGS, Mode.TITLE, false, () ->  {
			Sounds.SCARY.forcePlay();
		}));
		elements.add(new Button(Game.WIDTH * 0.5f, Game.HEIGHT * 0.1f, 0.15f, Textures.Title.QUIT, Mode.TITLE, true, () ->  {
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
