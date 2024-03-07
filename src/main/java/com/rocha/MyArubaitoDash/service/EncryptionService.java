package com.rocha.MyArubaitoDash.service;


import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;
import com.amazonaws.services.kms.model.EncryptRequest;
import com.amazonaws.services.kms.model.EncryptResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.Base64;

@Service
public class EncryptionService {

    private final AWSKMS kmsClient;

    // ARN of the Customer Master Key (CMK) stored in AWS KMS
    @Value("${aws.kms.cmkArn}")
    private String kmsKeyId; // ARN of the CMK

    public EncryptionService() {
        this.kmsClient = AWSKMSClientBuilder.standard()
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .withRegion(Regions.US_EAST_2) // Specify the correct region
                .build();
    }

    public String encrypt(String data) {
        // Convert the input data into a byte array
        ByteBuffer plaintext = ByteBuffer.wrap(data.getBytes());

        // Create an encryption request with the specified CMK ARN and plaintext data
        EncryptRequest encryptRequest = new EncryptRequest()
                .withKeyId(kmsKeyId)
                .withPlaintext(plaintext);

        // Perform the encryption operation using the AWS KMS client
        EncryptResult encryptResult = kmsClient.encrypt(encryptRequest);

        // Retrieve the encrypted ciphertext from the encryption result
        ByteBuffer encryptedData = encryptResult.getCiphertextBlob();

        // Encode the encrypted ciphertext as a Base64 string and return it
        // Turns binary data into a readable string, so it's good to "read" and store. That would be very hard with binary data
        return Base64.getEncoder().encodeToString(encryptedData.array());
    }

    public String decrypt(String encryptedData) {
        // Decode the Base64-encoded encrypted data into a byte array
        // blob = Binary Large Object.
        ByteBuffer ciphertextBlob = ByteBuffer.wrap(Base64.getDecoder().decode(encryptedData));

        // Create a decryption request with the ciphertext data
        DecryptRequest decryptRequest = new DecryptRequest()
                .withCiphertextBlob(ciphertextBlob);

        // Perform the decryption operation using the AWS KMS client
        DecryptResult decryptResult = kmsClient.decrypt(decryptRequest);

        // Retrieve the decrypted plaintext from the decryption result
        ByteBuffer decryptedData = decryptResult.getPlaintext();

        // Convert the decrypted plaintext byte array into a string and return it
        return new String(decryptedData.array());
    }

}
