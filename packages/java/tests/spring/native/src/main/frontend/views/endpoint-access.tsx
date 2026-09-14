import type { ViewConfig } from '@vaadin/hilla-file-router/types.js';
import { Button } from '@vaadin/react-components';
import { useState } from 'react';
import { AccessService } from 'Frontend/generated/endpoints.js';

export const config: ViewConfig = {
  title: 'Endpoint access',
};

export default function EndpointAccessView(): React.JSX.Element {
  const [result, setResult] = useState('');

  // The message is fixed instead of read from the error, because the class
  // names the client throws are mangled in a production bundle.
  async function call(callEndpoint: () => Promise<string | undefined>) {
    try {
      setResult((await callEndpoint()) ?? '');
    } catch {
      setResult('denied');
    }
  }

  return (
    <section className="flex p-m gap-m items-end">
      <Button id="anonymous" onClick={async () => await call(AccessService.forAnyone)}>
        Anyone
      </Button>
      <Button id="authenticated" onClick={async () => await call(AccessService.forAnyUser)}>
        Any user
      </Button>
      <Button id="admin" onClick={async () => await call(AccessService.forAdmin)}>
        Admin
      </Button>
      <span id="result">{result}</span>
    </section>
  );
}
