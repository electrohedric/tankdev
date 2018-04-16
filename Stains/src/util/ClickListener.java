package util;

import java.util.ArrayList;
import java.util.List;

import constants.Mode;

public interface ClickListener {
	
	public abstract void handleClick(int button);
	public abstract void handleRelease(int button);
	
	/** list that contains all Objects that wish to receive PLAY mouse click input */
	public static List<ClickListener> playMouseClickCallback = new ArrayList<>();
	/** list that contains all Objects that wish to receive TITLE mouse click input */
	public static List<ClickListener> titleMouseClickCallback = new ArrayList<>();
	
	public static void addToCallback(ClickListener self, Mode receiveType) {
		getCallbackList(receiveType).add(self);
	}
	
	public static List<ClickListener> getCallbackList(Mode mode) {
		switch(mode) {
		case EDITOR:
			break;
		case JANITOR:
			break;
		case PAUSED:
			break;
		case PLAY: return playMouseClickCallback;
		case TITLE: return titleMouseClickCallback;
		}
		return null;
	}
	
}
