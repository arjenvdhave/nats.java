// Copyright 2018 The NATS Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.nats.examples;

import io.nats.client.*;

import java.time.Duration;

public class ExampleUtils {

    public static String getServer(String[] args) {
        if (args.length == 1) {
            return args[0];
        } else if (args.length == 2 && args[0].equals("-s")) {
            return args[1];
        }
        return Options.DEFAULT_URL;
    }

    public static Options createExampleOptions(String[] args) throws Exception {
        String server = getServer(args);
        return createExampleOptions(server, false);
    }

    public static Options createExampleOptions(String[] args, boolean allowReconnect) throws Exception {
        String server = getServer(args);
        return createExampleOptions(server, allowReconnect);
    }

    public static Options createExampleOptions(String server) throws Exception {
        return createExampleOptions(server, false);
    }

    public static Options createExampleOptions(String server, boolean allowReconnect) throws Exception {
        Options.Builder builder = new Options.Builder()
                .server(server)
                .connectionTimeout(Duration.ofSeconds(5))
                .pingInterval(Duration.ofSeconds(10))
                .reconnectWait(Duration.ofSeconds(1))
                .errorListener(new ErrorListener() {
                    public void exceptionOccurred(Connection conn, Exception exp) {
                        System.out.println("Exception " + exp.getMessage());
                    }

                    public void errorOccurred(Connection conn, String type) {
                        System.out.println("Error " + type);
                    }

                    public void slowConsumerDetected(Connection conn, Consumer consumer) {
                        System.out.println("Slow consumer");
                    }
                })
                .connectionListener((conn, type) -> System.out.println("Status change "+type));

            if (!allowReconnect) {
                builder = builder.noReconnect();
            } else {
                builder = builder.maxReconnects(-1);
            }

            if (System.getenv("NATS_NKEY") != null && System.getenv("NATS_NKEY") != "") {
                AuthHandler handler = new ExampleAuthHandler(System.getenv("NATS_NKEY"));
                builder.authHandler(handler);
            } else if (System.getenv("NATS_CREDS") != null && System.getenv("NATS_CREDS") != "") {
                builder.authHandler(Nats.credentials(System.getenv("NATS_CREDS")));
            }

        return builder.build();
    }

    // Publish:   [options] <subject> <message>
    // Request:   [options] <subject> <message>

    // Subscribe: [options] <subject> <msgCount>
    // Reply:     [options] <subject> <msgCount>

    public static ExampleArgs expectSubjectAndMessage(String[] args, String usageString) {
        ExampleArgs ea = new ExampleArgs(args, ExampleArgs.Trail.MESSAGE, usageString);
        if (ea.message == null) {
            usage(usageString);
        }
        return ea;
    }

    public static ExampleArgs expectSubjectAndMsgCount(String[] args, String usageString) {
        ExampleArgs ea = new ExampleArgs(args, ExampleArgs.Trail.COUNT, usageString);
        if (ea.msgCount < 1) {
            usage(usageString);
        }
        return ea;
    }

    public static ExampleArgs expectSubjectQueueAndMsgCount(String[] args, String usageString) {
        ExampleArgs ea = new ExampleArgs(args, ExampleArgs.Trail.COUNT, usageString);
        if (ea.msgCount < 1) {
            usage(usageString);
        }
        return ea;
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    private static void usage(String usageString) {
        System.out.println(usageString);
        System.exit(-1);
    }
}
