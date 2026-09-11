import { _enum, EnumModel, makeEnumEmptyValueCreator } from "@vaadin/hilla-lit-form";
import NullHandling from "./NullHandling.js";
class NullHandlingModel extends EnumModel<typeof NullHandling> {
    static override createEmptyValue = makeEnumEmptyValueCreator(NullHandlingModel);
    readonly [_enum] = NullHandling;
}
export default NullHandlingModel;
