package divvyhost.utils;


import java.io.File;
import java.util.logging.Logger;

/**
 * Handles Directory Paths 
 * @author scopeinfinity
 */
public class Paths {
    private static final Logger log = Logger.getLogger(Paths.class.getName());
    private File rootDir, projectsDir, hostDir, confDir;

    public Paths() {
        rootDir = new File(System.getProperty("user.dir"));
        projectsDir = new File(rootDir,"Projects");
        hostDir = new File(rootDir,"Hosted");
        confDir = new File(rootDir,"Conf");
        projectsDir.mkdirs();
        hostDir.mkdirs();
        confDir.mkdirs();
        if(projectsDir.isDirectory() && hostDir.isDirectory() && confDir.isDirectory())
            log.info("Directories Ready : "+rootDir);
        else
            log.severe("Directories Prepartion Failed : "+rootDir);
        
    }

    public File getRootDir() {
        return rootDir;
    }

    public File getConfDir() {
        return confDir;
    }

    public File getProjectsDir() {
        return projectsDir;
    }

    public File getHostDir() {
        return hostDir;
    }
    
    
    
}
