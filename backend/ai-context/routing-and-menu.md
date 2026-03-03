# Routing and menu generation

## Backend menu contract
Menu node fields used by FE:
- `path`: parent usually starts with `/`, child often relative (e.g. `user`)
- `component`: `null` for directories; string like `system/user/index` for leaf pages
- `menuName`, `icon`, `visible` -> mapped to `meta`

## Frontend conversion rules
- `component=null` -> `ParentView` (RouterView wrapper)
- `component='Layout'` -> uses `@/layout/index.vue`
- leaf `component` -> dynamic import via `import.meta.glob('../../views/**/*.vue')`
- `meta.hidden = (visible === 0)`

## Injection rule
- Dynamic routes are injected under `name: "Layout"` route.
- Normalize routes to avoid nested layout wrapper.
