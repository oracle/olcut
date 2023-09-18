/*
 * Copyright (c) 2004-2022, Oracle and/or its affiliates.
 *
 * Licensed under the 2-clause BSD license.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.oracle.labs.mlrg.olcut.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisabledOnOs(OS.WINDOWS)
public class SubprocessConnectionTest {

    SubprocessConnection subproc;
    String process;
    List<String> work;
    List<String> expectedResponse;

    SubprocessConnection multiSubProc;

    @BeforeEach
    public void setup() {
        // TODO I know this isn't the correct way to do this
        process = String.format("python3 %s/src/test/resources/com/oracle/labs/mlrg/olcut/util/testProcess.py", System.getProperty("user.dir"));
        work = Arrays.asList("part_a", "part_b", "part_c", "part_d");
        expectedResponse = Arrays.asList("0:10:part_a\n", "1:15:part_b\n", "2:27:part_c\n", "3:38:part_d\n");
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

        multiSubProc = new SubprocessConnection(String.format("python3 %s/src/test/resources/com/oracle/labs/mlrg/olcut/util/testMultipleProcess.py", System.getProperty("user.dir")));
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
        assertEquals("0:5:part_b\n", resp.get());

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

        assertEquals("0:5:part_b\n",resp.get() );
    }

    @Test
    public void testMultipleReturnedRows() {
        List<String> responses = new ArrayList<>();
        assertDoesNotThrow(() -> {
            for (String piece : work) {
                responses.addAll(Arrays.asList(multiSubProc.run(piece).split("\n")));
            }
        });
        List<String> expected = new ArrayList<>();
        int[] expectNum = {10, 15, 27, 38};
        String[] expectPart = {"a", "b", "c", "d"};
        for (int cnt = 0; cnt < 4; cnt++) {
            for (int i = 0; i < expectNum[cnt]; i++) {
                expected.add(String.format("%d:%d:part_%s", cnt, i, expectPart[cnt]));
            }
        }
        assertEquals(expected, responses);
    }
}
