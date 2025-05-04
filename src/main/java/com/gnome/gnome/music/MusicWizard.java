package com.gnome.gnome.music;

import com.gnome.gnome.MainApplication;
import lombok.Getter;
import lombok.SneakyThrows;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller of all music and sound
 */
public class MusicWizard {
    static Clip music;
    static Clip ambient;

    static FloatControl musicControl ;
    static FloatControl ambientControl;

    public static boolean musicRunning = false;

    public static boolean ambientRunning = false;

    @Getter
    protected static float MAXvolume = 0.5f;

    static public boolean stop = false;
    static public boolean stopImmediate = false;
    static public List<String> playlist;

    static protected float ambientDB = -20;
    static protected float currDB = 0F;
    static protected float targetDB = 0F;
    static protected float fadePerStep = .03F; //TO big change will cause clicks
    static protected boolean fading = false;


    /**
     * Sets sent .wav file as an ambient and start to play it on a loop
     */
    public static void start_ambient(String filename) throws LineUnavailableException, UnsupportedAudioFileException, IOException {
        ambient = AudioSystem.getClip();

        AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File(filename));

        ambient.open(inputStream);

        setup_ambient();

        ambient.loop(Clip.LOOP_CONTINUOUSLY);

        ambientControl.setValue(ambientDB);

