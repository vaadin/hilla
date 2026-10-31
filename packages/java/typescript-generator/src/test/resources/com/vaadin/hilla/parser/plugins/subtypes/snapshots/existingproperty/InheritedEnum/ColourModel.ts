import m from '@vaadin/hilla-models';
import Colour from './Colour.js';

const ColourModel = m.enum(Colour, 'Colour');
type ColourModel = typeof ColourModel;

export default ColourModel;
