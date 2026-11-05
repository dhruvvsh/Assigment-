/** @type {import('jest').Config} */
module.exports = {
  testEnvironment: 'jsdom',
  setupFilesAfterEnv: ['<rootDir>/src/setupTests.ts'],
  testMatch: ['<rootDir>/src/**/*.test.{ts,tsx}'],
  transform: {
    '^.+\\.tsx?$': ['ts-jest', { tsconfig: '<rootDir>/tsconfig.jest.json' }],
  },
  moduleNameMapper: {
    // CSS Modules: class names come back as their own keys, which is enough for tests.
    '\\.module\\.css$': 'identity-obj-proxy',
    // Jest resolves axios to its browser ESM build, which it cannot parse; use the CommonJS build.
    '^axios$': 'axios/dist/node/axios.cjs',
  },
}
