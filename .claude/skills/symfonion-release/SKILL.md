---
name: symfonion-release
description: Step-by-step guide for releasing SyMFONION to Sonatype Central. Use this skill whenever the user wants to cut a release, publish a new version, run mvn release:prepare/perform, or deploy SyMFONION to Maven Central. Trigger on phrases like "release symfonion", "cut a release", "publish a new version", "do the release", or any mention of mvn release in the context of this project.
---

# SyMFONION Release Skill

This skill walks through a full SyMFONION release: pre-flight → prepare → perform → verify, with rollback guidance if anything goes wrong.

---

## Pre-flight Checklist

Run through all of these before touching Maven.

### 1. Environment
Verify `env.rc` has been sourced:
```bash
java -version   # must show JDK 21
mvn -version    # must show the SDKMAN-managed Maven
```
If either fails, ask the user to run `. env.rc` from the project root first.

### 2. Branch
Must be on `main`:
```bash
git branch --show-current
```
If not on `main`, stop and ask the user to switch before continuing.

### 3. Clean working tree
No uncommitted changes or untracked files that should be committed:
```bash
git status
```
Stash or commit anything pending before proceeding.

### 4. Up to date with remote
```bash
git fetch origin
git status   # should say "Your branch is up to date with 'origin/main'"
```
Pull if behind.

### 5. No stale release state
```bash
ls release.properties 2>/dev/null && echo "STALE — see Rollback section" || echo "Clean"
ls target/checkout    2>/dev/null && echo "STALE checkout present" || echo "Clean"
```
If `release.properties` exists, a previous release attempt was not cleaned up. See the **Rollback** section before proceeding.

### 6. GPG passphrase
Remind the user: **have your GPG passphrase ready** — it will be passed to Maven as `-Dgpg.passphrase=<passphrase>` in both the prepare and perform steps.

---

## Step 1 — `mvn release:prepare`

This step sets the release version in `pom.xml`, commits it, tags it, then bumps to the next development SNAPSHOT and commits that too. Both commits are pushed to `origin/main`.

```bash
mvn release:prepare -Dgpg.passphrase=<passphrase>
```

Maven will interactively ask for:
- **Release version** (e.g. `3.0.3`) — accept the default unless you need a different version
- **SCM release tag** (e.g. `symfonion-3.0.3`) — accept the default
- **Next development version** (e.g. `3.0.4-SNAPSHOT`) — accept the default

**What to check after:**
```bash
git log --oneline -3   # should show: prepare release + prepare for next dev iteration
git tag | grep 3.0     # new tag should appear
```

If prepare fails partway through, see **Rollback** before retrying.

---

## Step 2 — `mvn release:perform`

This checks out the tagged commit into `target/checkout`, builds it, signs the artifacts with GPG, and publishes to Sonatype Central via `central-publishing-maven-plugin`.

```bash
mvn release:perform -Dgpg.passphrase=<passphrase>
```

This takes a few minutes. The plugin is configured with `autoPublish=true`, so the artifacts go straight through without manual promotion.

**Common failures and fixes:**

| Error | Cause | Fix |
|---|---|---|
| `402 Payment Required` from `oss.sonatype.org` | Old Sonatype URL still in pom | The pom should now use `central-publishing-maven-plugin` — check `distributionManagement` and the plugin section |
| `403 Forbidden` from `central.sonatype.com/repository/maven-snapshots/` | Missing `central` server credentials | Add token to `~/.m2/settings.xml` — see below |
| `403 Forbidden` on release publish | Namespace not verified on Central Portal | Log in at central.sonatype.com and verify `com.github.dakusui` namespace |
| Duplicate artifacts error | `maven-source-plugin` `jar` goal still present | Goal must be `jar-no-fork` in `pluginManagement` |

**`~/.m2/settings.xml` — required `central` server entry:**
```xml
<server>
  <id>central</id>
  <username>YOUR_TOKEN_USERNAME</username>
  <password>YOUR_TOKEN_PASSWORD</password>
</server>
```
Generate the token at [central.sonatype.com](https://central.sonatype.com) → Account → Generate User Token.

**Important:** The `target/checkout` is a git checkout of the release tag. That tag must have been created (in the prepare step) from a commit that already contains `central-publishing-maven-plugin` in `pom.xml`. If the tag predates the plugin being added, perform will fail. See Rollback.

---

## Step 3 — Post-release Verification

### Tag on GitHub
```bash
git tag | grep <version>
git ls-remote --tags origin | grep <version>
```

### Artifact on Central
Search for `com.github.dakusui:symfonion:<version>` at [central.sonatype.com/search](https://central.sonatype.com/search). Note: propagation to Central can take 10–30 minutes.

---

## Rollback

If something went wrong after `release:prepare` (but before or during `release:perform`):

### 1. Rollback the pom changes
```bash
mvn release:rollback
```
This reverts the two prepare commits (release version + next dev version) by creating a new revert commit and pushing it.

### 2. Delete the remote tag
`release:rollback` removes the local tag but NOT the remote one:
```bash
git push origin :refs/tags/symfonion-<version>
```

### 3. Clean up
```bash
rm -f release.properties
rm -rf target/checkout
```

After rollback, fix the root cause, then restart from **Pre-flight**.

---

## Quick Reference

```bash
# Full happy path
. env.rc
git checkout main && git pull
mvn release:prepare -Dgpg.passphrase=<passphrase>
mvn release:perform -Dgpg.passphrase=<passphrase>

# Rollback
mvn release:rollback
git push origin :refs/tags/symfonion-<version>
rm -f release.properties && rm -rf target/checkout
```
