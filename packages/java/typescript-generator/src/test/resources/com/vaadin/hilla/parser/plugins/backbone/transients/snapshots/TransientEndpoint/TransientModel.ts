import type NonTransientEntity from "../NonTransientEntity.js";
interface TransientModel {
    nonTransientEntity?: NonTransientEntity;
    notTransientField?: string;
}
export default TransientModel;
