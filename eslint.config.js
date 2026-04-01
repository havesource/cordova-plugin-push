const { defineConfig, globalIgnores } = require('eslint/config');
const nodeConfig = require('@cordova/eslint-config/node');
const nodeTestConfig = require('@cordova/eslint-config/node-tests');
const browserConfig = require('@cordova/eslint-config/browser');

module.exports = defineConfig([
  globalIgnores([
    'www/push.js',
    'www/push.js'
  ]),
  ...browserConfig.map(config => ({
    ...config,
    languageOptions: {
      ...(config?.languageOptions || {}),
      globals: {
        ...(config.languageOptions?.globals || {}),
        cordova: false,
        module: false
      }
    },
    rules: {
      ...(config.rules || {}),
      indent: ['error', 2],
      'no-var': 0
    }
  })),
  ...nodeTestConfig.map(config => ({
    files: ['spec/**/*.js', 'example/server/**/*.js'],
    ...config,
    rules: {
      ...(config.rules || {}),
      indent: ['error', 2]
    }
  })),
  ...nodeConfig.map(config => ({
    files: ['eslint.config.js', 'hooks/**'],
    ...config,
    rules: {
      ...(config.rules || {}),
      indent: ['error', 2]
    }
  }))
]);
