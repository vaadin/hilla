declare global {
  // eslint-disable-next-line @typescript-eslint/no-namespace
  export namespace Chai {
    interface Assertion {
      looseDeepEqual(obj: object): Assertion;
    }
  }
}

function removeUndefined(obj: unknown): unknown {
  if (Array.isArray(obj)) {
    return obj.map((v) => (typeof v === 'object' ? removeUndefined(v) : v));
  }

  if (obj != null && typeof obj === 'object') {
    return Object.fromEntries(
      Object.entries(obj)
        .filter(([, value]) => value !== undefined)
        .map(([key, value]) => [key, removeUndefined(value)]),
    );
  }

  return obj;
}

export default function chaiLooseDeepEqual(chai: Chai.ChaiStatic, utils: Chai.ChaiUtils): void {
  utils.addMethod(
    chai.Assertion.prototype,
    'looseDeepEqual',
    // eslint-disable-next-line prefer-arrow-callback
    function looseDeepEqual(this: object, expected: object): void {
      const actual = utils.flag(this, 'object');

      const clone = removeUndefined(actual);

      chai.assert.equal(clone, expected);
    },
  );
}
