package me.chenleon.media.audio.opus;

import com.google.common.primitives.Bytes;
import me.chenleon.media.container.ogg.OggPage;
import me.chenleon.media.container.ogg.OggStream;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class OpusFile {
    private final CommentHeader commentHeader;
    private final IdHeader idHeader;
    private final OggStream oggStream;
    private final Queue<byte[]> lastPageLeftAudioDataPackets = new LinkedList<>();
    private boolean isLastReadPageCompleted = true;
    private long streamId;
    private boolean isEnd = false;

    public OpusFile(OggStream oggStream) throws IOException {
        idHeader = readIdHeader(oggStream);
        commentHeader = readCommentHeader(oggStream);
        this.oggStream = oggStream;
    }

    private IdHeader readIdHeader(OggStream oggStream) throws IOException {
        OggPage oggPage = readOpusBosPage(oggStream);
        streamId = oggPage.getSerialNum();
        return IdHeader.from(oggPage.getDataPackets().get(0));
    }

    private OggPage readOpusBosPage(OggStream oggStream) throws IOException {
        while (true) {
            OggPage oggPage = oggStream.readPage();
            if (oggPage == null) {
                throw new InvalidOpusException("No ID Header data in this opus file");
            }

            if (oggPage.isBOS()) {
                if (oggPage.getDataPackets().size() > 1) {
                    throw new InvalidOpusException("The ID Header Ogg page must NOT contain other data");
                }
                return oggPage;
            }
        }
    }

    private CommentHeader readCommentHeader(OggStream oggStream) throws IOException {
        byte[] commentHeaderData = new byte[0];
        while (true) {
            OggPage currentPage = oggStream.readPage(streamId);
            List<byte[]> currentPagePackets = currentPage.getDataPackets();
            if (currentPagePackets.size() != 1) {
                throw new InvalidOpusException("Comment Header ogg pages must only contain 1 data packet");
            }
            commentHeaderData = Bytes.concat(commentHeaderData, currentPagePackets.get(0));
            if (currentPage.getGranulePosition() == 0) break;
        }
        return CommentHeader.from(commentHeaderData);
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

    public AudioDataPacket readAudioPacket() throws IOException {
        if (lastPageLeftAudioDataPackets.isEmpty()) {
            if (isEnd) {
                return null;
            }

            byte[] data = new byte[0];
            while (true) {
                OggPage oggPage = oggStream.readPage(streamId);
                if (oggPage == null) {
                    throw new InvalidOpusException("Corrupted opus binary data");
                }
                isEnd = oggPage.isEOS();
                isLastReadPageCompleted = oggPage.isCompleted();
                lastPageLeftAudioDataPackets.addAll(oggPage.getDataPackets());
                data = Bytes.concat(data, lastPageLeftAudioDataPackets.poll());
                if (isEnd || oggPage.getDataPackets().size() != 1 || oggPage.isCompleted()) {
                    break;
                }
            }
            return new AudioDataPacket(data, idHeader.getStreamCount());
        }

        byte[] data = lastPageLeftAudioDataPackets.poll();

        if (!lastPageLeftAudioDataPackets.isEmpty()) {
            return new AudioDataPacket(data, idHeader.getStreamCount());
        }

        if (isLastReadPageCompleted) {
            return new AudioDataPacket(data, idHeader.getStreamCount());
        }

        while (true) {
            OggPage oggPage = oggStream.readPage(streamId);
            if (oggPage == null) {
                throw new InvalidOpusException("Corrupted opus binary data");
            }
            isEnd = oggPage.isEOS();
            isLastReadPageCompleted = oggPage.isCompleted();
            lastPageLeftAudioDataPackets.addAll(oggPage.getDataPackets());
            data = Bytes.concat(data, lastPageLeftAudioDataPackets.poll());
            if (isEnd || oggPage.getDataPackets().size() != 1 || oggPage.isCompleted()) {
                break;
            }
        }
        return new AudioDataPacket(data, idHeader.getStreamCount());
    }
}
