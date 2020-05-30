package me.chenleon.media.audio.opus;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.io.LittleEndianDataInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class CommentHeader {
    private static final byte[] MAGIC_SIGNATURE = {'O', 'p', 'u', 's', 'T', 'a', 'g', 's'};
    private String vendor;
    private final ListMultimap<String, String> tags = ArrayListMultimap.create();

    private CommentHeader() {
    }

    /**
     * Create {@code CommentHeader} from binary data, the data must start with 'OpusTags'.
     *
     * Based on the specification, tag fields are case-insensitive. They are all converted to upper case when parsing
     * from the binary data.
     *
     * @param data the binary data of Comment Header
     * @return the {@code CommentHeader}
     */
    public static CommentHeader from(byte[] data) {
        CommentHeader commentHeader = new CommentHeader();
        LittleEndianDataInputStream in = new LittleEndianDataInputStream(new ByteArrayInputStream(data));
        try {
            if (!Arrays.equals(in.readNBytes(8), MAGIC_SIGNATURE)) {
                throw new InvalidOpusException("Comment Header packet doesn't start with 'OpusTags'");
            }
            int vendorLen = in.readInt();
            if (vendorLen != 0) {
                commentHeader.vendor = new String(in.readNBytes(vendorLen));
            }
            int tagCount = in.readInt();
            for (int i = 0; i < tagCount; i++) {
                int tagStrLen = in.readInt();
                commentHeader.addTag(in.readNBytes(tagStrLen));
            }
            return commentHeader;
        } catch (IOException e) {
            throw new InvalidOpusException("Comment Header data is corrupted");
        }
    }

    /**
     * Get all tags in the comment header. One tag field may have different values, they are store as a collection and
     * share the same key in the returned {@code Map}.
     *
     * @return the tags
     */
    public Map<String, Collection<String>> getTags() {
        return tags.asMap();
    }

    /**
     * Get the Vendor string
     *
     * @return the Vendor string
     */
    public String getVendor() {
        return vendor;
    }

    private void addTag(byte[] data) {
        String tagStr = new String(data);
        String[] parts = tagStr.split("=");
        tags.put(parts[0].toUpperCase(), parts[1]);
    }
}
