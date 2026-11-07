import m from '@vaadin/hilla-models';
import Kind from './Kind.js';

const KindModel = m.enum(Kind, 'Kind');
type KindModel = typeof KindModel;

export default KindModel;
