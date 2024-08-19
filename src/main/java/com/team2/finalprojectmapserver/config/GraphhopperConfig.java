package com.team2.finalprojectmapserver.config;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.util.GHUtility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GraphhopperConfig {

    @Bean
    public GraphHopper graphHopper(@Value("${osm.file.path}") String osmFilePath, @Value("${graph.cache.path}") String graphCachePath) {
        GraphHopper hopper = new GraphHopper();
        hopper.setOSMFile(osmFilePath);
        hopper.setGraphHopperLocation(graphCachePath);
        hopper.setEncodedValuesString("car_access,car_average_speed, road_access, max_width, max_height, hgv");
        hopper.setProfiles(
            new Profile("truck")
                .setCustomModel(GHUtility.loadCustomModelFromJar("truck.json"))
                .setWeighting("custom"));
        hopper.importOrLoad();
        return hopper;
    }
}
