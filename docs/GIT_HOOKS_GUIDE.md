# Git Hooks Guide: Auto-Format with Spotless

## Table of Contents
- [What are Git Hooks?](#what-are-git-hooks)
- [How Git Hooks Work](#how-git-hooks-work)
- [Pre-commit Hook for Spotless](#pre-commit-hook-for-spotless)
- [Setup Instructions](#setup-instructions)
- [Testing the Hook](#testing-the-hook)
- [How to Remove/Disable the Hook](#how-to-removedisable-the-hook)
- [Troubleshooting](#troubleshooting)
- [Advanced Usage](#advanced-usage)

---

## What are Git Hooks?

**Git hooks** are scripts that Git automatically executes before or after specific events such as:
- `pre-commit`: Before a commit is created
- `pre-push`: Before code is pushed to remote
- `post-merge`: After a merge is completed
- `commit-msg`: After commit message is written

Think of them as **automated quality gates** that run checks or modifications on your code automatically.

### Key Characteristics:
- **Local only**: Hooks are stored in `.git/hooks/` and are NOT tracked by Git
- **Per-repository**: Each developer needs to set them up individually
- **Customizable**: Written as shell scripts (or any executable)
- **Bypassable**: Can be skipped with `--no-verify` flag if needed

---

## How Git Hooks Work

### Lifecycle Example (Pre-commit Hook):
```
You run: git commit -m "Add new feature"
           ↓
Git checks: Does .git/hooks/pre-commit exist and is it executable?
           ↓
    YES → Runs the script
           ↓
    Script exits with code 0 (success) → Commit proceeds
    Script exits with non-0 (failure)  → Commit is aborted
           ↓
    NO → Commit proceeds normally
```

### File Location:
- **Hooks directory**: `.git/hooks/`
- **Active hooks**: Executable files without `.sample` extension
- **Example**: `.git/hooks/pre-commit` (active) vs `.git/hooks/pre-commit.sample` (inactive)

---

## Pre-commit Hook for Spotless

Our pre-commit hook automatically formats Java code using **Spotless** before each commit.

### What It Does:
1. Detects when you run `git commit`
2. Automatically runs `./gradlew spotlessApply`
3. Stages the formatted files
4. Proceeds with the commit

### Benefits:
- ✅ Never forget to format code
- ✅ Prevents CI failures due to formatting
- ✅ Maintains consistent code style across the team
- ✅ No manual intervention needed

### The Hook Script:
```bash
#!/bin/sh
# Pre-commit hook to auto-format Java code with Spotless

echo "🔍 Running Spotless auto-formatter..."

# Run Spotless formatter
./gradlew spotlessApply --daemon --quiet

# Check if Spotless made any changes
if ! git diff --quiet; then
    echo "✨ Code formatted successfully"
    # Stage the formatted files
    git add -u
    echo "📝 Formatted files added to commit"
else
    echo "✅ Code already properly formatted"
fi

exit 0
```

---

## Setup Instructions

### Option A: Automated Setup (Recommended)

We've created a setup script for easy installation.

1. **Run the setup script**:
   ```bash
   ./scripts/setup-git-hooks.sh
   ```

2. **Verify installation**:
   ```bash
   ls -l .git/hooks/pre-commit
   ```
   You should see: `-rwxr-xr-x` (executable permissions)

### Option B: Manual Setup

1. **Create the hook file**:
   ```bash
   # Windows (Git Bash)
   cat > .git/hooks/pre-commit << 'EOF'
   #!/bin/sh
   echo "🔍 Running Spotless auto-formatter..."
   ./gradlew spotlessApply --daemon --quiet
   if ! git diff --quiet; then
       echo "✨ Code formatted successfully"
       git add -u
       echo "📝 Formatted files added to commit"
   else
       echo "✅ Code already properly formatted"
   fi
   exit 0
   EOF
   ```

2. **Make it executable**:
   ```bash
   chmod +x .git/hooks/pre-commit
   ```

3. **Verify**:
   ```bash
   cat .git/hooks/pre-commit
   ```

---

## Testing the Hook

### Test 1: Verify Hook is Active
```bash
# Check if pre-commit exists and is executable
test -x .git/hooks/pre-commit && echo "✅ Hook is active" || echo "❌ Hook not found"
```

### Test 2: Test with Unformatted Code

1. **Intentionally mess up formatting** in a Java file:
   ```java
   // Example: Add extra spaces, wrong indentation
   public class Test{
       public void method(  ){
           System.out.println( "test" );
       }
   }
   ```

2. **Try to commit**:
   ```bash
   git add src/main/java/Test.java
   git commit -m "Test commit"
   ```

3. **Expected output**:
   ```
   🔍 Running Spotless auto-formatter...
   ✨ Code formatted successfully
   📝 Formatted files added to commit
   [your-branch abc1234] Test commit
   ```

4. **Verify formatting**:
   ```bash
   cat src/main/java/Test.java
   ```
   Should now be properly formatted.

### Test 3: Test with Already Formatted Code

1. **Commit already formatted code**:
   ```bash
   git add .
   git commit -m "Another test"
   ```

2. **Expected output**:
   ```
   🔍 Running Spotless auto-formatter...
   ✅ Code already properly formatted
   ```

---

## How to Remove/Disable the Hook

### Temporary Disable (One-time bypass):
```bash
# Skip the hook for a single commit
git commit -m "Emergency fix" --no-verify
```

### Permanent Removal:
```bash
# Option 1: Delete the hook
rm .git/hooks/pre-commit

# Option 2: Rename it (to disable but keep for later)
mv .git/hooks/pre-commit .git/hooks/pre-commit.disabled

# Option 3: Remove execute permissions
chmod -x .git/hooks/pre-commit
```

### Re-enable After Disabling:
```bash
# If renamed
mv .git/hooks/pre-commit.disabled .git/hooks/pre-commit

# If permissions removed
chmod +x .git/hooks/pre-commit
```

---

## Troubleshooting

### Issue: Hook doesn't run

**Symptoms**: No "Running Spotless" message when committing

**Solutions**:
1. Check if file exists:
   ```bash
   ls -la .git/hooks/pre-commit
   ```

2. Verify executable permissions:
   ```bash
   # Should show: -rwxr-xr-x
   chmod +x .git/hooks/pre-commit
   ```

3. Check shebang line:
   ```bash
   head -n 1 .git/hooks/pre-commit
   # Should output: #!/bin/sh
   ```

### Issue: "gradlew: command not found"

**Cause**: Running from wrong directory

**Solution**: Hook must run from project root
```bash
# Fix: Add directory check to hook
#!/bin/sh
cd "$(git rev-parse --show-toplevel)" || exit 1
./gradlew spotlessApply --daemon --quiet
# ... rest of hook
```

### Issue: Hook runs but doesn't format

**Cause**: Spotless configuration issue

**Solution**:
```bash
# Test Spotless manually
./gradlew spotlessApply

# Check Spotless configuration
./gradlew spotlessCheck
```

### Issue: Very slow commits

**Cause**: Gradle startup overhead

**Solutions**:
1. Use Gradle daemon (already included via `--daemon` flag)
2. Only format changed files (see Advanced Usage)
3. Use pre-push hook instead (formats less frequently)

---

## Advanced Usage

### Format Only Changed Files (Faster)

Modify the hook to only format staged files:

```bash
#!/bin/sh
echo "🔍 Running Spotless on staged files..."

# Get list of staged Java files
STAGED_FILES=$(git diff --cached --name-only --diff-filter=ACM | grep '\.java$')

if [ -z "$STAGED_FILES" ]; then
    echo "✅ No Java files to format"
    exit 0
fi

# Run Spotless on specific files
./gradlew spotlessApply --daemon --quiet

# Re-stage formatted files
echo "$STAGED_FILES" | xargs git add

echo "✨ Formatted ${STAGED_FILES} files"
exit 0
```

### Add Additional Checks

Combine Spotless with other checks:

```bash
#!/bin/sh
echo "🔍 Running pre-commit checks..."

# 1. Format code
echo "📝 Formatting code..."
./gradlew spotlessApply --daemon --quiet
git add -u

# 2. Run tests
echo "🧪 Running tests..."
./gradlew test --daemon --quiet
if [ $? -ne 0 ]; then
    echo "❌ Tests failed. Commit aborted."
    exit 1
fi

# 3. Check for TODOs (optional warning)
TODO_COUNT=$(git diff --cached | grep -c "TODO")
if [ $TODO_COUNT -gt 0 ]; then
    echo "⚠️  Warning: Found $TODO_COUNT TODO comments in staged files"
fi

echo "✅ All checks passed"
exit 0
```

## Additional Resources

- [Git Hooks Documentation](https://git-scm.com/book/en/v2/Customizing-Git-Git-Hooks)
- [Spotless Gradle Plugin](https://github.com/diffplug/spotless/tree/main/plugin-gradle)
- [Pre-commit Framework](https://pre-commit.com/) (alternative solution)

---

## Quick Reference

| Command | Description |
|---------|-------------|
| `cat .git/hooks/pre-commit` | View hook contents |
| `chmod +x .git/hooks/pre-commit` | Make hook executable |
| `rm .git/hooks/pre-commit` | Remove hook |
| `git commit --no-verify` | Skip hook once |
| `./gradlew spotlessCheck` | Check formatting manually |
| `./gradlew spotlessApply` | Format manually |
