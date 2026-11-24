import groovy.json.JsonOutput

/**
 * Advisory AI triage for failed builds. Never fails the build.
 * Usage: post { failure { aiTriage(modelId: '...') } }
 */
def call(Map cfg = [:]) {
  String modelId = cfg.modelId ?: env.AI_TRIAGE_MODEL_ID
  String region  = cfg.region ?: 'eu-west-2'
  String ctr     = cfg.container ?: 'aws'
  int lines      = (cfg.lines ?: 200) as int
  if (!modelId) { echo 'aiTriage: no model ID configured, skipping'; return }

  try {
    String log = redact(currentBuild.rawBuild.getLog(lines).join('\n'))
    String prompt = 'You are a CI platform engineer. From this Jenkins log give: ' +
      '1) the most likely root cause in one sentence, ' +
      '2) whether it is the user code or the CI platform, ' +
      '3) the next step to fix it. Be brief.\n\nLOG:\n' + log
    writeFile file: 'ai-triage-request.json',
              text: JsonOutput.toJson([[role: 'user', content: [[text: prompt]]]])

    container(ctr) {
      String summary = sh(returnStdout: true, script: """
        aws bedrock-runtime converse --region ${region} --model-id '${modelId}' \
          --messages file://ai-triage-request.json \
          --inference-config maxTokens=400,temperature=0 \
          --query 'output.message.content[0].text' --output text
      """).trim()
      echo "AI triage (advisory):\n${summary}"
      writeFile file: 'ai-triage.txt', text: summary
      archiveArtifacts artifacts: 'ai-triage.txt', allowEmptyArchive: true
      currentBuild.description = 'AI triage: ' + summary.take(150)
    }
  } catch (err) {
    echo "aiTriage skipped: ${err}"     // advisory only: never break the build
  }
}

@NonCPS
private String redact(String text) {
  return text
    .replaceAll(/AKIA[0-9A-Z]{16}/, '[REDACTED]')
    .replaceAll(/(?i)(password|token|secret)\s*[=:]\s*\S+/, '$1=[REDACTED]')
}
