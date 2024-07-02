package com.mreugenej7.opencf.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum LaunchMethod {
    EXECUTABLE("<location>"),
    JAVA_JAR("java -jar <location>"),
    MPI("mpirun -np <param[np]> <location>");

    final String launchCommand;
}
