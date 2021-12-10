import { isAbsolute, resolve } from 'path';
import type GeneratorIO from './GeneratorIO.js';

export async function processInput(raw: string, io: GeneratorIO): Promise<string> {
  let result = raw;

  if (result.startsWith("'") || result.startsWith('"')) {
    result = raw.substring(1, raw.length - 1);
  }

  if (result.startsWith('{')) {
    return result;
  }

  return io.read(isAbsolute(result) ? result : resolve(io.cwd, result));
}
