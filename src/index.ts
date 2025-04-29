import { registerPlugin } from '@capacitor/core';

import type { toolsPlugin } from './definitions';

const tools = registerPlugin<toolsPlugin>('tools', {
  web: () => import('./web').then((m) => new m.toolsWeb()),
});

export * from './definitions';
export { tools };
