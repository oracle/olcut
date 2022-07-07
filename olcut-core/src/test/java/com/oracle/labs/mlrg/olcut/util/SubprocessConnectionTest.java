package com.oracle.labs.mlrg.olcut.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class SubprocessConnectionTest {

    SubprocessConnection subproc;
    String process;
    List<String> work;
    List<String> expectedResponse;

    @BeforeEach
    public void setup() {
        // TODO I know this isn't the correct way to do this
        process = String.format("python %s/src/test/resources/com/oracle/labs/mlrg/olcut/util/testProcess.py", System.getProperty("user.dir"));
        work = Arrays.asList("part_a", "part_b", "part_c", "part_d");
        expectedResponse = Arrays.asList("0:10:part_a", "1:15:part_b", "2:27:part_c", "3:38:part_d");
        subproc = new SubprocessConnection(process);
        subproc.addSubprocessListener(new SubprocessConnectionListener() {
            private int started=0;
            private int preShutdown=0;
            private int postShutdown=0;

            @Override
            public void subprocessStarted(SubprocessConnection connection) {
                started++;
                System.err.println("Subprocess started times: "+ started);
            }

            @Override
            public void subprocessPreShutdown(SubprocessConnection connection) {
                preShutdown++;
                System.err.println("Subprocess pre shutdown times: " +preShutdown);
            }

            @Override
            public void subprocessPostShutdown(SubprocessConnection connection) {
                postShutdown++;
                System.err.println("Subprocess post shutdown times: " + postShutdown);
            }
        });
    }

    @Test
    public void testNoTimeout() throws IOException,TimeoutException {
        for(int i=0; i< 4; i++) {
            assertEquals(expectedResponse.get(i), subproc.run(work.get(i)));
        }
    }

    @Test
    public void testReadTimeout() {

        subproc.setReadTimeout(7, TimeUnit.SECONDS);

        AtomicReference<String> resp = new AtomicReference<>();
        assertDoesNotThrow(() -> {
                    resp.set(subproc.run("part_b"));
                }
        );
        assertEquals("0:5:part_b", resp.get());

        assertThrows(TimeoutException.class,() -> {
            subproc.run("part_a");
        });
    }

    @Test
    public void testMultipleReadTimeouts() {

        subproc.setReadTimeout(7, TimeUnit.SECONDS);

        assertDoesNotThrow(() -> {
            for(String work: Arrays.asList("part_a", "part_d", "part_c", "part_a")) {
                try {
                    subproc.run(work);
                } catch (TimeoutException e) {
                    System.err.println("Expected TimeoutException");
                }
            }
        });

        AtomicReference<String> resp = new AtomicReference<>();
        assertDoesNotThrow(() -> {
            resp.set(subproc.run("part_b"));
        });

        assertEquals("0:5:part_b",resp.get() );
    }
}
