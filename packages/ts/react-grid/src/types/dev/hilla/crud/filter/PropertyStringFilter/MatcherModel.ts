import {
    _enum as _enum_1,
    EnumModel as EnumModel_1,
    makeEnumEmptyValueCreator
} from "@hilla/form";
import Matcher_1 from "./Matcher.js";
class MatcherModel extends EnumModel_1<typeof Matcher_1> {
    static override createEmptyValue = makeEnumEmptyValueCreator(MatcherModel);

    readonly [_enum_1] = Matcher_1;
}
export default MatcherModel;
