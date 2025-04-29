import { WebPlugin } from '@capacitor/core';

import type { toolsPlugin } from './definitions';

export class toolsWeb extends WebPlugin implements toolsPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
