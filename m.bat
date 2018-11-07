javac -Xlint -d bin -sourcepath src src/com/sogeti/ados/viewer/hmi/BSU.java

cd bin
jar cmf META-INF/MANIFEST.MF ../ATC_player_in_java_v0.06.jar *
