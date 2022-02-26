package HTTP;

import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Thread attending a connection of the TCP echo server.
 */
public class ServerThread extends Thread {

    private class Headers {

        private Date cDate;
        private String cHost;

        public Date getDate() {
            return this.cDate;
        }

        public void setDate(Date cDate) {
            this.cDate = cDate;
        }

        public String getHost() {
            return this.cHost;
        }

        public void setHost(String cHost) {
            this.cHost = cHost;
        }
    }
    private final Socket client;
    private String requestLine;
    private final BufferedReader input;
    private final OutputStream output;
    private final Headers args;

    public ServerThread(Socket s) {
        // Store the client, so it can be used in the run() method
        this.client = s;
        this.args = new Headers();
        try {
            // Set the input stream
            this.input = new BufferedReader(new InputStreamReader(client.getInputStream()));
            // Set the output stream
            this.output = client.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        String line;
        ArrayList<String> headerLines = new ArrayList<>();
        try {
            while (true) {
                try {
                    client.setSoTimeout(60000);
                    this.requestLine = input.readLine();
                    if (this.requestLine != null) {
                        System.out.println(this.requestLine);

                        do {
                            line = input.readLine();
                            headerLines.add(line);
                            System.out.println(line);
                        } while (!line.isEmpty());
                        process(headerLines);
                        headerLines.clear();
                        if (this.requestLine.split(" ")[2].equals("HTTP/1.0") || this.requestLine.split(" ")[2].equals("HTTP/1.1")) {
                            switch (this.requestLine.split(" ")[0]) {
                                case "GET":
                                    httpGET();
                                    break;
                                case "HEAD":
                                    httpHEAD();
                                    break;
                                default:
                                    answer("400 BAD REQUEST", null, true);
                            }
                        }
                        else answer("400 BAD REQUEST", null, true);
                    }
                } catch (SocketTimeoutException a) {
                    try {
                        this.input.close();
                        this.output.close();
                        this.client.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

        } catch (SocketException e) {
            try {
                this.input.close();
                this.output.close();
                this.client.close();
                System.err.println("Request timeout exceeded");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void process(ArrayList<String> headers) {
        SimpleDateFormat sdf;

        for (String header : headers) {
            switch (header.split(": ")[0]) {
                case "If-Modified-Since":
                    try {
                        sdf = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", new Locale("english"));
                        this.args.setDate(sdf.parse(header.split(": ")[1]));
                    } catch (ParseException e) {
                        System.err.println(e);
                    }
                    break;
                case "Host":
                    this.args.setHost(header.split(": ")[1]);

            }
        }
    }

    private void httpGET() {
        String path = this.requestLine.split(" ")[1];
        File resource = new File(HTTPServer.root + path), resource2;
        Date ServerModDate = new Date(resource.lastModified()), ClientModDate;
        String URL;

        if (path.contains(".do?")) {
            String[] pieces = path.split("\\.do?");
            String cl = "HTTP." + pieces[0].split("HTTP/")[1];
            pieces[1] = pieces[1].substring(1, pieces[1].length());
            String[] param = pieces[1].split("&");
            Map<String, String> mp = new HashMap<>();
            for (String pair : param) {
                String[] entry = pair.split("=");
                mp.put(entry[0], entry[1]);
            }
            try {
                PrintWriter pr = new PrintWriter(output, true);
                String body = ServerUtils.processDynRequest(cl, mp);
                String state = HTTPServer.HTTPVersion + " "
                        + "200 OK\n";
                Date dateObj = new Date();
                DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss zzz", new Locale("en"));
                String date = ("Date/Time: " + sdf.format(dateObj) + "\n");
                String contentLength = ("Bytes sent: " + body.length() + "\n");
                String contentType = ("Content type: texthtml \n\n");
                pr.println(state + date + contentLength + contentType);
                pr.println(body);
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        } else if (resource.exists()) {
            if (resource.isDirectory()) {
                if (!path.endsWith("/")) {
                    path += "/";
                }

                resource2 = new File(HTTPServer.root + path + HTTPServer.index);

                if (resource2.exists()) {
                    answer("200 OK", resource2, true);
                } else if (HTTPServer.allow) {
                    URL = "http://" + this.args.getHost() + path;
                    link(resource.list(), URL);
                } else {
                    answer("403 FORBIDDEN", null, true);
                }
            } else {
                ClientModDate = this.args.getDate();
                if (ClientModDate == null || ServerModDate.after(ClientModDate)) {
                    answer("200 OK", resource, true);
                } else {
                    answer("304 NOT MODIFIED", null, false);
                }
            }
        } else {
            answer("404 NOT FOUND", null, true);
        }
    }

    private void httpHEAD() {
        String path = this.requestLine.split(" ")[1];
        File resource = new File(HTTPServer.root + path);

        if (resource.exists()) {
            answer("200 OK", resource, false);
        } else {
            answer("404 NOT FOUND", null, false);
        }
    }

    private void answer(String state, File resource, boolean body) {
        PrintWriter pr = new PrintWriter(output, true);
        String stateLine;

        // Escogemos el estado a enviar
        switch (state) {
            case "200 OK":
                stateLine = HTTPServer.HTTPVersion + " "
                        + "200 OK";

                pr.println(stateLine);

                // Enviamos las cabeceras
                pr.println("Server: " + HTTPServer.name);
                pr.println("Date: " + new Date());
                if (resource != null) {
                    try {
                        pr.println("Content-Type: " + Files.probeContentType(resource.toPath()));
                    } catch (IOException ex) {
                        System.err.println(ex.getMessage());
                    }
                    pr.println("Content-Length: " + resource.length());
                    pr.println("Last-Modified: " + new Date(resource.lastModified()));
                }

                break;
            case "400 BAD REQUEST":
                stateLine = HTTPServer.HTTPVersion + " "
                        + "400 BAD REQUEST";

                pr.println(stateLine);
                pr.println("Date: " + new Date());
                resource = new File(HTTPServer.html400);
                pr.println("Date: " + new Date());
                pr.println("Server: " + HTTPServer.name);
                try {
                    pr.println("Content-Type: " + Files.probeContentType(resource.toPath()));
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                }
                pr.println("Content-Length: " + resource.length());

                break;
            case "404 NOT FOUND":
                stateLine = HTTPServer.HTTPVersion + " "
                        + "404 NOT FOUND";

                pr.println(stateLine);
                pr.println("Date: " + new Date());
                resource = new File(HTTPServer.html404);

                pr.println("Server: " + HTTPServer.name);
                try {
                    pr.println("Content-Type: " + Files.probeContentType(resource.toPath()));
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());

                }
                resource = new File(HTTPServer.html404);
                pr.println("Content-Length: " + resource.length());

                break;
            case "304 NOT MODIFIED":
                stateLine = HTTPServer.HTTPVersion + " "
                        + "304 NOT MODIFIED";

                pr.println(stateLine);
                pr.println("Date: " + new Date());
                //just to initialize it
                resource = new File(HTTPServer.html404);
                pr.println("Server: " + HTTPServer.name);
                break;
            case "403 FORBIDDEN":
                stateLine = HTTPServer.HTTPVersion + " "
                        + "403 FORBIDDEN";

                pr.println(stateLine);
                pr.println("Date: " + new Date());
                resource = new File(HTTPServer.html403);
                pr.println("Server: " + HTTPServer.name);
                try {
                    pr.println("Content-Type: " + Files.probeContentType(resource.toPath()));
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());

                }
                pr.println("Content-Length: " + resource.length());
                resource = new File(HTTPServer.html403);

                break;
            default:
                throw new IllegalArgumentException();
        }

        // Enviamos la linea en blanco
        pr.println();

        // Transferimos el cuerpo de entidad (o no)
        if (body) {
            transfer(resource);
        }

        writeLog(stateLine, resource);
    }

    private void link(String[] documents, String URL) {
        String links = "";

        links += "<html>"
                + "<head></head>"
                + "<body>"
                + "<ul>";

        for (String document : documents) {
            links += "<li>"
                    + "<a href=\"" + URL + document + "\">" + document + "</a>"
                    + "</li>";
        }

        links += "</ul>"
                + "</body>"
                + "</html>";

        try {
            answer("200 OK", null, false);
            this.output.write(links.getBytes());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void transfer(File resource) {
        FileInputStream fis = null;
        byte[] kbyte = new byte[1024];
        int tam;

        try {
            fis = new FileInputStream(resource);
            while ((tam = fis.read(kbyte)) != -1) {
                this.output.write(kbyte, 0, tam);
            }

        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());

        } catch (IOException e) {
            throw new RuntimeException(e);

        } finally {
            if (fis != null) {
                try {
                    fis.close();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void writeLog(String stateLine, File resource) {
        File log;
        PrintWriter pw;
        String code = stateLine.split(" ")[1];

        try {
            if (code.startsWith("2") || code.startsWith("3")) {
                log = new File(HTTPServer.accessLog);
                pw = new PrintWriter(new FileOutputStream(log, true), true);

                pw.println("Request: " + this.requestLine);
                pw.println("IP: " + this.client.getInetAddress().getHostName());
                Date dateObj = new Date();
                DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss zzz", new Locale("en"));
                pw.println("Date/Time: " + sdf.format(dateObj));
                pw.println("State code: " + code);
                if (resource != null) {
                    pw.println("Bytes sent: " + resource.length());
                }
                pw.println();
            } else if (code.startsWith("4")) {
                log = new File(HTTPServer.errorLog);
                pw = new PrintWriter(new FileOutputStream(log, true), true);

                pw.println("Request: " + this.requestLine);
                pw.println("IP: " + this.client.getInetAddress().getHostName());
                Date dateObj = new Date();

                DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss zzz", new Locale("en"));
                pw.println("Date/Time: " + sdf.format(dateObj));
                pw.println("Message: " + stateLine);
                pw.println();
            }

        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        }
    }
}
