package util;

public class Log {
	
	private static String history = "";
	private static int itersSinceChange = 0;
	private static int boopNum = 1;
	private static LogLevel logLevel = LogLevel.ALL;
	
	public static void log(String message) {
		if(logLevel.level <= LogLevel.ALL.level)
			System.out.println(message);
	}
	
	public static void warn(String message) {
		if(logLevel.level <= LogLevel.BAD.level)
			System.out.println("WARNING > " + message);
	}
	
	public static void err(String message) {
		if(logLevel.level <= LogLevel.FATAL.level)
			System.err.println("ERROR > " + message);
	}
	
	/**
	 * Useful debuging tool to only print changes between calls. Be sure
	 * no other logging occurs (e.g. logLevel == NONE) or confusion may incur.
	 * @param value Value to detect change
	 */
	public static void change(String value) {
		if(value.equals(history)) {
			itersSinceChange++;
		} else {
			StringBuilder spaces = new StringBuilder(history.length());
			for(int i = 0; i < history.length(); i++)
				spaces.append(' ');
			System.out.print("\rCHANGE <> > " + value + spaces.toString());
			history = value;
			itersSinceChange = 0;
		}
	}
	
	/**
	 * Quick debugging tool when you want it to just print SOMETHING.
	 * Useful in cases where you ask yourself: "Is this even running?"
	 */
	public static void ping() {
		System.out.println(String.format("pong! <%s>", boopNum++));
	}
	
	public static void setLevel(LogLevel logLevel) {
		Log.logLevel = logLevel;
	}
	
	public static enum LogLevel {
		ALL(0), BAD(1), FATAL(2), NONE(99);
		
		int level;
		
		LogLevel(int level) {
			this.level = level;
		}
	}
}
