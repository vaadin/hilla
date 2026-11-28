import m from '@vaadin/hilla-models';
import Taste from './Taste.js';

const TasteModel = m.enum(Taste, 'Taste');
type TasteModel = typeof TasteModel;

export default TasteModel;
