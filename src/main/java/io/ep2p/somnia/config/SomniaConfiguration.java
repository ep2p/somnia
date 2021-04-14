package io.ep2p.somnia.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SomniaConfiguration {
    private int networkSize;
    private String databaseFingerprint;


    private static SomniaConfiguration instance;

    private SomniaConfiguration(){}

    public synchronized SomniaConfiguration getInstance(){
        if (instance == null)
            instance = new SomniaConfiguration();

        return instance;
    }

}
