package guis;

import constants.Mode;
import constants.Textures;
import entities.Entity;
import entities.Player;
import gl.Texture;
import main.Game;
import util.Animation;

public class PlayScreen extends Gui {
	
	private static PlayScreen instance;
	
	public PlayScreen(Texture background) {
		super(background);
		
		instance = this;
	}

	@Override
	public void switchTo() {
		Game.mode = Mode.PLAY;
	}
	
	@Override
	public void update() {
		PlayScreen.getInstance().update();
		for(int i = Animation.queue.size() - 1; i >= 0; i--)
			Animation.queue.get(i).update();
		
		for(int i = Entity.list.size() - 1; i >= 0; i--)
			Entity.list.get(i).update();
		
		for(int i = Entity.list.size() - 1; i >= 0; i--)
			if(Entity.list.get(i).isDead())
				Entity.list.remove(i);
	}
	
	@Override
	public void render() {
		for(Entity e : Entity.list)
			e.render(Player.getInstance().getCamera());
	}
	
	public static PlayScreen getInstance() {
		if(instance != null)
			return instance;
		else
			return new PlayScreen(Textures.Title.BG);
	}
	
	public void loadMap() {
		
	}
	

}
