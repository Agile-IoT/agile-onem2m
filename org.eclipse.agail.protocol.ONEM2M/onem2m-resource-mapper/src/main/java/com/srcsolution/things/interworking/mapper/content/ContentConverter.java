package com.srcsolution.things.interworking.mapper.content;

import java.util.Map;

public interface ContentConverter {

    String getMediaType();

    String serialize(Map<String, Object> map);

    Map<String, Object> deserialize(String s);

}
