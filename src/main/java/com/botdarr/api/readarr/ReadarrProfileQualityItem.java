package com.botdarr.api.readarr;

public class ReadarrProfileQualityItem {
  public ReadarrProfileQuality getQuality() {
    return quality;
  }

  public void setQuality(ReadarrProfileQuality quality) {
    this.quality = quality;
  }

  public boolean isAllowed() {
    return allowed;
  }

  public void setAllowed(boolean allowed) {
    this.allowed = allowed;
  }

  private ReadarrProfileQuality quality;
  private boolean allowed;
}
