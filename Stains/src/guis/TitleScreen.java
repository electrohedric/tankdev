package guis;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

import constants.Mode;
import constants.Sounds;
import constants.Textures;
import staindev.Game;
import util.Sound;

public class TitleScreen extends Gui {
	
	public static TitleScreen instance;
	
	public TitleScreen() {
		super(Textures.Title.BG);
		elements.add(new Button(Game.WIDTH * 0.5f, Game.HEIGHT * 0.50f, 1.0f, Textures.Title.CONTINUE , Textures.Title.CONTINUE, Mode.TITLE, () ->  {
			// TODO figure it out
		}));
		elements.add(new Button(Game.WIDTH * 0.5f, Game.HEIGHT * 0.40f, 1.0f, Textures.Title.NEWGAME , Textures.Title.NEWGAME, Mode.TITLE, () ->  {
			Game.mode = Mode.PLAY;
		}));
		elements.add(new Button(Game.WIDTH * 0.5f, Game.HEIGHT * 0.30f, 1.0f, Textures.Title.SETTINGS , Textures.Title.SETTINGS, Mode.TITLE, () ->  {
			// TODO figure it out
		}));
		elements.add(new Button(Game.WIDTH * 0.5f, Game.HEIGHT * 0.20f, 1.0f, Textures.Title.EDITOR, Textures.Title.EDITOR, Mode.TITLE, () ->  {
			Game.mode = Mode.EDITOR;
		}));
		elements.add(new Button(Game.WIDTH * 0.5f, Game.HEIGHT * 0.10f, 1.0f, Textures.Title.QUIT , Textures.Title.QUIT, Mode.TITLE, () ->  {
			glfwSetWindowShouldClose(Game.window, true);
		}));
		
		Sounds.TITLE_INTRO.addToQueue();
		Sounds.TITLE_LOOP.addToQueue();
		Sound.startMusic();
		
		//Sounds.TITLE_LOOP.play();
		instance = this;
	}
	
	@Override
	public void update() {
		super.update(); // TODO make moving hand
		int numCleared = Sound.clearQueue(); // clear played music TODO maybe abstract this into Sound class
		for(int i = 0; i < numCleared; i++)
			Sounds.TITLE_LOOP.addToQueue();
	}

}
