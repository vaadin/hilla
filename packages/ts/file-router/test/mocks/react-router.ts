import sinon from 'sinon';

export const browserRouter = { 'browser-router': true };

export const createBrowserRouter = sinon.stub().returns(browserRouter);
