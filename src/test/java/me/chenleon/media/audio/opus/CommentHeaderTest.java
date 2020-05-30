package me.chenleon.media.audio.opus;

import com.google.common.primitives.Bytes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommentHeaderTest {
    @Test
    void should_create_comment_header_given_binary_data_with_zero_vendor_length_and_zero_comment_list_length() {
        byte[] data = {'O', 'p', 'u', 's', 'T', 'a', 'g', 's', 0, 0, 0, 0, 0, 0, 0, 0};
        CommentHeader commentHeader = CommentHeader.from(data);
        assertNull(commentHeader.getVendor());
        assertEquals(0, commentHeader.getTags().size());
    }

    @Test
    void should_read_vendor_string_and_tags_given_valid_binary_data() {
        byte[] data = Bytes.concat(
                new byte[]{'O', 'p', 'u', 's', 'T', 'a', 'g', 's', 11, 0, 0, 0},
                "test-vendor".getBytes(),
                new byte[]{2, 0, 0, 0, 17, 0, 0, 0},
                "TITLE=Space Bound".getBytes(),
                new byte[]{13, 0, 0, 0},
                "ARTIST=Eminem".getBytes()
        );
        CommentHeader commentHeader = CommentHeader.from(data);
        assertEquals("test-vendor", commentHeader.getVendor());
        assertEquals(2, commentHeader.getTags().size());
        assertEquals("Space Bound", String.join(",", commentHeader.getTags().get("TITLE")));
        assertEquals("Eminem", String.join(",", commentHeader.getTags().get("ARTIST")));
    }

    @Test
    void should_read_tags_with_same_field_name() {
        byte[] data = Bytes.concat(
                new byte[]{'O', 'p', 'u', 's', 'T', 'a', 'g', 's', 0, 0, 0, 0},
                new byte[]{2, 0, 0, 0, 11, 0, 0, 0},
                "ARTIST=Slim".getBytes(),
                new byte[]{13, 0, 0, 0},
                "ARTIST=Eminem".getBytes()
        );
        CommentHeader commentHeader = CommentHeader.from(data);
        assertEquals(1, commentHeader.getTags().size());
        assertEquals("Slim,Eminem", String.join(",", commentHeader.getTags().get("ARTIST")));
    }

    @Test
    void should_read_tags_case_insensitive() {
        byte[] data = Bytes.concat(
                new byte[]{'O', 'p', 'u', 's', 'T', 'a', 'g', 's', 0, 0, 0, 0},
                new byte[]{2, 0, 0, 0, 11, 0, 0, 0},
                "Artist=Slim".getBytes(),
                new byte[]{13, 0, 0, 0},
                "ARTIST=Eminem".getBytes()
        );
        CommentHeader commentHeader = CommentHeader.from(data);
        assertEquals(1, commentHeader.getTags().size());
        assertEquals("Slim,Eminem", String.join(",", commentHeader.getTags().get("ARTIST")));
    }

    @Test
    void should_read_tags_with_equal_symbol_in_the_value() {
        byte[] data = Bytes.concat(
                new byte[]{'O', 'p', 'u', 's', 'T', 'a', 'g', 's', 0, 0, 0, 0},
                new byte[]{1, 0, 0, 0, 13, 0, 0, 0},
                "ARTIST=Em=nem".getBytes()
        );
        CommentHeader commentHeader = CommentHeader.from(data);
        assertEquals(1, commentHeader.getTags().size());
        assertEquals("Em=nem", String.join(",", commentHeader.getTags().get("ARTIST")));
    }

    @Test
    void should_throw_exception_given_binary_not_start_with_correct_signature() {
        byte[] data = {'O', 'p', 'u', 's', 'T', 'a', 'g', 'e'};
        InvalidOpusException exception = assertThrows(InvalidOpusException.class, () -> {
            CommentHeader.from(data);
        });
        assertEquals("Comment Header packet doesn't start with 'OpusTags'", exception.getMessage());
    }

    @Test
    void should_throw_exception_given_binary_not_contains_enough_data() {
        byte[] data = {'O', 'p', 'u', 's', 'T', 'a', 'g', 's', 0};
        InvalidOpusException exception = assertThrows(InvalidOpusException.class, () -> {
            CommentHeader.from(data);
        });
        assertEquals("Comment Header data is corrupted", exception.getMessage());
    }
}