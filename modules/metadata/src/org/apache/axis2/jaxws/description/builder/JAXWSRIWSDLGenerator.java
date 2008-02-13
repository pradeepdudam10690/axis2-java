package org.apache.axis2.jaxws.description.builder;

import com.sun.tools.ws.spi.WSToolsObjectFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.dataretrieval.SchemaSupplier;
import org.apache.axis2.dataretrieval.WSDLSupplier;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.xml.sax.InputSource;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * This class will implement an interface that is defined by the
 * MDQ code. It will be registered within the MDQ framework, and the
 * MDQ code will call this when it finds an application that was
 * deployed without WSDL. This class will use the WsGen tool to
 * generate a WSDL Definition based on the Java source for the application.
 */
public class JAXWSRIWSDLGenerator implements SchemaSupplier, WSDLSupplier {

    private static final Log log = LogFactory.getLog(JAXWSRIWSDLGenerator.class);

    private String classPath;

    private AxisService axisService;
    private boolean init = false;
    private HashMap<String, XmlSchema> docMap;
    private HashMap<String, Definition> wsdlDefMap;

    public JAXWSRIWSDLGenerator(AxisService axisService) {
        this.axisService = axisService;
    }

    /**
     * This method will drive the call to WsGen to generate a WSDL file for
     * applications deployed without WSDL. We will then read this file in from
     * disk and create a Definition. After we are done with the file we will
     * remove it from disk.
     */
    public void generateWsdl(String className, String bindingType) throws
            WebServiceException {

        AxisConfiguration axisConfiguration = axisService.getAxisConfiguration();
        File tempFile = (File) axisConfiguration.getParameterValue(
                Constants.Configuration.ARTIFACTS_TEMP_DIR);
        if (tempFile == null) {
            tempFile = new File(System.getProperty("java.io.tmpdir"), "_axis2");
        }

        Parameter servletConfigParam = axisConfiguration
                .getParameter(HTTPConstants.HTTP_SERVLETCONFIG);

        if (servletConfigParam == null) {
            throw new WebServiceException("Axis2 Can't find ServletConfigParameter");
        }
        Object obj = servletConfigParam.getValue();
        ServletContext servletContext;
        String webBase = null;

        if (obj instanceof ServletConfig) {
            ServletConfig servletConfig = (ServletConfig) obj;
            servletContext = servletConfig.getServletContext();
            webBase = servletContext.getRealPath("/WEB-INF");
        } else {
            throw new WebServiceException("Axis2 Can't find ServletConfig");
        }

        this.classPath = getDefaultClasspath(webBase);
        if (log.isDebugEnabled()) {
            log.debug("For implementation class " + className +
                    " WsGen classpath: " +
                    classPath);
        }
        String localOutputDirectory = tempFile.getAbsolutePath() + className;
        if (log.isDebugEnabled()) {
            log.debug("Output directory for generated WSDL file: " + localOutputDirectory);
        }
        boolean errorOnRead = false;
        try {

            if (log.isDebugEnabled()) {
                log.debug("Generating new WSDL Definition");
            }

            createOutputDirectory(localOutputDirectory);
            WSToolsObjectFactory factory = WSToolsObjectFactory.newInstance();
            String[] arguments = getWsGenArguments(className, bindingType, localOutputDirectory);
            OutputStream os = new ByteArrayOutputStream();
            factory.wsgen(os, arguments);
            os.close();
            wsdlDefMap = readInWSDL(localOutputDirectory);
            if (wsdlDefMap.isEmpty()) {
                throw new Exception("A WSDL Definition could not be generated for " +
                        "the implementation class: " + className);
            }
            docMap = readInSchema(localOutputDirectory);
        }
        catch (Throwable t) {
            String msg =
                    "Error occurred generating WSDL file for Web service implementation class " +
                            "{" + className + "}: {" + t + "}";
            log.error(msg);
            throw new WebServiceException(msg, t);
        }
    }

