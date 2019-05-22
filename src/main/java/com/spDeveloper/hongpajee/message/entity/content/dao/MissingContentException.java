package com.spDeveloper.hongpajee.message.entity.content.dao;

public class MissingContentException extends RuntimeException {

	public MissingContentException() {
		super("The Content is missing from the content repository.");
	}

	public MissingContentException(Throwable e) {
		super("The Content is missing from the content repository.", e);
	}
	public MissingContentException(String msg) {
		super(msg);
	}
	public MissingContentException(Throwable e, String msg) {
		super(msg, e);
	}
}
