package com.xxmicloxx.NoteBlockAPI.player;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.level.sound.SoundEnum;
import cn.nukkit.math.Vector2;
import cn.nukkit.network.protocol.BatchPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import cn.nukkit.network.protocol.PlaySoundPacket;
import cn.nukkit.utils.Binary;
import cn.nukkit.utils.MainLogger;
import cn.nukkit.utils.Zlib;
import com.nukkitx.network.raknet.RakNetReliability;
import com.xxmicloxx.NoteBlockAPI.Song;
import com.xxmicloxx.NoteBlockAPI.note.Layer;
import com.xxmicloxx.NoteBlockAPI.note.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: ml
 * Date: 07.12.13
 * Time: 12:56
 */
public class RadioStereoSongPlayer extends SongPlayer {

    public RadioStereoSongPlayer(Song song) {
        super(song);
    }

    public Block bindBlock = null;

    private double addY = 0;

    @Override
    public void playTick(Player p, int tick) {
        List<DataPacket> batchedPackets = new ArrayList<>();
        //byte playerVolume = NoteBlockAPI.getInstance().getPlayerVolume(p);
        boolean limit = p.getProtocol() < 388;

        for (Layer l : song.getLayerHashMap().values()) {
            Note note = l.getNote(tick);
            if (note == null) {
                continue;
            }
            double subtractY = (double)(100 - l.getVolume()) / 25D;

            double addYaw;
            //if (l.getStereo() == 0) {
                double side = (double)(note.getKey() - 43) / 3D;
                double side0 = side / 4D;
                if (side0 > 1) side0 = 1;
                if (side0 < -1) side0 = -0.1;
                addYaw = 90 * side0;
            //} else {
            //    addYaw = 90 * ((double)l.getStereo() / 100 * 90);
            //}

            Vector2 add = this.getDirectionPlane(p.yaw + addYaw);

            int pitch = note.getKey() - 33;

            if (note.getInstrument(false) >= song.getFirstCustomInstrumentIndex()) {
                PlaySoundPacket psk = new PlaySoundPacket();
                psk.name = song.getCustomInstruments()[note.getInstrument(false) - song.getFirstCustomInstrumentIndex()].getName();
                psk.x = (int) ((float) p.x + (float) add.getX());
                psk.y = (int) ((float) p.y + (float) this.addY + p.getEyeHeight());
                psk.z = (int) ((float) p.z + (float) add.getY());
                psk.pitch = note.getNoteSoundPitch();
                psk.volume = (float) l.getVolume() / 100;
                psk.encode();
                batchedPackets.add(psk);
            } else if ((p.getProtocol() >= 312 && pitch < 0)) {
                PlaySoundPacket psk = new PlaySoundPacket();
                psk.name = note.getSoundEnum(true).getSound();
                psk.x = (int) ((float) p.x + (float) add.getX());
                psk.y = (int) ((float) p.y + (float) this.addY);
                psk.z = (int) ((float) p.z + (float) add.getY());
                psk.pitch = note.getNoteSoundPitch();
                psk.volume =  (float) l.getVolume() / 100;
                psk.encode();
                batchedPackets.add(psk);
            } else {
                LevelSoundEventPacket pk = new LevelSoundEventPacket();
                pk.x = (float) p.x + (float) add.getX();
                pk.y = (float) p.y - (float)subtractY + (float) this.addY;
                pk.z = (float) p.z + (float) add.getY();
                pk.sound = LevelSoundEventPacket.SOUND_NOTE;
                pk.extraData = note.getInstrument(limit);
                pk.pitch = pitch;
                pk.encode();

                batchedPackets.add(pk);
            }

        }

        for (DataPacket pk: batchedPackets) {
            p.dataPacket(pk.setReliability(RakNetReliability.UNRELIABLE));
        }
        //Server.getInstance().batchPackets(new Player[]{p}, batchedPackets.toArray(new DataPacket[0]), true);
    }

    public void setAddY(double addY) {
        this.addY = addY;
    }

    public double getAddY() {
        return addY;
    }

    private Vector2 getDirectionPlane(double yaw) {
        yaw = (yaw + 360) % 360;
        return (new Vector2((float) (-Math.cos(Math.toRadians(yaw) - Math.PI / 2)), (float) (-Math.sin(Math.toRadians(yaw) - Math.PI / 2)))).normalize();
    }
}
