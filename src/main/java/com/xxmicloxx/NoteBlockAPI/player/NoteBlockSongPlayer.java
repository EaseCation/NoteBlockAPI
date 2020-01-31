package com.xxmicloxx.NoteBlockAPI.player;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.network.protocol.BlockEventPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import cn.nukkit.network.protocol.PlaySoundPacket;
import com.nukkitx.network.raknet.RakNetReliability;
import com.xxmicloxx.NoteBlockAPI.*;
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
public class NoteBlockSongPlayer extends SongPlayer {
    private Block[] noteBlock;
    public int distance = 24;

    public NoteBlockSongPlayer(Song song) {
        super(song);
    }

    public Block[] getNoteBlock() {
        return noteBlock;
    }

    public void setNoteBlock(Block noteBlock) {
        this.setNoteBlock(new Block[]{noteBlock});
    }

    public void setNoteBlock(Block[] noteBlock) {
        this.noteBlock = noteBlock;
    }

    @Override
    public void playTick(Player p, int tick) {
        if (noteBlock.length <= 0) {
            return;
        }
        if (!p.getLevel().getFolderName().equals(noteBlock[0].getLevel().getFolderName())) {
            // not in same world
            return;
        }
        List<DataPacket> batchedPackets = new ArrayList<>();
        //byte playerVolume = NoteBlockAPI.getInstance().getPlayerVolume(p);
        int distanceSquared = distance * distance;
        for (Block noteBlock: this.noteBlock.clone()) {
            if (p.distanceSquared(noteBlock) < distanceSquared) {  //48
                for (Layer l : song.getLayerHashMap().values()) {
                    Note note = l.getNote(tick);
                    if (note == null) {
                        continue;
                    }

                    int pitch = note.getKey() - 33;

                    BlockEventPacket pk = new BlockEventPacket();
                    pk.x = (int) noteBlock.x;
                    pk.y = (int) noteBlock.y;
                    pk.z = (int) noteBlock.z;
                    pk.case1 = note.getInstrument(true);
                    pk.case2 = pitch;
                    pk.encode();

                    float subtractY = (float)(100 - l.getVolume()) / 25F;
                    if (p.getProtocol() >= 312 && pitch < 0) {
                        PlaySoundPacket psk = new PlaySoundPacket();
                        psk.name = note.getSoundEnum(true).getSound();
                        psk.x = (int) noteBlock.x;
                        psk.y = (int) noteBlock.y;
                        psk.z = (int) noteBlock.z;
                        psk.pitch = note.getNoteSoundPitch();
                        psk.volume = 10;
                        psk.encode();
                        batchedPackets.add(psk);
                    } else {
                        LevelSoundEventPacket pk1 = new LevelSoundEventPacket();
                        pk1.x = (float) noteBlock.x + 0.5f;
                        pk1.y = (float) noteBlock.y - subtractY + 0.5f;
                        pk1.z = (float) noteBlock.z + 0.5f;
                        pk1.sound = LevelSoundEventPacket.SOUND_NOTE;
                        pk1.extraData = note.getInstrument(true);
                        pk1.pitch = pitch;
                        pk1.encode();
                        batchedPackets.add(pk1);
                    }

                    batchedPackets.add(pk);

                }
            }
            //p.getLevel().addSound(new MusicBlocksSound(noteBlock, note.getInstrument(), note.getKey()), new Player[]{p});
        }
        for (DataPacket pk: batchedPackets) {
            p.dataPacket(pk.setReliability(RakNetReliability.UNRELIABLE));
        }
        //Server.getInstance().batchPackets(new Player[]{p}, batchedPackets.stream().toArray(DataPacket[]::new), true);
    }
}
