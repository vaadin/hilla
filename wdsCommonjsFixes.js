// From https://github.com/modernweb-dev/web/issues/1700 -> https://gist.github.com/lucaelin/ffa48cba2253e9ad662b71e38444a135
import { fromRollup } from '@web/dev-server-rollup';


// patch the modules load hook to clear the plugins resolveExclusions whenever a new build starts 
function fixLoadSkipSelf(module) {
  const originalLoad = module.load;
  module.load = function(...args) {
    module.resolveExclusions = new Set();
    return originalLoad.call(this, ...args);
  }
}

// patch the modules resolveId hook to skip files that are in the modules exclusion set
function fixResolveIdSkipSelf(module) {
  const originalResolveId = module.resolveId;
  module.resolveId = function (...args) {
    // skip running resolveId, if this file is already in the exclusion set
    if (module.resolveExclusions.has(args[0])) {
      return undefined;
    }

    // fixing the resolve from the plugins this context needs to be done every time resolveId is called, because the context is recreated every time
    fixResolveSkipSelf(this, module)

    const resolveReturn = originalResolveId.call(this, ...args);
    return resolveReturn;
  };

}

// patch the adapters resolve function to add the file to the modules exclusion list if skipSelf is set
function fixResolveSkipSelf(pluginContext, module) {
  // skip res

  const originalResolve = pluginContext.resolve;
  pluginContext.resolve = function(...args) {
    // add the file to the modules set of excluded files
    if (args[2].skipSelf) {
      //console.log('saving skipSelf for', args[0]);
      module.resolveExclusions.add(args[0]);
    } 
    return originalResolve.call(this, ...args);
  };
  return false;
}

// patch the modules options hook to extract injected plugins, then patch the module to move the resolveId hook back into it
function fixOptionsHookPluginInjection(module) {
    // we wrap the modules options call into our own function to process its return value and find the injected plugin
    const optionsHook = module.options;
    module.options = function(...args) {
      const optionsReturn = optionsHook.call(this, ...args);
      
      // we find the injected plugin
      const injectedPlugins = optionsReturn.plugins;
      if (injectedPlugins.length > 1) throw new Error('Multiple injected plugins detected. This wont work!')
      const [injectedResolvePlugin] = injectedPlugins;
      //console.log('injected plugins', injectedPlugins);

      // now we move the injected plugins resolveId hook back into the original module
      if (module.resolveId) throw new Error('Existing resolveId detected. This wont work!');
      module.resolveId = function (...args) {
        const resolveReturn = injectedResolvePlugin.resolveId.call(this, ...args);
        return resolveReturn;
      };

      // we return what was originally returned by the options call to allow a fixed version of wds to process it
      return optionsReturn;
    }
}

// patch the plugin to deal with issues from the adapter 
function preparePluginForAdapter(moduleFn) {
  return function(...args) {
    // instantiate the module
    const module = moduleFn(...args);
    //console.log('module', module);

    // fix plugin injection
    fixOptionsHookPluginInjection(module);
    
    // wait for buildStart because plugin injection above might take some time (options hook is called prior to buildStart)
    const originalBuildStart = module.buildStart;
    module.buildStart = function(...args) {
      fixLoadSkipSelf(module);
      fixResolveIdSkipSelf(module);
      return originalBuildStart.call(this, ...args);
    }

    return module;
  }
}

export function fromRollupWithFix(rollupPlugin) {
  return fromRollup(preparePluginForAdapter(rollupPlugin));
}

export function fixExportNamedExports() {
  return {
    name: 'fix-commonjs-module',
    transform(context) {
      // this fixes the missing export named exports
      if (context.url.endsWith(encodeURIComponent('?commonjs-module'))) {
        return `var exports = {}; var __module = {exports}; export {__module, exports}`;
      }
    },
  }
}