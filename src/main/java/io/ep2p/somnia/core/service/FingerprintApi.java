package io.ep2p.somnia.core.service;

import org.apache.commons.codec.digest.DigestUtils;

public interface FingerprintApi {
    /**
     * @param input input data to generate a fingerprint for
     * @return fingerprint result
     */
    String generateFingerprint(String input);

    class DefaultFingerprintApi implements FingerprintApi {

        @Override
        public String generateFingerprint(String input) {
            return DigestUtils.sha1Hex(input);
        }
    }
}
