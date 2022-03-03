#!/usr/bin/env bash

# Fail script on first error
set -e

#!/bin/sh
read -p "Enter the current build version (without snapshot): " currentVersion
read -p "Enter the version to release: " releaseVersion
read -p "Enter the next build version (without snapshot): " nextVersion
echo "Starting to release stub-http $releaseVersion, enter sonatype password if prompted." && \
git pull --rebase && \
lein clean && \
lein test && \
echo "Changing Build version to $releaseVersion" && \
sed -i "" "s/se\.haleby\/stub-http \"${currentVersion}-SNAPSHOT\"/se\.haleby\/stub-http \"${releaseVersion}\"/g" project.clj && \
echo "Pushing changes to git" && \
git ci -am "Preparing for release ${releaseVersion}" && \
git push && \
echo "Deploying ${releaseVersion} to clojars, enter clojars username as Username, and the stub-http deploy token as Password. Use sonatype password if GPG key password is prompted." && \
lein deploy clojars && \
echo "Will create and push git tags.." && \
git tag -a "${releaseVersion}" -m "Released ${releaseVersion}" && \
git push --tags && \
echo "Updating version to ${nextVersion}" && \
sed -i "" "s/se\.haleby\/stub-http \"${releaseVersion}\"/se\.haleby\/stub-http \"${nextVersion}-SNAPSHOT\"/g" project.clj && \
git ci -am "Setting build version to ${nextVersion}-SNAPSHOT" && \
git push && \
echo "Release of stub-http $releaseVersion completed successfully!"