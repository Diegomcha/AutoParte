import js from '@eslint/js';
import json from '@eslint/json';
import pluginQuery from '@tanstack/eslint-plugin-query';
import eslintConfigPrettier from 'eslint-config-prettier/flat';
import pluginReact from 'eslint-plugin-react';
import { defineConfig } from 'eslint/config';
import globals from 'globals';
import tseslint from 'typescript-eslint';

export default defineConfig([
	// Global ignores
	{
		ignores: [
			'node_modules',
			'build',
			'coverage',
			'.react-router',
			'eslint-report.json',
			'app/@types',
		],
	},
	// 1. Base configuration for all JavaScript and TypeScript files
	{
		files: ['**/*.{js,mjs,cjs,ts,mts,cts,jsx,tsx}'],
		...js.configs.recommended,
		languageOptions: { globals: { ...globals.browser, ...globals.node } },
	},
	// 2. TypeScript-specific configuration
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
		rules: {
			'@typescript-eslint/no-unused-vars': [
				'error',
				{
					argsIgnorePattern: '^_',
					varsIgnorePattern: '^_',
				},
			],
			'@typescript-eslint/only-throw-error': 'warn',
		},
	},
	// 3. React-specific configuration
	{
		files: ['**/*.{js,mjs,cjs,jsx,ts,mts,cts,tsx}'],
		...pluginReact.configs.flat.recommended,
		rules: {
			...pluginReact.configs.flat.recommended?.rules,
			'react/react-in-jsx-scope': 'off',
			'react/jsx-uses-react': 'off',
		},
		settings: {
			react: {
				version: '19.0',
			},
		},
	},
	// 4. TanStack Query-specific configuration
	...pluginQuery.configs['flat/recommended-strict'],
	// 5. JSON-specific configuration
	{
		files: ['**/*.json'],
		plugins: { json },
		language: 'json/json',
		extends: ['json/recommended'],
	},
	// 6. Prettier configuration to disable conflicting rules (MUST BE LAST ALWAYS!)
	eslintConfigPrettier,
]);
