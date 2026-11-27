import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import { afterEach, beforeEach, chai, describe, expect, it } from 'vitest';
import { FluxConnection, page } from '../src/FluxConnection.js';

chai.use(sinonChai);

// Kept apart from FluxConnection.test.ts, where every test creates a
// connection first: each test file loads the module afresh, so the page here
// starts without any FluxConnection.
describe('@vaadin/hilla-frontend', () => {
  describe('FluxConnection page reload', () => {
    let reloadStub: sinon.SinonStub;

    beforeEach(() => {
      reloadStub = sinon.stub(page, 'reload');
    });

    afterEach(() => {
      reloadStub.restore();
    });

    it('should reload a restored page only after a connection is created', () => {
      window.dispatchEvent(new PageTransitionEvent('pageshow', { persisted: true }));
      expect(reloadStub).not.to.have.been.called;

      // eslint-disable-next-line no-new -- only creating the connection matters here
      new FluxConnection('/connect');
      window.dispatchEvent(new PageTransitionEvent('pageshow', { persisted: true }));
      expect(reloadStub).to.have.been.calledOnce;
    });
  });
});
