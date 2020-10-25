#gradlew build
docker build -t trusted-anchor-server .
docker run -it --rm --name trusted-anchor-server trusted-anchor-server