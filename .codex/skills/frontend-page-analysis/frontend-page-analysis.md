---
name: frontend-page-analysis
description: Locate the files, routes, styles, and API calls related to a frontend page before making changes.
---

# Goal
Before editing any page, first identify the full impact area.

# Steps
1. Find the page entry component.
2. Find related route definitions.
3. Find shared components used by this page.
4. Find API requests triggered by this page.
5. Find local styles and global styles that affect this page.
6. Output a short change map before editing.

# Output format
- Main page file
- Related route file
- Related components
- Related API files
- Related style files
- Risk notes

# Rules
- Do not edit code in this step unless explicitly asked.
- Prefer precise file paths.
- If uncertain, say what is uncertain.