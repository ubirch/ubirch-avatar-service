java -Xms512M -Xmx1024M -Xss1M -XX:+CMSClassUnloadingEnabled -jar `dirname $0`/tools/sbt-launch.jar "$@"
