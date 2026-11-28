import m from '@vaadin/hilla-models';
import RenamedEnum from './RenamedEnum.js';

const RenamedEnumModel = m.enum(RenamedEnum, 'RenamedEnum');
type RenamedEnumModel = typeof RenamedEnumModel;

export default RenamedEnumModel;
