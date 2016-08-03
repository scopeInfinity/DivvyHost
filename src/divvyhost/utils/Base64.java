package divvyhost.utils;

import javax.xml.bind.DatatypeConverter;

/**
 * For java 6 and 7 support
 * @author scopeinfinity
 */
public class Base64 {
    
    /**
     * Encode argument to Base64 String
     * @param str
     * @return base64
     */
    public static String encode(String str) {
        return encode(str.getBytes());
    }
    
    /**
     * Encode data[] to Base64 String
     * @param data[]
     * @return base64
     */
    public static String encode(byte[] data) {
        return DatatypeConverter.printBase64Binary(data);
    }
    
    /**
     * Decode argument from Base64 String to text String
     * @param base64
     * @return text
     */
    public static String decode(String base64) {
        return new String(DatatypeConverter.parseBase64Binary(base64));
        
    }
    
    /**
     * Decode argument from Base64 byte[] to text String
     * @param base64
     * @return text
     */
    public static String decode(byte[] base64) {
        return decode(new String(base64));
    }
    
}
