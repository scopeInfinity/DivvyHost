package divvyhost.utils;

import static divvyhost.configuration.Configuration.SIGN_ALGO;
import static divvyhost.configuration.Configuration.SIGN_KEYSIZE;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import divvyhost.utils.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

/**
 *
 * @author scopeinfinity
 */
public class Utils {
    private static final Logger log = Logger.getLogger(Utils.class.getName());
    
    /**
     * Find Checksum for given file
     * @param file
     * @return checkSum
     */
    public static byte[] getChecksum(File file) {
        
        try {
            InputStream is = new FileInputStream(file);
            MessageDigest md = MessageDigest.getInstance("MD5");
            DigestInputStream dis = new DigestInputStream(is, md);
            return md.digest();
        } catch (FileNotFoundException ex) {
            log.severe("File not found : "+file );
        } catch (NoSuchAlgorithmException ex) {
            log.severe("MD5 Algorithm Not found");
        }
        return null;
       
    }
    
    /**
     * Generate MD5 for given Input
     * @param input
     * @return MD5 String
     */
    public static String getMD5(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            return Base64.encode(digest.digest(input));
        } catch (NoSuchAlgorithmException ex) {
            log.severe(ex.toString());
            log.severe("MD5 Hashing Failed");
        }
        return null;
    }
    
    /**
     * Generate RSA Keys
     * @return Pair(PublicKey, PrivateKey)
     */
    public static Pair<PublicKey, PrivateKey> generateDSAKeys() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(SIGN_ALGO);
            generator.initialize(SIGN_KEYSIZE);
            KeyPair pair = generator.genKeyPair();
            PublicKey publicKey = pair.getPublic();
            PrivateKey privateKey = pair.getPrivate();
            log.info(SIGN_ALGO+" Key Generated!");
            return new Pair<PublicKey, PrivateKey>(publicKey, privateKey);
        } catch (NoSuchAlgorithmException ex) {
            log.severe(ex.toString());
        }
        return null;
    }
    
    /**
     * Get PublicKey Object from DSA Encoded Public Key
     * @param publicKey
     * @return PublicKey
     */
    public static PublicKey getDSAPublicKey(byte[] publicKey) {
        try {
            X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(publicKey);
            KeyFactory keyFactory = KeyFactory.getInstance("DSA");
            return keyFactory.generatePublic(encodedKeySpec);
        } catch (NoSuchAlgorithmException ex) {
            log.severe(ex.toString());
        } catch (InvalidKeySpecException ex) {
            log.severe(ex.toString());
        }
        return null;
    }
    
    /**
     * Generate Sign using DSA
     * @param data
     * @param privateKey
     * @return sign
     */
    public static byte[] signData(byte[] data, PrivateKey privateKey) {
        try {
            Signature sign = Signature.getInstance(SIGN_ALGO);
            sign.initSign(privateKey);
            sign.update(data);
            return sign.sign();
        } catch (NoSuchAlgorithmException ex) {
            log.severe(ex.toString());
        } catch (InvalidKeyException ex) {
            log.severe(ex.toString());
        } catch (SignatureException ex) {
            log.severe(ex.toString());
        }
        return null;
    }
    
    /**
     * Verify using DSA
     * @param data
     * @param publicKey
     * @return isAllGood
     */
    public static boolean verifyData(byte[] data, byte[] signature, PublicKey publickey) {
        try {
            Signature sign = Signature.getInstance(SIGN_ALGO);
            sign.initVerify(publickey);
            sign.update(data);
            return sign.verify(signature);
        } catch (InvalidKeyException ex) {
            log.severe(ex.toString());
        } catch (NoSuchAlgorithmException ex) {
            log.severe(ex.toString());
        } catch (SignatureException ex) {
             log.severe(ex.toString());
        }
        return false;
    }
    
    
    
    
    /**
     * Extract InputStream to given Directory
     * @param is
     * @param directory
     * @return isUnzipped
     */
    public static boolean unzip(InputStream is, File directory){
        ZipInputStream zis = new ZipInputStream(is);
         try {
            ZipEntry entry;
            while( (entry = zis.getNextEntry())!=null) {
                String name = entry.getName();
                File target = new File(directory, name);
                if(entry.isDirectory()) {
                    target.mkdirs();
                } else {
                    if(target.getParentFile()!=null)
                        target.getParentFile().mkdirs();
                    
                    FileOutputStream fos = new FileOutputStream(target);
                    byte[] buffer = new byte[10240];
                    int l;
                    while( (l=zis.read(buffer))>0)
                        fos.write(buffer, 0, l);
                    fos.close();
                    log.info("Unzipped File : "+target.toString());
                }
            }
            zis.close();
            return true;
        } catch (IOException ex) {
           log.severe(ex.toString());
        }
        return false;
       
    }
    
    /**
     * Zip directory to byte[]
     * @param directory
     * @return zippedContent
     */
    public static byte[] zip(File directory) {
        if(directory == null || !directory.isDirectory() || !directory.exists()) {
            log.severe("Invalid Directory : "+directory);
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        List<File> allFiles = new ArrayList<File>();
        allFiles.add(directory);
        for(int i=0; i<allFiles.size();i++) {
            File file = allFiles.get(i);
            if(file.isDirectory()) {
                for (File innerFile : file.listFiles()) {
                    allFiles.add(innerFile);
                }
                
            } else {
                String filename = "";
                try {
                    filename = file.getCanonicalPath().substring(directory.getCanonicalPath().length()+1);
                    ZipEntry zipEntry = new ZipEntry(filename);
                    zos.putNextEntry(zipEntry);
                    FileInputStream fis = new FileInputStream(file);
                    byte[] buffer = new byte[10240];
                    int l;
                    while( (l=fis.read(buffer))>0)
                        zos.write(buffer, 0, l);
                    
                } catch (IOException ex) {
                    log.severe("Unable to Add "+filename+" : "+ex.toString());
                }
                
            }
           
        }
        try {
            zos.close();
            return baos.toByteArray();
        } catch (IOException ex) {
            log.severe(ex.toString());
        }
        return null;
    }
    
    /**
     * Try to Empty Directory
     * @param directory 
     */
    public static void emptyDirectory(File directory) {
        if(directory == null || !directory.isDirectory() || !directory.exists()) {
            return;
        }
        List<File> allFiles = new ArrayList<File>();
        allFiles.add(directory);
        for(int i=0; i<allFiles.size();i++) {
            File file = allFiles.get(i);
            if(file.isDirectory()) {
                for (File innerFile : file.listFiles()) {
                    allFiles.add(innerFile);
                }
                
            } else {
                if (!file.delete())
                    log.severe("Unable to Delete "+file);
            }  
        }
        for(int i=0; i<allFiles.size();i++) {
            File file = allFiles.get(i);
            if(file.isDirectory()){
                if (!file.delete())
                    log.severe("Unable to Delete Directory "+file);
            }
                
        }
    }
    
    /**
     * Sum Size of Files just Inside Directory
     * @param dir
     * @return size
     */
    public static long sizeOfFilesInDirectory(File dir) {
        File[] files = dir.listFiles();
        long sum = 0;
        for (File file : files) {
            if(file.isFile()) {
                sum+=file.length();
            }
        }
        return sum;
    }

    /**
     * Get Integer from Unsigned byte
     * @param ubyte
     * @return integer
     */
    public static int getInt(byte ubyte) {
        int num = 0;
        for (int i = 0; i < 8; i++) {
            if( (ubyte & ((byte)1<<i)) != 0)
                num|=1<<i;
        }
        return num;
    }
    
    
    
}