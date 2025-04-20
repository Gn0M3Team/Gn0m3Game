package com.gnome.gnome.music;

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

    protected static float MAXvolume = 0.5f;
    static public boolean stop = false;

    /**
     * Sets sent .wav file as an ambient and start to play it on a loop
     */
    public static void start_ambient(String filename) throws LineUnavailableException, UnsupportedAudioFileException, IOException {
        ambient = AudioSystem.getClip();

        AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File(filename));

        ambient.open(inputStream);

        setup_ambient();

        ambient.loop(Clip.LOOP_CONTINUOUSLY);

        ambientControl.setValue(-20);

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

        ambientControl.setValue(-20);

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
        ambientControl.shift(-20, -80,50000);
        ambient.stop();
    }

    /**
     * Starts a playlist of soundtrack clips on repeat, with fading between clips in own Thread
     */
    public static void start_music_loop() {
        musicRunning = true;
        stop = false;
        List<String> playlist = new ArrayList<>();

        playlist.add("src/main/java/com/gnome/gnome/music/1.wav");
        playlist.add("src/main/java/com/gnome/gnome/music/2.wav");
        playlist.add("src/main/java/com/gnome/gnome/music/3.wav");

        Thread musicThread = new Thread() {
            /**
             * Run by thread to gradually lower the volume
             * WE need fadePerStep=.1 to minimize clicks. Too fast change and it will awfully click
             */
            public void run() {
                while(!stop){
                    for (String track: playlist) {
                        try {
                            run_clip_till_finish(track);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        if(stop){
                            musicRunning = false;
                            stop = false;

                            currDB = 0;

                            musicControl.setValue(0);
                            return;
                        }
                    }

                }
            }
        };  // start a thread to fade volume

        musicThread.start();

    }

    static protected float currDB = 0F;
    static protected float targetDB = 0F;
    static protected float fadePerStep = .03F; //TO big change will cause clicks
    static protected boolean fading = false;

    /**
     * Starts a playlist of soundtrack clips on repeat, with fading between clips
     */
    protected static void run_clip_till_finish(String filename) throws InterruptedException {
        setup_music(filename);

        music.loop(Clip.LOOP_CONTINUOUSLY);

        shiftVolumeTo(1);
        do {
            Thread.sleep(1000);
        } while (fading);

        if (stop){
            shiftVolumeTo(0.0);
            do {
                Thread.sleep(1000);
            } while (fading);
            music.stop();
            music.close();
            return;
        }

        for(int i = 0; i <17000; i+=1000){ //2.9 minutes  170000 3 minutes
            Thread.sleep(1000);
            if (stop){
                shiftVolumeTo(0.0);
                do {
                    Thread.sleep(1000);
                } while (fading);
                music.stop();
                music.close();
                return;
            }
        }
//        System.out.println("BACK");
        shiftVolumeTo(0.0);
        Thread.sleep(1005);

        while (fading) {
            Thread.sleep(1000);
        }
        music.stop();
        music.close();

        musicControl.setValue(-80);
    }


    /**
     * Create new soundtrack clip. Assign volume control on it. Set a track from AudiFile
     */
    @SneakyThrows
    public static void setup_music(String audioFile){
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

        musicControl.setValue(-80);
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

        //Change value of from 1 to 0, to dB. Reverse of actual formula they use in documentation
        targetDB = (float)(Math.log(value)/Math.log(10.0)*20.0);

        if (!fading) {
            Thread t = new Thread() {
                /**
                 * Run by thread to gradually lower the volume
                 * WE need fadePerStep=.1 to minimize clicks. Too fast change and it will awfully click
                 */
                public void run() {
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
}
