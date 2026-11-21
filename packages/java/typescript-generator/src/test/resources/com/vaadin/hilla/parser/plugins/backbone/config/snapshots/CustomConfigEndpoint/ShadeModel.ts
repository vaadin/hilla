import m from '@vaadin/hilla-models';
import Shade from './Shade.js';

const ShadeModel = m.enum(Shade, 'Shade');
type ShadeModel = typeof ShadeModel;

export default ShadeModel;
