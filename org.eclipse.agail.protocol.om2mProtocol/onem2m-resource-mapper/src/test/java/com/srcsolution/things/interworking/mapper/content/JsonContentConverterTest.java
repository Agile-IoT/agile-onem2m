package com.srcsolution.things.interworking.mapper.content;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonContentConverterTest {

    public static final JsonContentConverter CONTENT_CONVERTER = new JsonContentConverter();

    //    @Test
    //    public void write() throws Exception {
    //        Map<String, ContentData> m2 = new HashMap<>();
    //        m2.put("hello", ContentData.of("world"));
    //        m2.put("ola", ContentData.of("que tal"));
    //
    //        Map<String, ContentData> m1 = new HashMap<>();
    //        m1.put("zero", null);
    //        m1.put("one", ContentData.of("1"));
    //        m1.put("two", ContentData.of(m2));
    //        m1.put("three", ContentData.of(3));
    //        m1.put("four", ContentData.of(4.001));
    //        m1.put("five", ContentData.of(true));
    //        m1.put("six", ContentData.of(false));
    //        m1.put("seven", ContentData.of(new ContentData[] {ContentData.of(1), ContentData.of(2), ContentData.of(3)}));
    //        m1.put("ten", ContentData.of(BigDecimal.TEN));
    //
    //        ObjectContentData content = ContentData.of(m1);
    //
    //        System.out.println(CONTENT_CONVERTER.write(content));
    //    }

    @Test
    public void write() throws Exception {
        Map<String, Object> m2 = new HashMap<>();
        m2.put("hello", "world");
        m2.put("ola", "que tal");

        Map<String, Object> m1 = new HashMap<>();
        m1.put("zero", null);
        m1.put("one", "un");
        m1.put("two", m2);
        m1.put("three", 3);
        m1.put("four", 4.001);
        m1.put("five", true);
        m1.put("six", false);
        m1.put("seven", new String[] {"un", "deux", "trois"});
        m1.put("ten", BigDecimal.TEN);

        System.out.println(CONTENT_CONVERTER.serialize(m1));
    }

    @Test
    public void read() throws Exception {
        String s = "{\"zero\":null,\"six\":false,\"four\":4.001,\"one\":\"1\",\"seven\":[1,2,3],\"ten\":10,\"two\":{\"ola\":\"que tal\",\"hello\":\"world\"},\"three\":3,\"five\":true}";

        String out = CONTENT_CONVERTER.serialize(CONTENT_CONVERTER.deserialize(s));
        System.out.println(out);

        assertThat(out).isEqualToNormalizingWhitespace(s);
    }

}