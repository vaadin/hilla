import { readFile } from 'node:fs/promises';
import cssnanoPlugin from 'cssnano';
import MagicString from 'magic-string';
import postcss from 'postcss';
import type { Plugin } from 'vite';

export type PluginOptions = Readonly<{
  root: URL;
}>;

export type LoadRegisterOptions = PluginOptions;

// This plugin adds "__REGISTER__()" function definition everywhere where it finds
// the call for that function. It is necessary for a correct code for tests.
export function loadRegisterJs({ root }: LoadRegisterOptions): Plugin {
  return {
    enforce: 'pre',
    name: 'vite-hilla-register',
    async transform(code) {
      if (code.includes('__REGISTER__()') && !code.includes('function __REGISTER__')) {
        const registerCode = await readFile(new URL('scripts/register.js', root), 'utf8').then((c) =>
          c.replace('export', ''),
        );

        const _code = new MagicString(code);
        _code.prepend(registerCode);

        return {
          code: _code.toString(),
          map: _code.generateMap(),
        };
      }

      return null;
    },
  };
}

// This plugin transforms CSS to Constructible CSSStyleSheet for easy
// installation it to the document styles.
export function constructCss(): Plugin {
  const cssTransformer = postcss([cssnanoPlugin()]);
  const css = new Map();

  return {
    enforce: 'post',
    name: 'vite-construct-css',
    async load(id) {
      if (id.endsWith('.obj.css')) {
        const content = await readFile(id, 'utf8');
        css.set(id, content);
        return {
          code: '',
        };
      }

      return null;
    },
    async transform(_, id) {
      if (id.endsWith('.obj.css')) {
        const { content } = await cssTransformer.process(css.get(id));

        return {
          code: `const css = new CSSStyleSheet();css.replaceSync(${JSON.stringify(content)});export default css;`,
        };
      }

      return null;
    },
  };
}
