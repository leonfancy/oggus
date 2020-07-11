package org.chenliang.oggus.opus;

/**
 * Opus packet only has two types of channel:
 * <ul>
 *    <li>stereo: left and right channels</li>
 *    <li>mono: only a single channel</li>
 * </ul>
 */
public enum Channel {
    MONO, STEREO
}
