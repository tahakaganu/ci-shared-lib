def call(Map cfg) {
    if (!cfg.bucket || !cfg.file) {
        error 'publishToS3 needs bucket and file'
    }
    String prefix = cfg.prefix ?: 'artifacts'
    sh "aws s3 cp '${cfg.file}' 's3://${cfg.bucket}/${prefix}/'"
}