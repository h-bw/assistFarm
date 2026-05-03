---
name: build-check
description: Verify that frontend changes compile and do not obviously break routes, imports, or page rendering.
---

# Goal
Validate that changes are safe enough to continue.

# Steps
1. Check for import errors and unused variables.
2. Run the recommended build or lint command if available.
3. Check route references for broken links.
4. Check whether component props/events still match usage.
5. Summarize any remaining risks.

# Output format
- Commands run
- Result
- Errors found
- Risks remaining