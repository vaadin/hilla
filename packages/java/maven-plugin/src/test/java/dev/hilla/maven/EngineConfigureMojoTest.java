package dev.hilla.maven;

import dev.hilla.engine.EngineConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EngineConfigureMojoTest extends AbstractMojoTest {

    @Test
    public void should_StoreConfigurationJson() throws Exception {
        // Lookup and initialize mojo
        var engineConfigureMojo = (EngineConfigureMojo) lookupMojo("configure",
                getTestConfigurartion());
        setVariableValueToObject(engineConfigureMojo, "project",
                getMavenProject());
        engineConfigureMojo.execute();

        var storedEngineConfiguration = EngineConfiguration
                .load(getBuildDirectory().toFile());
        assertNotNull(storedEngineConfiguration);
        assertEquals(getEngineConfiguration(), storedEngineConfiguration);
    }
}
