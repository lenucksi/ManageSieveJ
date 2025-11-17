/*
 * The MIT License
 *
 * Copyright 2013-2015 "Osric Wilkinson" <osric@fluffypeople.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.fluffypeople.managesieve.examples;

import com.fluffypeople.managesieve.ManageSieveClient;
import com.fluffypeople.managesieve.ManageSieveResponse;
import com.fluffypeople.managesieve.ParseException;
import com.fluffypeople.managesieve.SieveScript;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An example of how to use this package to connect and list scripts held on a
 * server.
 *
 * @author "Osric Wilkinson" &lt;osric@fluffypeople.com&gt;
 */
public class ConnectAndListScripts {

    private static final String SERVER_NAME = "mail.example.com";
    private static final int SERVER_PORT = 4190;
    private static final String USERNAME = "example";
    private static final String PASSWORD = "example1";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        try {

            // Setup a new client
            ManageSieveClient client = new ManageSieveClient();

            // Connect to the server
            ManageSieveResponse resp = client.connect(SERVER_NAME, SERVER_PORT);
            if (!resp.isOk()) {
                throw new IOException("Can't connect to server: " + resp.getMessage());
            }

            // Start SSL connection with proper certificate validation.
            // For self-signed certificates, configure your Java keystore instead of
            // bypassing validation. See: https://docs.oracle.com/javase/tutorial/security/toolsign/rstep2.html
            //
            // WARNING: Never use insecure trust managers in production!
            // The old getInsecureSSLFactory() method has been removed for security reasons.
            resp = client.starttls();
            if (!resp.isOk()) {
                throw new IOException("Can't start SSL:" + resp.getMessage());
            }

            // Authenticate the easy way. If your server does something complicated,
            // look at the other version of authenticate.
            resp = client.authenticate(USERNAME, PASSWORD);
            if (!resp.isOk()) {
                throw new IOException("Could not authenticate: " + resp.getMessage());
            }

            // Create a simple script
            final String scriptBody =
                    "require [\"fileinto\"];\n"
                    + "\n"
                    + "if address :is \"to\" \"managesieve@example.com\"\n"
                    + "{\n"
                    + "    fileinto \"managesieve\";\n"
                    + "}";

            final String scriptName = "MyFirstScript";

            // upload it to the server
            resp = client.putscript(scriptName, scriptBody);
            if (!resp.isOk()) {
                throw new IOException("Can't upload script to server: " + resp.getMessage());
            }

            // and set it active
            resp = client.setactive(scriptName);
            if (!resp.isOk()) {
                throw new IOException("Can't set script [" + scriptName + "] to active: " + resp.getMessage());
            }

            // Create a list to hold the result of the next command
            List<SieveScript> scripts = new ArrayList<SieveScript>();

            // Get the list of this users scripts. The current contents of
            // the list will be deleted first.
            resp = client.listscripts(scripts);
            if (!resp.isOk()) {
                throw new IOException("Could not get list of scripts: " + resp.getMessage());
            }

            // Dump the scripts to STDOUT
            for (SieveScript ss : scripts) {
                System.out.print(ss.getName());
                if (ss.isActive()) {
                    System.out.print(" (active)");
                }
                System.out.print(": ");
                resp = client.getScript(ss);
                if (!resp.isOk()) {
                    throw new IOException("Could not get body of script [" + ss.getName() + "]: " + resp.getMessage());
                }
                System.out.println(ss.getBody());
            }

            // Done. Close the connection.
            resp = client.logout();
            if (!resp.isOk()) {
                // At this poit, there's probably nothing useful to be done to
                // recover from a failed logout, but whe should check anyway.
                throw new IOException("Can't logout: " + resp.getMessage());
            }

        } catch (ParseException ex) {
            System.out.println("The server said something unexpeced:" + ex.getMessage());
        }
    }

    /*
     * SECURITY NOTE:
     *
     * The getInsecureSSLFactory() method has been removed because it bypassed
     * certificate validation, creating a critical security vulnerability (CWE-295).
     *
     * For testing with self-signed certificates, use proper certificate management:
     *
     * 1. Import the certificate into Java's keystore:
     *    keytool -import -alias myserver -file server.crt -keystore $JAVA_HOME/lib/security/cacerts
     *
     * 2. Or create a custom truststore for your application:
     *    keytool -import -alias myserver -file server.crt -keystore mytruststore.jks
     *
     * 3. For production applications (like SieveEditor), implement interactive
     *    certificate trust dialogs at the application level.
     *
     * See: https://docs.oracle.com/javase/tutorial/security/toolsign/rstep2.html
     */
}
