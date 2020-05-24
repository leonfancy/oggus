package me.chenleon.media.audio.opus;

import com.google.common.primitives.Bytes;
import me.chenleon.media.container.ogg.OggStream;
import me.chenleon.media.container.ogg.OggPacket;
import me.chenleon.media.container.ogg.OggPage;

import java.io.IOException;
import java.util.*;

public class OpusFile {
    private final CommentHeader commentHeader;
    private final IdHeader idHeader;
    private final OggStream oggStream;
    private final Queue<OggPacket> oggPacketBuffer = new LinkedList<>();

    public OpusFile(OggStream oggStream) throws IOException {
        idHeader = readIdHeader(oggStream);
        commentHeader = readCommentHeader(oggStream);
        this.oggStream = oggStream;
    }

    private IdHeader readIdHeader(OggStream oggStream) throws IOException {
        if (!oggStream.hasNextPage()) {
            throw new InvalidOpusException("No ID Header data in this opus file");
        }

        OggPage currentPage = oggStream.nextPage();
        LinkedList<OggPacket> currentPagePackets = currentPage.getOggPackets();
        if (currentPagePackets.size() != 1) {
            throw new InvalidOpusException("First ogg page must only contain 1 data packet");
        }
        OggPacket oggPacket = currentPagePackets.pollFirst();
        if (oggPacket.isPartial()) {
            throw new InvalidOpusException("ID Header data is corrupted");
        }
        return new IdHeader(oggPacket.getData());
    }

    private CommentHeader readCommentHeader(OggStream oggStream) throws IOException {
        OggPage currentPage;
        LinkedList<OggPacket> currentPagePackets;
        byte[] commentHeaderData = new byte[0];
        while (oggStream.hasNextPage()) {
            currentPage = oggStream.nextPage();
            currentPagePackets = currentPage.getOggPackets();
            if (currentPagePackets.size() != 1) {
                throw new InvalidOpusException("Comment Header ogg pages must only contain 1 data packet");
            }
            commentHeaderData = Bytes.concat(commentHeaderData, currentPagePackets.pollFirst().getData());
            if (currentPage.getGranulePosition() == 0) break;
        }
        return new CommentHeader(commentHeaderData);
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

    public AudioDataPacket readAudioDataPacket() throws IOException {
        OggPacket oggPacket = oggPacketBuffer.poll();
        if (oggPacket != null && !oggPacket.isPartial()) {
            return new AudioDataPacket(oggPacket.getData(), idHeader.getStreamCount());
        }
        while (oggStream.hasNextPage()) {
            OggPage oggPage = oggStream.nextPage();
            LinkedList<OggPacket> oggPackets = oggPage.getOggPackets();
            if (oggPackets.size() < 1) {
                throw new InvalidOpusException("At least one packet must be contained in a ogg page");
            }

            if (oggPage.isContinued() && oggPacket != null) {
                oggPacket = oggPacket.concat(oggPackets.poll());
            } else if (!oggPage.isContinued() && oggPacket == null) {
                oggPacket = oggPackets.pollFirst();
            } else {
                throw new InvalidOpusException("Invalid state of Opus file");
            }
            if (oggPage.getGranulePosition() != -1) {
                oggPacketBuffer.addAll(oggPackets);
                break;
            }
        }
        if(oggPacket == null) {
            return null;
        }
        return new AudioDataPacket(oggPacket.getData(), idHeader.getStreamCount());
    }
}
