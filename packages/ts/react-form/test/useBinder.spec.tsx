import { expect, use } from '@esm-bundle/chai';
import { Pattern, getBinderNode, type Validator, type ValidationCallback } from '@hilla/form';
import { act, render, type RenderResult } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import chaiDom from 'chai-dom';
import chaiThings from 'chai-things';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import { useBinder as _useBinder, useBinderNode as _useBinderNode } from '../src/index.js';
import { type Login, LoginModel, type UserModel } from './models.js';

use(sinonChai);
use(chaiDom);
use(chaiThings);

describe('@hilla/react-form', () => {
  describe('useBinder', () => {
    const useBinder = sinon.spy<typeof _useBinder<Login, LoginModel>>(_useBinder);
    const useBinderNode = sinon.spy<typeof _useBinderNode<UserModel>>(_useBinderNode);

    let user: ReturnType<(typeof userEvent)['setup']>;
    let onSubmit: (value: Login) => Promise<Login>;
    let result: RenderResult;

    type UserFormProps = Readonly<{
      model: UserModel;
    }>;

    function UserForm({ model: m }: UserFormProps) {
      const { field, model } = useBinderNode(m);
      const name = useBinderNode(model.name);
      const password = useBinderNode(model.password);

      return (
        <fieldset>
          <input data-testid="user.name" type="text" {...field(model.name)} />
          {name.invalid && (
            <output data-testid="user.name.validation">{name.ownErrors.map((e) => e.message).join(', ')}</output>
          )}
          <input data-testid="user.password" type="text" {...field(model.password)} />
          {password.invalid && (
            <output data-testid="user.password.validation">
              {password.ownErrors.map((e) => e.message).join(', ')}
            </output>
          )}
        </fieldset>
      );
    }

    function LoginForm() {
      const { field, model, submit, value } = useBinder(LoginModel, { onSubmit });

      return (
        <>
          <section>
            <UserForm model={model.user} />
            <input data-testid="rememberMe" type="checkbox" {...field(model.rememberMe)} />
          </section>
          {/* eslint-disable-next-line @typescript-eslint/no-misused-promises */}
          <button data-testid="submit" onClick={submit}>
            Submit
          </button>
        </>
      );
    }

    beforeEach(() => {
      user = userEvent.setup();
      onSubmit = sinon.stub();
      useBinder.resetHistory();
      result = render(<LoginForm />);
    });

    describe('HTML interaction', () => {
      it('collects info from a form', async () => {
        const { getByTestId } = result;

        await user.click(getByTestId('user.name'));
        await user.keyboard('johndoe');
        await user.click(getByTestId('user.password'));
        await user.keyboard('john123456');
        await user.click(getByTestId('rememberMe'));
        await user.click(getByTestId('submit'));

        expect(onSubmit).to.have.been.calledWithMatch({
          rememberMe: true,
          user: {
            name: 'johndoe',
            password: 'john123456',
          },
        });
      });

      it('shows empty values by default', async () => {
        const { getByTestId } = result;

        expect(getByTestId('user.name')).to.have.value('');
        expect(getByTestId('user.password')).to.have.value('');
        expect(getByTestId('rememberMe')).to.have.not.been.checked;
      });

      it('displays default value', async () => {
        const { value } = useBinder.returnValues[0];

        expect(value.user.name).to.equal('');
        expect(value.rememberMe).to.be.undefined;
      });

      it('updates displayed value', async () => {
        const { getByTestId } = result;

        await user.click(getByTestId('user.name'));
        await user.keyboard('johndoe');
        await user.click(getByTestId('rememberMe'));

        // Select returned value for the last call
        const { value } = useBinder.returnValues.at(-1)!;

        expect(value.user.name).to.equal('johndoe');
        expect(value.rememberMe).to.be.true;
      });

      describe('validation', () => {
        it('does not call onSubmit if the form is invalid', async () => {
          const { getByTestId } = result;

          await user.click(getByTestId('user.name'));
          await user.keyboard('johndoe');
          await user.click(getByTestId('submit'));

          expect(onSubmit).to.have.not.been.called;
        });

        it('does not call onSubmit if the form has not been touched', async () => {
          const { getByTestId } = result;

          await user.click(getByTestId('submit'));

          expect(onSubmit).to.have.not.been.called;
        });

        it('shows validation errors', async () => {
          function getNameFieldErrors() {
            const { model } = useBinderNode.returnValues.at(-1)!;
            const { ownErrors, invalid } = getBinderNode(model.name);
            return invalid ? ownErrors.map((e) => e.message) : null;
          }

          const { getByTestId } = result;

          await user.click(getByTestId('user.name'));
          await user.keyboard('Very lengthy name');
          await user.click(getByTestId('user.password'));

          expect(getNameFieldErrors()).to.contain.something.that.have.string('size');

          // clicking around should not hide the message
          await user.click(getByTestId('user.name'));
          await user.click(getByTestId('user.password'));

          expect(getNameFieldErrors()).to.contain.something.that.have.string('size');

          // clearing should show a required validator message
          await user.click(getByTestId('user.name'));
          await user.clear(getByTestId('user.name'));
          await user.click(getByTestId('user.password'));

          expect(getNameFieldErrors()).to.contain.something.that.have.string('invalid');

          // fix
          await user.click(getByTestId('user.name'));
          await user.keyboard('jane');

          expect(getNameFieldErrors()).to.be.null;
        });
      });
    });

    describe('API', () => {
      it('shows read values', async () => {
        const { getByTestId } = result;

        await act(async () => {
          const { read } = useBinder.returnValues[0];
          read({
            rememberMe: true,
            user: {
              id: 1,
              name: 'johndoe',
              password: 'john123456',
            },
          });
        });

        expect(getByTestId('user.name')).to.have.value('johndoe');
        expect(getByTestId('user.password')).to.have.value('john123456');
        expect(getByTestId('rememberMe')).to.have.been.checked;
      });

      it('sets the value programmatically', async () => {
        const { getByTestId } = result;

        await act(async () => {
          const { setValue } = useBinder.returnValues[0];
          setValue({
            rememberMe: true,
            user: {
              id: 1,
              name: 'johndoe',
              password: 'john123456',
            },
          });
        });

        expect(getByTestId('user.name')).to.have.value('johndoe');
        expect(getByTestId('user.password')).to.have.value('john123456');
        expect(getByTestId('rememberMe')).to.have.been.checked;
      });

      it('sets the "visited" flag programmatically', async () => {
        const { getByTestId } = result;

        await act(async () => {
          const { setVisited } = useBinder.returnValues[0];
          setVisited(true);
        });

        expect(getByTestId('validation.user.name')).to.contain.text('invalid');
      });

      it('adds a new validator', async () => {
        class LoginValidator implements Validator<Login> {
          message = 'invalid';
          #nested = new Pattern(/(?!12345)/u);

          validate({ user: { password } }: Login): boolean {
            return this.#nested.validate(password);
          }
        }

        const { getByTestId } = result;

        await act(async () => {
          const { addValidator } = useBinder.returnValues[0];
          addValidator(new LoginValidator());
        });

        await user.click(getByTestId('user.password'));
        await user.keyboard('12345');

        expect().to.contain.text('invalid');
      });
    });
  });
});
