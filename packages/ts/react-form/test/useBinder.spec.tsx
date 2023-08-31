import { expect, use } from '@esm-bundle/chai';
import { act, render } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import { useBinder as _useBinder, useBinderNode } from '../src/index.js';
import { type Login, LoginModel, type User, type UserModel } from './models.js';

use(sinonChai);

describe('@hilla/react-form', () => {
  type UseBinderSpy = sinon.SinonSpy<Parameters<typeof _useBinder>, ReturnType<typeof _useBinder>>;
  const useBinder = sinon.spy(_useBinder) as typeof _useBinder;

  let onSubmit: (value: Login) => Promise<Login>;
  let onChange: (value: Login) => void;

  type UserFormProps = Readonly<{
    model: UserModel;
  }>;

  function UserForm({ model: m }: UserFormProps) {
    const { field, model } = useBinderNode(m);
    const name = useBinderNode(m.name);

    return (
      <fieldset>
        <input data-testid="user.name" type="text" {...field(model.name)} />
        <output data-testid="validation.user.name">{name.invalid ? name.ownErrors.map(e => e.message).join(', ') : 'OK'}</output>
        <input data-testid="user.password" type="text" {...field(model.password)} />
      </fieldset>
    );
  }

  function LoginForm() {
    const { field, model, read, submit, value } = useBinder(LoginModel, { onChange, onSubmit });

    return (
      <>
        <section>
          <UserForm model={model.user} />
          <input data-testid="rememberMe" type="checkbox" {...field(model.rememberMe)} />
        </section>
        <output data-testid="output.user.name">{value.user.name}</output>
        <output data-testid="output.rememberMe">{String(value.rememberMe)}</output>
        {/* eslint-disable-next-line @typescript-eslint/no-misused-promises */}
        <button data-testid="submit" onClick={submit}>
          Submit
        </button>
      </>
    );
  }

  async function fillAndSubmitLoginForm(getByTestId: (id: string) => HTMLElement) {
    const user = userEvent.setup();
    await user.click(getByTestId('user.name'));
    await user.keyboard('johndoe');
    await user.click(getByTestId('user.password'));
    await user.keyboard('john123456');
    await user.click(getByTestId('rememberMe'));
    await user.click(getByTestId('submit'));
  }

  beforeEach(() => {
    onSubmit = sinon.stub();
    onChange = sinon.stub();
    (useBinder as UseBinderSpy).resetHistory();
  });

  describe('useBinder', () => {
    it('collects info from a form', async () => {
      const { getByTestId } = render(<LoginForm />);

      await fillAndSubmitLoginForm(getByTestId);

      expect(onSubmit).to.have.been.calledWithMatch({
        rememberMe: true,
        user: {
          name: 'johndoe',
          password: 'john123456',
        },
      });
    });

    it('does not call onSubmit if the form is invalid', async () => {
      const user = userEvent.setup();
      const { getByTestId } = render(<LoginForm />);

      await user.click(getByTestId('user.name'));
      await user.keyboard('johndoe');
      await user.click(getByTestId('submit'));

      expect(onSubmit).to.not.have.been.called;
    });

    it('does not call onSubmit if the form has not been touched', async () => {
      const user = userEvent.setup();
      const { getByTestId } = render(<LoginForm />);

      await user.click(getByTestId('submit'));

      expect(onSubmit).to.not.have.been.called;
    });

    it('shows empty values by default', async () => {
      const { getByTestId } = render(<LoginForm />);

      expect(getByTestId('user.name')).to.have.property('value', '');
      expect(getByTestId('user.password')).to.have.property('value', '');
      expect(getByTestId('rememberMe')).to.have.property('checked', false);
    });

    it('shows read values', async () => {
      const { getByTestId } = render(<LoginForm />);

      await act(() => {
        const { read } = (useBinder as UseBinderSpy).returnValues[0];
        read({
          rememberMe: true,
          user: {
            id: 1,
            name: 'johndoe',
            password: 'john123456',
          },
        });
      });

      expect(getByTestId('user.name')).to.have.property('value', 'johndoe');
      expect(getByTestId('user.password')).to.have.property('value', 'john123456');
      expect(getByTestId('rememberMe')).to.have.property('checked', true);
    });

    it('dispays default value', async () => {
      const { getByTestId } = render(<LoginForm />);

      expect(getByTestId('output.user.name')).to.have.property('textContent', '')
      expect(getByTestId('output.rememberMe')).to.have.property('textContent', 'undefined');
    });

    it('updates displayed value', async () => {
      const user = userEvent.setup();
      const { getByTestId } = render(<LoginForm />);

      await user.click(getByTestId('user.name'));
      await user.keyboard('johndoe');
      await user.click(getByTestId('rememberMe'));

      expect(getByTestId('output.user.name')).to.have.property('textContent', 'johndoe')
      expect(getByTestId('output.rememberMe')).to.have.property('textContent', 'true');
    });

    it('shows validation errors', async () => {
      const user = userEvent.setup();
      const { getByTestId } = render(<LoginForm />);

      await user.click(getByTestId('user.name'));
      await user.keyboard('Very lengthy name');
      await user.click(getByTestId('user.password'))

      expect(getByTestId('validation.user.name').textContent).to.have.string('size');

      // clicking around should not hide the message
      await user.click(getByTestId('user.name'));
      await user.click(getByTestId('user.password'))

      expect(getByTestId('validation.user.name').textContent).to.have.string('size');

      // clearing should show a required validator message
      await user.click(getByTestId('user.name'));
      await user.clear(getByTestId('user.name'));
      await user.click(getByTestId('user.password'))

      expect(getByTestId('validation.user.name').textContent).to.have.string('invalid');

      // fix
      await user.click(getByTestId('user.name'));
      await user.keyboard('jane');

      expect(getByTestId('validation.user.name').textContent).to.have.string('OK');
    });

    describe('configuration update', () => {
      it('should use updated onSubmit reference', async () => {
        // Initial render
        const { getByTestId, rerender } = render(<LoginForm />);

        // Update onSubmit reference, rerender, fill form and submit
        onSubmit = sinon.spy();
        rerender(<LoginForm />);
        await fillAndSubmitLoginForm(getByTestId);

        expect(onSubmit).to.have.been.calledOnce;
      });

      it('should use updated onChange reference', async () => {
        // Initial render
        const user = userEvent.setup();
        const { getByTestId, rerender } = render(<LoginForm />);

        // Update onChange reference, rerender, type a character
        onChange = sinon.spy();
        rerender(<LoginForm />);
        await user.click(getByTestId('user.name'));
        await user.keyboard('a');

        expect(onChange).to.have.been.calledOnce;
      });
    });
  });
});
