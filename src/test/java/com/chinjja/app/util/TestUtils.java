package com.chinjja.app.util;

import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.val;

public class TestUtils {
	private static final ObjectMapper mapper = new ObjectMapper();
	
	public static <T> T to(MvcResult result, Class<T> cls) throws Exception {
		val res = result.getResponse();
		return mapper.readValue(res.getContentAsByteArray(), cls);
	}
}
