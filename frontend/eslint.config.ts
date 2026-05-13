import js from '@eslint/js';
import json from '@eslint/json';
import eslintConfigPrettier from 'eslint-config-prettier/flat';
import pluginReact from 'eslint-plugin-react';
import { defineConfig } from 'eslint/config';
import globals from 'globals';
import tseslint from 'typescript-eslint';

export default defineConfig([
	{
		files: [
			'app/**/*.{js,mjs,cjs,ts,mts,cts,jsx,tsx}',
			'*.config.ts',
			'*.setup.ts',
		],
		plugins: { js },
		extends: ['js/recommended'],
		languageOptions: { globals: { ...globals.browser, ...globals.node } },
	},
	{
		files: ['app/**/*.{ts,tsx,mts,cts}', '*.config.ts', '*.setup.ts'],
		extends: [
			tseslint.configs.strictTypeChecked,
			tseslint.configs.stylisticTypeChecked,
		],
		languageOptions: {
			parserOptions: {
				projectService: true,
			},
		},
	},
	{
		files: [
			'app/**/*.{js,mjs,cjs,jsx,ts,mts,cts,tsx}',
			'*.config.ts',
			'*.setup.ts',
		],
		...pluginReact.configs.flat.recommended,
		rules: {
			...pluginReact.configs.flat.recommended.rules,
			'react/react-in-jsx-scope': 'off',
			'react/jsx-uses-react': 'off',
		},
		settings: {
			react: {
				version: '19.0',
			},
		},
	},
	{
		files: ['app/**/*.json', 'package.json', 'tsconfig.json'],
		plugins: { json },
		language: 'json/json',
		extends: ['json/recommended'],
	},
	eslintConfigPrettier,
]);
