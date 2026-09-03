import { _enum as _enum_1, EnumModel as EnumModel_1, makeEnumEmptyValueCreator as makeEnumEmptyValueCreator_1 } from "@vaadin/hilla-lit-form";
import NullHandling_1 from "./NullHandling.js";
class NullHandlingModel extends EnumModel_1<typeof NullHandling_1> {
    static override createEmptyValue = makeEnumEmptyValueCreator_1(NullHandlingModel);
    readonly [_enum_1] = NullHandling_1;
}
export default NullHandlingModel;
