import m from '@vaadin/hilla-models';
import NumberedEnum from './NumberedEnum.js';

const NumberedEnumModel = m.enum(NumberedEnum, 'NumberedEnum');
type NumberedEnumModel = typeof NumberedEnumModel;

export default NumberedEnumModel;
