package com.chinjja.app.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.val;
import lombok.var;

public class TestUtils {
	private static final ObjectMapper mapper = new ObjectMapper();
	
	public static <T> ResponseEntity<T> to(MvcResult result, Class<T> cls) {
		try {
			val res = result.getResponse();
			val etag = res.getHeader("Etag");
			val status = res.getStatus();
			var entity = ResponseEntity.status(status);
			if(etag != null) {
				entity = entity.eTag(etag);
			}
			if(status == HttpStatus.CREATED.value() || status == HttpStatus.OK.value()) {
				if(cls == Void.class) {
					return entity.build();
				}
				val body = mapper.readValue(res.getContentAsByteArray(), cls);
				return entity.body(body);
			}
			else {
				return entity.build();
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static byte[] toBytes(Object obj) throws Exception {
		return mapper.writeValueAsBytes(obj);
	}
}