    /**
     * This will set up the arguments that will be used by the WsGen tool.
     */
    private String[] getWsGenArguments(String className, String bindingType, String localOutputDirectory) throws
            WebServiceException {
        String[] arguments = null;
        if (bindingType == null || bindingType.equals("") || bindingType.equals(
                SOAPBinding.SOAP11HTTP_BINDING) || bindingType.equals(
                SOAPBinding.SOAP11HTTP_MTOM_BINDING)) {
            if (log.isDebugEnabled()) {
                log.debug("Generating WSDL with SOAP 1.1 binding type");
            }
            arguments = new String[]{"-cp", classPath, className, "-keep", "-wsdl:soap1.1", "-d",
                    localOutputDirectory};
        } else if (bindingType.equals(SOAPBinding.SOAP12HTTP_BINDING) || bindingType.equals(
                SOAPBinding.SOAP12HTTP_MTOM_BINDING)) {
            if (log.isDebugEnabled()) {
                log.debug("Generating WSDL with SOAP 1.2 binding type");
            }
            arguments = new String[]{"-cp", classPath, className, "-keep", "-extension",
                    "-wsdl:Xsoap1.2", "-d", localOutputDirectory};
        } else {
            throw new WebServiceException("The binding " + bindingType + " specified by the " +
                    "class " + className + " cannot be used to generate a WSDL. Please choose " +
                    "a supported binding type.");
        }
        return arguments;
    }

    /**
     * This method will be used to create a Definition based on the
     * WSDL file generated by WsGen.
     */
    private HashMap<String, Definition> readInWSDL(String localOutputDirectory) throws Exception {
        List<File> wsdlFiles = getWSDLFiles(localOutputDirectory);
        HashMap<String, Definition> wsdlDefMap = new HashMap<String, Definition>();
        for (File wsdlFile : wsdlFiles) {
            if (wsdlFile != null) {
                try {
                    WSDLFactory wsdlFactory = WSDLFactory.newInstance();
                    WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
                    InputStream is = wsdlFile.toURL().openStream();
                    Definition definition = wsdlReader.readWSDL(localOutputDirectory,
                            new InputSource(is));
                    try {
                        definition.setDocumentBaseURI(wsdlFile.toURI().toString());
                        if (log.isDebugEnabled()) {
                            log.debug("Set base document URI for generated WSDL: " +
                                    wsdlFile.toURI().toString());
                        }
                    }
                    catch (Throwable t) {
                        if (log.isDebugEnabled()) {
                            log.debug("Could not set base document URI for generated " +
                                    "WSDL: " + wsdlFile.getAbsolutePath() + " : " +
                                    t.toString());
                        }
                    }
                    wsdlDefMap.put(wsdlFile.getName().toLowerCase(), definition);
                }
                catch (WSDLException e) {
                    String msg = "Error occurred while attempting to create Definition from " +
                            "generated WSDL file {" + wsdlFile.getName() + "}: {" + e + "}";
                    log.error(msg);
                    throw new Exception(msg);
                }
                catch (IOException e) {
                    String msg = "Error occurred while attempting to create Definition from " +
                            "generated WSDL file  {" + wsdlFile.getName() + "}: {" + e + "}";
                    log.error(msg);
                    throw new Exception(msg);
                }
            }
        }
        return wsdlDefMap;
    }

    /**
     * This method will be used to locate the WSDL file that was
     * generated by WsGen. There should be only one file with the
     * ".wsdl" extension in this directory.
     */
    private List<File> getWSDLFiles(String localOutputDirectory) {
        File classDirectory = new File(localOutputDirectory);
        ArrayList<File> wsdlFiles = new ArrayList<File>();
        if (classDirectory.isDirectory()) {
            File[] files = classDirectory.listFiles();
            for (File file : files) {
                String fileName = file.getName();
                if (fileName.endsWith(".wsdl")) {
                    if (log.isDebugEnabled()) {
                        log.debug("Located generated WSDL file: " + fileName);
                    }
                    wsdlFiles.add(file);
                }
            }
        }
        return wsdlFiles;
    }

    /**
     * This file will create the directory we will use as the output
     * directory argument in our call to WsGen.
     */
    private void createOutputDirectory(String localOutputDirectory) {
        File directory = new File(localOutputDirectory);
        if (!directory.isDirectory()) {
            directory.mkdirs();
        }
    }

