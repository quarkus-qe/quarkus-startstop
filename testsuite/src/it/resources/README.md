# CtrlC.exe

When you start Quarkus on Windows as a process without
a console window, you cannot stop it gently with TASKKILL, 
you have to use TASKKILL /F. That terminates the process immediately
and it does not let it write "stopped in" to the process output.

To mitigate this, you have to use a native Windows API with
a small tool that detaches itself from its own console,
attaches to the non-existing console of the PID you want
to stop and sends it Ctrl+C.

```$c

#include <windows.h>
#include <stdio.h>

void ctrlC(int pid) {
    FreeConsole();
    if (AttachConsole(pid)) {
        SetConsoleCtrlHandler(NULL, true);
        GenerateConsoleCtrlEvent(CTRL_C_EVENT, 0);
    }
    exit(1);
}

int main(int argc, const char* argv[]) {
    if (argc != 2) {
        printf("provide a pid number");
        return 1;
    }
    int pid = atoi(argv[1]);
    printf("stopping pid %d", pid);
    ctrlC(pid);
    return 0;
}

``` 

# JSONtoEnum.java

This is a utility that generates a convenience enum from a Code Quarkus website instance, e.g:

```
javac JSONtoEnum.java  &&  java JSONtoEnum https://code.quarkus.stage.redhat.com/api/extensions
```

# API of code.quarkus
https://editor.swagger.io/?url=https://code.quarkus.io/q/openapi