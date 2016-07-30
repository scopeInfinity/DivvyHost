package divvyhost.host;

import static divvyhost.configuration.Configuration.WEB_PORT;
import divvyhost.project.ProjectManager;
import divvyhost.utils.Paths;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
     * Create Welcome Page
     * @return isCreated
     */
    public boolean createMainPage() {
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
}
