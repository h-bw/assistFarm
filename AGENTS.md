# Project
This is a graduation project frontend repository.

# Stack
- Vue 3
- Vue Router
- Axios
- Element Plus

# Commands
- install: npm install
- dev: npm run dev
- build: npm run build
- lint: npm run lint

# PowerShell（固定风格：UTF-8 编码 + 简单语法）
- **启用 UTF-8**：`scripts/powershell-utf8.ps1`
- **统一入口**：`scripts/run.ps1`
  - **查看端口状态**：`powershell -NoProfile -ExecutionPolicy Bypass -File scripts\\run.ps1 -Action status`
  - **启动后端**：`powershell -NoProfile -ExecutionPolicy Bypass -File scripts\\run.ps1 -Action backend`
  - **启动用户端**：`powershell -NoProfile -ExecutionPolicy Bypass -File scripts\\run.ps1 -Action user-ui`
  - **启动管理端**：`powershell -NoProfile -ExecutionPolicy Bypass -File scripts\\run.ps1 -Action admin-ui`
  - **构建前端（两个端）**：`powershell -NoProfile -ExecutionPolicy Bypass -File scripts\\run.ps1 -Action build`
  - **跑后端测试**：`powershell -NoProfile -ExecutionPolicy Bypass -File scripts\\run.ps1 -Action test`

# Editing rules
- Prefer reusing existing components.
- Do not change API field names unless required.
- Do not remove business logic for login, routing, or data submission.
- Prefer scoped styles for page-level changes.
- Keep naming clear and consistent.

# Workflow
1. First locate all related files before editing.
2. Then propose a short plan.
3. Then implement.
4. Then run basic checks.
5. Summarize changed files and risks.

# Done criteria
- Page renders normally
- No obvious import or route errors
- Existing API interactions still work