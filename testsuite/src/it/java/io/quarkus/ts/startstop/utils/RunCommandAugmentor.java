package io.quarkus.ts.startstop.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RunCommandAugmentor {

    public static List<String> setCommandPrefix(List<String> baseCommand, List<String> commandPrefix) {
        if (commandPrefix == null || (commandPrefix.size() == 1 && commandPrefix.get(0).length() == 0)) {
            return baseCommand;
        }
        List<String> runCmd = new ArrayList<>(baseCommand.size() + commandPrefix.size());
        runCmd.addAll(commandPrefix);
        for (String cmdPart : baseCommand) {
            runCmd.add(cmdPart);
        }
        return Collections.unmodifiableList(runCmd);
    }

    public static List<String> setMemoryLimits(List<String> baseCommand, List<String> memory, boolean isNative) {
        if (memory == null || (memory.size() == 1 && memory.get(0).length() == 0)) {
            return baseCommand;
        }
        List<String> runCmd = new ArrayList<>(baseCommand.size() + memory.size());
        for (String cmdPart : baseCommand) {
            runCmd.add(cmdPart);
            if (cmdPart.equals(Commands.JAVA_BIN)) {
                runCmd.addAll(memory);
            }
        }
        // for native command add memory at the end of the command
        if (isNative) {
            runCmd.addAll(memory);
        }
        return Collections.unmodifiableList(runCmd);
    }
}
