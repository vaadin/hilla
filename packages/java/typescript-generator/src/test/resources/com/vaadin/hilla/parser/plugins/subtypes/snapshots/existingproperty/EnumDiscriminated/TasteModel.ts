import { _enum as _enum_1, EnumModel as EnumModel_1, makeEnumEmptyValueCreator as makeEnumEmptyValueCreator_1 } from "@vaadin/hilla-lit-form";
import Taste_1 from "./Taste.js";
class TasteModel extends EnumModel_1<typeof Taste_1> {
    static override createEmptyValue = makeEnumEmptyValueCreator_1(TasteModel);
    readonly [_enum_1] = Taste_1;
}
export default TasteModel;
