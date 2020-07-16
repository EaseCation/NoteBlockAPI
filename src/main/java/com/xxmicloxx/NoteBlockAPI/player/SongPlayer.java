package com.xxmicloxx.NoteBlockAPI.player;

import cn.nukkit.Player;
import cn.nukkit.Server;
import com.xxmicloxx.NoteBlockAPI.event.SongEndEvent;
import com.xxmicloxx.NoteBlockAPI.note.FadeType;
import com.xxmicloxx.NoteBlockAPI.note.Interpolator;
import com.xxmicloxx.NoteBlockAPI.NoteBlockAPI;
import com.xxmicloxx.NoteBlockAPI.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class SongPlayer {

    protected Song song;
    protected boolean playing = false;
    protected short tick = -1;
    protected ArrayList<Player> playerList = new ArrayList<>();
    protected boolean autoDestroy = false;
    protected boolean destroyed = false;
    protected boolean autoCycle = true;
    protected byte fadeTarget = 100;
    protected byte volume = 100;
    protected byte fadeStart = volume;
    protected int fadeDuration = 60;
    protected int fadeDone = 0;
    protected FadeType fadeType = FadeType.FADE_LINEAR;
    private long lastPlayed = 0;
    protected float speedUp = 1;

    public SongPlayer(Song song) {
        this.song = song;
        NoteBlockAPI.getInstance().playing.add(this);
    }

    public boolean getAutoCycle() {
        return autoCycle;
    }

    public void setAutoCycle(boolean b) {
        autoCycle = b;
    }

    public FadeType getFadeType() {
        return fadeType;
    }

    public void setFadeType(FadeType fadeType) {
        this.fadeType = fadeType;
    }

    public byte getFadeTarget() {
        return fadeTarget;
    }

    public void setFadeTarget(byte fadeTarget) {
        this.fadeTarget = fadeTarget;
    }

    public byte getFadeStart() {
        return fadeStart;
    }

    public void setFadeStart(byte fadeStart) {
        this.fadeStart = fadeStart;
    }

    public int getFadeDuration() {
        return fadeDuration;
    }

    public void setFadeDuration(int fadeDuration) {
        this.fadeDuration = fadeDuration;
    }

    public int getFadeDone() {
        return fadeDone;
    }

    public void setFadeDone(int fadeDone) {
        this.fadeDone = fadeDone;
    }

    public void resetSong(Song song) {
        resetSong(song, -1);
    }

    public void resetSong(Song song, int waitTick) {
        tick = (short) -waitTick;
        this.song = song;
        this.fadeDone = 0;
        this.fadeDuration = 0;
        this.volume = 100;
        setPlaying(true);
    }

    protected void calculateFade() {
        if (fadeDone == fadeDuration) return; // no fade today
        double targetVolume = Interpolator.interpLinear(new double[]{0, fadeStart, fadeDuration, fadeTarget}, fadeDone);
        setVolume((byte) targetVolume);
        fadeDone++;
    }

    public List<Player> getPlayerList() {
        return Collections.unmodifiableList(playerList);
    }

    public void addPlayer(Player p) {
        synchronized (this) {
            if (!playerList.contains(p)) {
                playerList.add(p);
                ArrayList<SongPlayer> songs = NoteBlockAPI.getInstance().playingSongs.get(p.getName());
                if (songs == null) {
                    songs = new ArrayList<>();
                }
                songs.add(this);
                NoteBlockAPI.getInstance().playingSongs.put(p.getName(), songs);
            }
        }
    }

    public boolean getAutoDestroy() {
        synchronized (this) {
            return autoDestroy;
        }
    }

    public void setAutoDestroy(boolean value) {
        synchronized (this) {
            autoDestroy = value;
        }
    }

    public abstract void playTick(Player p, int tick);

    public void destroy() {
        synchronized (this) {
            destroyed = true;
            new ArrayList<>(playerList).forEach(this::removePlayer);
            playing = false;
            setTick((short) -1);
        }
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public short getTick() {
        return tick;
    }

    public void setTick(short tick) {
        this.tick = tick;
    }

    public void removePlayer(Player p) {
        playerList.remove(p);
        if (NoteBlockAPI.getInstance().playingSongs.get(p.getName()) == null) {
            return;
        }
        ArrayList<SongPlayer> songs = new ArrayList<>(
                NoteBlockAPI.getInstance().playingSongs.get(p.getName()));
        songs.remove(this);
        NoteBlockAPI.getInstance().playingSongs.put(p.getName(), songs);
        if (playerList.isEmpty() && autoDestroy) {
            destroy();
        }
    }

    public byte getVolume() {
        return volume;
    }

    public void setVolume(byte volume) {
        this.volume = volume;
    }

    public float getSpeedUp() {
        return speedUp;
    }

    public void setSpeedUp(float speedUp) {
        this.speedUp = speedUp;
    }

    public Song getSong() {
        return song;
    }

    public final void tryPlay() {
        if (!playing) return;
        calculateFade();
        if (System.currentTimeMillis() - lastPlayed < 50 * getSong().getDelay() / this.speedUp) return;
        tick++;
        //Server.getInstance().getLogger().notice("delay: " + getSong().getDelay());
        //Server.getInstance().getLogger().notice(String.valueOf(tick));
        if (tick > song.getLength()) {
            playing = false;
            tick = -1;
            if (autoDestroy) {
                destroy();
                return;
            }
            if (autoCycle) playing = true;
            SongEndEvent ev = new SongEndEvent(this);
            Server.getInstance().getPluginManager().callEvent(ev);
        }
        for (Player p : new ArrayList<>(playerList)) {
            try {
                if (!p.isConnected()) playerList.remove(p); //offline
                if (p.spawned) playTick(p, tick);
            } catch (Exception ignore) {
            }
        }
        lastPlayed = System.currentTimeMillis();
    }
}
