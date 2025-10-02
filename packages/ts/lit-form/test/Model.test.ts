import {
  Model,
  ArrayModel,
  EnumModel,
  type ModelMetadata,
  NumberModel,
  $defaultValue,
  $meta,
  $enum,
  $key,
} from '@vaadin/hilla-models';
import m from '@vaadin/hilla-models';
import { beforeEach, describe, expect, it } from 'vitest';
// API to test
import { Binder, type BinderNode, IsNumber, NotBlank, NotEmpty, NotNull, Positive, Size } from '../src/index.js';

import { getStringConverter } from '../src/stringConverters.js';
import {
  type IdEntity,
  IdEntityModel,
  RecordStatus,
  RecordStatusModel,
  TestModel,
  WithPossibleCharListModel,
} from './TestModels.js';

describe('@vaadin/hilla-lit-form', () => {
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

      it('should be undefined by the default', () => {
        const { value } = binder.for(binder.model.fieldNumber);
        expect(value).to.be.undefined;
      });

      describe('string converter', () => {
        let fromString: (str: string) => number | undefined;

        beforeEach(() => {
          const stringConverter = getStringConverter(binder.model.fieldNumber);
          if (stringConverter === undefined) {
            expect.fail('stringConverter is undefined');
          }
          // eslint-disable-next-line @typescript-eslint/prefer-destructuring
          fromString = stringConverter.fromString;
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
      describe('string converter', () => {
        let fromString: (str: string) => boolean | undefined;

        beforeEach(() => {
          const stringConverter = getStringConverter(binder.model.fieldBoolean);
          if (stringConverter === undefined) {
            throw new Error('stringConverter is undefined');
          }
          // eslint-disable-next-line @typescript-eslint/prefer-destructuring
          fromString = stringConverter.fromString;
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
      function* toBinderNode<M extends Model>(iterable: Iterable<M>): Generator<BinderNode<M>, undefined, void> {
        for (const value of iterable) {
          yield binder.for(value);
        }
      }

      const strings = ['foo', 'bar'];

      const idEntities: readonly IdEntity[] = [
        { ...IdEntityModel[$defaultValue], idString: 'id0' },
        { ...IdEntityModel[$defaultValue], idString: 'id1' },
      ];

      beforeEach(() => {
        binder.value = {
          ...binder.value,
          fieldArrayModel: idEntities.slice(),
          fieldArrayString: strings.slice(),
        };
      });

      it('should be iterable', () => {
        [binder.model.fieldArrayString, binder.model.fieldArrayModel].forEach((arrayModel: ArrayModel) => {
          const values = binder.for(arrayModel).value!;
          const iterator = toBinderNode(m.items(arrayModel));
          for (let i = 0; i < values.length; i++) {
            const iteratorResult = iterator.next();
            expect(iteratorResult.done).to.be.false;
            const binderNode = iteratorResult.value!;
            expect(binderNode.model[$key]).to.equal(i);
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
        const entityModels = [...m.items(binder.model.fieldArrayModel)];
        expect(binder.for(entityModels[0]).defaultValue).to.be.not.undefined;
      });

      it('should support removeSelf on binder node', () => {
        binder.for(binder.model.fieldArrayString).appendItem();
        const stringModels = [...m.items(binder.model.fieldArrayString)];
        binder.for(stringModels[1]).removeSelf();

        expect(binder.for(binder.model.fieldArrayString).value).to.deep.equal(['foo', '']);

        binder.for(binder.model.fieldArrayModel).appendItem();
        const entityModels = [...m.items(binder.model.fieldArrayModel)];
        binder.for(entityModels[1]).removeSelf();

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
          if (!(model instanceof Model)) {
            return;
          }
          const binderNode = binder.for(model);
          expect(() => {
            binderNode.removeSelf();
          }).to.throw('array');
        });
      });

      it('should reuse model instance for the same array item', () => {
        const nodes1 = [...toBinderNode(m.items(binder.model.fieldArrayModel))];
        [0, 1].forEach((i) => expect(nodes1[i].value).to.be.equal(idEntities[i]));

        binder.for(binder.model.fieldArrayModel).value = idEntities.slice();
        const nodes2 = [...toBinderNode(m.items(binder.model.fieldArrayModel))];
        [0, 1].forEach((i) => {
          expect(nodes1[i]).to.be.equal(nodes2[i]);
          expect(nodes1[i].model).to.be.equal(nodes2[i].model);
          expect(nodes2[i].value).to.be.equal(idEntities[i]);
        });
      });

      it('should reuse model instance for the same array item after it is modified', () => {
        const nodes1 = [...toBinderNode(m.items(binder.model.fieldArrayModel))];
        [0, 1].forEach((i) => expect(nodes1[i].value).to.be.equal(idEntities[i]));

        binder.for(nodes1[0].model.idString).value = 'foo';
        binder.for(nodes1[1].model.idString).value = 'bar';

        binder.for(binder.model.fieldArrayModel).value = idEntities.slice();
        binder.for(binder.model.fieldArrayModel).prependItem();
        binder.for(binder.model.fieldArrayModel).appendItem();

        const nodes2 = [...toBinderNode(m.items(binder.model.fieldArrayModel))];

        [0, 1].forEach((i) => {
          expect(nodes1[i]).to.be.equal(nodes2[i]);
          expect(nodes1[i].model).to.be.equal(nodes2[i].model);
          expect(nodes2[i + 1].value).to.be.equal(idEntities[i]);
        });
      });

      it('should update model keySymbol when inserting items', () => {
        const nodes1 = [...toBinderNode(m.items(binder.model.fieldArrayModel))];
        [0, 1].forEach((i) => expect(nodes1[i].value).to.be.equal(idEntities[i]));

        for (let i = 0; i < nodes1.length; i++) {
          expect(nodes1[i].model[$key]).to.be.equal(i);
        }

        binder.for(nodes1[0].model.idString).value = 'foo';
        expect(binder.for(binder.model.fieldArrayModel).value?.[0].idString).to.be.equal('foo');

        binder.for(binder.model.fieldArrayModel).prependItem();
        expect(binder.for(binder.model.fieldArrayModel).value?.[1].idString).to.be.equal('foo');

        const nodes2 = [...toBinderNode(m.items(binder.model.fieldArrayModel))];
        expect(nodes2.length).to.be.equal(3);
        for (let i = 0; i < nodes2.length; i++) {
          expect(nodes2[i].model[$key]).to.be.equal(i);
        }
      });

      it('should pass variable arguments down', () => {
        const matrix = [
          [0, 1],
          [2, 3],
        ];
        binder.for(binder.model.fieldMatrixNumber).value = matrix;
        let walkedCells = 0;
        Array.from(m.items(binder.model.fieldMatrixNumber)).forEach((rowModel, i) => {
          expect(rowModel).to.be.instanceOf(ArrayModel);
          Array.from(m.items(rowModel)).forEach((cellModel, j) => {
            expect(cellModel).to.be.instanceOf(NumberModel);
            expect(m.getValue(cellModel)).to.be.equal(matrix[i][j]);
            const [, lastValidator] = binder.for(cellModel).validators;
            expect(lastValidator).to.be.instanceOf(Positive);
            walkedCells += 1;
          });
        });
        expect(walkedCells).to.equal(4);
      });
    });

    describe('enum model', () => {
      it('should get default enum value', () => {
        expect(RecordStatusModel[$defaultValue]).to.equal(RecordStatus.CREATED);
      });

      it('should get the enum object from the model', () => {
        expect(binder.model.fieldEnum[$enum]).to.equal(RecordStatus);
      });

      it('should record and return the value', () => {
        binder.for(binder.model.fieldEnum).value = RecordStatus.REMOVED;
        expect(binder.for(binder.model.fieldEnum).value).to.equal(RecordStatus.REMOVED);
      });

      it('should be undefined if the EnumModel defaultValue is used', () => {
        expect(EnumModel[$defaultValue]).to.be.undefined;
      });

      it('should extract value from string', () => {
        const { fromString } = getStringConverter(binder.model.fieldEnum)!;
        expect(fromString('UPDATED')).to.equal(RecordStatus.UPDATED);
        expect(fromString('unknown')).to.be.undefined;
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
    it('should initialize with undefined metadata by default', () => {
      const model = m.object('MetadataObject').build();
      expect(model[$meta]).to.equal(undefined);
    });

    it('should initialize with metadata from options', () => {
      const meta: ModelMetadata = {
        jvmType: 'java.lang.String',
        annotations: [
          { jvmType: 'jakarta.persistence.Id', attributes: {} },
          { jvmType: 'jakarta.persistence.Version' },
        ],
      };
      const model = m.object('MetadataObject').meta(meta).build();
      expect(model[$meta]).to.equal(meta);
    });
  });
});
