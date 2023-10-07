package dev.hilla.devmode.hotswapagent;

import org.hotswap.agent.annotation.Init;
import org.hotswap.agent.annotation.LoadEvent;
import org.hotswap.agent.annotation.OnClassLoadEvent;
import org.hotswap.agent.annotation.Plugin;
import org.hotswap.agent.command.ReflectionCommand;
import org.hotswap.agent.command.Scheduler;
import org.hotswap.agent.javassist.CannotCompileException;
import org.hotswap.agent.javassist.CtClass;
import org.hotswap.agent.javassist.NotFoundException;
import org.hotswap.agent.logging.AgentLogger;
import org.hotswap.agent.util.PluginManagerInvoker;

@Plugin(name = "HillaPlugin", description = "HotSwapAgent plugin for Hilla", testedVersions = "2.3", expectedVersions = "2.3")
public class HillaPlugin {
    private static AgentLogger LOGGER = AgentLogger
            .getLogger(HillaPlugin.class);
    private final String hotSwapperClass = "dev.hilla.Hotswapper";
    private boolean hotswapperClassAvailable = false;

    @OnClassLoadEvent(classNameRegexp = "dev.hilla.EndpointController")
    public static void registerPlugin(CtClass ctClass)
            throws NotFoundException, CannotCompileException {
        String src = PluginManagerInvoker
                .buildInitializePlugin(HillaPlugin.class);
        src += PluginManagerInvoker.buildCallPluginMethod(HillaPlugin.class,
                "init", "this", "java.lang.Object");
        ctClass.getConstructors()[0].insertAfter(src);
    }

    @Init
    ClassLoader appClassLoader;

    @Init
    Scheduler scheduler;

    public void init(Object endpointController) {
        try {
            Class.forName(hotSwapperClass).getMethod("markInUse").invoke(null);
            hotswapperClassAvailable = true;
            LOGGER.info("Plugin {} initialized");
        } catch (Exception e) {
            // Older Hilla version
            LOGGER.info(
                    "Plugin {} initialized but will not be used as {} is not found",
                    getClass(), hotSwapperClass);
        }
    }

    @OnClassLoadEvent(classNameRegexp = ".*", events = LoadEvent.REDEFINE)
    public void classRedefined(CtClass ctClass) {
        if (!hotswapperClassAvailable) {
            return;
        }
        Object[] params = new Object[] { true,
                new String[] { ctClass.getName() } };
        scheduler.scheduleCommand(new ReflectionCommand(this, hotSwapperClass,
                "onHotswap", null, params));
    }

    @OnClassLoadEvent(classNameRegexp = ".*", events = LoadEvent.DEFINE)
    public void classChanged(CtClass ctClass) {
        if (!hotswapperClassAvailable) {
            return;
        }
        Object[] params = new Object[] { false,
                new String[] { ctClass.getName() } };
        scheduler.scheduleCommand(new ReflectionCommand(this, hotSwapperClass,
                "onHotswap", null, params));
    }

}
