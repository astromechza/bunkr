require 'tmpdir'
require_relative './docbuilder'

module CLIDemoBuilder

  module_function
  def run(jar_file, output_file)
    # convert to absolute path
    jar_file = File.expand_path(jar_file)
    unless File.exists?(jar_file)
      puts "Target jar #{jar_file} does not exist"
      exit! 1
    end

    # create temporary directory for working with
    working_dir = Dir.mktmpdir
    archive_path = File.join(working_dir, 'demo.bunkr')
    password_path = File.join(working_dir, '.password')

    d = DocumentationBuilder.new

    d.add_documentation <<-EOF
  # Bunkr CLI Usage Examples
  This document shows the current status of the CLI and some of the commands you can use.
  EOF

    d.add_documentation '## 1. Print the version of Bunkr you are running'
    d.add_command "java -jar #{jar_file} --version"

    d.add_documentation '## 2. Print the help information'
    d.add_command "java -jar #{jar_file} --help", allow_fail: true

    d.add_documentation '## 3. Create a new archive'
    d.add_command "java -jar #{jar_file} #{archive_path} create"
    d.add_command "echo DemoPassword > #{password_path} && chmod 600 #{password_path}", no_output: true
    d.add_command "java -jar #{jar_file} #{archive_path} change-security scrypt #{password_path}"

    d.add_documentation '## 4. Add some content from a file'
    file_one = File.join(working_dir, 'file1.txt')
    d.add_command "echo 'The quick brown fox jumped over the lazy dog' > #{file_one}", no_output: true
    d.add_command "java -jar #{jar_file} #{archive_path} -p #{password_path} write /file_one #{file_one} -t demo-tag --no-progress"
    d.add_command "java -jar #{jar_file} #{archive_path} -p #{password_path} ls /"

    d.add_documentation '## 5. Check integrity'
    d.add_command "java -jar #{jar_file} #{archive_path} -p #{password_path} hash /file_one -a md5 --no-progress"
    d.add_command "md5 #{file_one}"

    d.add_documentation '## 6. Add some folders and another file'
    file_two = File.join(working_dir, 'file2.txt')
    d.add_command "head -c 514229 /dev/urandom  > #{file_two}", no_output: true
    d.add_command "java -jar #{jar_file} #{archive_path} -p #{password_path} mkdir /sample/dir -r", no_output: true
    d.add_command "java -jar #{jar_file} #{archive_path} -p #{password_path} write /sample/dir/file_two #{file_two} --no-progress"

    d.add_documentation 'Show everything in the tree so far:'
    d.add_command "java -jar #{jar_file} #{archive_path} -p #{password_path} find /"

    d.add_documentation 'See how the file reflects the size in bytes'
    d.add_command "java -jar #{jar_file} #{archive_path} -p #{password_path} ls /sample/dir/ --machine-readable"
    d.add_command "ls -al #{archive_path}"
    d.add_documentation <<-EOF
  /dev/urandom didn't compress very well, so the compression offered by Bunkr hasn't really helped :(.
  EOF

    d.add_documentation '## 7. Lets try something more compressible'
    file_three = File.join(working_dir, 'file3.txt')
    d.add_command "for i in {1..400}; do echo 'Lorem ipsum dolor sit amet' >> #{file_three}; done; ls -al #{file_three}"
    d.add_command "java -jar #{jar_file} #{archive_path} -p #{password_path} write /file_three #{file_three}", no_output: true
    d.add_command "java -jar #{jar_file} #{archive_path} -p #{password_path} ls /"
    d.add_command "ls -al #{archive_path}"
    d.add_documentation <<-EOF
  That compressed much better. The file of 10800 bytes only added 1024 bytes to the archive size. Files are managed using blocks of 1024 bytes, so that is the minimum effect on disk size.
  EOF

    d.add_documentation 'Now move it to another location'
    d.add_command "java -jar #{jar_file} #{archive_path} -p #{password_path} mv /file_three /sample/file_three", no_output: true
    d.add_command "java -jar #{jar_file} #{archive_path} -p #{password_path} find / --type file"

    d.add_documentation 'And finally extract it and prove that we didnt lose anything'
    file_four = File.join(working_dir, 'file3-output.txt')
    d.add_command "java -jar #{jar_file} #{archive_path} -p #{password_path} read /sample/file_three #{file_four}", no_output: true
    d.add_command "md5 #{file_three} #{file_four}"

    d.add_generated_with(jar_file)
    File.open(output_file, 'w') do |f|
      all_content = d.get_all
      all_content.gsub!("java -jar #{jar_file}", 'bunkr')
      all_content.gsub!(jar_file, File.basename(jar_file))
      all_content.gsub!(working_dir + '/', '')
      f.write(all_content)
    end

  end
end