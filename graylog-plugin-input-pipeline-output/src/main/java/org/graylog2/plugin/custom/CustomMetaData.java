package org.graylog2.plugin.custom;

import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.ServerStatus;
import org.graylog2.plugin.Version;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

/**
 * Implement the PluginMetaData interface here.
 */
public class CustomMetaData implements PluginMetaData {
    @Override
    public String getUniqueId() {
        return "org.graylog2.plugin.custom.CustomPlugin";
    }

    @Override
    public String getName() {
        return "graylog-plugin-input-pipeline-output";
    }

    @Override
    public String getAuthor() {
        return "hollysys hf";
    }

    @Override
    public URI getURL() {
        return URI.create("https://www.graylog.org/");
    }

    @Override
    public Version getVersion() {
        return new Version(1, 0, 5);
    }

    @Override
    public String getDescription() {
        return "Input Pipeline Output Plugin";
    }

    @Override
    public Version getRequiredVersion() {
        return new Version(2, 0, 0);
    }

    @Override
    public Set<ServerStatus.Capability> getRequiredCapabilities() {
        return Collections.emptySet();
    }
}
