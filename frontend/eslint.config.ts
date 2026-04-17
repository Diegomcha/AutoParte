import js from '@eslint/js';
import json from '@eslint/json';
import eslintConfigPrettier from 'eslint-config-prettier/flat';
import pluginReact from 'eslint-plugin-react';
import { defineConfig } from 'eslint/config';
import globals from 'globals';
import tseslint from 'typescript-eslint';

export default defineConfig([
	{
		// Aplica reglas base de JavaScript a archivos JS, TS y variantes ESM/CJS.
		files: ['**/*.{js,mjs,cjs,ts,mts,cts,jsx,tsx}'],
		plugins: { js },
		extends: ['js/recommended'],
		// Expone variables globales del navegador y de Node para evitar falsos positivos.
		languageOptions: { globals: { ...globals.browser, ...globals.node } },
	},
	{
		// Activa el lint tipado de TypeScript solo para archivos TypeScript.
		files: ['**/*.{ts,tsx,mts,cts}'],
		extends: [
			// Conjunto estricto con reglas que requieren información de tipos.
			tseslint.configs.strictTypeChecked,
			// Reglas estilísticas adicionales para TypeScript tipado.
			tseslint.configs.stylisticTypeChecked,
		],
		languageOptions: {
			parserOptions: {
				// Hace que ESLint use el proyecto TypeScript para obtener tipos.
				projectService: true,
			},
		},
	},
	{
		// Añade reglas específicas de React solo en archivos con JSX/TSX.
		files: ['**/*.{js,mjs,cjs,jsx,ts,mts,cts,tsx}'],
		...pluginReact.configs.flat.recommended,
		rules: {
			// React 17+ con el runtime automático no necesita import React en cada archivo.
			...pluginReact.configs.flat.recommended.rules,
			// Desactiva la regla legacy de JSX runtime clásico.
			'react/react-in-jsx-scope': 'off',
			// Desactiva la regla legacy que esperaba usar React en el scope para JSX.
			'react/jsx-uses-react': 'off',
		},
		settings: {
			react: {
				// Fija la versión de React usada por el plugin.
				version: '19.0',
			},
		},
	},
	{
		// Reglas específicas para archivos JSON.
		files: ['**/*.json'],
		plugins: { json },
		// Usa el parser JSON de ESLint para validar package.json, tsconfig, etc.
		language: 'json/json',
		extends: ['json/recommended'],
	},
	// Desactiva reglas de ESLint que chocan con Prettier.
	eslintConfigPrettier,
]);
