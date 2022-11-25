package com.botdarr.api.readarr;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ReadarrCache {
  public ReadarrShow getExistingShowFromTvdbId(long tvdbId) {
    return existingTvdbIdsToShows.get(tvdbId);
  }

  public ReadarrShow getExistingShowFromSonarrId(long sonarrId) {
    return existingSonarrIdsToShows.get(sonarrId);
  }

  public boolean doesShowExist(String title) {
    return existingShowTitlesToSonarrId.containsKey(title.toLowerCase());
  }

  public void add(ReadarrShow show) {
    existingTvdbIdsToShows.put(show.getKey(), show);
    existingShowTitlesToSonarrId.put(show.getTitle().toLowerCase(), show.getId());
    existingSonarrIdsToShows.put(show.getId(), show);
  }

  public Collection<ReadarrProfile> getQualityProfiles() {
    return Collections.unmodifiableCollection(existingProfiles.values());
  }

  public void addProfile(ReadarrProfile qualityProfile) {
    existingProfiles.put(qualityProfile.getKey(), qualityProfile);
  }

  public ReadarrProfile getProfile(String qualityProfileName) {
    return existingProfiles.get(qualityProfileName.toLowerCase());
  }

  public void removeDeletedProfiles(List<String> addUpdatedProfiles) {
    existingProfiles.keySet().retainAll(addUpdatedProfiles);
  }

  public void removeDeletedShows(List<Long> addUpdatedTvdbShowIds) {
    List<String> existingShowTitles = new ArrayList<>();
    List<Long> existingShowIds = new ArrayList<>();
    for (Long tvdbId : addUpdatedTvdbShowIds) {
      ReadarrShow sonarrShow = existingTvdbIdsToShows.get(tvdbId);
      if (sonarrShow != null) {
        existingShowTitles.add(sonarrShow.getTitle().toLowerCase());
        existingShowIds.add(sonarrShow.getId());
      }
    }
    existingShowTitlesToSonarrId.keySet().retainAll(existingShowTitles);
    existingTvdbIdsToShows.keySet().retainAll(addUpdatedTvdbShowIds);
    existingSonarrIdsToShows.keySet().retainAll(existingShowIds);
  }

  private Map<String, ReadarrProfile> existingProfiles = new ConcurrentHashMap<>();
  private Map<String, Long> existingShowTitlesToSonarrId = new ConcurrentHashMap<>();
  private Map<Long, ReadarrShow> existingTvdbIdsToShows = new ConcurrentHashMap<>();
  private Map<Long, ReadarrShow> existingSonarrIdsToShows = new ConcurrentHashMap<>();
}
