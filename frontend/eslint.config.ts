import js from '@eslint/js';
import json from '@eslint/json';
import eslintConfigPrettier from 'eslint-config-prettier/flat';
import pluginReact from 'eslint-plugin-react';
import { defineConfig } from 'eslint/config';
import globals from 'globals';
import tseslint from 'typescript-eslint';

export default defineConfig([
	{
		ignores: ['node_modules', 'build', 'coverage', '.react-router'],
	},
	{
		files: ['**/*.{js,mjs,cjs,ts,mts,cts,jsx,tsx}'],
		plugins: { js },
		extends: ['js/recommended'],
		languageOptions: { globals: { ...globals.browser, ...globals.node } },
	},
	{
		files: ['**/*.{ts,tsx,mts,cts}'],
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
		files: ['**/*.{js,mjs,cjs,jsx,ts,mts,cts,tsx}'],
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
		files: ['**/*.json'],
		plugins: { json },
		language: 'json/json',
		extends: ['json/recommended'],
	},
	eslintConfigPrettier,
]);
