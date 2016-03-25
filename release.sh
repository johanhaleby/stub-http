#!/usr/bin/env bash

# Fail script on first error
set -e

read -p "Enter the version to release: " releaseVersion
echo "Releasing stub-http version $releaseVersion"
RELEASE_VERSION=0.1.1 lein release
echo "Release $releaseVersion completed successfully"