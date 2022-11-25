package com.botdarr.api.readarr;

public class ReadarrSeason {
  public int getSeasonNumber() {
    return seasonNumber;
  }

  public void setSeasonNumber(int seasonNumber) {
    this.seasonNumber = seasonNumber;
  }

  public boolean isMonitored() {
    return monitored;
  }

  public void setMonitored(boolean monitored) {
    this.monitored = monitored;
  }

  public ReadarrSeasonStatistics getStatistics() {
    return statistics;
  }

  public void setStatistics(ReadarrSeasonStatistics statistics) {
    this.statistics = statistics;
  }

  private int seasonNumber;
  private boolean monitored;
  private ReadarrSeasonStatistics statistics;
}
