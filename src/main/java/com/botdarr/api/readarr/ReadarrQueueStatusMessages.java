package com.botdarr.api.readarr;

public class ReadarrQueueStatusMessages {
  public String[] getMessages() {
    return messages;
  }

  public void setMessages(String[] messages) {
    this.messages = messages;
  }


  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  private String[] messages;
  private String title;
}