        ambientRunning = true;
    }

    /**
     * Starts default ambient music in loop
     */
    public static void start_ambient(){
        try {
            ambient = AudioSystem.getClip();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }

        AudioInputStream inputStream = null;
        try {
            inputStream = AudioSystem.getAudioInputStream(new File("src/main/java/com/gnome/gnome/music/ambient_cave.wav"));
        } catch (UnsupportedAudioFileException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            ambient.open(inputStream);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        setup_ambient();

        ambient.loop(Clip.LOOP_CONTINUOUSLY);

        ambientControl.setValue(ambientDB);

        ambientRunning = true;
    }

    /**
     * Setup volume control for an ambient clip
     */
    protected static void setup_ambient(){
        if (ambient.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            ambientControl = (FloatControl) ambient.getControl(FloatControl.Type.MASTER_GAIN);

        } else if (ambient.isControlSupported(FloatControl.Type.VOLUME)) {
            ambientControl = (FloatControl) ambient.getControl(FloatControl.Type.VOLUME);

        } else {
            throw new UnsupportedOperationException("My magic could not find any control"); //Should not happen
        }
    }

    /**
     * Abruptly stops the ambient clip
     */
    public static void stop_ambient(){
        ambientRunning = false;
        if (!ambient.isRunning()) {
            return;
        }
        ambientControl.shift(1, 0,50000);
        ambient.stop();
    }

    /**
     * Starts a music loop playing each track from the playlist in sequence with fading transitions.
     * If 'stop' is triggered, fades out and stops the music safely.
     */
    public static void start_music_loop() {
        musicRunning = true;
        stop = false;
        stopImmediate = false;
        currDB = 0;

        // Fallback playlist if none provided
        if (playlist==null){
            playlist=new ArrayList<>();
            playlist.add("src/main/java/com/gnome/gnome/music/1.wav");
            playlist.add("src/main/java/com/gnome/gnome/music/2.wav");
            playlist.add("src/main/java/com/gnome/gnome/music/3.wav");
        }

        runPlaylistInThread();

    }

    /**
     * Starts a playlist of soundtrack clips on repeat, with fading between clips
     */
    protected static void run_clip_till_finish(String filename) throws InterruptedException {
        setup_music(filename);

        music.loop(Clip.LOOP_CONTINUOUSLY);

        shiftVolumeTo(MAXvolume);

        do {
            Thread.sleep(500);
        } while (fading);

        if (stopImmediate){
            musicControl.shift(1, 0,5000);
            Thread.sleep(200);
            music.stop();
            music.close();
            return;
        }

        if (stop){
            shiftVolumeTo(0.0);
            do {
                Thread.sleep(1000);
            } while (fading);
            music.stop();
            music.close();
            return;
        }

        for(int i = 0; i <170000; i+=1000){ //2.9 minutes  170000 3 minutes
            Thread.sleep(500);

            if (stopImmediate){
                musicControl.shift(1, 0,5000);
                Thread.sleep(500);
                music.stop();
                music.close();
                return;
            }

            if (stop){
                break;
            }
        }
        shiftVolumeTo(0.0);
        Thread.sleep(305);

        while (fading) {
            Thread.sleep(500);
        }
        music.stop();
        music.close();
    }


    /**
     * Create new soundtrack clip. Assign volume control on it. Set a track from AudiFile
     */
    @SneakyThrows
    public static void setup_music(String audioFile){
        if (music != null && music.isOpen()) {
            music.stop();
            music.close();
        }
        if ( music == null || !music.isRunning()){
            try {
                music = AudioSystem.getClip();
            } catch (LineUnavailableException e) {
                throw new RuntimeException(e);
            }
        }

        AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File(audioFile));

        music.open(inputStream);

        musicControl = (FloatControl) music.getControl(FloatControl.Type.MASTER_GAIN);

        musicControl.setValue(currDB);
    }

    /**
     * Fade volume to a level. Value is from 1 to 0. Starts in own Thread
     */
    static protected void shiftVolumeTo(double value) {
        // value is between 0 and 1
        // HIGHLY ADVISED TO USE 0.1 OR LOWER because changes are exponential. So 1 and 0.5 unnoticeable

        if(value <= 0){
            value = 0.0001;
        }
        if(value >= MAXvolume){
            value = MAXvolume;
        }
        targetDB = (float)(Math.log(value)/Math.log(10.0)*30.0);

        if (targetDB < -80) targetDB = -80;

        System.out.println(MAXvolume + " " + currDB + " " + targetDB + " " + value);
        //Change value of from 1 to 0, to dB. Reverse of actual formula they use in documentation


        if (!fading) {
            Thread t = new Thread() {
                /**
                 * Run by thread to gradually lower the volume
                 * WE need fadePerStep=.1 to minimize clicks. Too fast change and it will awfully click
                 */
                public synchronized void run() {
                    fading = true;   // prevent running twice on same sound
                    if (currDB > targetDB + fadePerStep*2) {
                        while (currDB > targetDB + fadePerStep*6) {
                            currDB -= fadePerStep;
                            musicControl.setValue(currDB);
                            try {Thread.sleep(4);} catch (Exception e) {}
                        }
                    }
                    else if (currDB < targetDB - fadePerStep*2) {
                        while (currDB < targetDB - fadePerStep*6) {
                            currDB += fadePerStep;
                            musicControl.setValue(currDB);
                            try {Thread.sleep(4);} catch (Exception e) {}
                        }
                    }
                    fading = false;
                    currDB = targetDB;
                    musicControl.setValue(currDB);
                }
            };  // start a thread to fade volume

            t.start();  // calls run() below
        }
    }

    public static void setGlobalVolume(double sliderValue) {
        float volumeFraction = (float) (sliderValue / 200.0 );
        if (volumeFraction < 0.001f) volumeFraction = 0.001f;

        float dB = (float)(Math.log(volumeFraction)/Math.log(10.0)*30.0);

        if (dB < -80) dB = -80;

        if (musicControl != null && !fading) {
            musicControl.setValue(dB );
            currDB = (dB);
        }

        MAXvolume = volumeFraction;
    }

    private static void runPlaylistInThread() {
        Thread singleTrackThread = new Thread() {
            /**
             * Run by thread to gradually lower the volume
             * WE need fadePerStep=.1 to minimize clicks. Too fast change and it will awfully click
             */
            public void run() {
                while(!stop ) {
                    List<String> currentPlaylist;
                    synchronized (MusicWizard.class) {
                        currentPlaylist = new ArrayList<>(playlist);
                    }
                    for (String track: currentPlaylist) {
                        try {
                            run_clip_till_finish(track);

                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        if(stop){
                            musicRunning = false;
                            stop = false;
                            stopImmediate = false;
                            return;
                        }
                    }
                }
            }
        };

        singleTrackThread.start();
    }
    /**
     * Stops the currently playing music and resets the music state.
     */
    public static void stop_music_slowly() {
        stop = true;
    }
    public static void stop_music(){
        stop = true;
        stopImmediate = true;
    }
}
