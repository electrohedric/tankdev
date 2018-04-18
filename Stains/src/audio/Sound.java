package audio;

import static org.lwjgl.openal.AL10.*;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBVorbis;

import constants.Resources;
import util.Log;

public class Sound {
	
	private int buffer;
	private int[] source;
	
	/**
	 * Creates a new Sound which can be played with multiple players
	 * @param path
	 * @param volume
	 * @param loop
	 * @param maxPlayers
	 */
	public Sound(String path, float volume, boolean loop, int maxPlayers) {
		IntBuffer channelsBuf = BufferUtils.createIntBuffer(1);
		IntBuffer rateBuf = BufferUtils.createIntBuffer(1);
		ShortBuffer audioBuf = STBVorbis.stb_vorbis_decode_filename(Resources.SOUNDS_PATH + path, channelsBuf, rateBuf);
		int channels = channelsBuf.get();
		int rate = rateBuf.get();
		
		int format = -1;
		if(channels == 1) {
		    format = AL_FORMAT_MONO16;
		} else if(channels == 2) {
		    format = AL_FORMAT_STEREO16;
		} else throw new IllegalArgumentException("Couldn't Identify number of channels for: " + path);
		
		this.buffer = alGenBuffers();
		alBufferData(buffer, format, audioBuf, rate);
		Sounds.buffers.add(buffer);
		
		if(loop && maxPlayers > 1)
			Log.warn("There really shouldn't be more than one player on a loop: " + path);
		else if(maxPlayers < 0)
			throw new IllegalArgumentException("There must be at least one player: " + path);
		
		this.source = new int[maxPlayers];
		alGenSources(source);
		
		// specifically for more than one souce and no looping, but hey it works for everything.
		// it wouldn't make much sense to have the same sound playing on a loop and one not, so this works out
		for(int src : source) {
			alSourcei(src, AL_LOOPING, loop ? AL_TRUE : AL_FALSE);
			alSourcef(src, AL_GAIN, volume);
			alSourcef(src, AL_PITCH, 1.0f);
		}
	}
	
	/** Plays the sound from a new player at (x, y) if available and listening from (lx, ly). If no player is available, no action is taken 
	 * @return <code>true</code> if the sound was able to be played
	 */
	public boolean play(float x, float y, float lx, float ly) {
		boolean available = false;
		int nextSrc = 0;
		for(int src : source) {
			if(isStopped(src)) {
				nextSrc = src;
				available = true;
				break;
			}
		}
		if(available) {
			playBuffer(nextSrc, x, y, lx, ly);
			return true;
		} else
			return false;
			
	}
	
	/**
	 * Plays the sound from player 0, overriding its sound with this one at (x, y) and listening from (lx, ly)
	 */
	public void forcePlay(float x, float y, float lx, float ly) {
		alSourceStop(source[0]);
		playBuffer(source[0], x, y, lx, ly);
	}
	
	private void playBuffer(int src, float x, float y, float lx, float ly) {
		alListener3f(AL_POSITION, lx, ly, 0);
		alSourcei(src, AL_BUFFER, buffer);
		alSource3f(src, AL_POSITION, x, y, 0);
		alSourcePlay(src);
	}
	
	public void pauseAll() {
		for(int src : source)
			if(isPlaying(src))
				alSourcePause(src);
	}
	
	public void unpauseAll() {
		for(int src : source)
			if(isPaused(src))
				alSourcePlay(src);
	}
	
	public void stopAll() {
//		for(int src : source)
//			if(!isStopped(src))
//				alSourceStop(src);
	}
	
	/** 
	 * @return <code>true</code> if and only if sound is playing. i.e. NOT paused and NOT stopped
	 */
	public boolean isPlaying(int src) {
		return alGetSourcei(src, AL_SOURCE_STATE) == AL_PLAYING;
	}
	
	/** 
	 * @return <code>true</code> if and only if sound is paused. i.e. NOT stopped and NOT playing
	 */
	public boolean isPaused(int src) {
		return alGetSourcei(src, AL_SOURCE_STATE) == AL_PAUSED;
	}
	
	/** 
	 * @return <code>true</code> if and only if sound is stopped or never used. i.e. NOT paused and NOT playing
	 */
	public boolean isStopped(int src) {
		int srcstate = alGetSourcei(src, AL_SOURCE_STATE);
		return srcstate == AL_INITIAL || srcstate == AL_STOPPED;
	}
	
}
