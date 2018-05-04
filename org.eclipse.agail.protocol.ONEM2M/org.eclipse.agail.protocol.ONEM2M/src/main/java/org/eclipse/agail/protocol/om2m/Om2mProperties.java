package org.eclipse.agail.protocol.om2m;

import java.nio.file.Paths;
import java.util.*;

import com.github.drapostolos.typeparser.NoSuchRegisteredParserException;
import com.github.drapostolos.typeparser.TypeParser;
import com.github.drapostolos.typeparser.TypeParserException;
import embedded_libs.com.google.common.base.Strings;
import embedded_libs.com.google.common.net.UrlEscapers;
import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.compose.FallbackConfigurationSource;
import org.cfg4j.source.compose.MergeConfigurationSource;
import org.cfg4j.source.context.environment.DefaultEnvironment;
import org.cfg4j.source.context.environment.ImmutableEnvironment;
import org.cfg4j.source.empty.EmptyConfigurationSource;
import org.cfg4j.source.reload.ReloadStrategy;
import org.cfg4j.source.reload.strategy.ImmediateReloadStrategy;
import org.eclipse.agail.protocol.om2m.properties.ClasspathNoEnvConfigurationSource;
import org.eclipse.agail.protocol.om2m.properties.FilesNoEnvConfigurationSource;
import org.eclipse.agail.protocol.om2m.properties.LoggerConfigurator;
import org.slf4j.Logger;
import org.slf4j.impl.StaticLoggerBinder;

import com.srcsolution.things.onem2m_client.Node;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Properties to setup service runtime parameters
 */
public class Om2mProperties {

    public final Logger LOGGER = getLogger(getClass());

    protected Properties props;

    protected ConfigurationProvider provider;

    private String gatewayId;

    private static final EmptyConfigurationSource EMPTY_CONFIGURATION_SOURCE = new EmptyConfigurationSource();

    public Om2mProperties(ConfigurationSource... sources) {
        List<ConfigurationSource> list = new ArrayList<>();

        addConfigurationSource(list, classpathSource("org/eclipse/agail/protocol/om2m", "application"));

        if (sources != null) {
            Arrays.stream(sources).forEach(source -> addConfigurationSource(list, source));
        }
        String propertyConfigFile = System.getProperty("ipe.config_file");
        if (!Strings.isNullOrEmpty(propertyConfigFile)) {
            addConfigurationSource(list, fileSource(propertyConfigFile));
        }

        ConfigurationSource mergeSource = new MergeConfigurationSource(list.toArray(new ConfigurationSource[0]));

        //        ImmutableEnvironment environment = new ImmutableEnvironment(System.getProperty("user.dir"));
        ImmutableEnvironment environment = new DefaultEnvironment();

        ReloadStrategy reloadStrategy = new ImmediateReloadStrategy(); // new PeriodicalReloadStrategy(2, TimeUnit.MINUTES);

        this.provider = new ConfigurationProviderBuilder()
                .withConfigurationSource(mergeSource)
                .withEnvironment(environment)
                .withReloadStrategy(reloadStrategy)
                .build();

        this.props = provider.allConfigurationAsProperties();

        configureLogging();

        applyToSystemProperties();
    }

