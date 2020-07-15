FROM adoptopenjdk:11.0.6_10-jdk-hotspot

COPY target/classes/insilico/core/molecule/tools/Z.dat /tmp/Z.dat
COPY target/InSilicoCore-1.0-SNAPSHOT.jar /InsilicoCore.jar
COPY InchiGenerator.jar /InchiGenerator.jar
EXPOSE 5050:5050

RUN java -jar InchiGenerator.jar

ENTRYPOINT ["java", "-Dserver.port=5050","-jar", "/kodechm_calculator-1.0.jar"]