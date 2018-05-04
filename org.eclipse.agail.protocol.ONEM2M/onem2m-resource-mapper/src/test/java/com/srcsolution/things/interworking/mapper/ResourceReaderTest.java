package com.srcsolution.things.interworking.mapper;

import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.srcsolution.things.interworking.data.Thing;
import com.srcsolution.things.onem2m_client.Node;
import com.srcsolution.things.onem2m_client.Resource;
import com.srcsolution.things.onem2m_client.ResourceFactory;
import com.srcsolution.things.onem2m_client.ResourceType;
import com.srcsolution.things.onem2m_client.http.HttpResourceFactory;
import com.srcsolution.things.onem2m_client.resource.ApplicationEntity;

public class ResourceReaderTest {

    @Test
    public void read() throws Exception {
        ResourceReader reader = getReader();
        if (reader != null) {
            System.out.println(reader.getRootAe());
            Map<String, ThingResources> thingResourcesMap = reader.mapThingResources();
            thingResourcesMap.forEach((s, thingResources) -> {
                Thing thing = reader.readThingFully(thingResources);
                System.out.println(thing.getName());
                System.out.println("\t" + thing.getId());
                System.out.println("\t" + thing.getDevice().getName());
                thing.getTopics().forEach(topic -> {
                    System.out.println("\t" + topic.getName());
                    topic.getMessages().forEach(message -> {
                        System.out.println("\t\t" + message.getTimestamp() + ": " + message.getPayload());
                    });
                });

                System.out.println("-----");
            });
        }
    }

    private ResourceReader getReader() {
        //                Node node = new Node("http://localhost:8282", "mn-cse", "admin", "admin");
        Node node = new Node("http://localhost:8280", "in-local", "admin", "admin");
        //        Node node = new Node("http://pth.eastus.cloudapp.azure.com:17081", "in-cse", "admin", "1t!39fW_Lb6,LgR;");

        ResourceFactory httpResourceFactory = new HttpResourceFactory(node);
        Resource root = httpResourceFactory.retrieveRoot();
        System.out.println(root);

        Set<Resource> res = root.deepFind(ResourceType.APPLICATION_ENTITY);
        return res.stream().findFirst().map(resource -> new ResourceReader((ApplicationEntity) resource)).orElse(null);
    }

}