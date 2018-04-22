package util;

import static org.lwjgl.openal.AL10.*;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBVorbis;

import constants.Resources;
import constants.Sounds;

public class Sound {
	
	private int buffer;
	private int[] source;
	public static int musicSource = alGenSources();
	
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
			alSource3f(src, AL_POSITION, 0, 0, 0);
			alSource3f(src, AL_VELOCITY, 0, 0, 0);
			alSourcei(src, AL_BUFFER, buffer);
		}
	}
	
	/** Plays the sound in the buffer. If no player is available, no action is taken unless forced
	 * @param force if <code>true</code>, the sound will play even if no player is available
	 * @return <code>true</code> if the sound was able to be played
	 */
	private boolean play(boolean force) {
		boolean available = false;
		int nextSrc = 0;
		for(int src : source) {
			if(isStopped(src)) {
				nextSrc = src;
				available = true;
				break;
			}
		}
		if(available) { // if there's a slot available, use it
			playBuffer(nextSrc);
			return true;
		} else {
			if(force) // if we're forcing, play anyway
				playBuffer(source[0]);
			return force;
		}
	}
	
	/**
	 * Plays the sound in the buffer. Not forced.
	 * @return <code>true</code> if the sound was able to be played (i.e. maxPlayers not reached)
	 */
	public boolean play() {
		return play(false);
	}
	
	/**
	 * Plays the sound in the buffer. Forced.
	 * @return <code>true</code>
	 */
	public boolean forcePlay() {
		return play(true);
	}
	
	/**
	 * Adds a Sound to the music queue which will begin playing seamlessly as soon as the last sound has finished
	 * @param nextSound the loop to play after this intro sound
	 */
	public void addToQueue() {
		alSourceQueueBuffers(musicSource, buffer);
	}
	
	/**
	 * Clears the queue for new use
	 * @return number of sources that were cleared
	 */
	public static int clearQueue() {
		int num = alGetSourcei(musicSource, AL_BUFFERS_PROCESSED);
		alSourceUnqueueBuffers(musicSource);
		return num;
	}
	
	public static void startMusic() {
		playBuffer(musicSource);
	}
	
	public static void playBuffer(int src) {
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
	
	public void checkError() {
		Log.log(alGetError());
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
	
	public void setVolume(float volume) {
		for(int src : source)
			alSourcef(src, AL_GAIN, volume);
	}
	
}