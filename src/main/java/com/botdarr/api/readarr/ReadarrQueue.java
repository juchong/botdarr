package com.botdarr.api.readarr;

public class ReadarrQueue {

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getTimeleft() {
    return timeleft;
  }

  public void setTimeleft(String timeleft) {
    this.timeleft = timeleft;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public ReadarrQueueStatusMessages[] getStatusMessages() {
    return statusMessages;
  }

  public void setStatusMessages(ReadarrQueueStatusMessages[] statusMessages) {
    this.statusMessages = statusMessages;
  }

  public ReadarrProfileQualityItem getQuality() {
    return quality;
  }

  public void setQuality(ReadarrProfileQualityItem quality) {
    this.quality = quality;
  }

  public ReadarrQueueShow getSonarrQueueShow() {
    return series;
  }

  public void setRadarrQueueMovie(ReadarrQueueShow radarrQueueMovie) {
    this.series = radarrQueueMovie;
  }

  public ReadarrQueueEpisode getEpisode() {
    return episode;
  }

  public void setEpisode(ReadarrQueueEpisode episode) {
    this.episode = episode;
  }

  private String status;
  private String timeleft;
  private ReadarrProfileQualityItem quality;
  private long id;
  private ReadarrQueueStatusMessages[] statusMessages;
  private ReadarrQueueShow series;
  private ReadarrQueueEpisode episode;
}
