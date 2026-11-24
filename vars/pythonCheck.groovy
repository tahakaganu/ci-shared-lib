def call(Map cfg = [:}) {
    String path = cfg.get('dir', '.')
    dir(path) {
        sh 'python3 -m venv .venv && . .venv/bin/activate && pip install -q -r requirements.txt'
        sh '. .venv/bin/activate && pylint *.py && pytest --junitxml=reports/junit.xml'
    }
}