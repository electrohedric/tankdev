package helpers;

public class ShaderPair {
	private String vertexSource;
	private String fragmentSource;
	
	ShaderPair(String vs, String fs) {
		vertexSource = vs;
		fragmentSource = fs;
	}
	
	public String getVertex() {
		return vertexSource;
	}
	
	public String getFragment() {
		return fragmentSource;
	}
}
