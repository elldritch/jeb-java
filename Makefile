JEB_JAR=target/jeb-1.0-SNAPSHOT.jar
JAVA_SOURCES=$(shell find . -name *.java)
MAVEN_CLASSPATH=$(shell mvn dependency:build-classpath | grep -v "[INFO]")

$(JEB_JAR): $(JAVA_SOURCES) pom.xml
	mvn package

.PHONY: run
run: $(JEB_JAR)
	java -cp $(JEB_JAR):$(MAVEN_CLASSPATH) io.github.ilikebits.jeb.App
