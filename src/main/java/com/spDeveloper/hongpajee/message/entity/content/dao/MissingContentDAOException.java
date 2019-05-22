package com.spDeveloper.hongpajee.message.entity.content.dao;

public class MissingContentDAOException extends RuntimeException {

	public MissingContentDAOException() {
		super("The corresponding ContentDAO is missing for this type of Content.");
	}

	public MissingContentDAOException(Throwable e) {
		super("The corresponding ContentDAO is missing for this type of Content.", e);
	}
	public MissingContentDAOException(String msg) {
		super(msg);
	}
	public MissingContentDAOException(Throwable e, String msg) {
		super(msg, e);
	}
}
