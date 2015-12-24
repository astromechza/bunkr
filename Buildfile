require 'fileutils'
require 'buildr/jacoco'

PROJECT_NAME = 'bunkr'
PROJECT_GROUP = "org.#{PROJECT_NAME}"
PROJECT_VERSION = '0.3.0'
COMPATIBLE_PROJECT_VERSION = '0.3.0'

# where to pull maven dependencies from
repositories.remote << 'https://repo.maven.apache.org/maven2'
repositories.remote << 'https://repo1.maven.org/maven2'

# dependencies
JAR_JUNIT = "junit:junit:jar:4.12"
JAR_BC = "org.bouncycastle:bcprov-jdk15on:jar:1.53"
JAR_JSON_SIMPLE = "com.googlecode.json-simple:json-simple:jar:1.1.1"
JAR_ARGPARSE = "net.sourceforge.argparse4j:argparse4j:jar:0.6.0"

# define main project
define PROJECT_NAME do

    project.version = PROJECT_VERSION
    project.group = PROJECT_GROUP

    desc 'sub-project for cli'
    define 'cli' do

        CLI_MAIN_CLASS = "#{PROJECT_GROUP}.cli.CLI"
        CLI_OUTPUT_JAR = _('..', 'target', "bunkr-cli-#{project.version}.jar")

        test.with JAR_JUNIT

        compile.from(_('..', 'src', 'org', 'bunkr'))
        compile.with JAR_BC, JAR_ARGPARSE, JAR_JSON_SIMPLE
        compile.into(_('..', 'target', 'classes'))
        compile.using(source: '1.8', target: '1.8', lint: 'all')

        package(:jar, file: CLI_OUTPUT_JAR).merge(compile.dependencies).exclude('META-INF/BCKEY.*')
        package(:jar, file: CLI_OUTPUT_JAR).with(manifest: {'Main-Class' => CLI_MAIN_CLASS})

        run.using main: CLI_MAIN_CLASS
    end

    desc 'sub-project for gui'
        define 'gui' do

            GUI_MAIN_CLASS = "#{PROJECT_GROUP}.gui.GuiEntryPoint"
            GUI_OUTPUT_JAR = _('..', 'target', "bunkr-gui-#{project.version}.jar")

            test.with JAR_JUNIT

            compile.from(_('..', 'src', 'org', 'bunkr'))
            compile.with JAR_BC, JAR_ARGPARSE, JAR_JSON_SIMPLE
            compile.into(_('..', 'target', 'classes'))
            compile.using(source: '1.8', target: '1.8', lint: 'all')

            package(:jar, file: GUI_OUTPUT_JAR).merge(compile.dependencies).exclude('META-INF/BCKEY.*')
            package(:jar, file: GUI_OUTPUT_JAR).with(manifest: {'Main-Class' => GUI_MAIN_CLASS})

            run.using main: GUI_MAIN_CLASS
        end
end