    /**
     * Lookup for a slf4j implementation
     */
    private void configureLogging() {
        final StaticLoggerBinder binder = StaticLoggerBinder.getSingleton();
        String factoryClassStr = binder.getLoggerFactoryClassStr();
        // instantiate by string reference to avoid runtime link if classes are missing
        String className = null;
        if (factoryClassStr.startsWith("org.slf4j.impl.Log4jLoggerFactory")) {
            className = "org.eclipse.agail.protocol.om2m.properties.Log4jConfigurator";
        } else if (factoryClassStr.startsWith("org.slf4j.impl.JDK14LoggerFactory")) {
            className = "org.eclipse.agail.protocol.om2m.properties.JulConfigurator";
        }
        if (className == null) {
            System.out.println("Cannot configure logging, unknown implementation : " + factoryClassStr);
        } else {
            try {
                LoggerConfigurator configurator = (LoggerConfigurator) Class.forName(className).newInstance();
                configurator.configure(this, getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Set loaded non-IPE properties to system properties
     */
    private void applyToSystemProperties() {
        props.forEach((key, value) -> {
            if (!String.valueOf(key).startsWith("ipe.")) {
                System.setProperty(String.valueOf(key), String.valueOf(value));
            }
        });
    }

    private void addConfigurationSource(List<ConfigurationSource> list, ConfigurationSource source) {
        list.add(new FallbackConfigurationSource(source, EMPTY_CONFIGURATION_SOURCE));
    }

    public static ConfigurationSource classpathSource(String path, String... paths) {
        return new ClasspathNoEnvConfigurationSource(() -> Collections.singletonList(Paths.get(path, paths)));
    }

    public static ConfigurationSource fileSource(String path, String... paths) {
        return new FilesNoEnvConfigurationSource(() -> Collections.singletonList(Paths.get(path, paths)));
    }

    public String getProperty(String key) {
        return getProperty(key, String.class);
    }

    public String getProperty(String key, String def) {
        return getProperty(key, String.class, null);
    }

    public <T> T getProperty(String key, Class<T> type) {
        return getProperty(key, type, null);
    }

    public <T> T getProperty(String key, Class<T> type, T def) {
        String propertyStr = String.valueOf(props.getOrDefault(key, def));
        if (propertyStr != null) {
            try {
                TypeParser parser = TypeParser.newBuilder().build();
                return parser.parse(propertyStr, type);
            } catch (TypeParserException | NoSuchRegisteredParserException e) {
                throw new IllegalArgumentException("Unable to cast value \'" + propertyStr + "\' to " + type, e);
            }
        } else {
            return null;
        }
    }

    public String getName() {
        return getProperty("ipe.name", String.class);
    }

    public Integer getKeepAlivePeriod() {
        return getProperty("ipe.keep_alive", Integer.class);
    }

    public Integer getServerHttpPort() {
        return getProperty("ipe.server.http.port", Integer.class);
    }

    public Integer getServerHttpsPort() {
        return getProperty("ipe.server.https.port", Integer.class);
    }

    public String getServerHttpPath() {
        String path = getProperty("ipe.server.http.path", String.class);
        if (!Strings.isNullOrEmpty(path)) {
            return path.startsWith("/") ? path : "/" + path;
        } else {
            return "/" + UrlEscapers.urlPathSegmentEscaper().escape(getResourceAppName().toLowerCase());
        }
    }

    public String getRemoteNodeUrl() {
        return getProperty("ipe.remote.url", String.class);
    }

    public String getRemoteNodeId() {
        return getProperty("ipe.remote.id", String.class);
    }

    public String getRemoteNodeLogin() {
        return getProperty("ipe.remote.login", String.class);
    }

    public String getRemoteNodePassword() {
        return getProperty("ipe.remote.password", String.class);
    }

    public Node getRemoteNode() {
        return new Node(getRemoteNodeUrl(), getRemoteNodeId(), getRemoteNodeLogin(), getRemoteNodePassword());
    }

    public String getResourceAppName() {
        String value = getProperty("ipe.resource.app_name", String.class);
        return Strings.defaultIfEmpty(value, getName());
    }

    public String getResourceAppId() {
        String value = getProperty("ipe.resource.app_id", String.class);
        return Strings.defaultIfEmpty(value, getName());
    }

    public String getResourcePointOfAccess() {
        String value = getProperty("ipe.resource.poa", String.class);
        return Strings.defaultIfEmpty(value, getName());
    }

    public String getGatewayId() {
        if (this.gatewayId == null) {
            String value = getProperty("ipe.gatewayId", String.class);

            if (value == null || value.isEmpty()) {
                value = "Default_gateway";
            }
            this.gatewayId = value.replaceAll(" ", "_");
        }

        return this.gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        if(gatewayId == null || gatewayId.isEmpty()) {
            gatewayId = "Default_gateway";
        }
        this.gatewayId = gatewayId.replaceAll(" ", "_");
    }

    public String getResourceContentInfo() {
        return getProperty("ipe.resource.content_info", String.class);
    }

    public UUID getToken() {
        UUID token = null;
        String value = getProperty("ipe.token", String.class);

        if (value != null && !value.isEmpty()) {
            token = UUID.fromString(value);
        } else {
            token = UUID.randomUUID();
            props.setProperty("ipe.token", token.toString());
        }

        return token;
    }

    public String getProxyHost() {
        return getProperty("http.proxyHost");
    }

    public Integer getProxyPort() {
        return getProperty("http.proxyPort", Integer.class);
    }

    public int getMaxQueueSize() {
        return getProperty("ipe.resource.mapper.queue_size", Integer.class);
    }

}
