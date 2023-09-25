import {
    _enum as _enum_1,
    EnumModel as EnumModel_1,
    makeEnumEmptyValueCreator
} from "@hilla/form";
import NullHandling_1 from "./NullHandling.js";
class NullHandlingModel extends EnumModel_1<typeof NullHandling_1> {
    static override createEmptyValue = makeEnumEmptyValueCreator(NullHandlingModel);
    readonly [_enum_1] = NullHandling_1;
}
export default NullHandlingModel;
