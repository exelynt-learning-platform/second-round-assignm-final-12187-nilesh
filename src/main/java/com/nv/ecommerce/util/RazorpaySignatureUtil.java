package com.nv.ecommerce.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public final class RazorpaySignatureUtil {

	private static final String HMAC_SHA256 = "HmacSHA256";

	public static boolean verify(String payload, String secret, String actualSignature) {
		try {
			Mac mac = Mac.getInstance(HMAC_SHA256);
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

			String expectedSignature = Base64.getEncoder().encodeToString(digest);

			return MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8),
					actualSignature.getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			return false;
		}
	}
}
