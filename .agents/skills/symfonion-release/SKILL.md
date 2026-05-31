---
name: symfonion-release
description: Step-by-step guide for releasing SyMFONION to Sonatype Central. Use this skill whenever the user wants to cut a release, publish a new version, run mvn release:prepare/perform, or deploy SyMFONION to Maven Central. Trigger on phrases like "release symfonion", "cut a release", "publish a new version", "do the release", or any mention of mvn release in the context of this project.
---

# SyMFONION Release Skill

This skill walks through a full SyMFONION release: pre-flight → prepare → perform → verify, with rollback guidance if anything goes wrong. Run each step yourself using the Bash tool — minimise prompts to the user and only stop if a pre-flight check fails or an error occurs.

---

## Pre-flight Checklist

Run all checks before touching Maven. If any check fails, report the problem and stop — do not proceed to the release steps.

### 1. Source env.rc
Always source `env.rc` at the start of every Bash command that needs Java or Maven, since each shell invocation is fresh:
```bash
source env.rc && java -version 2>&1 | head -1
source env.rc && mvn -version 2>&1 | head -1
```
`java -version` must show JDK 21. If it doesn't, `env.rc` is broken — stop and report.

### 2. Branch
Must be on `main`:
```bash
git branch --show-current
```
If not on `main`, stop and tell the user to switch.

### 3. Clean working tree
```bash
git status --short
```
IDE-generated files (e.g. `.idea/`) that are tracked may have local drifts — restore them automatically with `git restore <file>` rather than asking the user. Only stop if there are meaningful uncommitted changes (source code, config files).

### 4. Up to date with remote
```bash
git fetch origin && git status
```
If behind, pull automatically: `git pull`.

### 5. No stale release state
```bash
ls release.properties 2>/dev/null && echo "STALE" || echo "clean"
ls target/checkout    2>/dev/null && echo "STALE" || echo "clean"
```
If `release.properties` exists, a previous release attempt was not cleaned up. See the **Rollback** section, clean up, then restart from pre-flight.

### 6. GPG and Central credentials in settings.xml
Verify both the GPG passphrase and the Central server token are present in `~/.m2/settings.xml` — no passphrase will be passed on the command line:
```bash
grep -c 'gpg.passphrase\|gpgPassphrase' ~/.m2/settings.xml
grep -c '<id>central</id>' ~/.m2/settings.xml
```
Both counts must be ≥ 1. If either is missing, stop and tell the user what to add:

- **GPG passphrase** — add to `~/.m2/settings.xml` under `<profiles>`:
  ```xml
  <profile>
    <id>gpg</id>
    <properties>
      <gpg.passphrase>YOUR_PASSPHRASE</gpg.passphrase>
    </properties>
  </profile>
  ```
  And activate it: `<activeProfiles><activeProfile>gpg</activeProfile></activeProfiles>`

- **Central token** — add to `~/.m2/settings.xml` under `<servers>`:
  ```xml
  <server>
    <id>central</id>
    <username>YOUR_TOKEN_USERNAME</username>
    <password>YOUR_TOKEN_PASSWORD</password>
  </server>
  ```
  Generate the token at [central.sonatype.com](https://central.sonatype.com) → Account → Generate User Token.

---

## Step 1 — `mvn release:prepare`

Sets the release version in `pom.xml`, commits and tags it, then bumps to the next SNAPSHOT and commits that too. Both commits are pushed to `origin/main`.

Maven will interactively prompt for release version, tag name, and next development version — accept the defaults.

```bash
source env.rc && mvn release:prepare
```

**Verify after:**
```bash
git log --oneline -3   # prepare release + prepare for next dev iteration
git tag | tail -5      # new tag should appear
```

If prepare fails partway through, see **Rollback** before retrying.

---

## Step 2 — `mvn release:perform`

Checks out the tagged commit into `target/checkout`, builds, signs with GPG, and publishes to Sonatype Central via `central-publishing-maven-plugin` (configured with `autoPublish=true`).

```bash
source env.rc && mvn release:perform
```

This takes a few minutes. Watch for `BUILD SUCCESS` and a line like:
```
Uploaded bundle successfully, deploymentId: <id>. Deployment will publish automatically
```

**Common failures and fixes:**

| Error | Cause | Fix |
|---|---|---|
| `402 Payment Required` from `oss.sonatype.org` | Old Sonatype URL still in pom | Check `distributionManagement` and that `central-publishing-maven-plugin` is present |
| `403 Forbidden` from snapshot repo | Missing `central` server entry in settings.xml | Add server entry — see pre-flight check 6 |
| `403 Forbidden` on release publish | Namespace not verified on Central Portal | Log in at central.sonatype.com and verify `com.github.dakusui` namespace |
| Duplicate artifacts error | `maven-source-plugin` using `jar` goal | Goal must be `jar-no-fork` in `pluginManagement` |
| GPG signing failure | Passphrase missing or wrong | Verify `gpg.passphrase` property in settings.xml |

**Important:** The `target/checkout` is a git checkout of the release tag. That tag must have been created from a commit that already contains `central-publishing-maven-plugin` in `pom.xml`. If the tag predates the plugin, perform will fail — rollback and redo prepare.

---

## Step 3 — Post-release Verification

```bash
git ls-remote --tags origin | tail -5   # new tag visible on remote
```

Report the release version and note that artifact propagation to Central can take 10–30 minutes. The user can verify at [central.sonatype.com/search](https://central.sonatype.com/search) by searching `com.github.dakusui:symfonion`.

---

## Rollback

If something went wrong after `release:prepare`:

```bash
# 1. Revert the pom changes and push the revert
source env.rc && mvn release:rollback

# 2. Delete the remote tag (rollback removes local tag only)
git push origin :refs/tags/symfonion-<version>

# 3. Clean up
rm -f release.properties && rm -rf target/checkout
```

After rollback, fix the root cause, then restart from **Pre-flight**.

---

## Quick Reference

```bash
# Pre-flight
source env.rc
git fetch origin && git status
grep -c 'gpg.passphrase\|gpgPassphrase' ~/.m2/settings.xml
grep -c '<id>central</id>' ~/.m2/settings.xml

# Release
source env.rc && mvn release:prepare
source env.rc && mvn release:perform

# Rollback
source env.rc && mvn release:rollback
git push origin :refs/tags/symfonion-<version>
rm -f release.properties && rm -rf target/checkout
```
