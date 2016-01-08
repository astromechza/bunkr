# How To Compile and Release a new Version

1. Modify `Buildfile` with new version number. Remember:
    - X.0.0 is a Major release. Used to indicate major milestones or large API changes.
    - 0.X.0 is a Minor release. Used to indicate new features or regular releases.
    - 0.0.X is a Bugfix or patch release. Used for important bugfixes and patches.

2. Commit new Version info.

3. Add a git tag for the version:

    ```
    git tag -a vX.X.X
    ```

4. Compile into Jars:

    ```
    bundle exec buildr bunkr:bunkr-core:package
    bundle exec buildr bunkr:bunkr-cli:package
    bundle exec buildr bunkr:bunkr-gui:package
    ```

5. Optimise jars for release:

    As mentioned in the README, you'll need to download Proguard from sourceforge.

    ```
    ./tools/jarsquasher/jarquasher.rb ./bunkr-gui/target/bunkr-cli-0.5.0.jar ~/Downloads/proguard5.2.1/bin/proguard.sh
    ./tools/jarsquasher/jarquasher.rb ./bunkr-gui/target/bunkr-gui-0.5.0.jar ~/Downloads/proguard5.2.1/bin/proguard.sh
    ```

6. (Optional) Generate new Demo file:

    ```
    # Substitue your new version number
    ./tools/clidocgen/build_documentation.rb ./bunkr-cli/target/bunkr-cli-X.X.X.jar DEMO.md
    ```

7. (Optional) Commit new Demo file

8. Push commits and tags. Also upload new Github release.

    ```
    git push --tags
    ```