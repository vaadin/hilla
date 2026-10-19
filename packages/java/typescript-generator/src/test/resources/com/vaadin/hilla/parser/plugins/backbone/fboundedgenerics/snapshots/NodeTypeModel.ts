import m from '@vaadin/hilla-models';
import NodeType from './NodeType.js';

const NodeTypeModel = m.enum(NodeType, 'NodeType');
type NodeTypeModel = typeof NodeTypeModel;

export default NodeTypeModel;
