require 'fileutils'

def make_mac_app_xml(working_dir)
    project_name = 'bunkr-gui'
    main_class = 'org.bunkr.gui.GuiEntryPoint'
    release_jar = project(project_name).path_to('target', "#{project_name}-#{project.version}-release.jar")

    unless File.exist? release_jar then
        puts "Error: release jar #{release_jar} does not exist!"
        exit! 1
    end

    xml_template = <<-EOF
<project name="Bunkr" default="bundleapp" basedir=".">
    <taskdef name="bundleapp"
             classname="com.oracle.appbundler.AppBundlerTask"
             classpath="#{project.path_to('..', 'lib', 'appbundler-1.0.jar')}" />
    <target name="bundleapp">
        <bundleapp outputdirectory="#{working_dir}"
                   name="Bunkr"
                   shortversion="#{project.version}"
                   icon="../../resources/images/bunkr-icon.icns"
                   displayname="Bunkr"
                   identifier="#{main_class}"
                   mainclassname="#{main_class}">

            <classpath file="#{release_jar}" />
        </bundleapp>
    </target>
</project>
EOF
    xml_template
end