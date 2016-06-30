package divvyhost.utils;

import com.sun.istack.internal.logging.Logger;
import static divvyhost.configuration.Configuration.RSA_KEYSIZE;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;

/**
 *
 * @author scopeinfinity
 */
public class utils {
    private static final Logger log = Logger.getLogger(Paths.class);
    
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
     * Generate RSA Keys
     * @return Pair(PublicKey, PrivateKey)
     */
    public static Pair<byte[], byte[]> generateRSAKeys() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(RSA_KEYSIZE);
            KeyPair pair = generator.genKeyPair();
            Key publicKey = pair.getPublic();
            Key privateKey = pair.getPrivate();
            return new Pair(publicKey.getEncoded(), privateKey.getEncoded());
        } catch (NoSuchAlgorithmException ex) {
            log.severe(ex.toString());
        }
        return null;
    }
    
    /**
     * @param data
     * @param privateKey
     * @return encodedData
     */
    public static byte[] encodeRSA(byte[] data, byte[] privateKey) throws RuntimeException {
        throw new RuntimeException("Not Done!");
    }
    
    /**
     * @param data
     * @param publicKey
     * @return decodedData
     */
    public static byte[] decodeRSA(byte[] data, byte[] publickey) throws RuntimeException {
        throw new RuntimeException("Not Done!");
    }
    
    
    
    
    /**
     * Extract InputStream to given Directory
     * @param is
     * @param directory
     * @return
     * @throws Exception 
     */
    public static boolean unzip(InputStream is, File directory) throws RuntimeException {
        throw new RuntimeException("Not Done!");
    }
    
}