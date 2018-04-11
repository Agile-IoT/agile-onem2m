package com.srcsolution.things.interworking.mapper;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;

import com.srcsolution.things.interworking.data.Thing;
import com.srcsolution.things.interworking.mapper.content.ContentConverter;
import com.srcsolution.things.interworking.mapper.content.JsonContentConverter;
import com.srcsolution.things.onem2m_client.resource.ApplicationEntity;

import static org.slf4j.LoggerFactory.getLogger;

public class ResourceMapper {

    private final ApplicationEntity rootAe;

    private final ContentConverter converter;

    private UUID token;

    private final ResourceWriter writer;

    private final ResourceReader reader;

    protected Map<String, ThingResources> thingResourcesCache = new ConcurrentHashMap<>();

    protected Map<String, Thing> lastThingsCache = new ConcurrentHashMap<>();

    public ResourceMapper(ApplicationEntity root, ContentConverter converter, UUID token) {
        this.rootAe = root;
        this.token = token;
        if (converter == null) {
            this.converter = new JsonContentConverter();
        } else {
            this.converter = converter;
        }
        this.writer = new ResourceWriter(this.rootAe, this.converter, this.token);
        this.reader = new ResourceReader(this.rootAe);
    }

    public Thing read(String id) {
        ThingResources thingResources = thingResourcesCache.get(id);
        if (thingResources != null) {
            return reader.readThingFully(thingResources);
        } else {
            return null;
        }
    }

    public void write(Collection<Thing> things) {
        if (things != null) {
            things.forEach((thing) -> {
                try {
                    write(thing);
                } catch (Exception ignored) {
                    // logged in write(thing) method
                }
            });
        }
    }

    public void write(Thing thing) {
        try {
            LOGGER.trace("Writing data for thing {}", thing);
            String id = thing.getId();
            Thing last = lastThingsCache.get(id);
            ThingResources thingResources = getOrCreateThingResources(thing.getId());

            writer.write(thing, thingResources, last);

            lastThingsCache.put(id, thing);
            thingResourcesCache.put(id, thingResources);

        } catch (Throwable t) {
            LOGGER.warn("Cannot write data for thing {}", thing, t);
            throw t;
        }
    }

    public ThingResources getThingResources(String thingId) {
        return thingResourcesCache.get(thingId);
    }

    public ThingResources getOrCreateThingResources(String thingId) {
        ThingResources thingResources = thingResourcesCache.get(thingId);
        if (thingResources == null) {
            thingResources = ThingResources.createThingResources(rootAe, thingId);
            thingResourcesCache.put(thingId, thingResources);
        } else {
            //            try {
            //                thingResources.getThingRootContainer().touch();
            //            } catch (Exception e) {
            //                thingResources = ThingResources.createThingResources(rootAe, thing);
            //            }
        }
        return thingResources;
    }

    /**
     * Remove all thing resources
     */
    public void clear() {
        rootAe.children().forEach(resource -> {
            if (resource.hasLabel(ThingResourcesConstants.THING_RESOURCE_LABEL)) {
                resource.delete();
            }
        });
        lastThingsCache.clear();
        thingResourcesCache.clear();
    }

    public ApplicationEntity getRootAe() {
        return rootAe;
    }

    public ContentConverter getConverter() {
        return converter;
    }

    public ResourceWriter getWriter() {
        return writer;
    }

    public ResourceReader getReader() {
        return reader;
    }

    public static final Logger LOGGER = getLogger(ResourceMapper.class);

}
