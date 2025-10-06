# Experiment Scheduling Guide

To run the ablation study two approaches are available:

- **offline**: uses the `ChatCompletionJobRequest` to invoke an LLM before the real simulation begins, then the `SimulatorJobRequest` to run the agent with the previously generated plans and the `EvalJobRequest` to compute metrics;
- **online**: uses the `SimulatorJobRequest` to both generate plans and execute them on-the-fly and the `EvalJobRequest` to compute metrics.

In general, to run experiments offline:

- choose and run a `LMInvoker` (either `AblationExpLMInvoker` or `BaseExpLMInvoker`);
- choose the corresponding experiment and replay it (either with the `AblationExpRunner` or `BaseExpRunner`, by setting `--replay-exp` to true and the `--exp-replay-path`);
- choose the corresponding evaluator and run it (either `AblationRunEvaluator` or `BaseExpRunEvaluator`).

To run experiments online:

- choose and run an experiment launcher among the available ones;
- choose the corresponding evaluator and run it.

To schedule a set of experiments one or more `ExperimentRequest`s can be submitted to an experiment server using a client. The `AblationStudyScheduler` provides an example that uses the `JobClient` to schedule offline experiments. What it does is equivalent to running these three commands:

Invoke the LLM and store the result for later use:

```shell
./gradlew :jakta-lm-invoker:run --quiet --args="--model-id deepseek/deepseek-chat-v3.1:free --lm-server-url https://openrouter.ai/api/v1/ --use-asl-syntax true --log-to-file true"
```

Run the simulator with the reactive PGP:

```shell
./gradlew :jakta-exp:run --quiet --args="--model-id deepseek/deepseek-chat-v3.1:free --lm-server-url https://openrouter.ai/api/v1/ --use-asl-syntax true --log-to-file true --prompt-snippets-path ../jakta-lm-invoker/prompt/"
```

Run the simulator by replaying a previous result:

```shell
./gradlew :jakta-exp:run --quiet --args="--run-timeout-millis 30000 --log-to-file true --replay-exp true --exp-replay-path ../jakta-lm-invoker/logs/<run-id>/ --prompt-snippets-path ../jakta-lm-invoker/prompt/"
```

// 66bf3114-f970-4b03-b1c2-df342e64e87c

Run the eval to compute metrics from the execution logs of the simulator:

```shell
./gradlew :jakta-evals:run --quiet --args="--run-dir ../jakta-exp/logs/<run-id>"
```

// 004f8946-500e-4cf0-9bb2-8d3a9d453516