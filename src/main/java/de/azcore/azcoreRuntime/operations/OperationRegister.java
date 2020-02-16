/*
 * Copyright (C) 2020. Niklas Linz - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the LGPLv3 license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the LGPLv3 license with
 * this file. If not, please write to: niklas.linz@enigmar.de
 *
 */

package de.azcore.azcoreRuntime.operations;

import de.azcore.azcoreRuntime.operations.defaultOperations.AZCoreOperations;
import de.azcore.azcoreRuntime.operations.defaultOperations.LinuxOperations;
import de.linzn.openJL.pairs.Pair;

import java.util.ArrayList;

public class OperationRegister {
    private static ArrayList<Pair<String, TaskOperation>> operations;

    static {
        operations = new ArrayList<>();
    }

    static {
        addOperation("run_linux_shell", LinuxOperations.run_linux_shell);
        addOperation("azcore_restart", AZCoreOperations.azCore_restart);
    }

    public static void addOperation(String key, TaskOperation operation) {
        operations.add(new Pair<>(key, operation));
    }

    public static TaskOperation getOperation(String operationKey) {
        return operations.stream()
                .filter(operationPair -> operationPair.getKey().equalsIgnoreCase(operationKey))
                .findFirst()
                .map(Pair::getValue)
                .orElse(null);
    }

}
