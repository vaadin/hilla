import { expect } from '@open-wc/testing';
// API to test
import {
  _fromString,
  _key,
  ArrayModel,
  Binder,
  IsNumber,
  NotBlank,
  NotEmpty,
  NotNull,
  NumberModel,
  Positive,
  Size,
} from '../src';

import { IdEntity, IdEntityModel, TestEntity, TestModel } from './TestModels';

describe('form/Model', () => {
  let binder: Binder<TestEntity, TestModel>;

  beforeEach(() => {
    binder = new Binder(document.createElement('div'), TestModel);
  });

  describe('model/requiredFlag', () => {
    it('should not be initially required', async () => {
      expect(binder.for(binder.model.fieldString).required).to.be.false;
    });

    it(`NotEmpty validator should mark a model as required`, async () => {
      binder.for(binder.model.fieldString).addValidator(new NotEmpty());
      expect(binder.for(binder.model.fieldString).required).to.be.true;
    });

    it(`NotNull validator should mark a model as required`, async () => {
      binder.for(binder.model.fieldString).addValidator(new NotNull());
      expect(binder.for(binder.model.fieldString).required).to.be.true;
    });

    it(`NotBlank validator should mark a model as required`, async () => {
      binder.for(binder.model.fieldString).addValidator(new NotBlank());
      expect(binder.for(binder.model.fieldString).required).to.be.true;
    });

    it(`Size validator with min bigger than 0 should mark a model as required`, async () => {
      binder.for(binder.model.fieldString).addValidator(new Size({ min: 1 }));
      expect(binder.for(binder.model.fieldString).required).to.be.true;
    });

    it(`Size validator with min 0 should not be mark a model as required`, async () => {
      binder.for(binder.model.fieldString).addValidator(new Size({ min: 0 }));
      expect(binder.for(binder.model.fieldString).required).to.be.false;
    });
  });

  describe('number model', () => {
    it('should contain IsNumber validator by default', async () => {
      const { validators } = binder.for(binder.model.fieldNumber);
      expect(validators[0]).to.be.instanceOf(IsNumber);
    });

    describe('_fromString', () => {
      let fromString: (str: string) => number;

      beforeEach(() => {
        fromString = binder.model.fieldNumber[_fromString];
      });

      it('should disallow empty string', async () => {
        expect(fromString('')).to.satisfy(Number.isNaN);
      });

      it('should integer format', async () => {
        expect(fromString('0')).to.equal(0);
        expect(fromString('01')).to.equal(1);
        expect(fromString('10')).to.equal(10);
        expect(fromString('+10')).to.equal(10);
        expect(fromString('-10')).to.equal(-10);
      });

      it('should support decimal format', async () => {
        expect(fromString('1.2')).to.equal(1.2);
        expect(fromString('.2')).to.equal(0.2);
        expect(fromString('+1.2')).to.equal(1.2);
        expect(fromString('-1.2')).to.equal(-1.2);
      });

      it('should disallow incorrect formats', async () => {
        expect(fromString('1.')).to.satisfy(Number.isNaN);
        // Wrong separator
        expect(fromString('1,')).to.satisfy(Number.isNaN);
        expect(fromString(',2')).to.satisfy(Number.isNaN);
        expect(fromString('1,2')).to.satisfy(Number.isNaN);
        // Extra symbols
        expect(fromString('e1')).to.satisfy(Number.isNaN);
        expect(fromString('1e')).to.satisfy(Number.isNaN);
        expect(fromString('1e0')).to.satisfy(Number.isNaN);
      });
    });
  });

  describe('array model', () => {
    const strings = ['foo', 'bar'];

    const idEntities: ReadonlyArray<IdEntity> = [
      { ...IdEntityModel.createEmptyValue(), idString: 'id0' },
      { ...IdEntityModel.createEmptyValue(), idString: 'id1' },
    ];

    beforeEach(() => {
      binder.value = {
        ...binder.value,
        fieldArrayString: strings.slice(),
        fieldArrayModel: idEntities.slice(),
      };
    });

    it('should be iterable', async () => {
      [binder.model.fieldArrayString, binder.model.fieldArrayModel].forEach((arrayModel) => {
        const values = binder.for(arrayModel).value!;
        const iterator = arrayModel[Symbol.iterator]();
        for (let i = 0; i < values.length; i++) {
          const iteratorResult = iterator.next();
          expect(iteratorResult.done).to.be.false;
          const binderNode = iteratorResult.value;
          expect(binderNode.model[_key]).to.equal(i);
          expect(binderNode.value).to.equal(values[i]);
        }

        expect(iterator.next().done).to.be.true;
      });
    });

    it('should support prependItem on binder node', async () => {
      binder.for(binder.model.fieldArrayString).prependItem();
      binder.for(binder.model.fieldArrayString).prependItem('new');

      expect(binder.for(binder.model.fieldArrayString).value).to.deep.equal(['new', '', 'foo', 'bar']);

      binder.for(binder.model.fieldArrayModel).prependItem();
      binder.for(binder.model.fieldArrayModel).prependItem({ idString: 'new' });

      expect(binder.for(binder.model.fieldArrayModel).value).to.deep.equal([
        { idString: 'new' },
        { idString: '' },
        { idString: 'id0' },
        { idString: 'id1' },
      ]);
    });

    it('should support appendItem on binder node', async () => {
      binder.for(binder.model.fieldArrayString).appendItem();
      binder.for(binder.model.fieldArrayString).appendItem('new');

      expect(binder.for(binder.model.fieldArrayString).value).to.deep.equal(['foo', 'bar', '', 'new']);

      binder.for(binder.model.fieldArrayModel).appendItem();
      binder.for(binder.model.fieldArrayModel).appendItem({ idString: 'new' });

      expect(binder.for(binder.model.fieldArrayModel).value).to.deep.equal([
        { idString: 'id0' },
        { idString: 'id1' },
        { idString: '' },
        { idString: 'new' },
      ]);
    });

    /**
     * default value is used for checking dirty state
     * use case: adding a new order line, which contains a product
     * and a quantity field. The product model's dirty state is
     * comparing the default value, which is getting from the parent
     * order line model, which is an array item.
     */
    it('array item defaultValue should not be undefined', async () => {
      binder.for(binder.model.fieldArrayModel).appendItem();
      const entityModels = [...binder.model.fieldArrayModel];
      expect(binder.for(entityModels[0].model).defaultValue).to.be.not.undefined;
    });

    it('should support removeSelf on binder node', async () => {
      binder.for(binder.model.fieldArrayString).appendItem();
      const stringModels = [...binder.model.fieldArrayString];
      binder.for(stringModels[1].model).removeSelf();

      expect(binder.for(binder.model.fieldArrayString).value).to.deep.equal(['foo', '']);

      binder.for(binder.model.fieldArrayModel).appendItem();
      const entityModels = [...binder.model.fieldArrayModel];
      binder.for(entityModels[1].model).removeSelf();

      expect(binder.for(binder.model.fieldArrayModel).value).to.deep.equal([{ idString: 'id0' }, { idString: '' }]);
    });

    it('should throw for prependItem on non-array binder node', async () => {
      [
        binder,
        binder.for(binder.model.fieldString),
        binder.for(binder.model.fieldBoolean),
        binder.for(binder.model.fieldNumber),
        binder.for(binder.model.fieldObject),
      ].forEach((binderNode) => {
        expect(() => {
          binderNode.prependItem();
        }).to.throw('array');
      });
    });

    it('should throw for appendItem on non-array binder node', async () => {
      [
        binder,
        binder.for(binder.model.fieldString),
        binder.for(binder.model.fieldBoolean),
        binder.for(binder.model.fieldNumber),
        binder.for(binder.model.fieldObject),
      ].forEach((binderNode) => {
        expect(() => {
          binderNode.appendItem();
        }).to.throw('array');
      });
    });

    it('should throw for removeSelf on non-array item binder node', async () => {
      expect(() => {
        binder.removeSelf();
      }).to.throw('array');

      Object.values(binder.model).forEach((model) => {
        const binderNode = binder.for(model);
        expect(() => {
          binderNode.removeSelf();
        }).to.throw('array');
      });
    });

    it('should reuse model instance for the same array item', async () => {
      const nodes1 = [...binder.model.fieldArrayModel].slice();
      [0, 1].forEach((i) => expect(nodes1[i].value).to.be.equal(idEntities[i]));

      binder.for(binder.model.fieldArrayModel).value = idEntities;
      const nodes2 = [...binder.model.fieldArrayModel].slice();
      [0, 1].forEach((i) => {
        expect(nodes1[i]).to.be.equal(nodes2[i]);
        expect(nodes1[i].model).to.be.equal(nodes2[i].model);
        expect(nodes2[i].value).to.be.equal(idEntities[i]);
      });
    });

    it('should reuse model instance for the same array item after it is modified', async () => {
      const nodes1 = [...binder.model.fieldArrayModel].slice();
      [0, 1].forEach((i) => expect(nodes1[i].value).to.be.equal(idEntities[i]));

      binder.for(nodes1[0].model.idString).value = 'foo';
      binder.for(nodes1[1].model.idString).value = 'bar';

      binder.for(binder.model.fieldArrayModel).value = idEntities.slice();
      binder.for(binder.model.fieldArrayModel).prependItem();
      binder.for(binder.model.fieldArrayModel).appendItem();

      const nodes2 = [...binder.model.fieldArrayModel].slice();

      [0, 1].forEach((i) => {
        expect(nodes1[i]).to.be.equal(nodes2[i]);
        expect(nodes1[i].model).to.be.equal(nodes2[i].model);
        expect(nodes2[i + 1].value).to.be.equal(idEntities[i]);
      });
    });

    it('should update model keySymbol when inserting items', async () => {
      const nodes1 = [...binder.model.fieldArrayModel].slice();
      [0, 1].forEach((i) => expect(nodes1[i].value).to.be.equal(idEntities[i]));

      for (let i = 0; i < nodes1.length; i++) {
        expect(nodes1[i].model[_key]).to.be.equal(i);
      }

      binder.for(nodes1[0].model.idString).value = 'foo';
      expect(binder.model.fieldArrayModel.valueOf()[0].idString).to.be.equal('foo');

      binder.for(binder.model.fieldArrayModel).prependItem();
      expect(binder.model.fieldArrayModel.valueOf()[1].idString).to.be.equal('foo');

      const nodes2 = [...binder.model.fieldArrayModel].slice();
      expect(nodes2.length).to.be.equal(3);
      for (let i = 0; i < nodes2.length; i++) {
        expect(nodes2[i].model[_key]).to.be.equal(i);
      }
    });

    it('should pass variable arguments down', async () => {
      const matrix = [
        [0, 1],
        [2, 3],
      ];
      binder.for(binder.model.fieldMatrixNumber).value = matrix;
      let walkedCells = 0;
      Array.from(binder.model.fieldMatrixNumber).forEach((rowBinder, i) => {
        expect(rowBinder.model).to.be.instanceOf(ArrayModel);
        Array.from(rowBinder.model).forEach((cellBinder, j) => {
          expect(cellBinder.model).to.be.instanceOf(NumberModel);
          expect(cellBinder.value).to.be.equal(matrix[i][j]);
          const [, lastValidator] = cellBinder.validators;
          expect(lastValidator).to.be.instanceOf(Positive);
          walkedCells += 1;
        });
      });
      expect(walkedCells).to.equal(4);
    });
  });
});
