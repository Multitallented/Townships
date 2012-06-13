/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package multitallented.redcastlemedia.bukkit.herostronghold;

/**
 *
 * @author multitallented
 * @author Alex_M
 */
public class Util {
    public static boolean validateFileName(String fileName) {
        return fileName.matches("^[^.\\\\/:*?\"<>|]?[^\\\\/:*?\"<>|]*") 
        && getValidFileName(fileName).length()>0;
    }

    public static String getValidFileName(String fileName) throws IllegalStateException {
        String newFileName = fileName.replaceAll("^[.\\\\/:*?\"<>|]?[\\\\/:*?\"<>|]*", "");
        if(newFileName.length()==0)
            throw new IllegalStateException(
                    "File Name " + fileName + " results in a empty fileName!");
        return newFileName;
    }
}
