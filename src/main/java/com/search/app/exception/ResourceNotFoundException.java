package com.search.app.exception;

import com.search.app.utils.MessageConstants;

public class ResourceNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1389724878927886276L;

	public ResourceNotFoundException() {
		super(MessageConstants.RESOURCE_NOT_FOUND);
	}

	public ResourceNotFoundException(String msg) {
		super(msg);
	}
}
