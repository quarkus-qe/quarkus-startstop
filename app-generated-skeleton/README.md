This is a placeholder directory to keep
[threshold.properties](./threshold.properties) related to generated artifacts.

It also keeps dummy [application.properties](./application.properties) used for the
generated skeleton purely to satisfy some extensions' expected non-empty directives.
No functional code is actually run for the skeleton though. It merely checks the generator and Hello World in dev mode.

When executed, the test actually uses ```ARTIFACT_GENERATOR_WORKSPACE``` directory
to generate the ```app-generated-skeleton``` project into. If neither sys prop nor env prop ```ARTIFACT_GENERATOR_WORKSPACE```
is set, it defaults to ```java.io.tmpdir``` on your system.
