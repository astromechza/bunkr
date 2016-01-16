require 'tmpdir'
require 'fileutils'

begin
  require 'faker'
rescue LoadError
  puts "This example builder requires the 'Faker' gem to generate data."
  exit! 1
end

module ExampleBuilder

    module_function
    def run(cli_jar, output_archive)
        puts "CLI jar: #{cli_jar}"
        puts "Output: #{output_archive}"

        useful_image = File.join(File.dirname(File.dirname(File.expand_path __FILE__)), 'bunkr-gui', 'resources', 'images', 'bunkr-logo-200x200.png')

        # create temporary directory for working with
        working_dir = Dir.mktmpdir
        password_path = File.join(working_dir, '.password')

        if File.exist? output_archive
            puts "Removing old example archive #{output_archive}"
            File.delete output_archive
        end

        cmd = "java -jar #{cli_jar} #{output_archive}"
        run_command "#{cmd} create"
        run_command "echo HunterTwo > #{password_path} && chmod 600 #{password_path}"
        run_command "#{cmd} change-security scrypt #{password_path}"
        cmd = "#{cmd} -p #{password_path}"

        d = "/#{Faker::Lorem.word}"
        run_command "#{cmd} mkdir '#{d}'"
        run_command "#{cmd} write '#{d}/#{Faker::Lorem.word}.txt' '#{create_fake_content_file(working_dir)}' -t text"
        run_command "#{cmd} write '#{d}/#{Faker::Lorem.word}' '#{create_fake_content_file(working_dir)}'"
        d += "/#{Faker::Lorem.word}"
        run_command "#{cmd} mkdir '#{d}'"
        run_command "#{cmd} write '#{d}/#{Faker::Lorem.word}.txt' '#{create_fake_content_file(working_dir)}' -t text"
        run_command "#{cmd} write '#{d}/#{Faker::Lorem.word}.txt' '#{create_fake_content_file(working_dir)}' -t text"
        run_command "#{cmd} write '#{d}/#{Faker::Lorem.word}.png' '#{useful_image}' -t image"
        run_command "#{cmd} write '#{d}/#{Faker::Lorem.word}.png' '#{useful_image}' -t image"
        run_command "#{cmd} write '#{d}/#{Faker::Lorem.word}' '#{create_fake_content_file(working_dir)}'"
        run_command "#{cmd} write '#{d}/#{Faker::Lorem.word}' '#{create_fake_content_file(working_dir)}'"
        d += "/#{Faker::Lorem.word}"
        run_command "#{cmd} mkdir '#{d}'"
        run_command "#{cmd} write '#{d}/#{Faker::Lorem.word}.txt' '#{create_fake_content_file(working_dir)}' -t text"
        run_command "#{cmd} write '#{d}/#{Faker::Lorem.word}.png' '#{useful_image}' -t image"

        d = "/#{Faker::Lorem.word}"
        run_command "#{cmd} mkdir '#{d}'"
        run_command "#{cmd} write '#{d}/#{Faker::Lorem.word}.txt' '#{create_fake_content_file(working_dir)}' -t text"
        run_command "#{cmd} write '#{d}/#{Faker::Lorem.word}' '#{create_fake_content_file(working_dir)}'"
        run_command "#{cmd} write '#{d}/#{Faker::Lorem.word}.png' '#{useful_image}' -t image"
        run_command "#{cmd} write '#{d}/#{Faker::Lorem.word}.png' '#{useful_image}' -t image"
        run_command "#{cmd} write '#{d}/#{Faker::Lorem.word}.png' '#{useful_image}' -t image"
        run_command "#{cmd} write '#{d}/#{Faker::Lorem.word}.txt' '#{create_fake_content_file(working_dir)}' -t text"
        d += "/#{Faker::Lorem.word}"
        run_command "#{cmd} mkdir '#{d}'"
        run_command "#{cmd} write '#{d}/#{Faker::Lorem.word}' '#{create_fake_content_file(working_dir)}'"
        run_command "#{cmd} write '#{d}/#{Faker::Lorem.word}' '#{create_fake_content_file(working_dir)}'"
        run_command "#{cmd} write '#{d}/#{Faker::Lorem.word}.txt' '#{create_fake_content_file(working_dir)}' -t text"

        d = "/#{Faker::Lorem.word}"
        run_command "#{cmd} mkdir '#{d}'"
        d += "/#{Faker::Lorem.word}"
        run_command "#{cmd} mkdir '#{d}'"
        d += "/#{Faker::Lorem.word}"
        run_command "#{cmd} mkdir '#{d}'"
        d += "/#{Faker::Lorem.word}"
        run_command "#{cmd} mkdir '#{d}'"
        run_command "#{cmd} write '#{d}/#{Faker::Lorem.word}.txt' '#{create_fake_content_file(working_dir)}' -t text"
    end

    private

    module_function
    def create_fake_content_file(tmp_dir)
        temp_file = File.join(tmp_dir, Faker::Lorem.word)
        File.open(temp_file, 'w') { |file| file.write(Faker::Lorem.paragraph(3, false, 100)) }
        temp_file
    end

    module_function
    def run_command(cmdline, allow_fail: false)
        if cmdline.is_a?(Array)
          formatted_cmdline = cmdline.map {|a| a.contains?(' ') ? "\"#{a}\"" : a}.join(' ')
        else
          formatted_cmdline = cmdline
        end

        puts "Executing #{formatted_cmdline}"

        IO.popen(cmdline, 'r', err: [:child, :out]) do |io|
          all_output = io.read.strip
          io.close
          code = ($? >> 8).to_i
          if (not allow_fail) and code != 0
            raise "Command '#{cmdline}' failed with code: #{code} output: #{all_output}"
          end
        end
    end



end