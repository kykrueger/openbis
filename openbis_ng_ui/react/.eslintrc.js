module.exports = {
  root: true,
  parser: "babel-eslint",
  parserOptions: {
    ecmaVersion: 2017,
    sourceType: 'module',
    ecmaFeatures: {
      jsx: true
    }
  },
  env: {
    browser: true,
  },
  extends: [
    'eslint:recommended',
    'plugin:react/recommended'
  ],
  plugins: [
    'react'
  ],
  settings: {
    react: {
      createClass: "createReactClass",
      pragma: "React",
      version: "16.4.2"
    },
    propWrapperFunctions: [ "forbidExtraProps" ]
  },
  rules: {
    "react/jsx-uses-react": "error",
    "react/jsx-uses-vars": "error",

    "indent": ["error", 2],
    "linebreak-style": ["error", "unix"],
    "quotes": ["error", "single"],
    "semi": ["error", "never"],
    "eqeqeq": ["error", "always"],

    "react/prop-types": "off",

    // override default options for rules from base configurations
    "no-cond-assign": ["error", "always"],
  }
}
