import { _enum as _enum_1, EnumModel as EnumModel_1, makeEnumEmptyValueCreator as makeEnumEmptyValueCreator_1 } from "@vaadin/hilla-lit-form";
import Colour_1 from "./Colour.js";
class ColourModel extends EnumModel_1<typeof Colour_1> {
    static override createEmptyValue = makeEnumEmptyValueCreator_1(ColourModel);
    readonly [_enum_1] = Colour_1;
}
export default ColourModel;
