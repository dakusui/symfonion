---
name: sync-branch
description: Keep a feature branch up to date with the main branch using rebase. Use this skill whenever the user wants to sync, update, or rebase their feature branch onto main (or master), after another PR has merged, when they are getting behind on main, or when they want to avoid merge conflicts before opening or updating a PR. Trigger on phrases like "sync my branch", "rebase onto main", "pull main into my branch", "update my branch", "I'm behind main", or "pull after the PR merged".
---

# Sync Branch

Keeps the current feature branch up to date with `main` by rebasing on top of it.
Run each step with the Bash tool. Only stop if a step fails.

---

## When to use this

Any time another PR has merged into `main` while you are working on a feature branch, run this procedure before continuing. Doing it promptly — as soon as you notice — keeps the rebase small and conflict-free. Skipping it causes conflicts to pile up, making the eventual rebase much harder.

---

## Steps

### 1. Identify the base branch

```bash
git remote show origin | grep "HEAD branch"
```

This is usually `main` or `master`. Use whatever it reports as BASE below.

### 2. Save the current branch name

```bash
git branch --show-current
```

### 3. Pull the base branch

```bash
git checkout <BASE>
git pull
```

### 4. Rebase the feature branch

```bash
git checkout <feature-branch>
git rebase <BASE>
```

Rebase replays the feature branch's commits on top of the latest `<BASE>`, keeping the history linear. A `git merge` would also work but adds a merge commit every time, which clutters the history and makes future rebases harder.

### 5. Push the updated branch

If the branch already has a remote tracking branch, the rebase rewrites history so a normal push will be rejected. Force-push with the safety flag:

```bash
git push --force-with-lease
```

`--force-with-lease` is safer than `--force`: it aborts if someone else pushed to the branch since your last fetch, preventing accidental overwrites.

---

## Handling rebase conflicts

If `git rebase <BASE>` stops with conflicts:

1. Open the conflicting files and resolve them (or use `git mergetool`).
2. Stage the resolved files:
   ```bash
   git add <file>
   ```
3. Continue the rebase:
   ```bash
   git rebase --continue
   ```
4. Repeat until the rebase completes.

To abort and return to the pre-rebase state at any point:

```bash
git rebase --abort
```

---

## Quick reference

```bash
BASE=main   # or master — confirm with step 1

git checkout $BASE && git pull
git checkout <feature-branch>
git rebase $BASE
git push --force-with-lease
```
