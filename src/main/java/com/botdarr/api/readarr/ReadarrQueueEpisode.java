package com.botdarr.api.readarr;

public class ReadarrQueueEpisode {
  public int getSeasonNumber() {
    return seasonNumber;
  }

  public void setSeasonNumber(int seasonNumber) {
    this.seasonNumber = seasonNumber;
  }

  public int getEpisodeNumber() {
    return episodeNumber;
  }

  public void setEpisodeNumber(int episodeNumber) {
    this.episodeNumber = episodeNumber;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getOverview() {
    return overview;
  }

  public void setOverview(String overview) {
    this.overview = overview;
  }

  public long getSeriesId() {
    return seriesId;
  }

  private int seasonNumber;
  private int episodeNumber;
  private String title;
  private String overview;
  private long seriesId;
}
