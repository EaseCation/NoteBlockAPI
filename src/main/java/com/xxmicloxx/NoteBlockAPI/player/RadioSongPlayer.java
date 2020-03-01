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

            boolean limit = p.getProtocol() < 388;

            int pitch = note.getKey() - 33;
            if (note.getInstrument(false) >= song.getFirstCustomInstrumentIndex()) {
                PlaySoundPacket psk = new PlaySoundPacket();
                psk.name = song.getCustomInstruments()[note.getInstrument(false) - song.getFirstCustomInstrumentIndex()].getName();
                psk.x = (int) ((float) p.x);
                psk.y = (int) ((float) p.y + p.getEyeHeight());
                psk.z = (int) ((float) p.z);
                psk.pitch = note.getNoteSoundPitch();
                psk.volume = (float) l.getVolume() / 100;
                psk.encode();
                batchedPackets.add(psk);
            } else if (p.getProtocol() >= 312 && pitch < 0) {
                PlaySoundPacket psk = new PlaySoundPacket();
                psk.name = note.getSoundEnum(limit).getSound();
                psk.x = (int) p.x;
                psk.y = (int) p.y;
                psk.z = (int) p.z;
                psk.pitch = note.getNoteSoundPitch();
                psk.volume = (float) l.getVolume() / 100;
                psk.encode();
                batchedPackets.add(psk);
            } else {
                LevelSoundEventPacket pk = new LevelSoundEventPacket();
                pk.x = (float) p.x;
                pk.y = (float) p.y;
                pk.z = (float) p.z;
                pk.sound = LevelSoundEventPacket.SOUND_NOTE;
                pk.extraData = note.getInstrument(limit);
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
