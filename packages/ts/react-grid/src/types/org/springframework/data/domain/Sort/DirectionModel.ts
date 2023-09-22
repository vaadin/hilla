import {
    _enum as _enum_1,
    EnumModel as EnumModel_1,
    makeEnumEmptyValueCreator,
} from "@hilla/form";
import Direction_1 from "./Direction.js";
class DirectionModel extends EnumModel_1<typeof Direction_1> {
    static override createEmptyValue = makeEnumEmptyValueCreator(DirectionModel);
    readonly [_enum_1] = Direction_1;
}
export default DirectionModel;
