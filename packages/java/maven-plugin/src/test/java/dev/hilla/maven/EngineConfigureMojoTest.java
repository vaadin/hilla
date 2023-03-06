package dev.hilla.maven;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import dev.hilla.engine.EngineConfiguration;
import dev.hilla.parser.testutils.JsonAssertions;

public class EngineConfigureMojoTest extends AbstractMojoTest {

    @Test
    public void should_StoreConfigurationJson() throws Exception {
        // Lookup and initialize mojo
        var engineConfigureMojo = (EngineConfigureMojo) lookupMojo("configure",
                getTestConfiguration());
        setVariableValueToObject(engineConfigureMojo, "project",
                getMavenProject());
        engineConfigureMojo.execute();

        var storedEngineConfiguration = EngineConfiguration
                .load(getBuildDirectory()
                        .resolve(EngineConfiguration.DEFAULT_CONFIG_FILE_NAME)
                        .toFile());
        assertNotNull(storedEngineConfiguration);
        JsonAssertions.assertEquals(getEngineConfiguration(),
                storedEngineConfiguration);
    }
}
