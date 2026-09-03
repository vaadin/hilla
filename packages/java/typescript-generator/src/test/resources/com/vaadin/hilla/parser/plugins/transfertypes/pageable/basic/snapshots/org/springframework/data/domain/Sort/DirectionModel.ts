import { _enum as _enum_1, EnumModel as EnumModel_1, makeEnumEmptyValueCreator as makeEnumEmptyValueCreator_1 } from "@vaadin/hilla-lit-form";
import Direction_1 from "./Direction.js";
class DirectionModel extends EnumModel_1<typeof Direction_1> {
    static override createEmptyValue = makeEnumEmptyValueCreator_1(DirectionModel);
    readonly [_enum_1] = Direction_1;
}
export default DirectionModel;
