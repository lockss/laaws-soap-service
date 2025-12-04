/*
 * LAAWS SOAP Service
 *
 * LOCKSS SOAP Service providing SOAP API for legacy clients.
 */

plugins {
    id("lockss-spring-boot-conventions")
}

group = "org.lockss.laaws"
version = "2.8.0-SNAPSHOT"
description = "LOCKSS SOAP Service"

dependencies {
    // Internal dependencies
    api(project(":lockss-spring-bundle"))

    // CXF 4.x for SOAP (jakarta namespace for Spring Boot 3.x)
    api(libs.cxf.jakarta.rt.frontend.jaxws)
    api(libs.cxf.jakarta.rt.transports.http)
    api(libs.cxf.jakarta.rt.ext.logging)

    // Test dependencies
    testImplementation(platform(project(":lockss-pom-bundles:lockss-junit5-bundle")))
    testImplementation(libs.junit.jupiter.engine)
}

// Docker configuration
docker {
    imageName.set("laaws-soap-service")
    restPort.set(24660)
    uiPort.set(24661)
}
