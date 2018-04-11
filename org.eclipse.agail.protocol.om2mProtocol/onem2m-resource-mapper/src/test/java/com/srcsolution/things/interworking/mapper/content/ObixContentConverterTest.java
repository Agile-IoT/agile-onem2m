package com.srcsolution.things.interworking.mapper.content;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import com.srcsolution.things.interworking.data.OperationData;

import static org.assertj.core.api.Assertions.assertThat;

public class ObixContentConverterTest {

    ObixContentConverter converter = new ObixContentConverter();

    //    @Test
    //    public void writeSolo() throws Exception {
    //        System.out.println(converter.write(ContentData.of("1")));
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
        m1.put("eight", ZonedDateTime.now());
        m1.put("ten", BigDecimal.TEN);

        m1.put("lala", new OperationContent(
                null,
                new OperationData("sendmsg").setUrl("http://google.fr"), UUID.randomUUID()));

        System.out.println(converter.serialize(m1));
    }

    @Test
    public void readObj() throws Exception {
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<obj>\n" +
                "    <str name=\"zero\"/>\n" +
                "    <bool val=\"false\" name=\"six\"/>\n" +
                "    <real val=\"4.001\" name=\"four\"/>\n" +
                "    <str val=\"1\" name=\"one\"/>\n" +
                //                "    <op href=\"http://google.fr\" name=\"lala\" is=\"execute\"/>\n" +
                "    <obj name=\"seven\">\n" +
                "        <real val=\"1.0\" name=\"0\"/>\n" +
                "        <real val=\"2.0\" name=\"1\"/>\n" +
                "        <real val=\"3.0\" name=\"2\"/>\n" +
                "    </obj>\n" +
                "    <real val=\"10.0\" name=\"ten\"/>\n" +
                "    <obj name=\"two\">\n" +
                "        <str val=\"que tal\" name=\"ola\"/>\n" +
                "        <str val=\"world\" name=\"hello\"/>\n" +
                "    </obj>\n" +
                "    <real val=\"3.0\" name=\"three\"/>\n" +
                "    <bool val=\"true\" name=\"five\"/>\n" +
                "</obj>";

        //        Map<String, Object> read = converter.read(s);
        //        System.out.println(converter.write(read));

        String out = converter.serialize(converter.deserialize(s));
        System.out.println(out);

        assertThat(out).isEqualToNormalizingWhitespace(s);
    }

    //    @Test
    //    public void readStr() throws Exception {
    //        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
    //                "    <str val=\"1\" name=\"one\"/>\n";
    //
    //        ContentData read = converter.read(s);
    //        System.out.println(converter.write(read));
    //    }

}