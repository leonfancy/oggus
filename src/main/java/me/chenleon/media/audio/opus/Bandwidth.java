package me.chenleon.media.audio.opus;

public enum Bandwidth {
   NB(4000), MB(6000), WB(8000), SWB(12000), FB(20000);

   private final int hz;

   Bandwidth(int hz) {
      this.hz = hz;
   }

   public int getHz() {
      return hz;
   }
}
