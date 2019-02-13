package com.spDeveloper.hongpajee.tag.entity;

import java.util.List;

public interface TagHolder {

	void addTag(String tag);
	List<String> getTags();
	boolean hasTag(String tag);
	void removeTag(String tag);
	
}