    /**
     * This method will read in all of the schema files that were generated
     * for a given application.
     */
    private HashMap<String, XmlSchema> readInSchema(String localOutputDirectory) throws Exception {
        try {

            XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
            schemaCollection.setBaseUri(new File(localOutputDirectory).getAbsolutePath());


            HashMap<String, XmlSchema> docMap = new HashMap<String, XmlSchema>();
            List<File> schemaFiles = getSchemaFiles(localOutputDirectory);
            for (File schemaFile : schemaFiles) {
                XmlSchema doc = schemaCollection.read(new InputSource(schemaFile.toURL().toString()), null);
                if (log.isDebugEnabled()) {
                    log.debug("Read in schema file: " + schemaFile.getName());
                }
                docMap.put(schemaFile.getName(), doc);
            }
            return docMap;
        }
        catch (Exception e) {
            String msg =
                    "Error occurred while attempting to read generated schema file {" + e + "}";
            log.error(msg);
            throw new Exception(msg);
        }
    }

    /**
     * This method will return a list of file objects that represent all the
     * schema files in the current directory.
     */
    private List<File> getSchemaFiles(String localOutputDirectory) {
        ArrayList<File> schemaFiles = new ArrayList<File>();
        File classDirectory = new File(localOutputDirectory);
        if (classDirectory.isDirectory()) {
            File[] files = classDirectory.listFiles();
            for (File file : files) {
                String fileName = file.getName();
                if (fileName.endsWith(".xsd")) {
                    if (log.isDebugEnabled()) {
                        log.debug("Located generated schema file: " + fileName);
                    }
                    schemaFiles.add(file);
                }
            }
        }
        return schemaFiles;
    }

    public Definition getWSDL(AxisService service) throws AxisFault {
        Parameter wsdlParameter = service.getParameter(WSDLConstants.WSDL_4_J_DEFINITION);
        if (wsdlParameter != null) {
            return (Definition) wsdlParameter.getValue();
        }
        initialize();
        return wsdlDefMap.values().iterator().next();
    }

    private synchronized void initialize() {
        String className = (String) axisService.getParameter(Constants.SERVICE_CLASS).getValue();
        if (!init) {
            generateWsdl(className, SOAPBinding.SOAP11HTTP_BINDING);
            init = true;
        }
    }

    public XmlSchema getSchema(AxisService service, String xsd) throws AxisFault {
        initialize();
        XmlSchema schema = docMap.get(xsd);
        if (schema == null) {
            docMap.values().iterator().next();
        }
        return schema;
    }

    /**
     * Expand a directory path or list of directory paths (File.pathSeparator
     * delimited) into a list of file paths of all the jar files in those
     * directories.
     *
     * @param dirPaths The string containing the directory path or list of
     *                 directory paths.
     * @return The file paths of the jar files in the directories. This is an
     *         empty string if no files were found, and is terminated by an
     *         additional pathSeparator in all other cases.
     */
    public static String expandDirs(String dirPaths) {
        StringTokenizer st = new StringTokenizer(dirPaths, File.pathSeparator);
        StringBuffer buffer = new StringBuffer();
        while (st.hasMoreTokens()) {
            String d = st.nextToken();
            File dir = new File(d);
            if (dir.isDirectory()) {
                File[] files = dir.listFiles(new JavaArchiveFilter());
                for (int i = 0; i < files.length; i++) {
                    buffer.append(files[i]).append(File.pathSeparator);
                }
            }
        }
        return buffer.toString();
    }

    /**
     * Check if this inputstream is a jar/zip
     *
     * @param is
     * @return true if inputstream is a jar
     */
    public static boolean isJar(InputStream is) {
        try {
            JarInputStream jis = new JarInputStream(is);
            if (jis.getNextEntry() != null) {
                return true;
            }
        } catch (IOException ioe) {
        }
        return false;
    }

