package com.xxmicloxx.NoteBlockAPI;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.plugin.PluginBase;
import com.xxmicloxx.NoteBlockAPI.player.SongPlayer;
import com.xxmicloxx.NoteBlockAPI.runnable.TickerRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Snake1999 on 2016/1/19.
 * Package com.xxmicloxx.NoteBlockAPI in project NuclearMusic.
 */
public class NoteBlockAPI extends PluginBase{

    private static NoteBlockAPI instance;
    public List<SongPlayer> playing = new ArrayList<>();
    public HashMap<String, ArrayList<SongPlayer>> playingSongs = new HashMap<>();
    public HashMap<String, Byte> playerVolume = new HashMap<>();

    public static NoteBlockAPI getInstance() {
        if (instance == null) instance = new NoteBlockAPI();
        return instance;
    }

    public void onLoad() {
        instance = this;
    }

    public void onEnable() {
        getLogger().info("! NoteBlockAPI !");
        Thread ticker = new Thread(new TickerRunnable());
        ticker.setName("NoteBlock Ticker");
        ticker.start();
    }

    public void onDisable() {
        getServer().getScheduler().cancelTask(this);
    }

    public boolean isReceivingSong(Player p) {
        return ((playingSongs.get(p.getName()) != null) && (!playingSongs.get(p.getName()).isEmpty()));
    }

    public void stopPlaying(Player p) {
        if (playingSongs.get(p.getName()) == null) {
            return;
        }
        for (SongPlayer s : playingSongs.get(p.getName())) {
            s.removePlayer(p);
        }
    }

    public void setPlayerVolume(Player p, byte volume) {
        playerVolume.put(p.getName(), volume);
    }

    public byte getPlayerVolume(Player p) {
        Byte b = playerVolume.get(p.getName());
        if (b == null) {
            b = 100;
            playerVolume.put(p.getName(), b);
        }
        return b;
    }
}
