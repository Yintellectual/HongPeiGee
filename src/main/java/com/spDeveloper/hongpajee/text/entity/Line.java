package com.spDeveloper.hongpajee.text.entity;

import java.util.List;

public interface Line {

	default boolean contentEquals(Line other) {
		if(other==null) {
			return false;
		}else {
			String content1 = this.getContent();
			String content2 = other.getContent();
			if(content1==null||content2==null) {
				return false;
			}else {
				return content1.equals(content2);
			}	
		}
		
	}
	
	
	String getContent();
	String getUuid();
	String getUpdateUUID();
	LineStatus getStatus();
	void setStatus(LineStatus status); 
}

