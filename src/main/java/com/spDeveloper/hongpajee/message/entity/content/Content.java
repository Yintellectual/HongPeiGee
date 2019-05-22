package com.spDeveloper.hongpajee.message.entity.content;

import com.spDeveloper.hongpajee.message.entity.content.visitor.ContentVisitor;

public interface Content {
	String getString();
	void accept(ContentVisitor visitor);
	String getUuid();
}
