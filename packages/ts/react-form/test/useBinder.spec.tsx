import { expect, use } from '@esm-bundle/chai';
import { render } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import { useBinder, useBinderNode } from '../src/index.js';
import { type Login, LoginModel, type User, type UserModel } from './models.js';

use(sinonChai);

describe('@hilla/react-form', () => {
  let onSubmit: sinon.SinonStub<[Login], Promise<Login> | Promise<void>>;

  type UserFormProps = Readonly<{
    model: UserModel;
  }>;

  function UserForm({ model: m }: UserFormProps) {
    const { field, model } = useBinderNode<User, UserModel>(m);

    return (
      <fieldset>
        <input data-testid="user.name" type="text" {...field(model.name)} />
        <input data-testid="user.password" type="text" {...field(model.password)} />
      </fieldset>
    );
  }

  function LoginForm() {
    const { field, model, submit } = useBinder(LoginModel, { onSubmit: sinon.stub() });

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
    onSubmit = sinon.stub();
  });

  describe('useBinder', () => {
    it('collects info from a form', async () => {
      const user = userEvent.setup();
      const { getByTestId } = render(<LoginForm />);

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
  });
});
