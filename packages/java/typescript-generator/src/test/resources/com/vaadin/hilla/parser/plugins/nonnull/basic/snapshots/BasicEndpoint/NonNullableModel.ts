import type NonNullableModel_1 from "./NonNullableModel.js";
interface NonNullableModel {
    complexTypeField: Record<string, Array<NonNullableModel_1>>;
    nullableField?: string;
    protectedField: string;
    publicField: string;
    typeWithTypeArgument?: Array<string>;
}
export default NonNullableModel;
