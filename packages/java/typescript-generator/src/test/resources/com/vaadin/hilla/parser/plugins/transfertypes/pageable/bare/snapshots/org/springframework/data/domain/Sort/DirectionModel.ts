import { _enum, EnumModel, makeEnumEmptyValueCreator } from "@vaadin/hilla-lit-form";
import Direction from "./Direction.js";
class DirectionModel extends EnumModel<typeof Direction> {
    static override createEmptyValue = makeEnumEmptyValueCreator(DirectionModel);
    readonly [_enum] = Direction;
}
export default DirectionModel;
