package dev.hilla.maven;

import java.util.List;

import dev.hilla.engine.EngineConfiguration;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EngineConfigureMojoTest extends AbstractMojoTest {

    @Test
    public void should_StoreConfigurationJson() throws Exception {
        var buildDirFile = getBuildDirectory().toFile();

        // Lookup and initialize mojo
        var engineConfigureMojo = (EngineConfigureMojo) lookupMojo("configure", getTestConfigurartion());
        setVariableValueToObject(engineConfigureMojo, "buildDirectory", buildDirFile);
        // Maven project is not initialized on the mojo, setup a mock manually
        var mockProject = Mockito.mock(MavenProject.class);
        var classPathElements = List.of("target/test-classes");
        Mockito.doReturn(classPathElements).when(mockProject).getCompileClasspathElements();
        Mockito.doReturn(classPathElements).when(mockProject).getRuntimeClasspathElements();
        Mockito.doReturn(getTemporaryDirectory().toFile()).when(mockProject).getBasedir();
        var mockBuild = Mockito.mock(Build.class);
        Mockito.doReturn("build").when(mockBuild).getDirectory();
        Mockito.doReturn(mockBuild).when(mockProject).getBuild();
        setVariableValueToObject(engineConfigureMojo, "project", mockProject);
        engineConfigureMojo.execute();

        var storedEngineConfiguration =
            EngineConfiguration.load(buildDirFile);
        assertNotNull(storedEngineConfiguration);
        assertEquals(getEngineConfiguration(), storedEngineConfiguration);
    }
}
