echo -e "Publishing to Bintray"
bash gradlew bintrayUpload -PbintrayUsername=$bintrayUsername -PbintrayApiKey=$bintrayApiKey
echo -e "Publishing to Github pages"
bash gradlew publishGhPages -PgithubUsername=$githubUsername -PgithubPassword=$githubPassword