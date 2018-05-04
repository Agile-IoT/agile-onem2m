package com.srcsolution.things.interworking.mapper.content;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.srcsolution.things.onem2m_client.resource.ContentInstance;

public abstract class ContentConverters {

    public static final ObixContentConverter OBIX_CONTENT_CONVERTER = new ObixContentConverter();

    public static final JsonContentConverter JSON_CONTENT_CONVERTER = new JsonContentConverter();

    public static final Map<String, ContentConverter> CONTENT_CONVERTERS;

    static {
        Map<String, ContentConverter> map = new HashMap<>();
        map.put(JSON_CONTENT_CONVERTER.getMediaType(), JSON_CONTENT_CONVERTER);
        map.put(OBIX_CONTENT_CONVERTER.getMediaType(), OBIX_CONTENT_CONVERTER);
        CONTENT_CONVERTERS = Collections.unmodifiableMap(map);
    }

    public static Map<String, Object> read(ContentInstance instance) {
        return getFor(instance).deserialize(instance.getContent());
    }

    public static ContentConverter getFor(ContentInstance instance) {
        String contentInfo = instance.getContentInfo();
        return CONTENT_CONVERTERS.getOrDefault(contentInfo, JSON_CONTENT_CONVERTER);
    }

    public static ContentConverter getFor(String mediaType) {
        return CONTENT_CONVERTERS.get(mediaType);
    }

}
