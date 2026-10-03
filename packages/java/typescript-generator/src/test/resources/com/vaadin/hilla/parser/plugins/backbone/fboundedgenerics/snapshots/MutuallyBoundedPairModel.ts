import { _getPropertyModel as _getPropertyModel_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, ObjectModel as ObjectModel_1 } from "@vaadin/hilla-lit-form";
import type MutuallyBoundedPair_1 from "./MutuallyBoundedPair.js";
class MutuallyBoundedPairModel<T extends MutuallyBoundedPair_1 = MutuallyBoundedPair_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(MutuallyBoundedPairModel);
    get left(): ObjectModel_1 {
        return this[_getPropertyModel_1]("left", (parent, key) => new ObjectModel_1(parent, key, true));
    }
    get right(): ObjectModel_1 {
        return this[_getPropertyModel_1]("right", (parent, key) => new ObjectModel_1(parent, key, true));
    }
}
export default MutuallyBoundedPairModel;
