package it.unibo.jakta.agents.bdi.generationstrategies.lm.pipeline.formatting

object DefaultPromptBuilderSnippetsPath {
    const val DEFAULT_PROMPT_DIR = "prompt/"
    const val DEFAULT_FEW_SHOT_PROMPT_DIR = DEFAULT_PROMPT_DIR + "gen_examples.txt"
    const val DEFAULT_BDI_AGENT_DEF_PROMPT_DIR = DEFAULT_PROMPT_DIR + "bdi_agent_definition.txt"
    const val DEFAULT_YAML_OUTPUT_FORMAT = DEFAULT_PROMPT_DIR + "yaml_output_format.txt"
    const val DEFAULT_ASL_OUTPUT_FORMAT = DEFAULT_PROMPT_DIR + "asl_output_format.txt"
}
