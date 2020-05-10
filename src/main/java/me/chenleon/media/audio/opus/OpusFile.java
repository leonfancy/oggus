package me.chenleon.media.audio.opus;

import com.google.common.primitives.Bytes;
import me.chenleon.media.container.ogg.OggFile;
import me.chenleon.media.container.ogg.OggPacket;
import me.chenleon.media.container.ogg.OggPage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class OpusFile {
    private final CommentHeader commentHeader;
    private final IdHeader idHeader;

    public OpusFile(OggFile oggFile) throws IOException {
        if (!oggFile.hasNextPage()) {
            throw new InvalidOpusException("No ID Header data in this opus file");
        }

        OggPage currentPage = oggFile.nextPage();
        ArrayList<OggPacket> currentPagePackets = currentPage.getOggPackets();
        if(currentPagePackets.size() != 1) {
            throw new InvalidOpusException("First ogg page must only contain 1 data packet");
        }
        OggPacket oggPacket = currentPagePackets.get(0);
        if(oggPacket.isPartial()) {
            throw new InvalidOpusException("ID Header data is corrupted");
        }
        this.idHeader = new IdHeader(oggPacket.getData());

        byte[] commentHeaderData = new byte[0];
        while(oggFile.hasNextPage()) {
            currentPage = oggFile.nextPage();
            currentPagePackets = currentPage.getOggPackets();
            if(currentPagePackets.size() != 1) {
                throw new InvalidOpusException("Comment Header ogg pages must only contain 1 data packet");
            }
            commentHeaderData = Bytes.concat(commentHeaderData, currentPagePackets.get(0).getData());
            if(currentPage.getGranulePosition() == 0) break;
        }
        commentHeader = new CommentHeader(commentHeaderData);
    }

    public IdHeader getIdHeader() {
        return this.idHeader;
    }

    public String getVendor() {
        return commentHeader.getVendor();
    }

    public Map<String, Collection<String>> getTags() {
        return commentHeader.getTags();
    }
}
