# Oggus

Oggus is a Java library for reading and writing Ogg Opus stream. Opus packet structure is supported, but encoding/decoding Opus frame is not supported.

## Introduction 

### Ogg

Ogg is a media container format maintained by the [Xiph.Org Foundation](https://en.wikipedia.org/wiki/Xiph.Org_Foundation). An Ogg bitstream consists of a sequence of Ogg pages. Each page begins with the characters, "OggS", to identify the stream as Ogg format.

The following is the field layout of an Ogg page header

```text
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1| Byte
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
| capture_pattern: Magic number for page start "OggS"           | 0-3
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
| version       | header_type   | granule_position              | 4-7
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                                                               | 8-11
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                               | bitstream_serial_number       | 12-15
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                               | page_sequence_number          | 16-19
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                               | CRC_checksum                  | 20-23
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                               |page_segments  | segment_table | 24-27
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
| ...                                                           | 28-
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
```

For more description about Ogg, please refer to following links:
- [RFC3533: The Ogg Encapsulation Format](https://tools.ietf.org/html/rfc3533)
- [Ogg bitstream overview](https://xiph.org/ogg/doc/oggstream.html)

### Opus

Opus is a lossy audio coding format developed by the [Xiph.Org Foundation](https://en.wikipedia.org/wiki/Xiph.Org_Foundation). Each Opus packet begins with a TOC byte:

```text
 0 1 2 3 4 5 6 7
+-+-+-+-+-+-+-+-+
| config  |s| c |
+-+-+-+-+-+-+-+-+
```
The top five bits of the TOC byte, labeled "config", encode one of 32 possible configurations of operating mode, audio bandwidth, and frame size.

One additional bit, labeled "s", signals mono vs. stereo, with 0 indicating mono and 1 indicating stereo. 

The remaining two bits of the TOC byte, labeled "c", code the number of frames per packet (codes 0 to 3) as follows:

- 0: 1 frame in the packet
- 1: 2 frames in the packet, each with equal compressed size
- 2: 2 frames in the packet, with different compressed sizes
- 3: an arbitrary number of frames in the packet

For more description about Opus, please refer to [RFC6716](https://tools.ietf.org/html/rfc6716#section-3.1).

### Ogg Encapsulation for the Opus Audio Codec

Ogg container format can be used to encapsulate Opus audio bitstream. An Ogg Opus stream is organized as follows:

```text
   Page 0         Pages 1 ... n        Pages (n+1) ...
+------------+ +---+ +---+ ... +---+ +-----------+ +---------+ +--
|            | |   | |   |     |   | |           | |         | |
|+----------+| |+-----------------+| |+-------------------+ +-----
|| ID Header|| ||  Comment Header || ||Audio Data Packet 1| | ...
|+----------+| |+-----------------+| |+-------------------+ +-----
|            | |   | |   |     |   | |           | |         | |
+------------+ +---+ +---+ ... +---+ +-----------+ +---------+ +--
^      ^                           ^
|      |                           |
|      |                           Mandatory Page Break
|      ID header is contained on a single page
'Beginning Of Stream'
```

There are two mandatory header packets.  The first packet in the logical Ogg bitstream MUST contain the identification (ID) header, which uniquely identifies a stream as Opus audio. The second packet in the logical Ogg bitstream MUST contain the comment header, which contains user-supplied metadata. 

All subsequent pages are audio data pages, and the Ogg packets they contain are audio data packets.  Each audio data packet contains one Opus packet for each of N different streams, where N is typically one for mono or stereo, but MAY be greater than one for multichannel audio. The value N is specified in the ID header, and is fixed over the entire length of the logical Ogg bitstream. 

The first (N - 1) Opus packets, if any, are packed one after another into the Ogg packet, using the self-delimiting framing. The remaining Opus packet is packed at the end of the Ogg packet using the regular, undelimited framing. 

For more information, please refer to [RFC7845](https://tools.ietf.org/html/rfc7845).
  
## How to use Oggus

Oggus library makes it easy to read Ogg and Opus stream.

### Read Ogg stream

If you just want to read the Ogg pages and don't want to look into encapsulated format, use the `OggStream` class.

```java
// Create OggStream from a file path
OggStream oggStream = OggStream.from("/some/path/to/test.ogg");

while (true) {
    // Read null if the end of Ogg stream is reached
    OggPage oggPage = oggStream.readPage();
    if (oggPage == null) break;

    // Do something with oggPage
}
```

The Ogg stream may be multiplexed by several logical streams. Each logical stream is marked by the serial number in Ogg page header. Following method of `OggStream` class could be used to read Ogg pages with given serial num.

```java
public OggPage readPage(long serialNum) 
```

### Read Ogg Opus stream

If you care the information of Opus packets in Ogg Opus stream, use the `OggOpusStream` class.

```java
// Create OggOpusStream from a file path
OggOpusStream oggOpusStream = OggOpusStream.from("audio/technology.opus");

// Get ID Header
IdHeader idHeader = oggOpusStream.getIdHeader();

// Get Comment Header
CommentHeader commentHeader = oggOpusStream.getCommentHeader();

while (true) {
    AudioDataPacket audioDataPacket = oggOpusStream.readAudioPacket();
    if (audioDataPacket == null) break;
    
    for (OpusPacket opusPacket : audioDataPacket.getOpusPackets()) {
        // Do something with opusPacket
    }
}
```

### Write Ogg stream

You can create an Ogg stream with Oggus library. It is as simple as following steps: 

- Create an OutputStream.
- Create empty OggPage objects, set header fields.
- Add data packets to OggPage objects.
- Dump the OggPage objects to binary.
- Write the binary to output stream.

```java
// Create an output stream
OutputStream outputStream = ...;

// Create a new Ogg page
OggPage oggPage = OggPage.empty();
// Set header fields by calling setX() method
oggPage.setSerialNum(100);
// Add a data packet to this page
oggPage.addDataPacket(data);
// Add a partial data packet if there is no enough room
oggPage.addPartialDataPacket(data);

// Call dump() method to dump the OggPage object to byte array binary 
byte[] binary = oggPage.dump();

// Write the binary to stream
outputStream.write(binary);
```

### Write Ogg Opus stream

Create Ogg Opus stream is much the same simple Ogg stream described above, except that the data packet in OggPage is dumped from AudioDataPacket object.

```java
// Create an AudioDataPacket which contains 2 Opus packets, that means this is multi-channel stream
AudioDataPacket audioDataPacket = AudioDataPacket.empty();
audioDataPacket.addOpusPacket(opusPack1);
audioDataPacket.addOpusPacket(opusPack2);
byte[] audioDataPacketBinary = audioDataPacket.dump();

// Encapsulate it into an Ogg page
oggPage.addDataPacket(audioDataPacketBinary);
```

An Opus packet could be created with `OpusPackets` class:

```java
OpusPacket opusPacket = OpusPackets.newPacket(Config.of(12), Channel.STEREO, 1);

// Or create from a TOC byte
OpusPacket opusPacket = OpusPackets.newPacketOfToc(14);
```

OpusPacket object could be dumped to two formats:

```java
OpusPacket opusPacket = ...;

// Standard format
opusPacket.dumpToStandardFormat();

// Self-delimiting format
opusPacket.dumpToSelfDelimitingFormat();
```

## License
WTFPL: <a href="http://www.wtfpl.net/"><img
       src="http://www.wtfpl.net/wp-content/uploads/2012/12/wtfpl-badge-4.png"
       width="80" height="15" alt="WTFPL" /></a>
