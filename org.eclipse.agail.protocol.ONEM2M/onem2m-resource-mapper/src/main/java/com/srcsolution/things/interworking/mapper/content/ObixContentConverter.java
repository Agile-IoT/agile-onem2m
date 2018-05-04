package com.srcsolution.things.interworking.mapper.content;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.om2m.commons.obix.*;
import org.slf4j.Logger;

import com.srcsolution.things.interworking.data.OperationData;

import static org.slf4j.LoggerFactory.getLogger;

public class ObixContentConverter implements ContentConverter {

    public static final Logger LOGGER = getLogger(ObixContentConverter.class);

    @Override
    public String getMediaType() {
        return "application/obix";
    }

    @Override
    public String serialize(Map<String, Object> content) {
        String out = "";
        if (content != null) {
            try {
                Obj obj = serializeObj(null, content);
                out = ObixMapper.encode(obj);
            } catch (IOException e) {
                throw new IllegalArgumentException("Cannot convert content data to " + getMediaType());
            }
        }
        return out;
    }

    private Obj serializeObj(String name, Object obj) throws IOException {
        if (obj == null) {
            return new Str(name, null);

        } else if (obj instanceof Map) {
            Obj o = new Obj();
            o.setName(name);
            Map<String, ?> map = (Map<String, ?>) obj;
            for (String key : map.keySet()) {
                o.add(serializeObj(key, map.get(key)));
            }
            return o;

        } else if (obj instanceof OperationContent) {
            OperationContent operationContent = (OperationContent) obj;
            OperationData operation = operationContent.getOperation();

            Op op = new Op();
            op.setName(name);
            op.setHref(operationContent.getUrl());

            if (operation.getParameters().size() > 0) {
                List<String> parametersStr = new ArrayList<>();
                operation.getParameters().forEach((key, value) -> parametersStr.add(key));
                String[] parametersArr = new String[parametersStr.size()];
                parametersArr = parametersStr.toArray(parametersArr);
                op.setIn(new Contract(parametersArr));

            } else {
                op.setIn(new Contract("obix:Nil"));
            }
            op.setOut(new Contract("obix:Nil"));
            op.setIs(new Contract("execute"));
            return op;

        } else if (obj.getClass().isArray()) {
            Obj o = new Obj();
            o.setName(name);
            Object[] array = (Object[]) obj;
            for (int i = 0; i < array.length; i++) {
                o.add(serializeObj(String.valueOf(i), array[i]));
            }
            return o;

        } else if (obj.getClass().isEnum()) {
            return new Str(name, String.valueOf(obj));

        } else if (obj instanceof ZonedDateTime) {
            String d = ((ZonedDateTime) obj).format(DateTimeFormatter.ISO_DATE_TIME);
            return new Str(name, d);

        } else if (obj instanceof Number) {
            if (obj instanceof Integer) {
                return new Int(name, ((Number) obj).intValue());
            } else if (obj instanceof Double || obj instanceof Float) {
                return new Real(name, ((Number) obj).intValue());
            } else {
                return new Str(name, obj.toString());
            }

        } else if (obj instanceof Boolean) {
            return new Bool(name, (Boolean) obj);

        } else if (obj instanceof String) {
            return new Str(name, (String) obj);

        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> deserialize(String s) {
        try {
            Obj obj = ObixMapper.decode(s);
            return (Map<String, Object>) deserializeObj(obj, new HashMap<>());
        } catch (Exception ioe) {
            throw new IllegalArgumentException("Cannot convert from OBIX", ioe);
        }
    }

    private Object deserializeObj(Obj obj, final Map<String, Object> parent) {
        Object content = null;
        if (obj != null) {
            if (obj instanceof Str) {
                content = ((Str) obj).getVal();

            } else if (obj instanceof Real) {
                content = ((Real) obj).getVal();

            } else if (obj instanceof Int) {
                content = ((Int) obj).getVal().intValue();

            } else if (obj instanceof Bool) {
                content = ((Bool) obj).getVal();

            } else if (obj instanceof Op) {
                String href = obj.getHref();
                try {
                    Field hrefField = obj.getClass().getSuperclass().getDeclaredField("href");
                    hrefField.setAccessible(true);
                    href = (String) hrefField.get(obj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                OperationData op = new OperationData(obj.getName(), href);
                //todo params
                content = new OperationContent(op);

            } else {
                final Map<String, Object> children = new HashMap<>();
                obj.getObjGroup().forEach(o -> deserializeObj((Obj) o, children));
                content = children;
            }

            if (parent != null) {
                parent.put(obj.getName(), content);
            }
        }
        return content;
    }

}
