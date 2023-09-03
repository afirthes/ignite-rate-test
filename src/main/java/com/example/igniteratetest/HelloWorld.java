package com.example.igniteratetest;

import io.github.bucket4j.*;
import io.github.bucket4j.grid.ignite.thin.cas.IgniteThinClientCasBasedProxyManager;
import org.apache.ignite.Ignition;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.LocalDateTime;

public class HelloWorld {

    public static void main(String[] args) throws Exception {
        // Preparing IgniteConfiguration using Java APIs

        ClientConfiguration cfg = new ClientConfiguration().setAddresses("127.0.0.1:10800");
        try (IgniteClient client = Ignition.startClient(cfg)) {

            ClientCache<String, ByteBuffer> cache = client.getOrCreateCache("1");
            IgniteThinClientCasBasedProxyManager<String> proxyManager = new IgniteThinClientCasBasedProxyManager<>(cache);

            Refill refill = Refill
                    .greedy(100, Duration.ofSeconds(1));

            Bandwidth limit = Bandwidth
                    .classic(100, refill);

            BucketConfiguration configuration = BucketConfiguration.builder()
                    .addLimit(limit)
                    .build();

            Bucket bucket = proxyManager.builder()
                    .build("1", configuration);


            while(true) {

                ConsumptionProbe probe =  bucket.tryConsumeAndReturnRemaining(1);

                if(probe.isConsumed()) {
                    System.out.println(LocalDateTime.now() + "+");
                } else {
                    System.out.println(LocalDateTime.now() + "-");
                }

                //System.out.println(probe.getNanosToWaitForRefill());

//                Thread.sleep(20);
            }
        }
    }
}
