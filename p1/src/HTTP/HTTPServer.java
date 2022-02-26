package HTTP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HTTPServer {
    enum config {
        DEFAULT_PATH("/home/danielquintillanquintillan/NetBeansProjects/p1/files"),
        PORT("5000"),
        DIRECTORY_INDEX("welcome.html"),
        DIRECTORY("/home/danielquintillanquintillan/NetBeansProjects/p1/files"),
        ALLOW("false");

        private final String value;

        config(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }
    private ServerSocket server;
    private Socket client;
    private ServerThread request;
    private final int wait = 300000;
    public static int port;
    public static String index;
    public static String root;
    public static boolean allow;
    public static String HTTPVersion;
    public static String name;
    public static String html400;
    public static String html404;
    public static String html403;
    public static String accessLog;
    public static String errorLog;

    public HTTPServer() {
        try {
            configure(config.DEFAULT_PATH.getValue());
            this.server = new ServerSocket(port);
            this.server.setSoTimeout(this.wait);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public HTTPServer(String configPath) {
        try {
            configure(configPath);
            this.server = new ServerSocket(port);
            this.server.setSoTimeout(this.wait);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void listen() {
        try {
            while (true) {
                // Aceptamos una nueva conexión
                this.client = this.server.accept();

                // Creamos un hilo de ejecución para la petición
                this.request = new ServerThread(this.client);

                // Atendemos la petición
                this.request.start();
            }

        } catch (SocketTimeoutException e) {
            System.err.println("Server waiting time has been exceeded.");            
        } catch (SocketException e) {
            System.err.println(e.getMessage());

        } catch (IOException e) {
            throw new RuntimeException(e);

        } finally {
            try {
                this.server.close();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void configure(String configPath) {
        Properties prop = new Properties();
        File fConfig;
        FileInputStream fis = null;
        FileOutputStream fos = null;
        String tmp;

        try {
            if (!configPath.endsWith("/")) {
                configPath += "/";
            }

            fConfig = new File(configPath + "server.properties");
            fConfig.createNewFile();
            fis = new FileInputStream(fConfig);
            prop.load(fis);

            while ((tmp = prop.getProperty(config.PORT.name())) == null) {
                prop.setProperty(config.PORT.name(), config.PORT.getValue());
            }

            port = Integer.parseInt(tmp);

            while ((index = prop.getProperty(config.DIRECTORY_INDEX.name())) == null) {
                prop.setProperty(config.DIRECTORY_INDEX.name(), config.DIRECTORY_INDEX.getValue());
            }

            if (index.startsWith("/")) {
                index = index.substring(1);
            }

            while ((root = prop.getProperty(config.DIRECTORY.name())) == null) {
                prop.setProperty(config.DIRECTORY.name(), config.DIRECTORY.getValue());
            }

            if (root.endsWith("/")) {
                root = root.substring(0, root.length() - 1);
            }

            while ((tmp = prop.getProperty(config.ALLOW.name())) == null) {
                prop.setProperty(config.ALLOW.name(), config.ALLOW.getValue());
            }

            allow = Boolean.parseBoolean(tmp);

            fos = new FileOutputStream(fConfig);
            prop.store(fos, "Propiedades del servidor");

            HTTPVersion = "HTTP/1.1";
            name = "Developed by Daniel Quintillán";
            html400 = configPath + "error400.txt";
            html404 = configPath + "error404.txt";
            html403 = configPath + "error403.txt";
            accessLog = configPath + "access.log";
            errorLog = configPath + "errors.log";

        } catch (IOException e) {
            throw new RuntimeException(e);

        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (fos != null) {
                    fos.close();
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

        public static void main (String[] args) {
        HTTPServer server;
         
        if (args.length > 0)
            if (args.length == 1 && new File(args[0]).exists())
                server = new HTTPServer(args[0]);
            else{
                //throw new IllegalArgumentException();
                System.err.println("FORMAT: HTTPSERVER [CONFIG_PATH]");
                 
                return;
            }
        else
            server = new HTTPServer();
         
        server.listen();
    }
}
