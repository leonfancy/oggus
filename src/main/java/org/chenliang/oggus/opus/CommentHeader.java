package org.chenliang.oggus.opus;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * The Comment Header packet of a Ogg Opus stream. It has following structure:
 * <pre>
 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |      'O'      |      'p'      |      'u'      |      's'      |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |      'T'      |      'a'      |      'g'      |      's'      |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                     Vendor String Length                      |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                                                               |
 * :                        Vendor String...                       :
 * |                                                               |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                   User Comment List Length                    |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                 User Comment #0 String Length                 |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                                                               |
 * :                   User Comment #0 String...                   :
 * |                                                               |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                 User Comment #1 String Length                 |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * :                                                               :
 * </pre>
 */
public class CommentHeader {
    public static final byte[] MAGIC_SIGNATURE = {'O', 'p', 'u', 's', 'T', 'a', 'g', 's'};
    private String vendor;
    private final ListMultimap<String, String> tags = ArrayListMultimap.create();

    private CommentHeader() {
    }

    /**
     * Parse {@code CommentHeader} from binary data, the data must start with 'OpusTags'.
     * <p>
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
                String tagStr = new String(in.readNBytes(tagStrLen));
                String[] parts = tagStr.split("=", 2);
                if (parts.length == 2) {
                    commentHeader.tags.put(parts[0].toUpperCase(), parts[1]);
                }
            }
            return commentHeader;
        } catch (IOException e) {
            throw new InvalidOpusException("Comment Header data is corrupted");
        }
    }

    /**
     * Create an empty Comment Header.
     *
     * @return CommentHeader
     */
    public static CommentHeader emptyHeader() {
        return new CommentHeader();
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

    /**
     * Set Vendor
     *
     * @param vendor the Vendor string
     */
    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    /**
     * Add a tag, the key will be transformed to upper case.
     *
     * @param key   tag name
     * @param value tag value
     */
    public void addTag(String key, String value) {
        this.tags.put(key.toUpperCase(), value);
    }

    /**
     * Dump {@code CommentHeader} to binary byte array.
     *
     * @return the binary data
     */
    public byte[] dump() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        LittleEndianDataOutputStream out = new LittleEndianDataOutputStream(byteArrayOutputStream);

        try {
            out.write(MAGIC_SIGNATURE);
            if (vendor == null) {
                out.writeInt(0);
            } else {
                out.writeInt(vendor.length());
                out.write(vendor.getBytes(StandardCharsets.UTF_8));
            }
            out.writeInt(tags.size());
            for (Map.Entry<String, String> tag : tags.entries()) {
                String commentString = tag.getKey() + "=" + tag.getValue();
                out.writeInt(commentString.length());
                out.write(commentString.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new RuntimeException("CommentHeader dump to byte array error", e);
        }

        return byteArrayOutputStream.toByteArray();
    }
}
