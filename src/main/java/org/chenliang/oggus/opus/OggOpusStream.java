package org.chenliang.oggus.opus;

import com.google.common.primitives.Bytes;
import org.chenliang.oggus.ogg.OggPage;
import org.chenliang.oggus.ogg.OggStream;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * A class that provide methods to read an Ogg opus stream. An Ogg Opus stream is organized as follows:
 *
 * <pre>
 *    Page 0         Pages 1 ... n        Pages (n+1) ...
 * +------------+ +---+ +---+ ... +---+ +-----------+ +---------+ +--
 * |            | |   | |   |     |   | |           | |         | |
 * |+----------+| |+-----------------+| |+-------------------+ +-----
 * || ID Header|| ||  Comment Header || ||Audio Data Packet 1| | ...
 * |+----------+| |+-----------------+| |+-------------------+ +-----
 * |            | |   | |   |     |   | |           | |         | |
 * +------------+ +---+ +---+ ... +---+ +-----------+ +---------+ +--
 * ^      ^                           ^
 * |      |                           |
 * |      |                           Mandatory Page Break
 * |      ID header is contained on a single page
 * 'Beginning Of Stream'
 * </pre>
 */
public class OggOpusStream implements Closeable {
    private final CommentHeader commentHeader;
    private final IdHeader idHeader;
    private final OggStream oggStream;
    private final Queue<byte[]> lastPageLeftAudioDataPackets = new LinkedList<>();
    private boolean isLastReadPageCompleted = true;
    private long streamId;
    private boolean isEnd = false;

    private OggOpusStream(OggStream oggStream) throws IOException {
        idHeader = readIdHeader(oggStream);
        commentHeader = readCommentHeader(oggStream);
        this.oggStream = oggStream;
    }

    /**
     * Read Ogg Opus stream from an InputStream.
     *
     * @param inputStream An InputStream that could read the Ogg Opus stream.
     * @return The OggOpusStream object
     * @throws IOException If IO read error
     */
    public static OggOpusStream from(InputStream inputStream) throws IOException {
        return new OggOpusStream(OggStream.from(inputStream));
    }

    /**
     * Read Ogg Opus stream from file
     *
     * @param filePath The file path
     * @return The OggOpusStream object
     * @throws IOException If IO read error
     */
    public static OggOpusStream from(String filePath) throws IOException {
        return new OggOpusStream(OggStream.from(filePath));
    }

    /**
     * Get the Id header of this Ogg Opus stream
     *
     * @return IdHeader
     */
    public IdHeader getIdHeader() {
        return this.idHeader;
    }

    /**
     * Get the Comment header of this Ogg Opus stream
     *
     * @return CommentHeader
     */
    public CommentHeader getCommentHeader() {
        return this.commentHeader;
    }

    /**
     * Read an AudioDataPacket from the Ogg Opus stream. Return {@code null} if this is not more data to read.
     *
     * <p>If there multiple logical streams, the first Opus stream is read.</p>
     *
     * @return AudioDataPacket
     * @throws IOException if IO read error
     */
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
            return AudioDataPacket.from(data, idHeader.getStreamCount());
        }

        byte[] data = lastPageLeftAudioDataPackets.poll();

        if (isLastReadPageCompleted || !lastPageLeftAudioDataPackets.isEmpty()) {
            return AudioDataPacket.from(data, idHeader.getStreamCount());
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
        return AudioDataPacket.from(data, idHeader.getStreamCount());
    }

    /**
     * Close the underlying {@link OggStream}
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        oggStream.close();
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
                throw new InvalidOpusException("Comment Header Ogg pages must only contain 1 data packet");
            }
            commentHeaderData = Bytes.concat(commentHeaderData, currentPagePackets.get(0));
            if (currentPage.getGranulePosition() == 0) break;
        }
        return CommentHeader.from(commentHeaderData);
    }
}
