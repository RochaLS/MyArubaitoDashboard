#!/bin/sh
# Starts the API with container-aware JVM limits.
# JAVA_OPTS replaces the whole set (it is not appended). Copy the default and edit it.
set -eu

if [ -z "${JAVA_OPTS:-}" ]; then
  JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=60.0 -XX:InitialRAMPercentage=20.0 -XX:MaxRAM=1024m -XX:MaxMetaspaceSize=192m -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/urandom"
fi

# Unquoted on purpose so JAVA_OPTS splits into separate JVM flags.
# exec replaces this shell so Coolify's stop signal reaches Java.
exec java $JAVA_OPTS -jar /app/app.jar
