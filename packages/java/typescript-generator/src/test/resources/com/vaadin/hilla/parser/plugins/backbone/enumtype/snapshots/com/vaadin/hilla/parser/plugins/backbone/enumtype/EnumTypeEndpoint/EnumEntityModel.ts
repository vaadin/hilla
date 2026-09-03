import { _enum as _enum_1, EnumModel as EnumModel_1, makeEnumEmptyValueCreator as makeEnumEmptyValueCreator_1 } from "@vaadin/hilla-lit-form";
import EnumEntity_1 from "./EnumEntity.js";
class EnumEntityModel extends EnumModel_1<typeof EnumEntity_1> {
    static override createEmptyValue = makeEnumEmptyValueCreator_1(EnumEntityModel);
    readonly [_enum_1] = EnumEntity_1;
}
export default EnumEntityModel;
