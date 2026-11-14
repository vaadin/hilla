import m from '@vaadin/hilla-models';
import LabelledEnum from './LabelledEnum.js';

const LabelledEnumModel = m.enum(LabelledEnum, 'LabelledEnum');
type LabelledEnumModel = typeof LabelledEnumModel;

export default LabelledEnumModel;
