#!/bin/bash
# This script initiates the Gradle publishing task when pushes to master occur.

if [ "$TRAVIS_REPO_SLUG" == "sleonidy/griffon-hibernate5-plugin" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "master" ]; then
  if [[ $(./gradlew -q getVersion) == *SNAPSHOT* ]]; then
      echo 'Travis can only publish release versions.'
      return 0
  fi

  echo -e "Starting publish to Github pages...\n"

  ./gradlew publishGhPages -PgithubUsername=$githubUsername -PgithubPassword=$githubPassword
  RETVAL=$?

  if [ $RETVAL -eq 0 ]; then
    echo 'Completed publish!'
  else
    echo 'Publish failed.'
    return 1
  fi

fi