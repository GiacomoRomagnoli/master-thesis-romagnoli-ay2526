# Experiments Guide

There are three ways to run an experiment:
1. **Direct execution**: by using the `run` Gradle task;
2. **Fat JAR**: by building a fat jar and execute it;
3. **Native executable**: by building a native executable and execute it (requires GraalVM).

## Prerequisites

- Unix shell environment with the root of the repo as working directory
- For native executable compilation: [GraalVM JDK](https://www.graalvm.org/jdk21/docs/) is required. 
If [SDKMAN](https://sdkman.io/) is available, the community edition can be installed by running `sdk install java 21.0.2-graalce`

## Building

### Fat JAR
To build the fat jar with all dependencies included:

```shell
./gradlew :jakta-exp:shadowJar
```

### Native Executable
To build a native executable using GraalVM Native Image. This process requires three steps:

```shell
# Runs with the agent to collect metadata for native compilation
./gradlew :jakta-exp:run -Pagent --args="--lm-server-url https://openrouter.ai/api/v1/ --model-id deepseek/deepseek-chat-v3.1:free --log-to-file --exp-timeout-millis 50000 --temperature 0.1"

# Copies the collected metadata
./gradlew :jakta-exp:metadataCopy

# Compiles to a native executable
./gradlew :jakta-exp:nativeCompile
```

**Note:** The first step runs the application with GraalVM's tracing agent (`-Pagent`) to collect metadata about reflection usage, dynamic class loading, and other runtime behaviors needed for native compilation.

## Configuration

### API Setup
Any OpenAI-compliant endpoint can be used. For OpenRouter:

1. The following URL must be set: `https://openrouter.ai/api/v1/`
2. An API key must be specified by creating a `.env` file in the repository root (`.env.template` provides an example)

For authentication details, refer to: https://openrouter.ai/docs/api-reference/authentication#using-an-api-key

## Command Line Arguments

### Base Configuration
- `--run-id`: Identifier of the experimental run (by default a UUID)
- `--run-timeout-millis`: Experiment timeout in milliseconds (default: two minutes)
- `--replay-exp`: Replay a past experiment instead of creating new one (default: false)
- `--exp-replay-path`: Path to experiment log directory or specific chat file to replay (default: logs)

### Model Configuration
- `--model-id`: ID of the model to use (default: `test`)
- `--temperature`: Sampling temperature between 0.0-2.0. Higher values (0.8) = more random, lower values (0.2) = more focused/deterministic (default: 0.5)
- `--top-p`: Limits model choices to top tokens whose probabilities add up to P. Lower = more predictable (default: 1.0)
- `--reasoning-effort`: Set the level of reasoning effort for processing (default: none)
- `--max-tokens`: Maximum number of tokens for generated answers (default: 2048)

### Language Model Server Configuration
- `--lm-server-url`: URL of the server with OpenAI-compliant API (default: http://localhost:8080)
- `--lm-server-token`: Secret API key for authentication (uses the `API_KEY` environment variable by default)
- `--request-timeout`: Time to process HTTP call from request to response in milliseconds (default: two minutes)
- `--connect-timeout`: Time to establish connection with server in milliseconds (default: ten seconds)
- `--socket-timeout`: Maximum inactivity time between data packets in milliseconds (default: one minute)

### Logging Configuration
- `--log-level`: Minimum log level (default: INFO)
- `--log-to-console`: Output logs to standard output (default: true)
- `--log-to-file`: Output logs to local filesystem (default: false)
- `--log-dir`: Directory for log output (default: "logs")
- `--log-to-server`: Send logs to a log server (default: false)
- `--log-server-url`: URL of the log server (default: http://localhost:8081)

### Prompt Configuration
- `--without-admissible-beliefs`: Whether to exclude or not admissible beliefs from the prompt (default: false)
- `--without-admissible-goals`: Whether to exclude or not admissible goals from the prompt (default: false)
- `--expected-result-explanation-level`: The level of detail with which the expected result is explained in the prompt (one of {standard (default), detailed})
- `--asl-syntax-explanation-level`: The level of detail with which the AgentSpeak syntax is explained in the prompt (one of {standard (default), detailed})
- `--with-bdi-agent-definition`: Whether to include or not a definition of what a BDI agent is in the prompt (default: false)
- `--few-shot`: Whether to include or not plan generation examples in the prompt (default: false)
- `--without-logic-description`: Whether to exclude or not logic descriptions of beliefs and goals from the prompt (default: false)
- `--without-nl-description`: Whether to exclude or not natural language descriptions of beliefs and goals from the prompt (default: false)
- `--prompt-technique`: The kind of technique to use for prompting (one of {NoCoT (default), CoT, CoTMulti})
- `--remarks`: Path to the file that stores remarks to include in the prompt (default: none)
- `--environment-type`: The type of environment to use (one of {Standard (default), HSpace or Channel})

## Running Experiments

### Running Ablation Experiments

Option 1: Gradle task

```shell
./gradlew :jakta-exp:run --args="--lm-server-url https://openrouter.ai/api/v1/ --model-id deepseek/deepseek-chat-v3.1:free --log-to-file --exp-timeout-millis 50000 --temperature 0.1"
```

Option 2: fat JAR
```shell
# Using Gradle wrapper
./gradlew :jakta-exp:runShadow --args="--lm-server-url https://openrouter.ai/api/v1/ --model-id deepseek/deepseek-chat-v3.1:free --log-to-file --exp-timeout-millis 50000 --temperature 0.1"

# Or executes JAR directly (update api-key and jakta-version)
API_KEY="<api-key>" java -jar ./jakta-exp/build/libs/jakta-exp-<jakta-version>-all.jar --lm-server-url https://openrouter.ai/api/v1/ --model-id deepseek/deepseek-chat-v3.1:free --log-to-file --exp-timeout-millis 50000 --temperature 0.1
```

Option 3: native executable
```shell
# Using Gradle wrapper
./gradlew :jakta-exp:nativeRun -Pargs="--lm-server-url https://openrouter.ai/api/v1/ --model-id deepseek/deepseek-chat-v3.1:free --log-to-file --exp-timeout-millis 50000 --temperature 0.1"

# Or executes binary directly (update api-key)
API_KEY="<api-key>" ./jakta-exp/build/native/nativeCompile/jakta-exp --lm-server-url https://openrouter.ai/api/v1/ --model-id deepseek/deepseek-chat-v3.1:free --log-to-file --exp-timeout-millis 50000 --temperature 0.1
```

### Running ECAI Experiments

Using a Gradle task:

```shell
./gradlew :jakta-exp:runEcaiExperiment --args="--lm-server-url https://openrouter.ai/api/v1/ --model-id deepseek/deepseek-chat-v3.1:free --log-to-file --exp-timeout-millis 50000 --temperature 0.1"
```

## Replaying a past experiment

To replay a past experiment:

```shell
./gradlew :jakta-exp:run --args="--log-to-file --run-timeout-millis 30000 --replay-exp --exp-replay-path logs/<run-id>"
# or for ECAI experiments
./gradlew :jakta-exp:runEcaiExperiment --args="--log-to-file --run-timeout-millis 30000 --replay-exp --exp-replay-path logs/<run-id>"
```

Where `<run-id>` is the id of a previous experiment, which is printed on console when an experiment starts and ends. It is also used to name the directory where the results are put (provided by the `--log-dir` parameter).

## Analyzing the results of an experiment

Once an experiment is executed in one of the ways outlined before, the expected directory structure, relative to the root of the repo, will be:

```shell
.
└── jakta-exp
    └── logs
        └── <run-id>
            ├── Mas-<mas-id>.jsonl
            ├── Mas-<mas-id>.log
            ├── Mas-<mas-id>-ExplorerRobot-<agent-id>.jsonl
            ├── Mas-<mas-id>-ExplorerRobot-<agent-id>.log
            ├── Mas-<mas-id>-ExplorerRobot-<agent-id>-<pgp-name>-<pgp-id>.jsonl
            └── Mas-<mas-id>-ExplorerRobot-<agent-id>-<pgp-name>-<pgp-id>.log

```

Three kind of traces are produced:
- A trace for the Mas;
- A trace for each agent of the Mas (here only `ExplorerRobot`);
- A trace for each PGP invocation by an agent.

For each trace both a `.log` and a `.jsonl` are produced. The first provides a brief overview of what happened during the run. The latter is used for the automated evaluation.

Given this directory structure, the gradle task `evalRun` can be run:

```shell
./gradlew :jakta-evals:evalRun --args="--run-dir ../jakta-exp/logs/<run-id>/"
# or for ECAI experiments
./gradlew :jakta-evals:ecaiEvalRun --args="--run-dir ../jakta-exp/logs/<run-id>/"
```

This will create the `metrics/` directory under the `jakta-evals` module:

```shell
.
└── jakta-evals
    └── metrics
        └── <run-id>
            ├── chat_history.txt
            ├── evaluation_result.json
            └── generation_result.txt
 ```

The result of the evaluation will be stored in three files:

- `chat_history.txt` holds the conversation history in an unstructured format;
- `evaluation_result.json` is obtained by deserializing the `RunEvaluation`. It provides the invocation context, the PGP invocation, the metrics computed and data relative to the generation request. It is used for data analysis;
- `generation_result.txt` provides the generated plans in AgentSpeak syntax.
