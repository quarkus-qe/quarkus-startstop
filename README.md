# Usage

The TS expects you run Java 11+ and have ```ps``` program available on your Linux/Mac and ```wmic``` (by default present) on your Windows system.
Native image build requires GraalVM with Native image toolchain installed. Refer to [Building Native Image Guide](https://quarkus.io/guides/building-native-image) for details. 


All, including Quarkus builds nad native images:

```
mvn clean verify -Ptestsuite \
 -Dquarkus.version=1.3.0.Final-redhat-00019 \
 -Dquarkus.platform.version=1.3.0.Final-redhat-00010 \
 -Dtests.maven.repo.local=/home/karm/QUARKUS/quarkus-1.3.0.ER11/maven-repository \
 -Dmaven.repo.local=/home/karm/QUARKUS/quarkus-1.3.0.ER11/maven-repository
```

Community version, including native images:

```
mvn clean verify -Ptestsuite-community -Dquarkus.version=1.3.0.Final
```

One can fine-tune excluded test cases or tests with ```excludeTags```, e.g. ```-DexcludeTags=startstop```.

**Linux/Mac:**
```
mvn clean verify -Ptestsuite
```

**Windows:**
```
mvn clean verify -Ptestsuite-no-native
```
Native compilation is not yet supported on Windows. 
You may also want to disable native tests with ```-Ptestsuite-no-native``` if you need just a quick check on the JVM mode. 

## StartStopTest

The goal is to build and start applications with some real source code that actually
exercises some rudimentary business logic of selected extensions.

Collect results:

```
cat  testsuite/target/archived-logs/io.quarkus.ts.startstop.StartStopTest/measurements.csv
```

e.g. on Windows:
```
λ type testsuite\target\archived-logs\io.quarkus.ts.startstop.StartStopTest\measurements.csv
App,Mode,buildTimeMs,timeToFirstOKRequestMs,startedInMs,stoppedInMs,RSSKb,FDs
FULL_MICROPROFILE,JVM,9391,2162,1480,54,3820,78
JAX_RS_MINIMAL,JVM,6594,1645,949,34,3824,78
```

and on Linux:
```
$ cat ./testsuite/target/archived-logs/io.quarkus.ts.startstop.StartStopTest/measurements.csv
App,Mode,buildTimeMs,timeToFirstOKRequestMs,startedInMs,stoppedInMs,RSSKb,FDs
FULL_MICROPROFILE,JVM,9117,1439,1160,18,179932,307
FULL_MICROPROFILE,NATIVE,142680,22,17,1,51592,129
JAX_RS_MINIMAL,JVM,5934,1020,745,22,141996,162
JAX_RS_MINIMAL,NATIVE,93943,10,7,3,29768,74
```

## ArtifactGeneratorTest

The goal of this test is to test Quarkus maven artifact generator, i.e. to to use it to generate an
empty (Hello World) skeleton and build it in Quarkus dev mode. The objective is to make sure all required
extensions are correctly found.

Next, the project is run in dev mode, time to the first O.K. request is measured, a ```.java``` file is changed and the time
it took to get the expected results after hot reload is measured.

The whole run is executed as a warm-up to download the Internet and then again to measure the times.
The properties for thresholds are stored in [app-generated-skeleton/threshold.properties](./app-generated-skeleton/threshold.properties).

Build and run logs are archived and checked for errors, see:

```
**/io.quarkus.ts.startstop.ArtifactGeneratorTest/manyExtensions/dev-run.log
**/io.quarkus.ts.startstop.ArtifactGeneratorTest/manyExtensions/artifact-build.log
**/io.quarkus.ts.startstop.ArtifactGeneratorTest/manyExtensions/warmup-artifact-build.log
**/io.quarkus.ts.startstop.ArtifactGeneratorTest/manyExtensions/warmup-dev-run.log
```

Measurements example, e.g. Windows and OpenJDK 11 J9:

```
λ type testsuite\target\archived-logs\io.quarkus.ts.startstop.ArtifactGeneratorTest\measurements.csv
App,Mode,buildTimeMs,timeToFirstOKRequestMs,timeToReloadMs,startedInMs,stoppedInMs,RSSKb,FDs
GENERATED_SKELETON,GENERATOR,3766,37064,8859,18249,1172,4240,81
```

e.g. it took 3.766s to generate the skeleton project, it took 37.064s to build and start the Dev mode and it
took 8.859s to do the live reload and get the expected response to a request.

Linux and OpenJDK 11 HotSpot:

```
App,Mode,buildTimeMs,timeToFirstOKRequestMs,timeToReloadMs,startedInMs,stoppedInMs,RSSKb,FDs
GENERATED_SKELETON,GENERATOR,2644,13871,3091,5597,1154,565340,198
```

See [ArtifactGeneratorTest#manyExtensions](./testsuite/src/it/java/io/quarkus/ts/startstop/ArtifactGeneratorTest.java) for the list of used extensions.

## Code Quarkus test

Add ```-Dcode.quarkus.url=``` to test against a selected Code Quarkus site.

## Values

 * App - the test app used
 * Mode - one in DEV, NATIVE, JVM
 * buildTimeMs - how long it tool the build process to terminate
 * timeToFirstOKRequestMs - how long it took since the process was started till the first request got a valid response
 * timeToReloadMs - how long it took to get a valid response after dev mode reload
 * startedInMs - "started in" value reported in Quarkus log
 * stoppedInMs - "stopped in" value reported in Quarkus log
 * RSSkB - memory used in kB; not comparable between Linux and Windows, see below
 * FDs - file descriptors held by the process; Windows is lower due to static linking of JVM libs etc.

## Logs and Whitelist

Both build logs and runtime logs are checked for error messages. Expected error messages can be whitelisted in [Whitelist.java](./testsuite/src/it/java/io/quarkus/ts/startstop/utils/Whitelist.java).

To examine logs yourself see ```./testsuite/target/archived-logs/``` , e.g. 

```
./testsuite/target/archived-logs/io.quarkus.ts.startstop.StartStopTest/jaxRsMinimalNative/native-build.log
./testsuite/target/archived-logs/io.quarkus.ts.startstop.StartStopTest/jaxRsMinimalNative/native-run.log
./testsuite/target/archived-logs/io.quarkus.ts.startstop.StartStopTest/fullMicroProfileJVM/jvm-run.log
./testsuite/target/archived-logs/io.quarkus.ts.startstop.StartStopTest/fullMicroProfileJVM/jvm-build.log
./testsuite/target/archived-logs/io.quarkus.ts.startstop.StartStopTest/jaxRsMinimalJVM/jvm-run.log
./testsuite/target/archived-logs/io.quarkus.ts.startstop.StartStopTest/jaxRsMinimalJVM/jvm-build.log
./testsuite/target/archived-logs/io.quarkus.ts.startstop.StartStopTest/fullMicroProfileNative/native-build.log
./testsuite/target/archived-logs/io.quarkus.ts.startstop.StartStopTest/fullMicroProfileNative/native-run.log
```

## What I Did Report

The test suite records Maven commands it used, the directories where those commands were executed etc. in
a neat markdown file for each test run. e.g.

```
# io.quarkus.ts.startstop.StartStopTest, jaxRsMinimalJVM
/home/karm/workspaceRH/quarkus-startstop/app-jax-rs-minimal

mvn clean compile quarkus:build -Dquarkus.package.output-name=quarkus -Dmaven.repo.local=/home/karm/QUARKUS/quarkus-1.3.2.CR1/maven-repository

---
/home/karm/workspaceRH/quarkus-startstop/app-jax-rs-minimal

java -jar target/quarkus-runner.jar

---
Measurements:

|App|Mode|buildTimeMs|timeToFirstOKRequestMs|startedInMs|stoppedInMs|RSSKb|FDs|
| --- | --- | --- | --- | --- | --- | --- | --- |
|JAX_RS_MINIMAL|JVM|4787|906|643|18|140504|162|

```

The file format is somewhat loose and it differs a bit test to test, it is meant for humans to take a quick look at what the TS did.
If you need to machine process the data, we suggest: 
 * capturing the TS stdout log where all commands are logged in a machine friendly format
 * reading archived measurements.csv files

One can also look into ```./testsuite/target/archived-logs/aggregated-report.md``` for an overall concatenation of all reports from all test runs.
Subsequent test suite executions without ```mvn clean``` keep appending to this aggregate file on purpose. 

## Thresholds

The test suite works with ```threshold.properties``` for each test app. E.g. ```app-jax-rs-minimal/threshold.properties```:

```
linux.jvm.time.to.first.ok.request.threshold.ms=1500
linux.jvm.RSS.threshold.kB=170000
linux.native.time.to.first.ok.request.threshold.ms=35
linux.native.RSS.threshold.kB=75000
windows.jvm.time.to.first.ok.request.threshold.ms=2000
windows.jvm.RSS.threshold.kB=4000
```

The measured values are simply compared to be less or equal to the set threshold. One can overwrite the threshold properties
by using env variables or system properties (in this order). All letter are capitalized and dot is replaced with underscore, e.g.

```
APP_JAX_RS_MINIMAL_LINUX_JVM_TIME_TO_FIRST_OK_REQUEST_THRESHOLD_MS=500 mvn clean verify -Ptestsuite
```

Results in:

```
[INFO] Results:
[INFO] 
[ERROR] Failures: 
[ERROR]   StartStopTest.jaxRsMinimalJVM:137->testRuntime:121 Application JAX_RS_MINIMAL 
          in JVM mode took 957 ms to get the first OK request, which is over 500 ms threshold. 
          ==> expected: <true> but was: <false>
[INFO] 
```

## Windows notes

### Note on Native image

Not yet supported on Windows.

### Stopping Quarkus
Works well, see details:
See [README.md](./testsuite/src/it/resources/README.md)

### Parsing Quarkus logs
Works well, see caveats in [Logs.java](./testsuite/src/it/java/io/quarkus/ts/startstop/utils/Logs.java), e.g.
control characters such as: ```stopped in [38;5;188m0.024[39ms[39m[38;5;203m[39m[38;5;227m```

### Memory consumption, RSS

We use "Working Set Size", see [win32-process](https://docs.microsoft.com/en-us/windows/win32/cimwin32prov/win32-process), 
to measure the memory used. It is not calculated the same way as ``ps`` does it on Linux and one cannot compare it directly to the Linux RSS.

## Note on Dev Mode

Not yet supported in the TS.