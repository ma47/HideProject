package com.hide.project.hideproject;

import java.security.MessageDigest;

public class Hashing {

    /*
    Solution for the SHA1 cryptographic function, code was borrowed and slightly adjusted for my needs from: https://stackoverflow.com/users/423171/cprcrack,
    Available at: https://stackoverflow.com/posts/33260623/revisions
     */

    public static String getSha1 (String pass)
    {
        try
        {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(pass.getBytes("UTF-8"));

            byte[] temp = messageDigest.digest();
            StringBuilder buffer = new StringBuilder();
            for (byte i : temp)
            {
                buffer.append(Integer.toString((i & 0xff) + 0x100, 16).substring(1));
            }
            return buffer.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
