package com.botdarr.api.readarr;

import com.botdarr.api.KeyBased;

import java.util.List;

public class ReadarrProfile implements KeyBased<String> {
  @Override
  public String getKey() {
    return name.toLowerCase();
  }

  public String getName() {
    return name;
  }

  public ReadarrProfileCutoff getCutoff() {
    return cutoff;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setCutoff(ReadarrProfileCutoff cutoff) {
    this.cutoff = cutoff;
  }

  public List<ReadarrProfileQualityItem> getItems() {
    return items;
  }

  public void setItems(List<ReadarrProfileQualityItem> items) {
    this.items = items;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  private String name;
  private ReadarrProfileCutoff cutoff;
  private List<ReadarrProfileQualityItem> items;
  private long id;
}
