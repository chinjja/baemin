package com.chinjja.app.etag;

import java.util.Base64;

public interface Etag {
	Long getVersion();
	
	default String etag() {
		String etag = getClass().getCanonicalName() + getVersion();
		return "\""+Base64.getEncoder().encodeToString(etag.getBytes())+"\"";
	}
}
