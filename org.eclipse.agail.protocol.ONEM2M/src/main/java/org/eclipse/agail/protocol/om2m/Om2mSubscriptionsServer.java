package org.eclipse.agail.protocol.om2m;

import java.io.IOException;
import java.util.HashMap;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.agail.protocol.om2m.utils.Request;
import org.eclipse.agail.protocol.om2m.utils.Response;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Om2mSubscriptionsServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Om2mSubscriptionsServer.class);

    private static HttpServlet httpServlet;

    static void startServer(OneM2MProtocol oneM2MProtocol, int httpPort, String path, String resourceId) throws Exception {
        Server server = new Server();

        server.setConnectors(new Connector[] {createConnector(httpPort)});

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        context.addServlet(new ServletHolder(initHttpServlet(oneM2MProtocol, resourceId)), path + "/*");

        try {
            LOGGER.debug("Starting HTTP server (http:{})", httpPort);
            server.start();
            LOGGER.debug("Server started, listening on path {}", path);
            server.join();
        } catch (Exception e) {
            LOGGER.warn("Server error", e);
        } finally {
            server.destroy();
        }
    }

    protected static final HttpServlet initHttpServlet(OneM2MProtocol oneM2MProtocol, String resourceId) {
        if (httpServlet == null) {

            httpServlet = new HttpServlet() {

                @Override
                protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                    oneM2MProtocol.processNotification(new Request(req, resourceId), new Response(resp), new HashMap<>());
                }
            };
        }
        return httpServlet;
    }

    private static Connector createConnector(int port) {
        Connector connector = new SelectChannelConnector();
        connector.setPort(port);

        return connector;
    }

}
