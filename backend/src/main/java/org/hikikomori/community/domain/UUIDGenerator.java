package org.hikikomori.community.domain;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;
import java.util.UUID;

public final class UUIDGenerator {

    private static final TimeBasedEpochGenerator GENERATOR = Generators.timeBasedEpochGenerator();

    private UUIDGenerator() {
    }

    public static UUID generate() {
        return GENERATOR.generate();
    }
}
