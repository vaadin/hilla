import { expect, use } from '@esm-bundle/chai';
import { act, render, type RenderResult, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import chaiAsPromised from 'chai-as-promised';
import chaiDom from 'chai-dom';
import { useEffect, useState } from 'react';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import { useForm as _useForm, useFormPart } from '../src/index.js';
import { type Contract, EntityModel, type Login, LoginModel, type Project, type UserModel } from './models.js';

use(sinonChai);
use(chaiDom);
use(chaiAsPromised);

describe('@hilla/react-form', () => {
  type UseFormSpy = sinon.SinonSpy<Parameters<typeof _useForm>, ReturnType<typeof _useForm>>;
  const useForm = sinon.spy(_useForm) as typeof _useForm;

  let onSubmit: (value: Login) => Promise<Login>;
  let onChange: (value: Login) => void;

  type UserFormProps = Readonly<{
    model: UserModel;
  }>;

  function UserForm({ model: user }: UserFormProps) {
    const { field, model } = useFormPart(user);
    const name = useFormPart(user.name);
    const passwordHint = useFormPart(user.passwordHint);

    return (
      <fieldset>
        <input data-testid="user.name" type="text" {...field(model.name)} />
        <output data-testid="validation.user.name">
          {name.invalid ? name.ownErrors.map((e) => e.message).join(', ') : 'OK'}
        </output>
        <input data-testid="user.password" type="text" {...field(model.password)} />
        <input data-testid="user.passwordHint" type="text" {...field(model.passwordHint)} />
        <output data-testid="validation.user.passwordHint">
          {passwordHint.invalid ? passwordHint.ownErrors.map((e) => e.message).join(', ') : 'OK'}
        </output>
      </fieldset>
    );
  }

  function LoginForm() {
    const { field, model, submit, value, submitting } = useForm(LoginModel, { onChange, onSubmit });

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
        {submitting ? <span data-testid="submitting">Submitting...</span> : null}
      </>
    );
  }

  async function fillAndSubmitLoginForm(
    getByTestId: RenderResult['getByTestId'],
    user: ReturnType<(typeof userEvent)['setup']>,
  ) {
    await user.type(getByTestId('user.name'), 'johndoe');
    await user.type(getByTestId('user.password'), 'john123456');
    await user.click(getByTestId('rememberMe'));
    await user.click(getByTestId('submit'));
  }

  beforeEach(() => {
    onSubmit = sinon.stub();
    onChange = sinon.stub();
    (useForm as UseFormSpy).resetHistory();
  });

  describe('useForm', () => {
    let user: ReturnType<(typeof userEvent)['setup']>;

    beforeEach(() => {
      user = userEvent.setup();
    });

    it('collects info from a form', async () => {
      const { getByTestId } = render(<LoginForm />);

      await fillAndSubmitLoginForm(getByTestId, user);

      expect(onSubmit).to.have.been.calledWithMatch({
        rememberMe: true,
        user: {
          name: 'johndoe',
          password: 'john123456',
        },
      });
    });

    it('does not call onSubmit if the form is invalid', async () => {
      const { getByTestId } = render(<LoginForm />);

      await user.type(getByTestId('user.name'), 'johndoe');
      await user.click(getByTestId('submit'));

      expect(onSubmit).to.not.have.been.called;
    });

    it('does not call onSubmit if the form has not been touched', async () => {
      const { getByTestId } = render(<LoginForm />);

      await user.click(getByTestId('submit'));

      expect(onSubmit).to.not.have.been.called;
    });

    it('forwards submitting state from Binder', async () => {
      let resolveSubmit: () => void = () => {};
      onSubmit = async (login) =>
        new Promise((resolve) => {
          resolveSubmit = () => resolve(login);
        });

      // Initial render - submitting is false
      const { getByTestId, queryByTestId } = render(<LoginForm />);
      expect(queryByTestId('submitting')).to.not.exist;

      // Submit - submitting is true
      await fillAndSubmitLoginForm(getByTestId, user);
      expect(queryByTestId('submitting')).to.exist;

      // Resolve submit - submitting is false
      resolveSubmit();
      await waitFor(() => expect(queryByTestId('submitting')).to.not.exist);
    });

    it('shows empty values by default', () => {
      const { getByTestId } = render(<LoginForm />);

      expect(getByTestId('user.name')).to.have.value('');
      expect(getByTestId('user.password')).to.have.value('');
      expect(getByTestId('rememberMe')).to.not.be.checked;
    });

    it('shows read values', async () => {
      const { getByTestId } = render(<LoginForm />);

      // eslint-disable-next-line @typescript-eslint/require-await
      await act(async () => {
        const { read } = (useForm as UseFormSpy).returnValues[0];
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
      expect(getByTestId('rememberMe')).to.be.checked;
    });

    it('displays default value', () => {
      const { getByTestId } = render(<LoginForm />);

      expect(getByTestId('output.user.name')).to.have.text('');
      expect(getByTestId('output.rememberMe')).to.have.text('undefined');
    });

    it('updates displayed value', async () => {
      const { getByTestId } = render(<LoginForm />);

      await user.type(getByTestId('user.name'), 'johndoe');
      await user.click(getByTestId('rememberMe'));

      expect(getByTestId('output.user.name')).to.have.text('johndoe');
      expect(getByTestId('output.rememberMe')).to.have.text('true');
    });

    it('shows validation errors', async () => {
      const { getByTestId } = render(<LoginForm />);

      await user.type(getByTestId('user.name'), 'Very lengthy name');
      await user.click(getByTestId('user.password'));

      expect(getByTestId('validation.user.name')).to.contain.text('size');

      // clicking around should not hide the message
      await user.click(getByTestId('user.name'));
      await user.click(getByTestId('user.password'));

      expect(getByTestId('validation.user.name')).to.contain.text('size');

      // clearing should show a required validator message
      await user.clear(getByTestId('user.name'));
      await user.click(getByTestId('user.password'));

      expect(getByTestId('validation.user.name')).to.contain.text('invalid');

      // fix
      await user.type(getByTestId('user.name'), 'jane');

      expect(getByTestId('validation.user.name')).to.contain.text('OK');
    });

    it('should correctly handle validators on optional fields', async () => {
      const { getByTestId } = render(<LoginForm />);

      // eslint-disable-next-line @typescript-eslint/require-await
      await act(async () => {
        const { read } = (useForm as UseFormSpy).returnValues[0];
        read({
          user: {
            id: 1,
            name: 'johndoe',
            password: 'john123456',
          },
        });
      });

      expect(getByTestId('validation.user.passwordHint')).to.have.text('OK');

      await user.type(getByTestId('user.passwordHint'), 'a');
      await user.click(getByTestId('submit'));

      expect(getByTestId('validation.user.passwordHint')).to.have.text('OK');
    });

    describe('configuration update', () => {
      it('should use updated onSubmit reference', async () => {
        // Initial render
        const { getByTestId, rerender } = render(<LoginForm />);

        // Update onSubmit reference, rerender, fill form and submit
        onSubmit = sinon.spy();
        rerender(<LoginForm />);
        await fillAndSubmitLoginForm(getByTestId, user);

        expect(onSubmit).to.have.been.calledOnce;
      });

      it('should use updated onChange reference', async () => {
        // Initial render
        const { getByTestId, rerender } = render(<LoginForm />);

        // Update onChange reference, rerender, type a character
        onChange = sinon.spy();
        rerender(<LoginForm />);
        await user.click(getByTestId('user.name'));
        await user.keyboard('a');

        expect(onChange).to.have.been.calledOnce;
      });
    });

    it('should be updatable', async () => {
      function UpdatableForm() {
        const [projects, setProjects] = useState<Project[]>([
          { id: 1, name: 'P1' },
          { id: 2, name: 'P2' },
        ]);
        const [contracts, setContracts] = useState<Contract[]>([]);
        const { model, value, field, clear, read, update } = useForm(EntityModel);
        const contractField = useFormPart(model.contractId);

        useEffect(() => {
          if (value.projectId != null) {
            setContracts([
              { id: value.projectId * 100 + 1, name: `${value.projectId}-C1` },
              { id: value.projectId * 100 + 2, name: `${value.projectId}-C2` },
            ]);
          } else {
            setContracts([]);
          }
        }, [value.projectId]);

        useEffect(() => {
          if (value.contractId != null && !contracts.find((c) => c.id === value.contractId)) {
            contractField.setValue(undefined);
          }
          update();
        }, [contracts]);

        function loadForm() {
          read({ projectId: 2, contractId: 202 });
        }

        return (
          <>
            <select data-testid="projects" {...field(model.projectId)}>
              {projects.map(({ id, name }) => (
                <option key={id} value={id}>
                  {name}
                </option>
              ))}
            </select>
            <select data-testid="contracts" {...field(model.contractId)}>
              <option>-</option>
              {contracts.map(({ id, name }) => (
                <option key={id} value={id}>
                  {name}
                </option>
              ))}
            </select>
            <button data-testid="load" onClick={loadForm}>
              Load
            </button>
          </>
        );
      }

      const { findByTestId } = render(<UpdatableForm />);
      const loadFormBtn = await findByTestId('load');
      await user.click(loadFormBtn);
      await expect(findByTestId('contracts')).to.eventually.have.nested.property('selectedOptions.0.value', '202');
    });
  });
});
