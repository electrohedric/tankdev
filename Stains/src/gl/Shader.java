package gl;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import constants.Resources;

public class Shader {
	
	private int id;
	private Map<String, Integer> uniforms = new HashMap<String, Integer>();
	
	public Shader(String shaderName, String ... uniforms) {
		List<String> source = null;
		try {
			 // read in file decoded as utf-8 to list
			source = Files.readAllLines(Paths.get(Resources.SHADERS_PATH + shaderName));
		} catch (IOException e) {
			e.printStackTrace();
			return;
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
		
		id = createShader(shaders[0].toString(), shaders[1].toString());
		bind();
		
		for(String uniform : uniforms)
			linkUniform(uniform);
	}
	
	public void bind() {
		glUseProgram(id);
	}
	
	public void unbind() {
		glUseProgram(0);
	}
	
	public void delete() {
		glDeleteProgram(id);
	}
	
	public void linkUniform(String name) {
		int location = glGetUniformLocation(id, name);
		if(location == -1)
			System.out.println("WARNING: uniform " + name + " does not exist"); //TODO make Log class to do stuff like this
		else
			uniforms.put(name, location);
	}
	
	public void set(String uniform, float a, float b, float c, float d) {
		glUniform4f(uniforms.get(uniform), a, b, c, d);
	}
	
	public void set(String uniform, float a, float b, float c) {
		glUniform3f(uniforms.get(uniform), a, b, c);
	}
	
	public void set(String uniform, float a, float b) {
		glUniform2f(uniforms.get(uniform), a, b);
	}
	
	public void set(String uniform, float a) {
		glUniform1f(uniforms.get(uniform), a);
	}
	
	public void set(String uniform, int a) {
		glUniform1i(uniforms.get(uniform), a);
	}
	
	private int compileShader(String source, int type) {
		int shader = glCreateShader(type);
		glShaderSource(shader, source);
		glCompileShader(shader);
		int[] result = new int[1];
		glGetShaderiv(shader, GL_COMPILE_STATUS, result);
		if (result[0] == GL_FALSE) { // bad compile
			String log = glGetShaderInfoLog(shader);
			System.err
					.println("Failed to compile " + (type == GL_VERTEX_SHADER ? "VERTEX" : "FRAGMENT") + " shader.\n");
			System.err.flush();
			System.out.println(log + "\n");
		}
		return shader;
	}

	private int createShader(String vertex, String fragment) {
		int program = glCreateProgram();

		int vs = compileShader(vertex, GL_VERTEX_SHADER);
		int fs = compileShader(fragment, GL_FRAGMENT_SHADER);

		glAttachShader(program, vs);
		glAttachShader(program, fs);
		glLinkProgram(program);
		glValidateProgram(program);

		glDeleteShader(vs);
		glDeleteShader(fs);

		return program;
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
