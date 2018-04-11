package com.srcsolution.things.interworking.mapper.content;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.eclipse.om2m.commons.obix.Obj;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class ObixMapper {

    public static final Logger LOGGER = getLogger(ObixMapper.class);

    private static ObixMapper obixMapper;

    private JAXBContext context;

    private ObixMapper() {
        try {
            this.context = JAXBContext.newInstance("org.eclipse.om2m.commons.obix");
        } catch (JAXBException var2) {
            LOGGER.error("Error creating the JAXB context for Obix objects", var2);
        }

    }

    public static ObixMapper getInstance() {
        if (obixMapper == null) {
            obixMapper = new ObixMapper();
        }

        return obixMapper;
    }

    protected JAXBContext getJAXBContext() {
        return this.context;
    }

    public static String encode(Obj obj) {
        try {
            Marshaller marshaller = ObixMapper.getInstance().getJAXBContext().createMarshaller();
            marshaller.setProperty("jaxb.formatted.output", false);
            OutputStream outputStream = new ByteArrayOutputStream();
            marshaller.marshal(obj, outputStream);
            return outputStream.toString();
        } catch (JAXBException var3) {
            LOGGER.error("Error in encoding oBIX object", var3);
            return null;
        }
    }

    public static Obj decode(String representation) {
        StringReader stringReader = new StringReader(representation);

        try {
            Unmarshaller unmarshaller = ObixMapper.getInstance().getJAXBContext().createUnmarshaller();
            return (Obj) unmarshaller.unmarshal(stringReader);
        } catch (JAXBException var3) {
            LOGGER.error("Error in decoding oBIX object", var3);
            return null;
        }
    }

}
