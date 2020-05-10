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

    public CommentHeader(byte[] commentHeaderData) throws IOException {
        LittleEndianDataInputStream in = new LittleEndianDataInputStream(new ByteArrayInputStream(commentHeaderData));
        if(!Arrays.equals(in.readNBytes(8), MAGIC_SIGNATURE)) {
            throw new InvalidOpusException("Comment Header Packet doesn'tt start with 'OpusTags'");
        }
        int vendorLen = in.readInt();
        if(vendorLen != 0) {
            vendor = new String(in.readNBytes(vendorLen));
        }
        int tagCount = in.readInt();
        for (int i = 0; i < tagCount; i++) {
            int tagStrLen = in.readInt();
            addTag(in.readNBytes(tagStrLen));
        }
    }

    private void addTag(byte[] data) {
        String tagStr = new String(data);
        String[] parts = tagStr.split("=");
        tags.put(parts[0], parts[1]);
    }

    public Map<String, Collection<String>> getTags() {
        return tags.asMap();
    }

    public String getVendor() {
        return vendor;
    }
}
