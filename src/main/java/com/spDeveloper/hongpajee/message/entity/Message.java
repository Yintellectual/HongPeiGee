package com.spDeveloper.hongpajee.message.entity;

import com.spDeveloper.hongpajee.message.entity.content.Content;
import com.spDeveloper.hongpajee.message.entity.content.HTMLContent;
import com.spDeveloper.hongpajee.message.entity.content.proxy.HTMLContentVirtualProxy;

public interface Message {
	String getUuid();
	Long getTimestamp();
	String getUsername();
	String getContentString();
	Content getContent();
}
