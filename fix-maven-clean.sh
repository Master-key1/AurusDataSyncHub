
#!/bin/bash

# Enable local variable scope
# (Equivalent to Windows SETLOCAL)

echo "=========================================="
echo "Maven Build Script"
echo "=========================================="
echo "Current Directory: $(pwd)"
echo

# Check whether the target directory exists
if [ -d "target" ]; then
    echo "Attempting to delete target folder..."

    # Remove read-only restrictions where possible
    chmod -R u+w target >/dev/null 2>&1

    # Attempt to delete the target directory
    rm -rf target >/dev/null 2>&1
fi

# Check whether the target directory still exists
if [ -d "target" ]; then
    echo
    echo "WARNING: Target folder is locked."
    echo "Running Maven INSTALL only..."
    echo

    mvn install -DskipTests
else
    echo
    echo "Target folder deleted successfully."
    echo "Running Maven CLEAN INSTALL..."
    echo

    mvn clean install -DskipTests
fi

# Return Maven's exit status
exit $?
