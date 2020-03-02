# Usage

```
mvn clean verify -Ptestsuite
```

Collect results:

```
cat  testsuite/target/archived-logs/io.quarkus.ts.startstop.StartStopTest/measurements.csv
```

e.g. on Windows:
```
C:\Users\Administrator\source\quarkus-startstop (master -> origin)
λ type testsuite\target\archived-logs\io.quarkus.ts.startstop.StartStopTest\measurements.csv
App,Mode,buildTimeMs,timeToFirstOKRequestMs,startedInMs,stoppedInMs,RSS,FDs
FULL_MICROPROFILE,JVM,13111,2881,2201,117,3660,71
JAX_RS_MINIMAL,JVM,12589,3415,2235,42,3680,71
```

and on Linux:
```
karm@local:~/workspaceRH/quarkus-startstop (master *%)$ cat testsuite/target/archived-logs/io.quarkus.ts.startstop.StartStopTest/measurements.csv 
App,Mode,buildTimeMs,timeToFirstOKRequestMs,startedInMs,stoppedInMs,RSS,FDs
FULL_MICROPROFILE,JVM,12017,1850,1430,32,181820,305
FULL_MICROPROFILE,NATIVE,154319,32,25,4,51260,129
JAX_RS_MINIMAL,JVM,7157,1125,794,21,139292,162
JAX_RS_MINIMAL,NATIVE,111296,10,8,1,28192,74
```

## Values

 * App - the test app used
 * Mode - one in DEV, NATIVE, JVM
 * buildTimeMs - how long it tool the build process to terminate
 * timeToFirstOKRequestMs - how long it took since the process was started till the first request got a valid response
 * startedInMs - "started in" value reported in Quarkus log
 * stoppedInMs - "stopped in" value reported in Quarkus log
 * RSS - memory used in kB; not comparable between Linux and Windows, see below
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