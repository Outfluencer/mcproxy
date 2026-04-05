package dev.outfluencer.mcproxy.proxy.connection;

import com.google.common.base.Preconditions;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ClientboundLoginEncryptionRequestPacket;
import dev.outfluencer.mcproxy.networking.protocol.packets.login.ServerboundLoginEncryptionResponsePacket;
import lombok.SneakyThrows;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ThreadLocalRandom;

public class CryptUtil {
    public static final KeyPair keys;

    static {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(1024);
            keys = generator.generateKeyPair();
        } catch (NoSuchAlgorithmException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static ClientboundLoginEncryptionRequestPacket encryptRequest(boolean auth) {
        String hash = Long.toString(ThreadLocalRandom.current().nextLong(), 16);
        byte[] pubKey = keys.getPublic().getEncoded();
        byte[] verify = new byte[4];
        ThreadLocalRandom.current().nextBytes(verify);
        return new ClientboundLoginEncryptionRequestPacket(hash, pubKey, verify, auth);
    }


    @SneakyThrows
    public static void check(ServerboundLoginEncryptionResponsePacket resp, ClientboundLoginEncryptionRequestPacket request) {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, keys.getPrivate());
        byte[] decrypted = cipher.doFinal(resp.getVerifyToken());
        Preconditions.checkState(MessageDigest.isEqual(request.getVerifyToken(), decrypted), "Invalid verification");
    }

    @SneakyThrows
    public static SecretKey getSecret(ServerboundLoginEncryptionResponsePacket resp) {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, keys.getPrivate());
        return new SecretKeySpec(cipher.doFinal(resp.getSharedSecret()), "AES");
    }

}
