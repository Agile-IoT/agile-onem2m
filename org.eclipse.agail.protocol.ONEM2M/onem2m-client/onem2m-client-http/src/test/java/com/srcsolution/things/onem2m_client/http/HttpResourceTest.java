package com.srcsolution.things.onem2m_client.http;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;

import com.srcsolution.things.onem2m_client.Node;
import com.srcsolution.things.onem2m_client.Resource;
import com.srcsolution.things.onem2m_client.ResourceType;
import com.srcsolution.things.onem2m_client.resource.ApplicationEntity;
import com.srcsolution.things.onem2m_client.resource.Container;
import com.srcsolution.things.onem2m_client.resource.builder.ApplicationEntityBuilder;

import static org.slf4j.LoggerFactory.getLogger;

public class HttpResourceTest {

    public static final Logger LOGGER = getLogger(HttpResourceTest.class);

    @Test
    public void testRetrieve() throws Exception {
        Node node = new Node("http://localhost:8280", "mn-cse", "admin", "admin");

        HttpResourceFactory httpResourceFactory = new HttpResourceFactory(node);

        Resource root = httpResourceFactory.retrieve("/mn-cse");

        System.out.println("\n>>> Get one resource :");
        System.out.println(root.getId());

        System.out.println("\n>>> All children :");
        root.children().forEach(System.out::println);

        System.out.println("\n>>> All AE children :");
        root.children(ResourceType.APPLICATION_ENTITY).forEach(System.out::println);

        System.out.println("\n>>> Get acp_admin child :");
        System.out.println(root.child("acp_admin"));

        System.out.println("\n>>> Find resource label :");
        System.out.println(root.find(null, "com.srcsolution.things.interworking.ipe.common.IpeActivator"));

        System.out.println("\n>>> Find resource unknown label :");
        System.out.println(root.find(null, "sjdqhfjhfgqsd"));

        System.out.println("\n>>> Find resource type remote CSE :");
        System.out.println(root.find(ResourceType.REMOTE_CSE));

    }

    @Test
    public void testListChild() throws Exception {
        Node node = new Node("http://localhost:8280", "in-local", "admin", "admin");

        HttpResourceFactory httpResourceFactory = new HttpResourceFactory(node);

        Resource root = httpResourceFactory.retrieveRoot();
        LOGGER.info("{}", root);

        Resource mn = httpResourceFactory.retrieve("/mn-cse1");
        System.out.println(mn.deepChild("fake", "thing_in-cse_thing8", "properties", "device", "la"));

        System.exit(89);

        HttpResource resource = httpResourceFactory.retrieve("/mn-cse1/CAE490423987");
        List<Resource> children = resource.children();
        children.forEach((Resource x) -> {
            System.out.println(x);
            //            String hierarchicalUrl = x.getHierarchicalUrl();

            //            System.out.println(hierarchicalUrl);
            //            if (x.getLabels().contains("com.srcsolution.pilot_things.THING") && !Strings.isNullOrEmpty(hierarchicalUrl)) {
            //                HttpResource deviceResource = httpResourceFactory.retrieve(hierarchicalUrl + "/properties/device");
            //                System.out.println(deviceResource);
            //            }

            if (x.getLabels().contains("com.srcsolution.pilot_things.THING")) {
                //                x.child("properties").ifPresent(resource1 -> resource1.child("device").ifPresent(System.out::println));
                System.out.println(x.deepChild("properties", "device", "la"));
            }

        });

    }

    @Test
    public void testCreateChild() throws Exception {
        Node node = new Node("http://localhost:8181", "mn-cse", "admin", "admin");
        HttpResourceFactory httpResourceFactory = new HttpResourceFactory(node);
        Resource root = httpResourceFactory.retrieve("/mn-cse");

        ApplicationEntity ae = root.buildChild(ApplicationEntityBuilder.class).addLabels("lala")
                                   .create("ouhlala" + ZonedDateTime.now().toEpochSecond());
        Container container = ae.buildContainer().setMaxNumberOfIntance(3L).create("properties");
        container.buildContentInstance().setContentInfo("text/plain").setContent("hello").create();
        container.buildContentInstance().setContentInfo("application/json").setContent("{hello: 1}").create();
        container.buildContentInstance().setContentInfo("application/xml").setContent("<p>hello</p>").create();
        container.buildContentInstance().setContentInfo("text/plain").setContent("world!").create();

        System.out.println("\n>>> container.getLatestInstance :");
        System.out.println(container.getLatestInstance());
        System.out.println("\n>>> container.getOldestInstance :");
        System.out.println(container.getOldestInstance());

        //        for (int i = 0; i < 100; i++) {
        //            container.buildContentInstance().setContentInfo("text/plain").setContent("world!").create();
        //        }
    }

    @Test
    public void testDeleteChild() throws Exception {
        Node node = new Node("http://localhost:8181", "mn-cse", "admin", "admin");
        HttpResourceFactory httpResourceFactory = new HttpResourceFactory(node);
        Resource root = httpResourceFactory.retrieve("/mn-cse");

        ApplicationEntity turlututu = root.buildChild(ApplicationEntityBuilder.class).create("tralala_" + ZonedDateTime.now().toEpochSecond());
        System.out.println(turlututu);

        turlututu.delete();
    }

    @Test
    public void testDeepFind() throws Exception {
        Node node = new Node("http://dev1.pilot:8081", "in-cse", "admin", "admin");
        //        Node node = new Node("http://dev1.pilot:8082", "mn-cse", "admin", "admin");
        HttpResourceFactory httpResourceFactory = new HttpResourceFactory(node);
        Resource root = httpResourceFactory.retrieveRoot();

        final Collection<Resource> resources = root.deepFind(ResourceType.CSE_BASE);
        resources.forEach(System.out::println);
    }

}