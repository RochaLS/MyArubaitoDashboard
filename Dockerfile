# Production image for the My Arubaito API.
# The build stage has a JDK. The image that actually runs is a JRE, as a non-root user.
# Official Temurin images are published for both amd64 (Hetzner CX) and arm64 (Hetzner CAX).

FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src src

RUN chmod +x mvnw \
    && ./mvnw -B -ntp -DskipTests package

FROM eclipse-temurin:17-jre-jammy AS runtime

RUN groupadd --system --gid 1001 app \
    && useradd --system --uid 1001 --gid app --home-dir /app --shell /usr/sbin/nologin app \
    && mkdir -p /app \
    && chown app:app /app

WORKDIR /app

COPY --from=build --chown=app:app /workspace/target/My-Arubaito-Dash-0.0.1-SNAPSHOT.jar /app/app.jar
COPY --chown=app:app docker/entrypoint.sh /app/entrypoint.sh
RUN chmod 755 /app/entrypoint.sh

USER app

EXPOSE 8080

# MaxRAMPercentage is a share of the container memory limit (set that to 1024M in Coolify).
# MaxRAM=1024m caps the base used for that percentage, so a missing limit cannot
# make the heap grow to a share of the whole 4 GB VPS. Heap lands around 600 MB.
# Override JAVA_OPTS to change this. Replacing it replaces every flag below.
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=60.0 -XX:InitialRAMPercentage=20.0 -XX:MaxRAM=1024m -XX:MaxMetaspaceSize=192m -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/urandom"

# Port open means Spring has finished binding. start-period covers a cold boot on a small CPU.
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=5 \
    CMD bash -c 'echo > /dev/tcp/127.0.0.1/${SERVER_PORT:-8080}' || exit 1

ENTRYPOINT ["/app/entrypoint.sh"]
