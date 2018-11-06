package guis;

import constants.Mode;
import entities.Entity;
import entities.Player;
import main.Game;
import map.Map;
import util.Animation;

public class PlayScreen extends Gui {
	
	private static PlayScreen instance;
	
	Map map;
	
	public PlayScreen() {
		super(null);
		map = null; // no map loaded
		
		instance = this;
	}

	@Override
	public void switchTo() {
		Game.mode = Mode.PLAY;
	}
	
	@Override
	public void update() {
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
		if(map != null)
			map.render();
		for(Entity e : Entity.list)
			e.render(Player.getInstance().getCamera());
	}
	
	public static PlayScreen getInstance() {
		if(instance != null)
			return instance;
		else
			return new PlayScreen();
	}
	
	public void loadMap(String name) {
		this.map = new Map(name);
	}
	
}
