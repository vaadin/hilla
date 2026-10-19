import type { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { LoginForm } from '@vaadin/react-components/LoginForm.js';
import { useState } from 'react';
import { useAuth } from 'Frontend/util/auth.js';

export const config: ViewConfig = {
  menu: { exclude: true },
};

export default function LoginView(): React.JSX.Element {
  const { login } = useAuth();
  const [error, setError] = useState(false);

  return (
    <div className="flex items-center justify-center h-full">
      <LoginForm
        error={error}
        noForgotPassword
        onLogin={async ({ detail: { username, password } }) => {
          const result = await login(username, password);
          if (result.error) {
            setError(true);
          } else {
            const url = result.redirectUrl ?? result.defaultUrl ?? '/';
            document.location = new URL(url, document.baseURI).pathname;
          }
        }}
      />
    </div>
  );
}
