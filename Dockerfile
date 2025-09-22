# ---------- Base Gradle Builder ----------
FROM eclipse-temurin:21 as gradle-builder
RUN apt-get update && apt-get install -y git && rm -rf /var/lib/apt/lists/*
WORKDIR /app

# Copy only wrapper + gradle setup first (cache dependencies)
COPY gradlew gradlew.bat settings.gradle* build.gradle* /app/
COPY gradle /app/gradle
COPY alchemist-jakta-incarnation/build.gradle* /app/alchemist-jakta-incarnation/
COPY jakta-bdi/build.gradle* /app/jakta-bdi/
COPY jakta-dsl/build.gradle* /app/jakta-dsl/
COPY jakta-evals/build.gradle* /app/jakta-evals/
COPY jakta-exp/build.gradle* /app/jakta-exp/
COPY jakta-konsist-test/build.gradle* /app/jakta-konsist-test/
COPY jakta-plan-generation/build.gradle* /app/jakta-plan-generation/
COPY jakta-exp-scheduler/build.gradle* /app/jakta-exp-scheduler/
COPY jakta-state-machine/build.gradle* /app/jakta-state-machine/

# Copy git repo for the semantic versioning gradle plugin (otherwise build fails)
COPY .git .git

# Download dependencies (cached unless build files change)
RUN ./gradlew --no-daemon help

# Now copy full source
COPY . .


# ---------- Stage 1: Native Image ----------
FROM ghcr.io/graalvm/native-image-community:21 AS native-builder
WORKDIR /app

RUN microdnf install findutils git

# Copy prepared project (with cached deps)
COPY --from=gradle-builder /app /app

# Generate config for and build the executable of jakta-lm-invoker
RUN ./gradlew :jakta-lm-invoker:run \
    -Pagent \
    --args="--run-id test-run \
            --lm-server-url https://openrouter.ai/api/v1/ \
            --model-id deepseek/deepseek-chat-v3.1:free \
            --log-to-file \
            --remarks prompt_snippets/base_exp_explorer_remarks.txt \
            --temperature 0.1" \
    --no-daemon
RUN ./gradlew :jakta-lm-invoker:metadataCopy --no-daemon
RUN ./gradlew :jakta-lm-invoker:nativeCompile --no-daemon

# Generate config for and build the executable of jakta-exp
RUN ./gradlew :jakta-exp:run \
    -Pagent \
    --args="--run-id test-run
            --replay-exp true
            --exp-replay-path ../jakta-lm-invoker/logs/test-run" \
    --no-daemon
RUN ./gradlew :jakta-exp:metadataCopy --no-daemon
RUN ./gradlew :jakta-exp:nativeCompile --no-daemon

# Generate config for and build the executable of jakta-evals
RUN ./gradlew :jakta-evals:run \
    -Pagent \
    --args="--run-dir ../jakta-exp/logs/test-run" \
    --no-daemon
RUN ./gradlew :jakta-evals:metadataCopy --no-daemon
RUN ./gradlew :jakta-evals:nativeCompile --no-daemon

# ---------- Stage 2: Fat JAR ----------
FROM gradle-builder AS jar-builder
WORKDIR /app

RUN ./gradlew :jakta-exp-scheduler:buildFatJar --no-daemon

# ---------- Stage 3: Runtime ----------
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

COPY --from=jar-builder /app/jakta-scheduler/build/libs/jakta-scheduler-all.jar /app/jakta-scheduler.jar

COPY --from=native-builder /app/jakta-lm-invoker/build/native/nativeCompile/invoker /app/invoker
COPY --from=native-builder /app/jakta-exp/build/native/nativeCompile/simulator /app/simulator
COPY --from=native-builder /app/jakta-evals/build/native/nativeCompile/evaluator /app/evaluator

# Add non-root user
RUN adduser -D -u 1000 appuser
USER appuser

# Default: run jar, which then invokes the native binary
ENTRYPOINT ["java", "-jar", "/app/jakta-evals.jar"]
