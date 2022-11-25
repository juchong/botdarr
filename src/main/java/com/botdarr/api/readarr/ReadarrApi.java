package com.botdarr.api.readarr;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;

import com.botdarr.Config;
import com.botdarr.api.AddStrategy;
import com.botdarr.api.Api;
import com.botdarr.api.ApiRequestThreshold;
import com.botdarr.api.ApiRequestType;
import com.botdarr.api.ApiRequests;
import com.botdarr.api.CacheContentStrategy;
import com.botdarr.api.CacheProfileStrategy;
import com.botdarr.api.ContentType;
import com.botdarr.api.DownloadsStrategy;
import com.botdarr.api.LookupStrategy;
import com.botdarr.commands.CommandContext;
import com.botdarr.commands.responses.CommandResponse;
import com.botdarr.commands.responses.ErrorResponse;
import com.botdarr.commands.responses.ExistingShowResponse;
import com.botdarr.commands.responses.NewShowResponse;
import com.botdarr.commands.responses.ShowDownloadResponse;
import com.botdarr.commands.responses.ShowProfileResponse;
import com.botdarr.commands.responses.ShowResponse;
import com.botdarr.commands.responses.SuccessResponse;
import com.botdarr.connections.ConnectionHelper;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class ReadarrApi implements Api {
  @Override
  public String getUrlBase() {
    return Config.getProperty(Config.Constants.SONARR_URL_BASE);
  }

  @Override
  public String getApiUrl(String path) {
    return getApiUrl(Config.Constants.SONARR_URL, Config.Constants.SONARR_TOKEN, path);
  }

  @Override
  public List<CommandResponse> downloads() {
    return getDownloadsStrategy().downloads();
  }

  public CommandResponse addWithId(String searchText, String id) {
    return getAddStrategy().addWithSearchId(searchText, id);
  }

  public List<CommandResponse> addWithTitle(String searchText) {
    return getAddStrategy().addWithSearchTitle(searchText);
  }

  public List<CommandResponse> lookup(String search, boolean findNew) {
    return new LookupStrategy<ReadarrShow>(ContentType.SHOW) {

      @Override
      public ReadarrShow lookupExistingItem(ReadarrShow lookupItem) {
        return SONARR_CACHE.getExistingShowFromTvdbId(lookupItem.getTvdbId());
      }

      @Override
      public List<ReadarrShow> lookup(String searchTerm) throws Exception {
        return lookupShows(searchTerm);
      }

      @Override
      public CommandResponse getExistingItem(ReadarrShow existingItem) {
        return new ExistingShowResponse(existingItem);
      }

      @Override
      public CommandResponse getNewItem(ReadarrShow lookupItem) {
        return new NewShowResponse(lookupItem);
      }

      @Override
      public boolean isPathBlacklisted(ReadarrShow item) {
        return ReadarrApi.this.isPathBlacklisted(item);
      }
    }.lookup(search, findNew);
  }

  public List<CommandResponse> getProfiles() {
    Collection<ReadarrProfile> profiles = SONARR_CACHE.getQualityProfiles();
    if (profiles == null || profiles.isEmpty()) {
      return Collections.singletonList(new ErrorResponse("Found 0 profiles, please setup Sonarr with at least one profile"));
    }

    List<CommandResponse> profileMessages = new ArrayList<>();
    for (ReadarrProfile sonarrProfile : profiles) {
      profileMessages.add(new ShowProfileResponse(sonarrProfile));
    }
    return profileMessages;
  }

  @Override
  public void cacheData() {
    new CacheProfileStrategy<ReadarrProfile, String>() {
      @Override
      public void deleteFromCache(List<String> profilesAddUpdated) {
        SONARR_CACHE.removeDeletedProfiles(profilesAddUpdated);
      }

      @Override
      public List<ReadarrProfile> getProfiles() {
        return ConnectionHelper.makeGetRequest(ReadarrApi.this, ReadarrUrls.PROFILE, new ConnectionHelper.SimpleEntityResponseHandler<List<ReadarrProfile>>() {
          @Override
          public List<ReadarrProfile> onSuccess(String response) {
            List<ReadarrProfile> sonarrProfiles = new ArrayList<>();
            JsonParser parser = new JsonParser();
            JsonArray json = parser.parse(response).getAsJsonArray();
            for (int i = 0; i < json.size(); i++) {
              ReadarrProfile sonarrProfile = new Gson().fromJson(json.get(i), ReadarrProfile.class);
              sonarrProfiles.add(sonarrProfile);
            }
            return sonarrProfiles;
          }
        });
      }

      @Override
      public void addProfile(ReadarrProfile profile) {
        SONARR_CACHE.addProfile(profile);
      }
    }.cacheData();

    new CacheContentStrategy<ReadarrShow, Long>(this, ReadarrUrls.SERIES_BASE) {
      @Override
      public void deleteFromCache(List<Long> itemsAddedUpdated) {
        SONARR_CACHE.removeDeletedShows(itemsAddedUpdated);
      }

      @Override
      public Long addToCache(JsonElement cacheItem) {
        ReadarrShow sonarrShow = new Gson().fromJson(cacheItem, ReadarrShow.class);
        SONARR_CACHE.add(sonarrShow);
        return sonarrShow.getKey();
      }
    }.cacheData();
  }

  @Override
  public String getApiToken() {
    return Config.Constants.SONARR_TOKEN;
  }

  private AddStrategy<ReadarrShow> getAddStrategy() {
    return new AddStrategy<ReadarrShow>(ContentType.SHOW) {
      @Override
      public List<ReadarrShow> lookupContent(String search) throws Exception {
        return lookupShows(search);
      }

      @Override
      public List<ReadarrShow> lookupItemById(String id) {
        //TODO: if sonarr has a lookup by id, implement
        return Collections.emptyList();
      }

      @Override
      public boolean doesItemExist(ReadarrShow content) {
        return SONARR_CACHE.doesShowExist(content.getTitle());
      }

      @Override
      public String getItemId(ReadarrShow item) {
        return String.valueOf(item.getTvdbId());
      }

      @Override
      public CommandResponse addContent(ReadarrShow content) {
        return addShow(content);
      }

      @Override
      public CommandResponse getResponse(ReadarrShow item) {
        return new ShowResponse(item);
      }
    };
  }

  private DownloadsStrategy getDownloadsStrategy() {
    return new DownloadsStrategy(this, ReadarrUrls.DOWNLOAD_BASE) {
      @Override
      public CommandResponse getResponse(JsonElement rawElement) {
        ReadarrQueue showQueue = new Gson().fromJson(rawElement, ReadarrQueue.class);
        ReadarrQueueEpisode episode = showQueue.getEpisode();
        if (episode == null) {
          //something is wrong with the download, skip
          LOGGER.error("Series " + showQueue.getSonarrQueueShow().getTitle() + " missing episode info for id " + showQueue.getId());
          return null;
        }
        ReadarrShow sonarrShow = SONARR_CACHE.getExistingShowFromSonarrId(showQueue.getEpisode().getSeriesId());
        if (sonarrShow == null) {
          LOGGER.warn("Could not load sonarr show from cache for id " + showQueue.getEpisode().getSeriesId() + " title=" + showQueue.getSonarrQueueShow().getTitle());
          return null;
        }
        if (isPathBlacklisted(sonarrShow)) {
          LOGGER.warn("The following show is blacklisted: " + sonarrShow.getTitle() + " from being displayed in downloads");
          return null;
        }
        return new ShowDownloadResponse(showQueue);
      }
    };
  }

  private CommandResponse addShow(ReadarrShow sonarrShow) {
    String title = sonarrShow.getTitle();
    //make sure we specify where the show should get downloaded
    sonarrShow.setPath(Config.getProperty(Config.Constants.SONARR_PATH) + "/" + title);
    //make sure the show is monitored
    sonarrShow.setMonitored(true);
    //make sure to have seasons stored in separate folders
    sonarrShow.setSeasonFolder(true);

    String sonarrProfileName = Config.getProperty(Config.Constants.SONARR_DEFAULT_PROFILE);
    ReadarrProfile sonarrProfile = SONARR_CACHE.getProfile(sonarrProfileName.toLowerCase());
    if (sonarrProfile == null) {
      return new ErrorResponse("Could not find sonarr profile for default " + sonarrProfile);
    }
    sonarrShow.setQualityProfileId((int) sonarrProfile.getId());
    String username = CommandContext.getConfig().getUsername();
    ApiRequests apiRequests = new ApiRequests();
    ApiRequestType apiRequestType = ApiRequestType.SHOW;
    if (apiRequests.checkRequestLimits(apiRequestType) && !apiRequests.canMakeRequests(apiRequestType, username)) {
      ApiRequestThreshold requestThreshold = ApiRequestThreshold.valueOf(Config.getProperty(Config.Constants.MAX_REQUESTS_THRESHOLD));
      return new ErrorResponse("Could not add show, user " + username + " has exceeded max show requests for " + requestThreshold.getReadableName());
    }
    try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
      HttpPost post = new HttpPost(getApiUrl(ReadarrUrls.SERIES_BASE));

      post.addHeader("content-type", "application/x-www-form-urlencoded");
      post.setEntity(
        new StringEntity(
          new GsonBuilder().addSerializationExclusionStrategy(excludeUnnecessaryFields).create().toJson(sonarrShow, ReadarrShow.class), Charset.forName("UTF-8")));

      try (CloseableHttpResponse response = client.execute(post)) {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200 && statusCode != 201) {
          return new ErrorResponse("Could not add show, status-code=" + statusCode + ", reason=" + response.getStatusLine().getReasonPhrase());
        }
        //cache show after successful request
        SONARR_CACHE.add(sonarrShow);
        LogManager.getLogger("AuditLog").info("User " + username + " added " + title);
        apiRequests.auditRequest(apiRequestType, username, title);
        return new SuccessResponse("Show " + title + " added, sonarr-detail=" + response.getStatusLine().getReasonPhrase());
      }
    } catch (IOException e) {
      LOGGER.error("Error trying to add show=" + title, e);
      return new ErrorResponse("Error adding show=" + title + ", error=" + e.getMessage());
    }
  }

  private List<ReadarrShow> lookupShows(String search) throws Exception {
    return ConnectionHelper.makeGetRequest(this, ReadarrUrls.LOOKUP_SERIES, "&term=" + URLEncoder.encode(search, "UTF-8"), new ConnectionHelper.SimpleEntityResponseHandler<List<ReadarrShow>>() {
      @Override
      public List<ReadarrShow> onSuccess(String response) {
        List<ReadarrShow> movies = new ArrayList<>();
        JsonParser parser = new JsonParser();
        JsonArray json = parser.parse(response).getAsJsonArray();
        for (int i = 0; i < json.size(); i++) {
          movies.add(new Gson().fromJson(json.get(i), ReadarrShow.class));
        }
        return movies;
      }
    });
  }

  private final ExclusionStrategy excludeUnnecessaryFields = new ExclusionStrategy() {
    @Override
    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
      //profileId breaks the post request to /series for some reason and I don't believe its a required field
      return fieldAttributes.getName().equalsIgnoreCase("profileId");
    }

    @Override
    public boolean shouldSkipClass(Class<?> aClass) {
      return false;
    }
  };

  private boolean isPathBlacklisted(ReadarrShow item) {
    for (String path : Config.getExistingItemBlacklistPaths()) {
      if (item.getPath() != null && item.getPath().startsWith(path)) {
        return true;
      }
    }
    return false;
  }

  private static final ReadarrCache SONARR_CACHE = new ReadarrCache();
  public static final String ADD_SHOW_COMMAND_FIELD_PREFIX = "Add show command";
  public static final String SHOW_LOOKUP_FIELD = "TvdbId";
}
