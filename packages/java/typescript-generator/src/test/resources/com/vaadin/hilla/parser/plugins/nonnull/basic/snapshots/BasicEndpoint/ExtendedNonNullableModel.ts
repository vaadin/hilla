import type NonNullableModel from "./NonNullableModel.js";
interface ExtendedNonNullableModel extends NonNullableModel {
    mixedAnnotations: Array<string>;
    nonTypeAnnotation: string;
}
export default ExtendedNonNullableModel;
