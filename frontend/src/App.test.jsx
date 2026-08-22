import { describe, expect, it } from 'vitest';

describe('portal smoke test', () => {
  it('keeps the test runner wired', () => {
    expect('AI Powered Crop Disease Detection and Farmer Support Portal').toContain('Crop Disease');
  });
});
