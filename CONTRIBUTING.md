# Contributing Code

A good pull request:

- Is clear.
- Works across all supported versions of Java
- Follows the existing style of the code base (see Codestyle section).
- Has comments included as needed.
- Must be appropriately licensed (MIT).

## Reporting An Issue/Feature

If you have a bugfix or new feature that you would like to contribute to SDK, please find or open an issue about it first.
Talk about what you would like to do.
It may be that somebody is already working on it, or that there are particular issues that you should know about before implementing the change.


## Contributing Code Changes

1. Run the test suite to ensure your changes do not break existing code:
   ```sh
   # Build docker to run test cases using Maven.
   make test
   ```

2. To build your own Java SDK after code change, you can run the following command to create the SDK jar in the output/target folder.
   ```sh
   # Build docker to package Java SDK using Maven.
   make build
   ```
