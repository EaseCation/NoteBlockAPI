package com.xxmicloxx.NoteBlockAPI.player;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import cn.nukkit.network.protocol.PlaySoundPacket;
import com.nukkitx.network.raknet.RakNetReliability;
import com.xxmicloxx.NoteBlockAPI.*;
import com.xxmicloxx.NoteBlockAPI.note.Layer;
import com.xxmicloxx.NoteBlockAPI.note.Note;

import java.util.*;

public class RadioSongPlayer extends SongPlayer {

    public RadioSongPlayer(Song song) {
        super(song);
    }

    @Override
    public void playTick(Player p, int tick) {
        List<DataPacket> batchedPackets = new ArrayList<>();
        for (Layer l : song.getLayerHashMap().values()) {
            Note note = l.getNote(tick);
            if (note == null) {
                continue;
            }

            int pitch = note.getKey() - 33;

            if (p.getProtocol() >= 312 && pitch < 0) {
                PlaySoundPacket psk = new PlaySoundPacket();
                psk.name = note.getSoundEnum(true).getSound();
                psk.x = (int) p.x;
                psk.y = (int) p.y;
                psk.z = (int) p.z;
                psk.pitch = note.getNoteSoundPitch();
                psk.volume = 10;
                psk.encode();
                batchedPackets.add(psk);
            } else {
                LevelSoundEventPacket pk = new LevelSoundEventPacket();
                pk.x = (float) p.x;
                pk.y = (float) p.y;
                pk.z = (float) p.z;
                pk.sound = LevelSoundEventPacket.SOUND_NOTE;
                pk.extraData = note.getInstrument(true);
                pk.pitch = note.getKey() - 33;
                pk.encode();
                batchedPackets.add(pk);
            }

        }

        for (DataPacket pk: batchedPackets) {
            p.dataPacket(pk.setReliability(RakNetReliability.UNRELIABLE));
        }
        //Server.getInstance().batchPackets(new Player[]{p}, batchedPackets.stream().toArray(DataPacket[]::new), true);
    }

}
