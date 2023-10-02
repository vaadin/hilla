import { expect } from '@esm-bundle/chai';
// API to test
import {
  _enum,
  _fromString,
  _key,
  _meta,
  ArrayModel,
  Binder,
  EnumModel,
  IsNumber,
  NotBlank,
  NotEmpty,
  NotNull,
  NumberModel,
  ObjectModel,
  type ModelMetadata,
  Positive,
  Size,
} from '../src/index.js';

import {
  type IdEntity,
  IdEntityModel,
  RecordStatus,
  RecordStatusModel,
  TestModel,
  WithPossibleCharListModel,
} from './TestModels.js';

describe('@hilla/form', () => {
  describe('Model', () => {
    let binder: Binder<TestModel>;

    beforeEach(() => {
      binder = new Binder(document.createElement('div'), TestModel);
    });

    describe('model/requiredFlag', () => {
      it('should not be initially required', () => {
        expect(binder.for(binder.model.fieldString).required).to.be.false;
      });

      it(`NotEmpty validator should mark a model as required`, () => {
        binder.for(binder.model.fieldString).addValidator(new NotEmpty());
        expect(binder.for(binder.model.fieldString).required).to.be.true;
      });

      it(`NotNull validator should mark a model as required`, () => {
        binder.for(binder.model.fieldString).addValidator(new NotNull());
        expect(binder.for(binder.model.fieldString).required).to.be.true;
      });

      it(`NotBlank validator should mark a model as required`, () => {
        binder.for(binder.model.fieldString).addValidator(new NotBlank());
        expect(binder.for(binder.model.fieldString).required).to.be.true;
      });

      it(`Size validator with min bigger than 0 should mark a model as required`, () => {
        binder.for(binder.model.fieldString).addValidator(new Size({ min: 1 }));
        expect(binder.for(binder.model.fieldString).required).to.be.true;
      });

      it(`Size validator with min 0 should not be mark a model as required`, () => {
        binder.for(binder.model.fieldString).addValidator(new Size({ min: 0 }));
        expect(binder.for(binder.model.fieldString).required).to.be.false;
      });
    });

    describe('number model', () => {
      it('should contain IsNumber validator by default', () => {
        const { validators } = binder.for(binder.model.fieldNumber);
        expect(validators[0]).to.be.instanceOf(IsNumber);
      });

      describe('_fromString', () => {
        let fromString: (str: string) => number | undefined;

        beforeEach(() => {
          fromString = binder.model.fieldNumber[_fromString];
        });

        it('should disallow empty string', () => {
          expect(fromString('')).to.equal(undefined);
        });

        it('should integer format', () => {
          expect(fromString('0')).to.equal(0);
          expect(fromString('01')).to.equal(1);
          expect(fromString('10')).to.equal(10);
          expect(fromString('+10')).to.equal(10);
          expect(fromString('-10')).to.equal(-10);
        });

        it('should support decimal format', () => {
          expect(fromString('1.2')).to.equal(1.2);
          expect(fromString('.2')).to.equal(0.2);
          expect(fromString('+1.2')).to.equal(1.2);
          expect(fromString('-1.2')).to.equal(-1.2);
        });

        it('should disallow incorrect formats', () => {
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

    describe('boolean model', () => {
      describe('_fromString', () => {
        let fromString: (str: string) => boolean;

        beforeEach(() => {
          fromString = binder.model.fieldBoolean[_fromString];
        });

        it('should do semantic conversion from string to boolean model', () => {
          // The validator.js library is used as a reference of valid boolean values
          // see https://github.com/validatorjs/validator.js/blob/master/src/lib/isBoolean.js
          expect(fromString('true')).to.be.true;
          expect(fromString('false')).to.be.false;
          expect(fromString('1')).to.be.true;
          expect(fromString('0')).to.be.false;
          // loose values are also converted and case doesn't matter (again, see validator)
          expect(fromString('yes')).to.be.true;
          expect(fromString('no')).to.be.false;
          expect(fromString('TRUE')).to.be.true;
          expect(fromString('FALSE')).to.be.false;
          expect(fromString('Yes')).to.be.true;
          expect(fromString('No')).to.be.false;
          // all other values are treated as false
          expect(fromString('')).to.be.false;
          expect(fromString('other')).to.be.false;
        });
      });
    });

    describe('array model', () => {
      const strings = ['foo', 'bar'];

      const idEntities: readonly IdEntity[] = [
        { ...IdEntityModel.createEmptyValue(), idString: 'id0' },
        { ...IdEntityModel.createEmptyValue(), idString: 'id1' },
      ];

      beforeEach(() => {
        binder.value = {
          ...binder.value,
          fieldArrayModel: idEntities.slice(),
          fieldArrayString: strings.slice(),
        };
      });

      it('should be iterable', () => {
        [binder.model.fieldArrayString, binder.model.fieldArrayModel].forEach((arrayModel) => {
          const values = binder.for(arrayModel).value!;
          const iterator = arrayModel[Symbol.iterator]();
          for (let i = 0; i < values.length; i++) {
            const iteratorResult = iterator.next();
            expect(iteratorResult.done).to.be.false;
            const binderNode = iteratorResult.value;
            // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
            expect(binderNode.model[_key]).to.equal(i);
            // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
            expect(binderNode.value).to.equal(values[i]);
          }

          expect(iterator.next().done).to.be.true;
        });
      });

      it('should support prependItem on binder node', () => {
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

      it('should support appendItem on binder node', () => {
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
      it('array item defaultValue should not be undefined', () => {
        binder.for(binder.model.fieldArrayModel).appendItem();
        const entityModels = [...binder.model.fieldArrayModel];
        expect(binder.for(entityModels[0].model).defaultValue).to.be.not.undefined;
      });

      it('should support removeSelf on binder node', () => {
        binder.for(binder.model.fieldArrayString).appendItem();
        const stringModels = [...binder.model.fieldArrayString];
        binder.for(stringModels[1].model).removeSelf();

        expect(binder.for(binder.model.fieldArrayString).value).to.deep.equal(['foo', '']);

        binder.for(binder.model.fieldArrayModel).appendItem();
        const entityModels = [...binder.model.fieldArrayModel];
        binder.for(entityModels[1].model).removeSelf();

        expect(binder.for(binder.model.fieldArrayModel).value).to.deep.equal([{ idString: 'id0' }, { idString: '' }]);
      });

      it('should throw for prependItem on non-array binder node', () => {
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

      it('should throw for appendItem on non-array binder node', () => {
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

      it('should throw for removeSelf on non-array item binder node', () => {
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

      it('should reuse model instance for the same array item', () => {
        const nodes1 = [...binder.model.fieldArrayModel].slice();
        [0, 1].forEach((i) => expect(nodes1[i].value).to.be.equal(idEntities[i]));

        binder.for(binder.model.fieldArrayModel).value = idEntities.slice();
        const nodes2 = [...binder.model.fieldArrayModel].slice();
        [0, 1].forEach((i) => {
          expect(nodes1[i]).to.be.equal(nodes2[i]);
          expect(nodes1[i].model).to.be.equal(nodes2[i].model);
          expect(nodes2[i].value).to.be.equal(idEntities[i]);
        });
      });

      it('should reuse model instance for the same array item after it is modified', () => {
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

      it('should update model keySymbol when inserting items', () => {
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

      it('should pass variable arguments down', () => {
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

    describe('enum model', () => {
      it('should get default enum value', () => {
        expect(RecordStatusModel.createEmptyValue()).to.equal(RecordStatus.CREATED);
      });

      it('should get the enum object from the model', () => {
        expect(binder.model.fieldEnum[_enum]).to.equal(RecordStatus);
      });

      it('should record and return the value', () => {
        binder.for(binder.model.fieldEnum).value = RecordStatus.REMOVED;
        expect(binder.model.fieldEnum.valueOf()).to.equal(RecordStatus.REMOVED);
      });

      it('should be undefined if the EnumModel.createEmptyValue() is used', () => {
        expect(EnumModel.createEmptyValue()).to.be.undefined;
      });

      it('should extract value from string', () => {
        expect(binder.model.fieldEnum[_fromString]('UPDATED')).to.equal(RecordStatus.UPDATED);
        expect(binder.model.fieldEnum[_fromString]('unknown')).to.be.undefined;
      });
    });

    describe('misc', () => {
      it('should correctly read for the model with a single optional value', async () => {
        const withPossibleCharListBinder = new Binder(document.createElement('div'), WithPossibleCharListModel);
        const charListBinder = withPossibleCharListBinder.for(withPossibleCharListBinder.model.charList);

        charListBinder.addValidator({
          message: 'error',
          validate(value: string) {
            return value.length >= 2 && value.length <= 3;
          },
        });
        withPossibleCharListBinder.read({});

        charListBinder.value = 'a';

        let results = await withPossibleCharListBinder.validate();

        expect(results.length).to.equal(1);
        expect(results[0].property).to.equal('charList');
        expect(results[0].message).to.equal('error');

        charListBinder.value = 'aa';

        results = await withPossibleCharListBinder.validate();

        expect(results.length).to.equal(0);
      });
    });
  });

  describe('metadata', () => {
    it('should initialize with empty metadata by default', () => {
      const model = new ObjectModel(null as any, '', true);
      expect(model[_meta]).to.eql({});
    });

    it('should initialize with metadata from options', () => {
      const meta: ModelMetadata = {
        javaType: 'java.lang.String',
        annotations: [{ name: 'jakarta.persistence.Id' }, { name: 'jakarta.persistence.Version' }],
      };
      const model = new ObjectModel(null as any, '', true, { meta });
      expect(model[_meta]).to.equal(meta);
    });
  });
});
