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

4. Compile into release jars:

    ```
    bundle exec buildr bunkr:bunkr-core:package
    bundle exec buildr bunkr:bunkr-cli:build_release
    bundle exec buildr bunkr:bunkr-gui:build_release
    ```

5. (Optional) Generate new Demo file:

    ```
    bundle exec buildr bunkr:bunkr-cli:generate_demo
    ```

6. (Optional) Commit new Demo file

7. Push commits and tags. Also upload new Github release.

    ```
    git push --tags
    ```
