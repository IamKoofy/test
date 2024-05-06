<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.myorg.sharepoint</groupId>
    <artifactId>sharepointfascade</artifactId>
    <version>0.0.1-SNAPSHOT</version>

    <dependencies>
        <dependency>
            <groupId>com.microsoft.graph</groupId>
            <artifactId>microsoft-graph</artifactId>
            <version>6.7.0</version>
        </dependency>
    </dependencies>

    <repositories>
        <repository>
            <id>central-proxy</id>
            <name>Central proxy</name>
            <url>https://my-org.com/repository/maven-central/</url>
            <releases>
                <enabled>true</enabled>
            </releases>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
        </repository>
    </repositories>

    <pluginRepositories>
        <!-- Optional: If you use plugins from Maven Central -->
        <pluginRepository>
            <id>central-proxy</id>
            <name>Central proxy for plugins</name>
            <url>https://my-org.com/repository/maven-central/</url>
            <releases>
                <enabled>true</enabled>
            </releases>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
        </pluginRepository>
    </pluginRepositories>

    <build>
        <plugins>
            <plugin>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.5.1</version>
                <configuration>
                    <source>1.7</source>
                    <target>1.7</target>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
