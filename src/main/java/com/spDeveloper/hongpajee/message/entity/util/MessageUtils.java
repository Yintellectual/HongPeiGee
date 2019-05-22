package com.spDeveloper.hongpajee.message.entity.util;

import com.spDeveloper.hongpajee.message.entity.HeavyMessage;
import com.spDeveloper.hongpajee.message.entity.Message;
import com.spDeveloper.hongpajee.message.entity.content.HTMLContent;
import com.spDeveloper.hongpajee.message.entity.content.TextContent;
import com.spDeveloper.hongpajee.message.entity.content.proxy.HTMLContentVirtualProxy;
import com.spDeveloper.hongpajee.message.entity.content.proxy.TextContentVirtualProxy;

public class MessageUtils {
	public static Message createHTMLMessage(String username, String input) {
		HTMLContentVirtualProxy content = new HTMLContentVirtualProxy(new HTMLContent(input));
		Message htmlMessage = new HeavyMessage(username, content);
		return htmlMessage;
	}
	public static Message createTextMessage(String username, String input) {
		TextContentVirtualProxy content = new TextContentVirtualProxy(new TextContent(input));
		Message message = new HeavyMessage(username, content);
		return message;
	}
}
