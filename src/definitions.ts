export interface toolsPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
