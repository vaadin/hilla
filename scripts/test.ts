import { startTestRunner } from '@web/test-runner';
import config from '../web-test-runner.config.js';

await startTestRunner({ config, readFileConfig: false });
