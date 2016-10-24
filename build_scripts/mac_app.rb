require 'fileutils'

def make_mac_app_xml(working_dir)
    project_name = 'bunkr-gui'

    # find release jar
    release_jar_name = "#{project_name}-#{project.version}-release.jar"
    release_jar_path = project(project_name).path_to('target', release_jar_name)

    unless File.exist? release_jar_path then
        puts "Error: release jar #{release_jar_path} does not exist!"
        exit! 1
    end

    # copy release jar into classpath
    system("cp #{release_jar_path} #{working_dir}")

    # create package/macosx directory
    package_dir = File.join(working_dir, 'package', 'macosx')
    system("mkdir -p #{package_dir}")

    # copy icon
    icns_source = project(project_name).path_to('resources', 'images', 'bunkr-icon.icns')
    icns_dest = File.join(package_dir, 'Bunkr.icns')
    system("cp #{icns_source} #{icns_dest}")

    xml_template = <<-EOF
<?xml version="1.0" encoding="UTF-8"?>
<project name="Bunkr"
         default="default"
         basedir="."
         xmlns:fx="javafx:com.sun.javafx.tools.ant">

<target name="default">
    <taskdef resource="com/sun/javafx/tools/ant/antlib.xml"
             uri="javafx:com.sun.javafx.tools.ant"
             classpath=".:#{File.join(ENV['JAVA_HOME'], "lib", "ant-javafx.jar")}"/>

    <fx:deploy nativeBundles="dmg" outdir="." verbose="true">
        <fx:info title="Bunkr">
            <fx:icon href="#{icns_dest}" kind="default"/>
        </fx:info>
        <fx:application name="Bunkr" version="#{project.version}" mainClass="#{GUI_MAIN_CLASS}"/>
        <fx:resources>
            <fx:fileset dir="." includes="#{release_jar_name}"/>
        </fx:resources>
    </fx:deploy>
</target>
</project>
EOF
    xml_template
end
