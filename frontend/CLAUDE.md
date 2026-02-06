# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

```bash
# Install dependencies
npm install

# Dev server (port 3000)
npm run dev

# Production build & start
npm run build
npm start

# Lint (ESLint 9 flat config)
npm run lint

# Format (Prettier)
npm run format
npm run format:check
```

## Architecture

### Next.js App Router

- **Next.js 16.1.6** with App Router — all routes live in `app/`
- Root layout (`app/layout.tsx`) loads Geist Sans/Mono fonts via `next/font/google`
- Root page (`app/page.tsx`) renders the top-level `<Home />` component
- Currently single-page; no nested routes, route groups, API routes, or middleware

### Component Organization

```
Components/
  {Feature}/          # Feature-specific components
    {Component}.tsx
  Common/             # Shared/reusable components
    {ComponentName}/
      {ComponentName}.tsx
```

- `Components/Home/` — Home feature (`Home.tsx`, `Navbar/`, `Body/`, `Footer/`)
- `Components/Common/` — Shared components (`Button/`, `Modals/`)
- Server Components by default; only add `'use client'` when React hooks or browser APIs are needed (e.g., `Body.tsx` uses `useState` for modal state)

### Type Definitions

- Shared types live in `lib/types/` (e.g., `lib/types/button.ts` exports `ButtonProps`)
- Import types with `@/lib/types/...`

### Import Conventions

- Path alias: `@/*` maps to project root (`./`)
- Always use absolute imports: `@/Components/...`, `@/lib/types/...`

## Styling

- **Tailwind CSS v4** — configured via `@import "tailwindcss"` in `app/globals.css` (no `tailwind.config.js`)
- Theme customization uses `@theme inline` directive and CSS custom properties in `globals.css`
- Dark mode via `prefers-color-scheme` media query
- PostCSS plugin: `@tailwindcss/postcss`

## Code Style

- TypeScript strict mode enabled
- Prettier: single quotes, semicolons, trailing commas (es5), 2-space indent, 80 char width
- ESLint: `eslint-config-next/core-web-vitals` + `eslint-config-next/typescript` (flat config format)
