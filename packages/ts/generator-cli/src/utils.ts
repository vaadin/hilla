import { isAbsolute, resolve } from 'path';
import getStdin from 'get-stdin';
import type GeneratorIO from './GeneratorIO.js';

export async function processInput(io: GeneratorIO, raw?: string): Promise<string> {
  if (raw) {
    let result = raw;

    if (result.startsWith("'") || result.startsWith('"')) {
      result = raw.substring(1, raw.length - 1);
    }

    if (result.startsWith('{')) {
      return result;
    }

    return io.read(isAbsolute(result) ? result : resolve(io.cwd, result));
  }

  return getStdin();
}
