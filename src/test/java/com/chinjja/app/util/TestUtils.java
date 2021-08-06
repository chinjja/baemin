package com.chinjja.app.util;

import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.val;

public class TestUtils {
	private static final ObjectMapper mapper = new ObjectMapper();
	
	public static <T> T to(MvcResult result, Class<T> cls) throws Exception {
		int status = result.getResponse().getStatus();
		if(status >= 200 && status < 300) {
			val res = result.getResponse();
			return mapper.readValue(res.getContentAsByteArray(), cls);
		}
		else {
			throw new ResponseStatusException(HttpStatus.valueOf(status));
		}
	}
	
	public static byte[] toBytes(Object obj) throws Exception {
		return mapper.writeValueAsBytes(obj);
	}
}
