package divvyhost.host;

import static divvyhost.configuration.Configuration.WEB_PORT;
import divvyhost.project.Details;
import divvyhost.project.Project;
import divvyhost.project.ProjectManager;
import divvyhost.utils.Paths;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author scopeinfinity
 */
public class Host {
    private static final Logger log = Logger.getLogger(Host.class.getName());
    
    private ProjectManager manager;
    private File hostDir;
    
    private static String currentOS;
    private static String INDEXHTML_REPLACER = "[[PROJECT_LIST]]";
    
    public Host(ProjectManager manager) {
        Paths paths = new Paths();
        this.manager = manager;
        manager.setHoster(this);
        hostDir = paths.getHostDir();
    }
    
    /**
     * Start HTML Hosting Server
     * @return isStarted
     */
    public void start() {
        
        log.info("Starting Hosting Server");
        ArrayList<Script> list = new ArrayList<>();
        list.add(new Script("run.sh", "cd "+hostDir.getAbsolutePath()+"; python -m SimpleHTTPServer "+WEB_PORT+"", "Linux"));
        list.add(new Script("run.bat", "cd "+hostDir.getAbsolutePath()+"; python -m SimpleHTTPServer "+WEB_PORT+"", "Windows"));

        for (Script script : list) {
            if(script.create(hostDir))
            {
                final Script _script = script;
                new Thread(script.OS) {

                    @Override
                    public void run() {
                        if(_script.execute())
                        {
                            log.info("Starting Hosting Script Called for "+_script.OS);
                        }
                        
                    }
                    
                }.start();
            }
        }
            
      
        log.severe("Starting Hosting Failed");
    }
    
    /**
     * Filter to Prevent HTML and JavaScript Injection
     * @param str
     * @return 
     */
    private String htmlFilter(String str) {
        return str.replaceAll("</", "&lt;").replaceAll(">/", "&gt;");
    }
    
    /**
     * Create Welcome Page
     * @return isCreated
     */
    public boolean createMainPage() {
        log.info("Creating Main Page");
        List<Project> list = manager.listCompleteDetails();
        StringBuilder sb = new StringBuilder();
        String securityTagStart = "";
        String securityTagEnd = "";
        
        for (Project project : list) {
            sb.append("<project><name><a href=\"./")
                    .append(htmlFilter(project.getDetails().getFileName()))
                    .append("\">"+securityTagStart)
                    .append(htmlFilter(project.getData().getTitle()))
                    .append(securityTagEnd+"</a></name><desc>"+securityTagStart)
                    .append(htmlFilter(project.getData().getDescription()))
                    .append(securityTagEnd+"</desc></project>\n");
        }
        String content = INDEX_HTML.replace(INDEXHTML_REPLACER, sb.toString());
        File index_html = new File(hostDir,"index.html");
        try {
            FileOutputStream fos = new FileOutputStream(index_html);
            fos.write(content.getBytes());
            fos.close();
            log.info("Main Page Created");
        
            return true;
        } catch (FileNotFoundException ex) {
            log.severe(ex.toString());
        } catch (IOException ex) {
            log.severe(ex.toString());
        }
        log.info("Main Page Createaion Failed!");
        
        return false;
    }
    
    /**
     * Script for Host Execution
     */
    private class Script {
        private String filename;
        private String code;
        private String OS;
        private File file;
        
            
        public Script(String filename, String code, String OS) {
            this.filename = filename;
            this.code = code;
            this.OS = OS;
        }

        public boolean create(File tempDir) {
            try {
                file = new File(tempDir, filename);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(code.getBytes());
                fos.close();
                log.info(filename+" Script Created!");
                return true;
            } catch (FileNotFoundException ex) {
                log.severe(ex.toString());
            } catch (IOException ex) {
                log.severe(ex.toString());
            }
            log.severe(filename+" Script Create Failed");
            return false;
        }
        
        public boolean execute() {
            Runtime runtime = Runtime.getRuntime();
            String prefix = checkOS();
            if(prefix==null) {
                log.severe("OS "+OS+" is Not Running!");
                return false;
            }
            try {
                String escapedFilename=file.getAbsolutePath().replaceAll(" ", "\\ ");
                String cmd = prefix+" "+escapedFilename+"";
                Process process = runtime.exec(cmd);
                
                log.info("Wait For "+process.waitFor()+", Exit Value"+process.exitValue());
                log.info("Executing "+cmd);
                return true;
            } catch (Exception ex) {
                log.severe(filename+" Script Execution Failed\n"+ex);
            }
            log.severe("Hosting Failed for "+OS);
            return false;
        }
        
        private String checkOS() {
            if(currentOS==null) {
                currentOS = System.getProperty("os.name");
                log.info("Current OS : "+currentOS);
            }
            if (currentOS.toLowerCase().startsWith("linux") && OS.equalsIgnoreCase("Linux")) 
                return "sh";
            else if (currentOS.toLowerCase().startsWith("window") && OS.equalsIgnoreCase("Windows")) 
                return "cmd";
            else
                return null;
        }
    }
    
    
    private static String INDEX_HTML = 
"<!DOCTYPE html>\n" +
"<html>\n" +
"<head>\n" +
"	<title>DivvyHost</title>\n" +
"	<meta charset=\"UTF-8\"> \n" +
"</head>\n" +
"<body>\n" +
"<style type=\"text/css\">\n" +
"project{\n" +
"	display: block;\n" +
"	background: #ccff99;\n" +
"	margin: 10px;\n" +
"	padding: 10px;\n" +
"	word-wrap: break-word;\n" +
"	\n" +
"}\n" +
"name{\n" +
"	display: block;\n" +
"	font-weight: bold;\n" +
"	margin-left: 10px;\n" +
"	margin-right: 10px;\n" +
"	margin-top: 5px;\n" +
"	margin-bottom: 5px;\n" +
"	max-height: 1.5em;\n" +
"	overflow: hidden;\n" +
"}\n" +
"desc{\n" +
"	display: block;\n" +
"	margin-left: 10px;\n" +
"	max-height: 4em;\n" +
"	overflow: hidden;\n" +
"}\n" +
"body{\n" +
"	background: linear-gradient(\n" +
"  to bottom,\n" +
"  #5d9634,\n" +
"  #5d9634 50%,\n" +
"  #538c2b 50%,\n" +
"  #538c2b\n" +
");\n" +
"}\n" +
"</style>\n" +
"<h3 style=\"background: #669900;left: 0;right: 0\">Shared Projects</h3>\n" +
"<!--\n" +
"<project><name><a href=\"./uuid/\">Project Name</a></name><desc>This is project description</desc></project>\n" +
"-->\n" + INDEXHTML_REPLACER +
"<main>\n" +
"</main>\n" +
"</body>\n" +
"</html>";
    
}
