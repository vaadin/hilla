import m, { StringModel } from '../../../src/index.js';
import type ZipCode from './ZipCode.js';

const ZipCodeModel = m.object<ZipCode>('ZipCode').property('code', StringModel).build();
type ZipCodeModel = typeof ZipCodeModel;
export default ZipCodeModel;
