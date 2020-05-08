package me.chenleon.media.audio.opus;

import me.chenleon.media.container.ogg.OggFile;
import me.chenleon.media.container.ogg.OggPacket;
import me.chenleon.media.container.ogg.OggPage;

import java.io.IOException;
import java.util.ArrayList;

public class OpusFile {
    private IdHeader idHeader = null;

    public OpusFile(OggFile oggFile) throws IOException {
        if(oggFile.hasNextPage()) {
            OggPage oggPage = oggFile.nextPage();
            ArrayList<OggPacket> oggPackets = oggPage.getOggPackets();
            if(oggPackets.size() != 1) {
                throw new InvalidOpusException("First ogg page must only contain 1 data packet");
            }
            OggPacket oggPacket = oggPackets.get(0);
            if(oggPacket.isPartial()) {
                throw new InvalidOpusException("ID Header data is corrupted");
            }
            this.idHeader = new IdHeader(oggPacket.getData());
        } else {
            throw new InvalidOpusException("Ogg file doesn't contains enough data to ");
        }
    }

    public IdHeader getIdHeader() {
        return this.idHeader;
    }
}
