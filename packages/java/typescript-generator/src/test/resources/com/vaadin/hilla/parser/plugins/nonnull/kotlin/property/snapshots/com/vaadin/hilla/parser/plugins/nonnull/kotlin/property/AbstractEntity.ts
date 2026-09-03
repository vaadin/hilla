interface AbstractEntity<ID = unknown> {
    version: number;
    id: ID;
}
export default AbstractEntity;
