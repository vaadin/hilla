import type SharedStorage from './SharedStorage';

export default abstract class Plugin {
  abstract execute(storage: SharedStorage): void;
}
