package com.xxmicloxx.NoteBlockAPI.note;

import cn.nukkit.level.sound.SoundEnum;

public class Note {

    private byte instrument;
    private byte key;

    public Note(byte instrument, byte key) {
        switch (instrument) {
            case 1:
                instrument = 4;
                break;
            case 2:
                instrument = 1;
                break;
            case 3:
                instrument = 2;
                break;
            case 4:
                instrument = 3;
                break;
        }
        this.instrument = instrument;
        this.key = key;
    }

    public byte getInstrument(boolean limit) {
        if (limit && instrument > 4) return 0;
        return instrument;
    }

    public SoundEnum getSoundEnum(boolean limit) {
        switch (getInstrument(limit)) {
            case 0:
                return SoundEnum.NOTE_HARP;
            case 1:
                return SoundEnum.NOTE_BASS;
            case 2:
                return SoundEnum.NOTE_SNARE;
            case 3:
                return SoundEnum.NOTE_HAT;
            case 4:
                return SoundEnum.NOTE_BASSATTACK;
            default:
                return SoundEnum.NOTE_HARP;
        }
    }

    public void setInstrument(byte instrument) {
        this.instrument = instrument;
    }

    public byte getKey() {
        return key;
    }

    public void setKey(byte key) {
        this.key = key;
    }

    public float getNoteSoundPitch() {
        return (float) Math.pow(2d, ((double) key - 33d - 12d) / 12d);
    }

}
