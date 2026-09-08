import { _enum as _enum_1, EnumModel as EnumModel_1, makeEnumEmptyValueCreator as makeEnumEmptyValueCreator_1 } from "@vaadin/hilla-lit-form";
import NodeType_1 from "./NodeType.js";
class NodeTypeModel extends EnumModel_1<typeof NodeType_1> {
    static override createEmptyValue = makeEnumEmptyValueCreator_1(NodeTypeModel);
    readonly [_enum_1] = NodeType_1;
}
export default NodeTypeModel;
