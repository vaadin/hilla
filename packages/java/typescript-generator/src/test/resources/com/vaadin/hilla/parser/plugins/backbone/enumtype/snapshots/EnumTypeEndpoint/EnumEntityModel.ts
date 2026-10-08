import { _enum, EnumModel, makeEnumEmptyValueCreator } from "@vaadin/hilla-lit-form";
import EnumEntity from "./EnumEntity.js";
class EnumEntityModel extends EnumModel<typeof EnumEntity> {
    static override createEmptyValue = makeEnumEmptyValueCreator(EnumEntityModel);
    readonly [_enum] = EnumEntity;
}
export default EnumEntityModel;
