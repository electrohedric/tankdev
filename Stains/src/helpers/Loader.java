package helpers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import constants.Resources;

public class Loader {
	
	public static ShaderPair loadShader(String shaderName) {
		List<String> source = null;
		try {
			 // read in file decoded as utf-8 to list
			source = Files.readAllLines(Paths.get(Resources.RES_PATH + Resources.SHADER_FOLDER + shaderName + Resources.SHADER_EXT));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		StringBuilder[] shaders = new StringBuilder[2];
		for(int i = 0; i < shaders.length; i++)
			shaders[i] = new StringBuilder();
		ShaderMode mode = ShaderMode.NONE;
		for(String line : source) {
			if(line.startsWith("#shader")) {
				if(line.contains("vertex")) {
					mode = ShaderMode.VERTEX;
				} else if(line.contains("fragment")) {
					mode = ShaderMode.FRAGMENT;
				} // else do not change mode
			} else if(mode != ShaderMode.NONE){
				shaders[mode.index].append(line).append("\n");
			}
		}
		return new ShaderPair(shaders[0].toString(), shaders[1].toString());
	}
	
	enum ShaderMode {
		NONE(-1),
		VERTEX(0),
		FRAGMENT(1);
		
		final int index;
		
		ShaderMode(int index) {
			this.index = index;
		}
	}
}
