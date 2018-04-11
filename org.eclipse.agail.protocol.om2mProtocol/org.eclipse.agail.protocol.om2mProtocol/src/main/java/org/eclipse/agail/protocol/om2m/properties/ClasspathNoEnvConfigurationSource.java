package org.eclipse.agail.protocol.om2m.properties;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.context.environment.Environment;
import org.cfg4j.source.context.environment.MissingEnvironmentException;
import org.cfg4j.source.context.filesprovider.ConfigFilesProvider;
import org.cfg4j.source.context.propertiesprovider.*;
import org.slf4j.Logger;

import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * {@link ConfigurationSource} reading configuration from classpath files.
 */
public class ClasspathNoEnvConfigurationSource implements ConfigurationSource {

    private final ConfigFilesProvider configFilesProvider;

    private final PropertiesProviderSelector propertiesProviderSelector;

    /**
     * Construct {@link ConfigurationSource} backed by classpath files. Uses "application.properties" file
     * located in the path specified by the {@link Environment} provided to {@link #getConfiguration(Environment)}
     * calls (see corresponding javadoc for detail).
     */
    public ClasspathNoEnvConfigurationSource() {
        this(new ConfigFilesProvider() {
            @Override
            public Iterable<Path> getConfigFiles() {
                return Collections.singletonList(
                        Paths.get("application.properties")
                );
            }
        });
    }

    /**
     * Construct {@link ConfigurationSource} backed by classpath files. File paths should by provided by
     * {@link ConfigFilesProvider} and will be treated as relative paths to the environment provided in
     * {@link #getConfiguration(Environment)} calls (see corresponding javadoc for detail). Configuration
     * file type is detected using file extension (see {@link PropertiesProviderSelector}).
     *
     * @param configFilesProvider {@link ConfigFilesProvider} supplying a list of configuration files to use
     */
    public ClasspathNoEnvConfigurationSource(ConfigFilesProvider configFilesProvider) {
        this(configFilesProvider, new PropertiesProviderSelector(
                new PropertyBasedPropertiesProvider(), new YamlBasedPropertiesProvider(), new JsonBasedPropertiesProvider()
        ));
    }

    /**
     * Construct {@link ConfigurationSource} backed by classpath files. File paths should by provided by
     * {@link ConfigFilesProvider} and will be treated as relative paths to the environment provided in
     * {@link #getConfiguration(Environment)} calls (see corresponding javadoc for detail).
     *
     * @param configFilesProvider        {@link ConfigFilesProvider} supplying a list of configuration files to use
     * @param propertiesProviderSelector selector used for choosing {@link PropertiesProvider} based on a configuration file extension
     */
    public ClasspathNoEnvConfigurationSource(ConfigFilesProvider configFilesProvider, PropertiesProviderSelector propertiesProviderSelector) {
        this.configFilesProvider = requireNonNull(configFilesProvider);
        this.propertiesProviderSelector = requireNonNull(propertiesProviderSelector);
    }

    /**
     * Get configuration set for a given {@code environment} from this source in a form of {@link Properties}.
     * {@link Environment} name is prepended to all file paths from {@link ConfigFilesProvider}
     * to form an absolute configuration file path. Trailing slashes in environment name are not supported (due
     * to Java disallowing classpath locations starting with slash).
     *
     * @param environment environment to use
     * @return configuration set for {@code environment}
     * @throws MissingEnvironmentException when requested environment couldn't be found
     * @throws IllegalStateException       when unable to fetch configuration
     */
    @Override
    public Properties getConfiguration(Environment environment) {
        Properties properties = new Properties();

        //        Path pathPrefix = Paths.get(environment.getName());
        //
        //        URL url = getClass().getClassLoader().getResource(pathPrefix.toString());
        //        if (url == null && !environment.getName().isEmpty()) {
        //            throw new MissingEnvironmentException("Directory doesn't exist: " + environment.getName());
        //        }

        List<Path> paths = new ArrayList<>();
        for (Path path : configFilesProvider.getConfigFiles()) {
            //            paths.add(pathPrefix.resolve(path));
            paths.add(path);
        }

        for (Path p : paths) {
            for (Path path : supportedFileExtensions(p)) {
                try (InputStream input = getClass().getClassLoader().getResourceAsStream(path.toString())) {

                    if (input == null) {
                        LOGGER.trace("Unable to load properties from classpath: {}", path);
                        //                        throw new IllegalStateException("Unable to load properties from classpath: " + path);
                    } else {
                        PropertiesProvider provider = propertiesProviderSelector.getProvider(path.getFileName().toString());
                        properties.putAll(provider.getProperties(input));
                        LOGGER.debug("Found configuration classpath: {}", path);
                    }
                } catch (IOException e) {
                    LOGGER.trace("Unable to load properties from classpath: {}", path);
                    //                    throw new IllegalStateException("Unable to load properties from classpath: " + path, e);
                }
            }
        }

        return properties;
    }

    private Path[] supportedFileExtensions(Path path) {
        return new Path[] {
                path.resolveSibling(path.getFileName() + ".properties"),
                path.resolveSibling(path.getFileName() + ".json"),
                path.resolveSibling(path.getFileName() + ".yaml"),
                path.resolveSibling(path.getFileName() + ".yml")
        };
    }

    @Override
    public void init() {
        // NOP
    }

    @Override
    public void reload() {
        // NOP
    }

    @Override
    public String toString() {
        return "ClasspathConfigurationSource{" +
                "configFilesProvider=" + configFilesProvider +
                '}';
    }

    public final Logger LOGGER = getLogger(getClass());
}

