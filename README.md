# CloudCondensePoc

## build the image and run the container

- docker build -t cloud-condense .
- docker run -it -e WEBDAV_USERNAME={username} -e WEBDAV_URL={url} -e WEBDAV_PASSWORD={password} -p 8080:8080 cloud-condense

## observe working directory
- docker run -it -e WEBDAV_USERNAME={username} -e WEBDAV_URL={url} -e WEBDAV_PASSWORD={password} -p 8080:8080 -v {localdir}:/work cloud-condense
