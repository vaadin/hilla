export type ListPosition = Readonly<{
  after?: number;
  before?: number;
}>;

export function findInsertIndex(children: readonly number[], { before, after }: ListPosition): number {
  if (after != null) {
    let position: number;

    // After edge -> insert first
    if (after === 0) {
      position = 0;
    } else {
      const idx = children.findIndex((childId) => childId === after);
      if (idx === -1) {
        return -1;
      }
      position = idx + 1;
    }

    // Validate before constraint if there is one
    if (before != null) {
      const atPosition = position < children.length ? children[position] : 0;
      if (atPosition !== before) {
        return -1;
      }
    }

    return position;
  }

  // If 'after' is not specified
  if (before == null) {
    return -1;
  }

  // Before edge -> insert last
  if (before === 0) {
    return children.length;
  }

  return children.findIndex((childId) => childId === before);
}