    /**
     * Get the default classpath from various thingies in the message context
     *
     * @param msgContext
     * @return default classpath
     */
    public static String getDefaultClasspath(String webBase) {
        HashSet classpath = new HashSet();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        fillClassPath(cl, classpath);

        // Just to be safe (the above doesn't seem to return the webapp
        // classpath in all cases), manually do this:
        if (webBase != null) {
            addPath(classpath, webBase + File.separatorChar + "classes");
            try {
                String libBase = webBase + File.separatorChar + "lib";
                File libDir = new File(libBase);
                String[] jarFiles = libDir.list();
                for (int i = 0; i < jarFiles.length; i++) {
                    String jarFile = jarFiles[i];
                    if (jarFile.endsWith(".jar")) {
                        addPath(classpath, libBase +
                                File.separatorChar +
                                jarFile);
                    }
                }
            } catch (Exception e) {
                // Oh well.  No big deal.
            }
        }

        // axis.ext.dirs can be used in any appserver
        getClassPathFromDirectoryProperty(classpath, "axis.ext.dirs");

        // classpath used by Jasper 
        getClassPathFromProperty(classpath, "org.apache.catalina.jsp_classpath");

        // websphere stuff.
        getClassPathFromProperty(classpath, "ws.ext.dirs");
        getClassPathFromProperty(classpath, "com.ibm.websphere.servlet.application.classpath");

        // java class path
        getClassPathFromProperty(classpath, "java.class.path");

        // Load jars from java external directory
        getClassPathFromDirectoryProperty(classpath, "java.ext.dirs");

        // boot classpath isn't found in above search
        getClassPathFromProperty(classpath, "sun.boot.class.path");
        
        StringBuffer path = new StringBuffer();
        for (Iterator iterator = classpath.iterator(); iterator.hasNext();) {
            String s = (String) iterator.next();
            path.append(s);
            path.append(File.pathSeparatorChar);
        }
        log.info(path);
        return path.toString();
    }

    private static void addPath(HashSet classpath, String s) {
        String path = s.replace(((File.separatorChar == '/') ? '\\' : '/'), File.separatorChar).trim();
        File file = new File(path);
        if (file.exists()) {
            path = file.getAbsolutePath();
            classpath.add(path);
        }
    }

    /**
     * Add all files in the specified directory to the classpath
     *
     * @param classpath
     * @param property
     */
    private static void getClassPathFromDirectoryProperty(HashSet classpath, String property) {
        String dirs = System.getProperty(property);
        String path = null;
        try {
            path = expandDirs(dirs);
        } catch (Exception e) {
            // Oh well.  No big deal.
        }
        if (path != null) {
            addPath(classpath, path);
        }
    }

    /**
     * Add a classpath stored in a property.
     *
     * @param classpath
     * @param property
     */
    private static void getClassPathFromProperty(HashSet classpath, String property) {
        String path = System.getProperty(property);
        if (path != null) {
            addPath(classpath, path);
        }
    }

    /**
     * Walk the classloader hierarchy and add to the classpath
     *
     * @param cl
     * @param classpath
     */
    private static void fillClassPath(ClassLoader cl, HashSet classpath) {
        while (cl != null) {
            if (cl instanceof URLClassLoader) {
                URL[] urls = ((URLClassLoader) cl).getURLs();
                for (int i = 0; (urls != null) && i < urls.length; i++) {
                    String path = urls[i].getPath();
                    //If it is a drive letter, adjust accordingly.
                    if (path.length() >= 3 && path.charAt(0) == '/' && path.charAt(2) == ':')
                        path = path.substring(1);
                    addPath(classpath, URLDecoder.decode(path));

                    // if its a jar extract Class-Path entries from manifest
                    File file = new File(urls[i].getFile());
                    if (file.isFile()) {
                        FileInputStream fis = null;
                        try {
                            fis = new FileInputStream(file);
                            if (isJar(fis)) {
                                JarFile jar = new JarFile(file);
                                Manifest manifest = jar.getManifest();
                                if (manifest != null) {
                                    Attributes attributes = manifest.getMainAttributes();
                                    if (attributes != null) {
                                        String s = attributes.getValue(Attributes.Name.CLASS_PATH);
                                        String base = file.getParent();
                                        if (s != null) {
                                            StringTokenizer st = new StringTokenizer(s, " ");
                                            while (st.hasMoreTokens()) {
                                                String t = st.nextToken();
                                                addPath(classpath, base + File.separatorChar + t);
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (IOException ioe) {
                        } finally {
                            if (fis != null) {
                                try {
                                    fis.close();
                                } catch (IOException ioe2) {
                                }
                            }
                        }
                    }
                }
            }
            cl = cl.getParent();
        }
    }

    /**
     * Filter for zip/jar
     */
    private static class JavaArchiveFilter implements FileFilter {
        public boolean accept(File file) {
            String name = file.getName().toLowerCase();
            return (name.endsWith(".jar") || name.endsWith(".zip"));
        }
    }
}